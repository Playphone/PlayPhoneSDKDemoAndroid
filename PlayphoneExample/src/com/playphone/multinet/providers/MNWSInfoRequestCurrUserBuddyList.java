//
//  MNWSInfoRequestCurrUserBuddyList.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import java.util.ArrayList;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSBuddyListItem;

public class MNWSInfoRequestCurrUserBuddyList extends MNWSInfoRequest
 {
  public MNWSInfoRequestCurrUserBuddyList (IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addCurrUserBuddyList();
   }

  @SuppressWarnings("unchecked")
  void handleRequestCompleted (MNWSResponse       response)
   {
    ArrayList<MNWSBuddyListItem> data = (ArrayList<MNWSBuddyListItem>)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult(data != null ?
                                             data.toArray(new MNWSBuddyListItem[data.size()]) :
                                             new MNWSBuddyListItem[0]);

    eventHandler.onCompleted(result);
   }

  void handleRequestError (MNWSRequestError   error)
   {
    RequestResult result = new RequestResult(null);

    result.setError(error.getMessage());

    eventHandler.onCompleted(result);
   }

  public interface IEventHandler
   {
    public void onCompleted (RequestResult result);
   }

  public static class EventHandlerAbstract implements IEventHandler
   {
    public void onCompleted (RequestResult result)
     {
     }
   }

  public static class RequestResult extends MNWSInfoRequest.RequestResult
   {
    public MNWSBuddyListItem[] getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSBuddyListItem[] data)
     {
      super();

      this.data = data;
     }

    private MNWSBuddyListItem[] data;
   }

  private IEventHandler eventHandler;
  private String        blockName;
 }

