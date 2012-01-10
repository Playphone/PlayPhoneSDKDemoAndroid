//
//  MNWSInfoRequestCurrGameRoomList.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import java.util.ArrayList;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSRoomListItem;

public class MNWSInfoRequestCurrGameRoomList extends MNWSInfoRequest
 {
  public MNWSInfoRequestCurrGameRoomList (IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addCurrGameRoomList();
   }

  @SuppressWarnings("unchecked")
  void handleRequestCompleted (MNWSResponse       response)
   {
    ArrayList<MNWSRoomListItem> data = (ArrayList<MNWSRoomListItem>)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult(data != null ?
                                             data.toArray(new MNWSRoomListItem[data.size()]) :
                                             new MNWSRoomListItem[0]);

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
    public MNWSRoomListItem[] getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSRoomListItem[] data)
     {
      super();

      this.data = data;
     }

    private MNWSRoomListItem[] data;
   }

  private IEventHandler eventHandler;
  private String        blockName;
 }

