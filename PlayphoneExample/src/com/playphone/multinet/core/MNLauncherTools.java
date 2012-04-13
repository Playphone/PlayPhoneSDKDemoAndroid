//
//  MNLauncherTools.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//
package com.playphone.multinet.core;

import android.content.Intent;
import android.content.Context;

class MNLauncherTools
 {
  static boolean isApplicationInstalled (Context context, String appPackageName)
   {
    boolean isInstalled = true;

    try
     {
      context.getPackageManager().getApplicationInfo(appPackageName,0);
     }
    catch (Exception e)
     {
      isInstalled = false;
     }

    return isInstalled;
   }

  static boolean launchApplication (Context context, String appPackageName, String param)
   {
    boolean ok = false;

    try
     {
      Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);

      if (intent != null)
       {
        intent.putExtra(LAUNCH_PARAM_INTENT_EXTRA_NAME,param);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

        ok = true;
       }
     }
    catch (Exception e)
     {
     }

    return ok;
   }

  static String getLaunchParam (Intent intent, int gameId)
   {
    if (isLaunchUrlScheme(intent.getScheme(),gameId))
     {
      // it is safe to assume that intent.getData() != null since
      // scheme check passed

      String param = intent.getDataString();

      int pos = param.indexOf(':');

      if (pos < 0)
       {
        return null;
       }

      param = param.substring(pos + 1);

      for (pos = 0;
           pos < 2 && param.length() > 0 && param.charAt(pos) == '/';
           pos++)
       {
       }

      if (pos > 0)
       {
        param = param.substring(pos);
       }

      return param;
     }
    else
     {
      return intent.getStringExtra(LAUNCH_PARAM_INTENT_EXTRA_NAME);
     }
   }

  private static boolean isLaunchUrlScheme (String scheme, int gameId)
   {
    if (scheme == null)
     {
      return false;
     }

    String launchScheme = buildLaunchUrlScheme(gameId);

    int testedSchemeLength = scheme.length();
    int launchSchemeLength = launchScheme.length();

    if      (testedSchemeLength < launchSchemeLength)
     {
      return false;
     }
    else if (testedSchemeLength == launchSchemeLength)
     {
      return scheme.equals(launchScheme);
     }
    else
     {
      return scheme.startsWith(launchScheme) &&
             scheme.charAt(launchSchemeLength) == '-';
     }
   }

  private static String buildLaunchUrlScheme (int gameId)
   {
    return "com-" + INSTANCE_ID + "-game-" + Integer.toString(gameId);
   }

  private static final String INSTANCE_ID                    = "playphone";
  private static final String LAUNCH_PARAM_INTENT_EXTRA_NAME = "com_" + INSTANCE_ID + "_game_start_param";
 }

