//
//  MNWSInfoRequestSystemGameNetStats.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSSystemGameNetStats;

public class MNWSInfoRequestSystemGameNetStats extends MNWSInfoRequest
 {
  public MNWSInfoRequestSystemGameNetStats (IEventHandler eventHandler)
   {
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addSystemGameNetStats();
   }

  void handleRequestCompleted (MNWSResponse       response)
   {
    MNWSSystemGameNetStats data = (MNWSSystemGameNetStats)response.getDataForBlock(blockName);

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
    public MNWSSystemGameNetStats getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSSystemGameNetStats data)
     {
      super();

      this.data = data;
     }

    private MNWSSystemGameNetStats data;
   }

  private IEventHandler eventHandler;
  private String        blockName;
 }

