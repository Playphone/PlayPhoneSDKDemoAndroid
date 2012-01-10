//
//  MNWSInfoRequestAnyUserGameCookies.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import java.util.ArrayList;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSUserGameCookie;

public class MNWSInfoRequestAnyUserGameCookies extends MNWSInfoRequest
 {
  public MNWSInfoRequestAnyUserGameCookies (long[] userIdList, int[] cookieKeyList, IEventHandler eventHandler)
   {
    this.userIdList    = userIdList;
    this.cookieKeyList = cookieKeyList;
    this.eventHandler  = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addAnyUserGameCookies(userIdList,cookieKeyList);
   }

  @SuppressWarnings("unchecked")
  void handleRequestCompleted (MNWSResponse       response)
   {
    ArrayList<MNWSUserGameCookie> data = (ArrayList<MNWSUserGameCookie>)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult
                                (data != null ?
                                 data.toArray(new MNWSUserGameCookie[data.size()]) :
                                 new MNWSUserGameCookie[0]);

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
    public MNWSUserGameCookie[] getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSUserGameCookie[] data)
     {
      super();

      this.data = data;
     }

    private MNWSUserGameCookie[] data;
   }

  private final long[]        userIdList;
  private final int[]         cookieKeyList;
  private final IEventHandler eventHandler;
  private String              blockName;
 }

