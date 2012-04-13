//
//  MNTrackingSystem.java
//  MultiNet client
//
//  Copyright 2011 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import java.net.URLEncoder;

import java.io.UnsupportedEncodingException;

import com.playphone.multinet.MNConst;

public class MNTrackingSystem
 {
  public interface IEventHandler
   {
    public void onAppBeaconResponseReceived (MNAppBeaconResponse response);
   }

  public MNTrackingSystem (MNSession session, IEventHandler eventHandler)
   {
    beaconUrlTemplate    = null;
    shutdownUrlTemplate  = null;
    launchTracked        = false;
    installTracker       = new InstallTracker();
    trackingVars         = setupTrackingVars(session);
    this.eventHandler    = eventHandler;
   }

  public void trackLaunchWithUrlTemplate (String urlTemplate, MNSession session)
   {
    synchronized (this)
     {
      if (launchTracked)
       {
        return;
       }
      else
       {
        launchTracked = true;
       }
     }

    UrlTemplate launchUrlTemplate = new UrlTemplate(urlTemplate,trackingVars);

    launchUrlTemplate.sendLaunchTrackingRequest(session);
   }

  public void trackInstallWithUrlTemplate (String urlTemplate, MNSession session)
   {
    installTracker.setUrlTemplate(urlTemplate,session);
   }

  public synchronized void setShutdownUrlTemplate (String urlTemplate, MNSession session)
   {
    shutdownUrlTemplate = new UrlTemplate(urlTemplate,trackingVars);
   }

  public synchronized void trackShutdown (MNSession session)
   {
    if (shutdownUrlTemplate != null)
     {
      shutdownUrlTemplate.sendShutdownTrackingRequest(session);
     }
   }

  public synchronized void setBeaconUrlTemplate (String urlTemplate, MNSession session)
   {
    beaconUrlTemplate = new UrlTemplate(urlTemplate,trackingVars);
   }

  public synchronized void sendBeacon (String beaconAction, String beaconData, long beaconCallSeqNumber, MNSession session)
   {
    if (beaconUrlTemplate != null)
     {
      beaconUrlTemplate.sendBeacon(beaconAction,beaconData,beaconCallSeqNumber,session,eventHandler);
     }
   }

  public Map<String,String> getTrackingVars ()
   {
    return trackingVars;
   }

  private void setupTrackingVar (HashMap<String,String> vars, String name, String value)
   {
    if (value == null)
     {
      return;
     }

    vars.put(name,value);
    vars.put("@" + name,MNUtils.stringGetMD5String(value));
   }

  private HashMap<String,String> setupTrackingVars (MNSession session)
   {
    IMNPlatform            platform = session.getPlatform();
    Locale                 locale   = Locale.getDefault();
    TimeZone               timeZone = TimeZone.getDefault();

    String phoneId      = platform.getDeviceProperty(IMNPlatform.PROPERTY_PHONE_DEVICE_ID);
    String subscriberId = platform.getDeviceProperty(IMNPlatform.PROPERTY_PHONE_SUBSCRIBER_ID);
    String phoneNumber  = platform.getDeviceProperty(IMNPlatform.PROPERTY_PHONE_NUMBER);

    String wiFiMAC          = null;
    String allowReadWiFiMAC = session.getConfigData().allowReadWiFiMAC;

    if (allowReadWiFiMAC != null && !allowReadWiFiMAC.trim().equals("0"))
     {
      wiFiMAC = platform.getWiFiMACAddress();
     }

    HashMap<String,String> vars = new HashMap<String,String>();

    setupTrackingVar(vars,"tv_udid",session.getUniqueAppId());
    setupTrackingVar(vars,"tv_udid2",phoneId);
    setupTrackingVar(vars,"tv_udid3",subscriberId);
    setupTrackingVar(vars,"tv_udid4",phoneNumber);
    setupTrackingVar(vars,"tv_mac_addr",wiFiMAC);
//    setupTrackingVar(vars,"tv_device_name","");
    setupTrackingVar(vars,"tv_device_type",android.os.Build.MODEL);
    setupTrackingVar(vars,"tv_os_version",android.os.Build.VERSION.RELEASE);
    setupTrackingVar(vars,"tv_country_code",locale.getISO3Country());
    setupTrackingVar(vars,"tv_language_code",locale.getLanguage());
    setupTrackingVar(vars,"mn_game_id",Integer.toString(session.getGameId()));
    setupTrackingVar(vars,"mn_dev_type",Integer.toString(platform.getDeviceType()));
    setupTrackingVar(vars,"mn_dev_id",MNUtils.stringGetMD5String(session.getUniqueAppId()));
    setupTrackingVar(vars,"mn_client_ver",MNSession.CLIENT_API_VERSION);
    setupTrackingVar(vars,"mn_client_locale",locale.toString());

    String appVerExternal = platform.getAppVerExternal();
    String appVerInternal = platform.getAppVerInternal();

    setupTrackingVar(vars,"mn_app_ver_ext",appVerExternal);
    setupTrackingVar(vars,"mn_app_ver_int",appVerInternal);

    setupTrackingVar(vars,"mn_launch_time",Long.toString(session.getLaunchTime()));
    setupTrackingVar(vars,"mn_launch_id",session.getLaunchId());
    setupTrackingVar(vars,"mn_install_id",session.getInstallId());

    setupTrackingVar(vars,"mn_tz_info",
                     Integer.toString(timeZone.getRawOffset() / 1000) +
                      "+*+" +
                       timeZone.getID().replace('|',' ').replace(',','-'));

    Map<String,String> appExtParams = session.getAppExtParams();

    for (String extParamName : appExtParams.keySet())
     {
      setupTrackingVar(vars,extParamName,appExtParams.get(extParamName));
     }

    populateReferrerVars(vars,session);

    return vars;
   }

  private void populateReferrerVars (HashMap<String,String> vars, MNSession session)
   {
    String referrerString = session.varStorageGetValueForVariable
                             (MNInstallReferrerReceiver.INSTALL_REFERRER_DATA_VAR_NAME);

    if (referrerString != null)
     {
      try
       {
        HashMap<String,String> referrerData = MNUtils.httpGetRequestParseParams(referrerString);

        for (String key : referrerData.keySet())
         {
          setupTrackingVar(vars,"gm_referrer." + key,referrerData.get(key));
         }
       }
      catch (UnsupportedEncodingException e)
       {
       }
     }

    String referrerParsedAt = session.varStorageGetValueForVariable
                               (MNInstallReferrerReceiver.INSTALL_REFERRER_PARSED_AT_VAR_NAME);

    setupTrackingVar(vars,"gm_referrer_parsed_at",referrerParsedAt);
   }

  private static class UrlTemplate
   {
    public UrlTemplate (String urlTemplate, HashMap<String,String> trackingVars)
     {
      parseTemplate(urlTemplate,trackingVars);
     }

    public void sendLaunchTrackingRequest (MNSession session)
     {
      sendBeacon(null,null,0,session,null);
     }

    public void sendInstallTrackingRequest (MNSession session, IEventHandler eventHandler)
     {
      sendBeacon(null,null,0,session,eventHandler);
     }

    public void sendShutdownTrackingRequest (MNSession session)
     {
      sendBeacon(null,null,0,session,null);
     }

    private void AddDynamicVars (MNUtils.HttpPostBodyStringBuilder builder,
                                 ArrayList<DynamicVar>             vars,
                                 String                            value)
     {
      if (vars == null)
       {
        return;
       }

      if (value == null)
       {
        value = "";
       }

      String hashedValue = null;

      for (DynamicVar var : vars)
       {
        if (var.getUseHashedValue())
         {
          if (hashedValue == null)
           {
            hashedValue = MNUtils.stringGetMD5String(value);
           }

          builder.addParamWithEncodingFlags(var.name,hashedValue,false,true);
         }
        else
         {
          builder.addParamWithEncodingFlags(var.name,value,false,true);
         }
       }
     }

    public void sendBeacon (String beaconAction, String beaconData, final long requestNumber, MNSession session, final IEventHandler eventHandler)
     {
      String postBody;

      if (userIdVars == null       &&
          userSIdVars == null      &&
          beaconActionVars == null &&
          beaconDataVars == null)
       {
        postBody = postBodyStringBuilder.toString();
       }
      else
       {
        MNUtils.HttpPostBodyStringBuilder builder = new MNUtils.HttpPostBodyStringBuilder(postBodyStringBuilder);

        long   userId     = session.getMyUserId();
        String userSIdStr = session.getMySId();

        String userIdStr = userId != MNConst.MN_USER_ID_UNDEFINED ?
                            Long.toString(userId) : "";

        AddDynamicVars(builder,userIdVars,userIdStr);
        AddDynamicVars(builder,userSIdVars,userSIdStr);
        AddDynamicVars(builder,beaconActionVars,beaconAction);
        AddDynamicVars(builder,beaconDataVars,beaconData);

        postBody = builder.toString();
       }

      MNURLStringDownloader downloader = new MNURLStringDownloader();

      downloader.loadURL(url,postBody,new MNURLStringDownloader.IEventHandler()
       {
        public void downloaderDataReady (MNURLDownloader downloader, String str)
         {
          dispatchResponseReceivedEvent(str);
         }

        public void downloaderLoadFailed (MNURLDownloader downloader, MNURLDownloader.ErrorInfo errorInfo)
         {
          dispatchResponseReceivedEvent(null);
         }

        private void dispatchResponseReceivedEvent (String responseText)
         {
          if (eventHandler != null)
           {
            eventHandler.onAppBeaconResponseReceived
             (new MNAppBeaconResponse(responseText,requestNumber));
           }
         }
       });
     }

    private void parseTemplate (String urlTemplate, HashMap<String,String> trackingVars)
     {
      int    pos;
      String paramString;

      pos = urlTemplate.indexOf('?');

      if (pos >= 0)
       {
        url         = urlTemplate.substring(0,pos);
        paramString = urlTemplate.substring(pos + 1);
       }
      else
       {
        url         = urlTemplate;
        paramString = "";
       }

      postBodyStringBuilder = new MNUtils.HttpPostBodyStringBuilder();

      userIdVars       = null;
      userSIdVars      = null;
      beaconActionVars = null;
      beaconDataVars   = null;

      if (paramString.length() > 0)
       {
        String[] params = paramString.split("&");

        for (int index = 0; index < params.length; index++)
         {
          String param = params[index];
          String name;
          String value;

          pos = param.indexOf('=');

          if (pos >= 0)
           {
            name  = param.substring(0,pos);
            value = param.substring(pos + 1);
           }
          else
           {
            name  = param;
            value = "";
           }

          String  metaVarName    = getMetaVarName(value);
          boolean useHashedValue = metaVarName != null && metaVarName.startsWith("@");
          String  dynVarName     = useHashedValue ? metaVarName.substring(1) :
                                                    metaVarName;

          if (metaVarName != null)
           {
            value = trackingVars.get(metaVarName);

            if (value != null)
             {
              postBodyStringBuilder.addParamWithEncodingFlags(name,value,false,true);
             }
            else if (dynVarName.equals("mn_user_id"))
             {
              if (userIdVars == null)
               {
                userIdVars = new ArrayList<DynamicVar>();
               }

              userIdVars.add(new DynamicVar(name,useHashedValue));
             }
            else if (dynVarName.equals("mn_user_sid"))
             {
              if (userSIdVars == null)
               {
                userSIdVars = new ArrayList<DynamicVar>();
               }

              userSIdVars.add(new DynamicVar(name,useHashedValue));
             }
            else if (dynVarName.equals("bt_beacon_action_name"))
             {
              if (beaconActionVars == null)
               {
                beaconActionVars = new ArrayList<DynamicVar>();
               }

              beaconActionVars.add(new DynamicVar(name,useHashedValue));
             }
            else if (dynVarName.equals("bt_beacon_data"))
             {
              if (beaconDataVars == null)
               {
                beaconDataVars = new ArrayList<DynamicVar>();
               }

              beaconDataVars.add(new DynamicVar(name,useHashedValue));
             }
            else
             {
              postBodyStringBuilder.addParamWithoutEncoding(name,"");
             }
           }
          else
           {
            postBodyStringBuilder.addParamWithoutEncoding(name,value);
           }
         }
       }
     }

    private static String getMetaVarName (String str)
     {
      if (str.startsWith("{") && str.endsWith("}"))
       {
        return str.substring(1,str.length() - 1);
       }
      else
       {
        return null;
       }
     }

    private String                            url;
    private MNUtils.HttpPostBodyStringBuilder postBodyStringBuilder;
    private ArrayList<DynamicVar>             userIdVars;
    private ArrayList<DynamicVar>             userSIdVars;
    private ArrayList<DynamicVar>             beaconActionVars;
    private ArrayList<DynamicVar>             beaconDataVars;

    private static class DynamicVar
     {
      public DynamicVar (String name, boolean useHashedValue)
       {
        this.name           = name;
        this.useHashedValue = useHashedValue;
       }

      public String getName ()
       {
        return name;
       }

      public boolean getUseHashedValue ()
       {
        return useHashedValue;
       }

      private String  name;
      private boolean useHashedValue;
     }
   }

  private class InstallTracker implements IEventHandler
   {
    public InstallTracker ()
     {
      requestInProgress     = false;
      installAlreadyTracked = false;
     }

    public synchronized void setUrlTemplate (String urlTemplate, MNSession session)
     {
      if (requestInProgress || installAlreadyTracked)
       {
        return;
       }

      String timestamp = session.varStorageGetValueForVariable(RESPONSE_TIMESTAMP_VARNAME);

      if (timestamp != null)
       {
        installAlreadyTracked = true;

        return; // install has been tracked already
       }

      requestInProgress = true;
      this.session      = session;

      UrlTemplate installUrlTemplate = new UrlTemplate(urlTemplate,trackingVars);

      installUrlTemplate.sendInstallTrackingRequest(session,this);
     }

    public synchronized void onAppBeaconResponseReceived (MNAppBeaconResponse response)
     {
      String responseText = response.getResponseText();

      if (responseText != null)
       {
        if (session != null)
         {
          session.varStorageSetValue(RESPONSE_TIMESTAMP_VARNAME,Long.toString(MNUtils.getUnixTime()));

          try
           {
            String encodedText = URLEncoder.encode(responseText,"UTF-8");

            session.varStorageSetValue(RESPONSE_TEXT_VARNAME,encodedText);
           }
          catch (Exception e)
           {
           }

          installAlreadyTracked = true;
         }
       }

      requestInProgress = false;
      this.session      = null;
     }

    private MNSession session;
    private boolean   requestInProgress;
    private boolean   installAlreadyTracked;

    private static final String RESPONSE_TIMESTAMP_VARNAME = "app.install.track.done";
    private static final String RESPONSE_TEXT_VARNAME      = "app.install.track.response";
   }

  private UrlTemplate            beaconUrlTemplate;
  private UrlTemplate            shutdownUrlTemplate;
  private boolean                launchTracked;
  private HashMap<String,String> trackingVars;
  private InstallTracker         installTracker;
  private IEventHandler          eventHandler;
 }

