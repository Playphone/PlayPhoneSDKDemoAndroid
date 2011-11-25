//
//  MNSocNetSessionFB.java
//  MultiNet client
//
//  Copyright 2009 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.net.MalformedURLException;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.android.Facebook;

public class MNSocNetSessionFB
 {
  public static final int USER_ID_UNDEFINED = -1;
  public static final int SOCNET_ID = 1;

  public MNSocNetSessionFB (IMNPlatform platform,IEventHandler eventHandler)
   {
    this.platform     = (MNPlatformAndroid)platform;
    this.eventHandler = eventHandler;
    this.facebook     = null;
   }

  private static String getPrefNameForFbApp (String prefName, String fbAppId)
   {
    return prefName + "_" + fbAppId;
   }

  private void restoreAccessToken ()
   {
    String fbAppId = facebook.getAppId();
    SharedPreferences preferences = platform.getContext().getSharedPreferences
                                     (FbSSOTokenStorageName,Context.MODE_PRIVATE);

    String accessToken = preferences.getString
                          (getPrefNameForFbApp("access_token",fbAppId),null);
    long   expires     = preferences.getLong
                          (getPrefNameForFbApp("access_expires",fbAppId),0);

    if (accessToken != null)
     {
      facebook.setAccessToken(accessToken);
     }

    if (expires != 0)
     {
      facebook.setAccessExpires(expires);
     }
   }

  private synchronized void storeAccessToken ()
   {
    String fbAppId = facebook.getAppId();
    SharedPreferences.Editor editor = platform.getContext().getSharedPreferences
                                       (FbSSOTokenStorageName,Context.MODE_PRIVATE).edit();

    editor.putString(getPrefNameForFbApp("access_token",fbAppId),
                     facebook.getAccessToken());
    editor.putLong(getPrefNameForFbApp("access_expires",fbAppId),
                     facebook.getAccessExpires());

    editor.commit();
   }

  private void removeAccessToken()
   {
    String fbAppId = facebook.getAppId();
    SharedPreferences.Editor editor = platform.getContext().getSharedPreferences
                                       (FbSSOTokenStorageName,Context.MODE_PRIVATE).edit();

    editor.remove(getPrefNameForFbApp("access_token",fbAppId));
    editor.remove(getPrefNameForFbApp("access_expires",fbAppId));

    editor.commit();
   }

  public synchronized void setFbAppId (String fbAppId)
   {
    facebook = new Facebook(fbAppId);

    restoreAccessToken();
   }

  public synchronized String connect (String[] permissions)
   {
    if (facebook == null)
     {
      return MNI18n.getLocalizedString
              ("Facebook application id is undefined",
               MNI18n.MESSAGE_CODE_FACEBOOK_API_KEY_OR_SESSION_PROXY_URL_IS_INVALID_OR_NOT_SET_ERROR);
     }

    MNSocNetSessionFBUI.authorize(platform.getContext(),
                                  facebook,
                                  permissions,
                                  useSSO,
                                  new MNSocNetSessionFBUI.IFBDialogEventHandler()
     {
      public void onSuccess ()
       {
        storeAccessToken();

        eventHandler.socNetFBLoginOk(MNSocNetSessionFB.this);
       }

      public void onError   (String message)
       {
        eventHandler.socNetFBLoginFailedWithError("Facebook connection failed (" + message + ")");
       }

      public void onCancel  ()
       {
        eventHandler.socNetFBLoginCanceled();
       }
     });

    return null;
   }

  public synchronized String resume ()
   {
    if (facebook == null)
     {
      return MNI18n.getLocalizedString
              ("Facebook application id is undefined",
               MNI18n.MESSAGE_CODE_FACEBOOK_API_KEY_OR_SESSION_PROXY_URL_IS_INVALID_OR_NOT_SET_ERROR);
     }

    if (facebook.isSessionValid())
     {
      eventHandler.socNetFBLoginOk(MNSocNetSessionFB.this);

      return null;
     }
    else
     {
      return MNI18n.getLocalizedString
              ("Facebook connection failed (session cannot be resumed)",
               MNI18n.MESSAGE_CODE_FACEBOOK_CONNECTION_RESUME_FAILED_ERROR);
     }
   }

  public synchronized void logout ()
   {
    if (facebook != null)
     {
      removeAccessToken();

      // logout call is a blocking call, so we have to run it on separate
      // thread to prevent ANR
      final Facebook fb      = facebook;
      final Context  context = platform.getContext();

      (new Thread()
        {
         public void run ()
          {
           try
            {
             fb.logout(context);
            }
           catch (MalformedURLException e)
            {
            }
           catch (IOException e)
            {
            }
          }
        }).start();
     }
   }

  public synchronized boolean isConnected ()
   {
    return facebook != null && facebook.isSessionValid();
   }

  public Facebook getFBConnect ()
   {
    return facebook;
   }

  public long getUserId ()
   {
    return USER_ID_UNDEFINED;
   }

  public synchronized String getSessionKey ()
   {
    if (isConnected())
     {
      return "";
     }
    else
     {
      return null;
     }
   }

  public synchronized String getSessionSecret ()
   {
    if (isConnected())
     {
      return facebook.getAccessToken();
     }
    else
     {
      return null;
     }
   }

  public synchronized boolean didUserStoreCredentials ()
   {
    return isConnected() && facebook.getAccessExpires() != 0;
   }

  public synchronized String showStreamDialog (String prompt,
                                               String attachment,
                                               String targetId,
                                               String actionLinks,
                                               final IStreamDialogEventHandler eventHandler)
   {
    if (isConnected())
     {
      MNSocNetSessionFBUI.publish(platform.getContext(),facebook,
                                  prompt,attachment,targetId,actionLinks,
                                  new MNSocNetSessionFBUI.IFBDialogEventHandler()
       {
        public void onSuccess ()
         {
          eventHandler.socNetFBStreamDialogOk();
         }

        public void onError   (String message)
         {
          eventHandler.socNetFBStreamDialogFailedWithError(message);
         }

        public void onCancel  ()
         {
          eventHandler.socNetFBStreamDialogCanceled();
         }
       });
     }

    return null;
   }

  public synchronized String showPermissionDialog (String permission,
                                                   final IPermissionDialogEventHandler eventHandler)
   {
    if (isConnected())
     {
      MNSocNetSessionFBUI.askPermissions(platform.getContext(),facebook,
                                         permission,
                                         new MNSocNetSessionFBUI.IFBDialogEventHandler()
       {
        public void onSuccess ()
         {
          eventHandler.socNetFBPermissionDialogOk();
         }

        public void onError   (String message)
         {
          eventHandler.socNetFBPermissionDialogFailedWithError(message);
         }

        public void onCancel  ()
         {
          eventHandler.socNetFBPermissionDialogCanceled();
         }
       });
     }

    return null;
   }

  interface IEventHandler
   {
    void socNetFBLoginOk              (MNSocNetSessionFB session);
    void socNetFBLoginCanceled        ();
    void socNetFBLoginFailedWithError (String            error);
    void socNetFBLoggedOut            ();
   }

  interface IStreamDialogEventHandler
   {
    void socNetFBStreamDialogOk                 ();
    void socNetFBStreamDialogCanceled           ();
    void socNetFBStreamDialogFailedWithError    (String    error);
   }

  interface IPermissionDialogEventHandler
   {
    void socNetFBPermissionDialogOk             ();
    void socNetFBPermissionDialogCanceled       ();
    void socNetFBPermissionDialogFailedWithError(String    error);
   }

  void enableSingleSignOn (boolean enable)
   {
    this.useSSO = enable;
   }

  Facebook getFacebook ()
   {
    return facebook;
   }

  MNPlatformAndroid platform;
  private IEventHandler eventHandler;
  private Facebook facebook;
  private boolean useSSO;

  private static final String FbSSOTokenStorageName = "MNFbSSOData";
 }

