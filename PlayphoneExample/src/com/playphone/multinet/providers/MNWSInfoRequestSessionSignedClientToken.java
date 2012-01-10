//
//  MNWSInfoRequestSessionSignedClientToken.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSSessionSignedClientToken;

public class MNWSInfoRequestSessionSignedClientToken extends MNWSInfoRequest
 {
  public MNWSInfoRequestSessionSignedClientToken (String payload, IEventHandler eventHandler)
   {
    this.payload      = payload;
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addGetSessionSignedClientToken(payload);
   }

  void handleRequestCompleted (MNWSResponse       response)
   {
    MNWSSessionSignedClientToken data = (MNWSSessionSignedClientToken)response.getDataForBlock(blockName);

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
    public MNWSSessionSignedClientToken getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSSessionSignedClientToken data)
     {
      super();

      this.data = data;
     }

    private MNWSSessionSignedClientToken data;
   }

  private final String        payload;
  private final IEventHandler eventHandler;
  private String              blockName;
 }

