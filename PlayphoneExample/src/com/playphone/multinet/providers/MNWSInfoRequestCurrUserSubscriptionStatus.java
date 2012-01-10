//
//  MNWSInfoRequestCurrUserSubscriptionStatus.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSCurrUserSubscriptionStatus;

public class MNWSInfoRequestCurrUserSubscriptionStatus extends MNWSInfoRequest
 {
  public MNWSInfoRequestCurrUserSubscriptionStatus (IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
    this.socNetId     = MNWSRequestContent.SN_ID_PLAYPHONE;
   }

  protected MNWSInfoRequestCurrUserSubscriptionStatus (int socNetId, IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
    this.socNetId     = socNetId;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addCurrUserSubscriptionStatus(socNetId);
   }

  void handleRequestCompleted (MNWSResponse       response)
   {
    MNWSCurrUserSubscriptionStatus data = (MNWSCurrUserSubscriptionStatus)response.getDataForBlock(blockName);

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
    public MNWSCurrUserSubscriptionStatus getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSCurrUserSubscriptionStatus data)
     {
      super();

      this.data = data;
     }

    private MNWSCurrUserSubscriptionStatus data;
   }

  private final int           socNetId;
  private final IEventHandler eventHandler;
  private String              blockName;
 }

