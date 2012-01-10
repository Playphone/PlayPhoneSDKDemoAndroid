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
  int              getDeviceType             ();
  String           getUniqueDeviceIdentifier ();
  String           getUniqueDeviceIdentifier2();
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

  void              logWarning               (String tag, String message);
 }

