//
//  IMNPlatform.java
//  MultiNet client
//
//  Copyright 2009 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;

import java.util.Map;

public interface IMNPlatform
 {
  public static final int PROPERTY_UDID                = 0;
  public static final int PROPERTY_PHONE_DEVICE_ID     = 1;
  public static final int PROPERTY_PHONE_SUBSCRIBER_ID = 2;
  public static final int PROPERTY_PHONE_NUMBER        = 3;

  int              getDeviceType             ();
  String           getDeviceProperty         (int property);
  String           getDeviceInfoString       ();
  String           getMultiNetConfigURL      ();

  String           getAppVerExternal         ();
  String           getAppVerInternal         ();

  Map<String,String> readAppExtParams        ();

  InputStream      openFileForInput          (String path) throws FileNotFoundException;
  OutputStream     openFileForOutput         (String path) throws FileNotFoundException;

  InputStream      openAssetForInput         (String name);

  File             getCacheDir               ();
  File             getMultiNetRootDir        ();

  MNUserProfileView createUserProfileView    ();
  void              runOnUiThread            (Runnable action);

  String           getWiFiMACAddress         ();

  void              logWarning               (String tag, String message);
 }

