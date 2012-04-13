//
//  MNConfigData.java
//  MultiNet client
//
//  Copyright 2009 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.util.Hashtable;

class MNConfigData implements MNURLTextDownloader.IEventHandler
 {
  public interface IEventHandler
   {
    void mnConfigDataLoaded     (MNConfigData configData);
    void mnConfigDataLoadFailed (String       errorMessage);
   }

  public MNConfigData (String configUrl)
   {
    this.configUrl = configUrl;
    loaded         = false;
    downloader     = new MNURLTextDownloader();
   }

  public synchronized boolean isLoaded ()
   {
    return loaded;
   }

  public synchronized void clear ()
   {
    loaded = false;

    smartFoxAddr       = null;
    smartFoxPort       = 0;
    blueBoxAddr        = null;
    blueBoxPort        = 0;
    smartConnect       = false;
    webServerUrl       = null;
    facebookAPIKey     = null;
    facebookAppId      = null;
    facebookSSOMode    = 0;
    launchTrackerUrl   = null;
    installTrackerUrl  = null;
    shutdownTrackerUrl = null;
    beaconTrackerUrl   = null;
    gameVocabularyVersion = null;
    tryFastResumeMode  = 0;
    multiWebFrontLoadDelay = 0;
    allowReadWiFiMAC   = null;
    useInstallIdInsteadOfUDID = 0;
   }

  public synchronized void load (IEventHandler eventHandler)
   {
    clear();

    this.eventHandler = eventHandler;

    if (configUrl != null)
     {
      downloader.loadURL(configUrl,this);
     }
    else
     {
      eventHandler.mnConfigDataLoadFailed("Configuration URL is not set");
     }
   }

  public synchronized void downloaderDataReady  (MNURLDownloader downloader, String[] data)
   {
    boolean ok = true;

    Hashtable<String,String> params = parseConfig(data);

    if (params != null)
     {
      try
       {
        smartFoxAddr       = parseParamString(params,SMARTFOX_SERVER_ADDR_PARAM);
        smartFoxPort       = parseParamInteger(params,SMARTFOX_SERVER_PORT_PARAM);
        blueBoxAddr        = parseParamString(params,BLUEBOX_SERVER_ADDR_PARAM);
        blueBoxPort        = parseParamInteger(params,BLUEBOX_SERVER_PORT_PARAM);
        smartConnect       = parseParamBoolean(params,SMARTFOX_SMART_CONNECT_PARAM);
        webServerUrl       = parseParamString(params,MULTINET_WEBSERVER_URL_PARAM);
        facebookAPIKey     = parseParamString(params,FACEBOOK_API_KEY_PARAM);
        facebookAppId      = parseParamString(params,FACEBOOK_APP_ID_PARAM);
        facebookSSOMode    = parseParamInteger(params,FACEBOOK_SSO_MODE_PARAM,true,0);
        tryFastResumeMode  = parseParamInteger(params,TRY_FAST_RESUME_MODE_PARAM,true,0);
        launchTrackerUrl   = params.get(LAUNCH_TRACKER_URL_PARAM);
        installTrackerUrl  = params.get(INSTALL_TRACKER_URL_PARAM);
        shutdownTrackerUrl = params.get(SHUTDOWN_TRACKER_URL_PARAM);
        beaconTrackerUrl   = params.get(BEACON_TRACKER_URL_PARAM);
        gameVocabularyVersion = params.get(GAME_VOCABULARY_VERSION_PARAM);
        multiWebFrontLoadDelay = parseParamInteger(params,MULTIWEBFRONT_LOAD_DELAY_PARAM,true,0);
        allowReadWiFiMAC    = params.get(ALLOW_READ_WIFI_MAC_PARAM);
        useInstallIdInsteadOfUDID = parseParamInteger(params,USE_INSTALL_ID_NOT_UDID_PARAM,true,0);
       }
      catch (IllegalArgumentException e)
       {
        ok = false;
       }
     }
    else
     {
      ok = false;
     }

    if (ok)
     {
      loaded = true;

      if (eventHandler != null)
       {
        eventHandler.mnConfigDataLoaded(this);
       }
     }
    else
     {
      clear();

      if (eventHandler != null)
       {
        eventHandler.mnConfigDataLoadFailed("Invalid configuration file format");
       }
     }

    eventHandler = null;
   }

  public synchronized void downloaderLoadFailed (MNURLDownloader downloader, MNURLDownloader.ErrorInfo errorInfo)
   {
    if (eventHandler != null)
     {
      eventHandler.mnConfigDataLoadFailed(errorInfo.getMessage());
     }

    eventHandler = null;
   }

  private Hashtable<String,String> parseConfig (String[] content)
   {
    Hashtable<String,String> result = new Hashtable<String,String>();
    int index  = 0;
    int count  = content.length;
    boolean ok = true;
    String   line;
    MNUtils.StringKeyValuePair keyValuePair = new MNUtils.StringKeyValuePair();

    while (index < count && ok)
     {
      ok = MNUtils.StringKeyValuePair.parseKeyValueString(keyValuePair,content[index]);

      if (ok && !keyValuePair.isEmpty())
       {
        result.put(keyValuePair.getKey(),keyValuePair.getValue());
       }

      index++;
     }

    if (ok)
     {
      return result;
     }
    else
     {
      return null;
     }
   }

  private String parseParamString (Hashtable<String,String> params, String name)
                 throws IllegalArgumentException
   {
    String result = params.get(name);

    if (result == null)
     {
      throw new IllegalArgumentException();
     }

    return result;
   }

  private int parseParamInteger (Hashtable<String,String> params, String name)
              throws IllegalArgumentException
   {
    return parseParamInteger(params,name,false,0);
   }

  private int parseParamInteger (Hashtable<String,String> params, String name, boolean isOptional, int defaultValue)
              throws IllegalArgumentException
   {
    int    result = 0;
    String value  = params.get(name);

    if (value == null)
     {
      if (isOptional)
       {
        return defaultValue;
       }
      else
       {
        throw new IllegalArgumentException();
       }
     }

    try
     {
      result = Integer.parseInt(value,10);
     }
    catch (NumberFormatException e)
     {
      throw new IllegalArgumentException();
     }

    return result;
   }

  private boolean parseParamBoolean (Hashtable<String,String> params, String name)
                  throws IllegalArgumentException
   {
    boolean result = false;
    String value   = params.get(name);

    if (value == null)
     {
      throw new IllegalArgumentException();
     }

    if      (value.equals("true"))
     {
      result = true;
     }
    else if (value.equals("false"))
     {
      result = false;
     }
    else
     {
      throw new IllegalArgumentException();
     }

    return result;
   }

  private String              configUrl;
  private boolean             loaded;
  private MNURLTextDownloader downloader;
  private IEventHandler       eventHandler;

  public String  smartFoxAddr;
  public int     smartFoxPort;
  public String  blueBoxAddr;
  public int     blueBoxPort;
  public boolean smartConnect;
  public String  webServerUrl;
  public String  facebookAPIKey;
  public String  facebookAppId;
  public int     facebookSSOMode;
  public String  launchTrackerUrl;
  public String  installTrackerUrl;
  public String  beaconTrackerUrl;
  public String  shutdownTrackerUrl;
  public String  gameVocabularyVersion;
  public int     tryFastResumeMode;
  public int     multiWebFrontLoadDelay;
  public String  allowReadWiFiMAC;
  public int     useInstallIdInsteadOfUDID;

  private static final String SMARTFOX_SERVER_ADDR_PARAM   = "SmartFoxServerAddr";
  private static final String SMARTFOX_SERVER_PORT_PARAM   = "SmartFoxServerPort";
  private static final String BLUEBOX_SERVER_ADDR_PARAM    = "BlueBoxServerAddr";
  private static final String BLUEBOX_SERVER_PORT_PARAM    = "BlueBoxServerPort";
  private static final String SMARTFOX_SMART_CONNECT_PARAM = "BlueBoxSmartConnect";
  private static final String MULTINET_WEBSERVER_URL_PARAM = "MultiNetWebServerURL";
  private static final String FACEBOOK_API_KEY_PARAM       = "FacebookApiKey";
  private static final String FACEBOOK_APP_ID_PARAM        = "FacebookAppId";
  private static final String FACEBOOK_SSO_MODE_PARAM      = "FacebookSSOMode";
  private static final String LAUNCH_TRACKER_URL_PARAM     = "LaunchTrackerURL";
  private static final String INSTALL_TRACKER_URL_PARAM    = "InstallTrackerURL";
  private static final String SHUTDOWN_TRACKER_URL_PARAM   = "ShutdownTrackerURL";
  private static final String BEACON_TRACKER_URL_PARAM     = "BeaconTrackerURL";
  private static final String GAME_VOCABULARY_VERSION_PARAM = "GameVocabularyVersion";
  private static final String TRY_FAST_RESUME_MODE_PARAM   = "TryFastResumeMode";
  private static final String MULTIWEBFRONT_LOAD_DELAY_PARAM = "MultiWebFrontLoadDelay";
  private static final String ALLOW_READ_WIFI_MAC_PARAM    = "AllowReadWiFiMAC";
  private static final String USE_INSTALL_ID_NOT_UDID_PARAM = "UseInstallIdInsteadOfUDID";
 }

