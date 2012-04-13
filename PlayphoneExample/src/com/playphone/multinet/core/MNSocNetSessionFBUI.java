//
//  MNSocNetSessionFBUI.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.DialogError;

class MNSocNetSessionFBUI
 {
  public static interface IFBDialogEventHandler
   {
    void onSuccess (Bundle response);
    void onError   (String message);
    void onCancel  ();
   }

  public static boolean authorize (Context context, Facebook facebook, String[] permissions, boolean useSSO, IFBDialogEventHandler dialogEventHandler)
   {
    LoginActivityEventHandler eventHandler = new LoginActivityEventHandler(facebook,permissions,useSSO,dialogEventHandler);

    if (!MNProxyActivity.startProxyActivity(context,eventHandler))
     {
      eventHandler.cleanup();

      return false;
     }
    else
     {
      return true;
     }
   }

  public static void publish (Context context, Facebook facebook,
                              String prompt, String attachment,
                              String targetId, String actionLinks,
                              IFBDialogEventHandler dialogEventHandler)
   {
    PublishActivityEventHandler eventHandler =
     new PublishActivityEventHandler
          (facebook,prompt,attachment,targetId,actionLinks,dialogEventHandler);

    if (!MNProxyActivity.startProxyActivity(context,eventHandler))
     {
      eventHandler.cleanup();
     }
   }

  public static void askPermissions (Context context, Facebook facebook,
                                     String permission,
                                     IFBDialogEventHandler dialogEventHandler)
   {
    AskPermissionsActivityEventHandler eventHandler =
     new AskPermissionsActivityEventHandler
          (facebook,permission,dialogEventHandler);

    if (!MNProxyActivity.startProxyActivity(context,eventHandler))
     {
      eventHandler.cleanup();
     }
   }

  public static void showGenericDialog (Context  context,
                                        Facebook facebook,
                                        String   action,
                                        Map<String,String> params,
                                        IFBDialogEventHandler dialogEventHandler)
   {
    GenericDialogActivityEventHandler eventHandler =
     new GenericDialogActivityEventHandler
          (facebook,action,params,dialogEventHandler);

    if (!MNProxyActivity.startProxyActivity(context,eventHandler))
     {
      eventHandler.cleanup();
     }
   }

  private abstract static class ActivityEventHandlerBase extends MNProxyActivity.ActivityDelegate implements Facebook.DialogListener
   {
    public ActivityEventHandlerBase (Facebook facebook, IFBDialogEventHandler dialogEventHandler)
     {
      this.facebook           = facebook;
      this.dialogEventHandler = dialogEventHandler;
      this.activity           = null;
     }

    public void onCreate (Activity activity, Bundle savedInstanceState)
     {
      this.activity = activity;

      LinearLayout layout = new LinearLayout(activity);

      layout.setOrientation(LinearLayout.VERTICAL);

      activity.setContentView(layout);

      executeFacebookCall();
     }

    public synchronized void cleanup ()
     {
      facebook           = null;
      dialogEventHandler = null;
      activity           = null;
     }

    public void onDestroy (Activity activity)
     {
      cleanup();
     }

    public synchronized void onComplete(Bundle values)
     {
      if (dialogEventHandler != null)
       {
        dialogEventHandler.onSuccess(values);
       }

      activity.finish();
     }

    public synchronized void onFacebookError(FacebookError e)
     {
      if (dialogEventHandler != null)
       {
        dialogEventHandler.onError(e.toString());
       }

      activity.finish();
     }

    public synchronized void onError(DialogError e)
     {
      if (dialogEventHandler != null)
       {
        dialogEventHandler.onError(e.toString());
       }

      activity.finish();
     }

    public synchronized void onCancel()
     {
      if (dialogEventHandler != null)
       {
        dialogEventHandler.onCancel();
       }

      activity.finish();
     }

    protected abstract void executeFacebookCall ();

    protected Facebook              facebook;
    protected IFBDialogEventHandler dialogEventHandler;
    protected Activity              activity;
   }

  private static class LoginActivityEventHandler extends ActivityEventHandlerBase
   {
    public LoginActivityEventHandler (Facebook facebook, String[] permissions, boolean useSSO, IFBDialogEventHandler dialogEventHandler)
     {
      super(facebook,dialogEventHandler);

      this.permissions = permissions != null ? permissions : new String[0];
      this.useSSO      = useSSO;
     }

    protected void executeFacebookCall ()
     {
      if (useSSO)
       {
        facebook.authorize(activity,permissions,this);
       }
      else
       {
        facebook.authorize(activity,permissions,Facebook.FORCE_DIALOG_AUTH,this);
       }
     }

    public void onActivityResult (Activity activity, int requestCode, int resultCode, Intent data)
     {
      facebook.authorizeCallback(requestCode,resultCode,data);
     }

    private final String[] permissions;
    private final boolean  useSSO;
   }

  private static class PublishActivityEventHandler extends ActivityEventHandlerBase
   {
    public PublishActivityEventHandler (Facebook facebook,
                                        String prompt, String attachment,
                                        String targetId, String actionLinks,
                                        IFBDialogEventHandler dialogEventHandler)
     {
      super(facebook,dialogEventHandler);

      this.prompt      = prompt;
      this.attachment  = attachment;
      this.targetId    = targetId;
      this.actionLinks = actionLinks;
     }

    protected void executeFacebookCall ()
     {
      Bundle params = new Bundle();

      params.putString("message",prompt);
      params.putString("attachment",attachment);
      params.putString("action_links",actionLinks);
      params.putString("target_id",targetId);

      facebook.dialog(activity,"stream.publish",params,this);
     }

    private String prompt;
    private String attachment;
    private String targetId;
    private String actionLinks;
   }

  private static class AskPermissionsActivityEventHandler extends ActivityEventHandlerBase
   {
    public AskPermissionsActivityEventHandler (Facebook facebook,
                                               String permissions,
                                               IFBDialogEventHandler dialogEventHandler)
     {
      super(facebook,dialogEventHandler);

      this.permissions = permissions;
     }

    protected void executeFacebookCall ()
     {
      Bundle params = new Bundle();

      params.putString("perms",permissions);

      facebook.dialog(activity,"permissions.request",params,this);
     }

    private String permissions;
   }

  private static class GenericDialogActivityEventHandler extends ActivityEventHandlerBase
   {
    public GenericDialogActivityEventHandler (Facebook              facebook,
                                              String                action,
                                              Map<String,String>    params,
                                              IFBDialogEventHandler dialogEventHandler)
     {
      super(facebook,dialogEventHandler);

      this.action = action;
      this.params = new Bundle();

      for (String key : params.keySet())
       {
        this.params.putString(key,params.get(key));
       }
     }

    protected void executeFacebookCall ()
     {
      facebook.dialog(activity,action,params,this);
     }

    private String action;
    private Bundle params;
   }
 }

