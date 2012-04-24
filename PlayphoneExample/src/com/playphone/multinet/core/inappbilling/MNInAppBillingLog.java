//
//  MNInAppBillingLog.java
//  MultiNet client
//
//  Copyright 2011 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core.inappbilling;

public class MNInAppBillingLog
 {
  public static void i (Object source, String message)
   {
    logMessage(source,LOG_LEVEL_INFO,message);
   }

  public static void w (Object source, String message)
   {
    logMessage(source,LOG_LEVEL_WARNING,message);
   }

  public static void e (Object source, String message)
   {
    logMessage(source,LOG_LEVEL_ERROR,message);
   }

  public static void d (Object source, String message)
   {
    if (debugLogEnabled)
     {
      logMessage(source,LOG_LEVEL_DEBUG,message);
     }
   }

  public static void enableDebugLog ()
   {
    debugLogEnabled = true;
   }

  public static void disableDebugLog ()
   {
    debugLogEnabled = false;
   }

  private static void logMessage (Object source, String level, String message)
   {
    String sourceName;

    if (source instanceof Class)
     {
      sourceName = ((Class)source).getSimpleName();
     }
    else
     {
      sourceName = source.getClass().getSimpleName();
     }

    System.out.println(sourceName + ": " + level + ": " + message);
   }

  private static boolean debugLogEnabled = false;

  private static final String LOG_LEVEL_DEBUG   = "DEBUG";
  private static final String LOG_LEVEL_INFO    = "INFO";
  private static final String LOG_LEVEL_WARNING = "WARN";
  private static final String LOG_LEVEL_ERROR   = "ERR";
 }

