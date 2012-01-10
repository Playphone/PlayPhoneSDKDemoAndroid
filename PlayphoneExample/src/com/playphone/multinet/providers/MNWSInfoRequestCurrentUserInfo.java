//
//  MNWSInfoRequestCurrentUserInfo.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSCurrentUserInfo;

public class MNWSInfoRequestCurrentUserInfo extends MNWSInfoRequest
 {
  public MNWSInfoRequestCurrentUserInfo (IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addCurrentUserInfo();
   }

  void handleRequestCompleted (MNWSResponse       response)
   {
    MNWSCurrentUserInfo data = (MNWSCurrentUserInfo)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult(data);

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
    public MNWSCurrentUserInfo getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSCurrentUserInfo data)
     {
      super();

      this.data = data;
     }

    private MNWSCurrentUserInfo data;
   }

  private IEventHandler eventHandler;
  private String        blockName;
 }

