//
//  MNServerInfoProvider.java
//  MultiNet client
//
//  Copyright 2011 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;
import com.playphone.multinet.core.MNEventHandlerArray;
import com.playphone.multinet.MNUserInfo;

/**
 * A class representing "server information" MultiNet provider.
 * This provider allows to retrieve server information
 */
public class MNServerInfoProvider
 {
  public static final int SERVER_TIME_INFO_KEY = 1;

  /**
   * Interface handling server information retrieval events.
   */
  public interface IEventHandler
   {
    /**
     * Invoked when server information has been successfully retrieved.
     * @param key describes a kind of the retrieved server information
     * @param value server value
     */
    void onServerInfoItemReceived (int key, String value);

    /**
     * Invoked when server information retrieval failed.
     * @param key describes a kind of the server information
     * @param error error message
     */
    void onServerInfoItemRequestFailedWithError (int key, String error);
   }

  /**
   * A class which implements IEventHandler interface by ignoring all
   * received events.
   */
  public static class EventHandlerAbstract implements IEventHandler
   {
    public void onServerInfoItemReceived (int key, String value)
     {
     }

    public void onServerInfoItemRequestFailedWithError (int key, String error)
     {
     }
   }

  /**
   * Constructs a new <code>MNServerInfoProvider</code> object.
   *
   * @param session MultiNet session instance
   */
  public MNServerInfoProvider (MNSession session)
   {
    sessionEventHandler = new SessionEventHandler(session);
   }

  /**
   * Stops provider and frees all allocated resources.
   */
  public synchronized void shutdown ()
   {
    sessionEventHandler.shutdown();
   }

  /**
   * Retrieves server information
   * @param key describes a kind of the server information
   */
  public void requestServerInfoItem (int key)
   {
    sessionEventHandler.session.sendPluginMessage
     (PROVIDER_NAME,"g" + Integer.toString(key) + ":" +
                    Integer.toString(REQUEST_NUMBER_API));
   }

  /**
   * Adds event handler
   *
   * @param eventHandler an object that implements
   * {@link IEventHandler IEventHandler} interface
   */
  public void addEventHandler (IEventHandler eventHandler)
   {
    sessionEventHandler.eventHandlers.add(eventHandler);
   }

  /**
   * Removes event handler
   *
   * @param eventHandler an object that implements
   * {@link IEventHandler IEventHandler} interface
   */
  public void removeEventHandler (IEventHandler eventHandler)
   {
    sessionEventHandler.eventHandlers.remove(eventHandler);
   }

  private class SessionEventHandler extends MNSessionEventHandlerAbstract
   {
    public SessionEventHandler (MNSession session)
     {
      this.session       = session;
      this.eventHandlers = new MNEventHandlerArray<IEventHandler>();

      session.addEventHandler(this);
     }

    public synchronized void shutdown ()
     {
      session.removeEventHandler(this);
      eventHandlers.clearAll();
     }

    private void handleGetResponse (String[] responseComponents)
     {
      if (responseComponents.length < 4)
       {
        return;
       }

      try
       {
        final int key = Integer.parseInt(responseComponents[0]);

        //check if request number is valid
        int requestNumber = Integer.parseInt(responseComponents[1]);

        if (requestNumber != REQUEST_NUMBER_API)
         {
          return;
         }

        if      (responseComponents[2].equals(RESPONSE_STATUS_OK))
         {
          final String value = responseComponents[3];

          eventHandlers.callHandlers(new MNEventHandlerArray.ICaller<IEventHandler>()
           {
            public void callHandler (IEventHandler handler)
             {
              handler.onServerInfoItemReceived(key,value);
             }
           });
         }
        else if (responseComponents[2].equals(RESPONSE_STATUS_ERROR))
         {
          final String message = responseComponents[3];

          eventHandlers.callHandlers(new MNEventHandlerArray.ICaller<IEventHandler>()
           {
            public void callHandler (IEventHandler handler)
             {
              handler.onServerInfoItemRequestFailedWithError(key,message);
             }
           });
         }
       }
      catch (NumberFormatException e)
       {
        return;
       }
     }

    public void mnSessionPluginMessageReceived (String     pluginName,
                                                String     message,
                                                MNUserInfo sender)
     {
      if (sender != null || !pluginName.equals(PROVIDER_NAME))
       {
        return;
       }

      if (message.length() == 0)
       {
        return;
       }

      char     cmd        = message.charAt(0);
      String[] components = message.substring(1).split(":",4);

      if (cmd == REQUEST_CMD_GET)
       {
        handleGetResponse(components);
       }
     }

    final MNSession                          session;
    final MNEventHandlerArray<IEventHandler> eventHandlers;

    private static final char   REQUEST_CMD_GET       = 'g';
    private static final String RESPONSE_STATUS_OK    = "s";
    private static final String RESPONSE_STATUS_ERROR = "e";
   }

  private static final int REQUEST_NUMBER_API = 0;

  private final SessionEventHandler sessionEventHandler;
  private static final String PROVIDER_NAME = "com.playphone.mn.si";
 }

