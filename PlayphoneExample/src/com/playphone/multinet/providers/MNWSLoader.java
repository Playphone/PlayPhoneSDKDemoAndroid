//
//  MNWSLoader.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.IMNWSRequest;

public class MNWSLoader
 {
  public void cancel ()
   {
    request.cancel();
   }

  /*package*/ MNWSLoader (IMNWSRequest request)
   {
    this.request = request;
   }

  private IMNWSRequest request;
 }

