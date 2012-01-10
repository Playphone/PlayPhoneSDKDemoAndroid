//
//  MNWSProvider.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.ws.MNWSRequestSender;
import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.IMNWSRequestEventHandler;

/**
 * A class representing "Web service" MultiNet provider.
 *
 * "Web service" provider provides ability to send information requests to the server.
 */
public class MNWSProvider
 {
  /**
   * Constructs a new <code>MNWSProvider</code> object.
   *
   * @param session         MultiNet session instance
   */
  public MNWSProvider (MNSession session)
   {
    this.session = session;
   }

  /**
   * Stops provider and frees all allocated resources.
   */
  public synchronized void shutdown ()
   {
    session = null;
   }

  /*
   * Sends information request to server
   *
   * @param request request to be sent
   * @return <code>MNWSLoader<code> object (it can be used to cancel request)
   */
  public MNWSLoader send (MNWSInfoRequest request)
   {
    MNWSInfoRequest[] requests = { request };

    return send(requests);
   }

  /*
   * Sends several information requests to server simultaneously
   *
   * @param requests array of requests to be sent
   * @return <code>MNWSLoader<code> object (it can be used to cancel request)
   */
  public MNWSLoader send (MNWSInfoRequest[] requests)
   {
    EventHandler       eventHandler = new EventHandler(requests);
    MNWSRequestSender  sender       = new MNWSRequestSender(session);
    MNWSRequestContent content      = prepareContent(requests);

    return new MNWSLoader(sender.sendWSRequestSmartAuth(content,eventHandler));
   }

  private MNWSRequestContent prepareContent (MNWSInfoRequest[] requests)
   {
    MNWSRequestContent content = new MNWSRequestContent();

    for (MNWSInfoRequest request : requests)
     {
      request.addContent(content);
     }

    return content;
   }

  private static class EventHandler implements IMNWSRequestEventHandler
   {
    public EventHandler (MNWSInfoRequest[] requests)
     {
      this.requests = requests;
     }

    public void onRequestCompleted (MNWSResponse     response)
     {
      for (MNWSInfoRequest request : requests)
       {
        request.handleRequestCompleted(response);
       }
     }

    public void onRequestError (MNWSRequestError error)
     {
      for (MNWSInfoRequest request : requests)
       {
        request.handleRequestError(error);
       }
     }

    MNWSInfoRequest[] requests;
   }

  private MNSession session;
 }

