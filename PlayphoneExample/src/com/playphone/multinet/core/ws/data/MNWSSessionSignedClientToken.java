//
//  MNWSSessionSignedClientToken.java
//  MultiNet client
//
//  Copyright 2011 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core.ws.data;

public class MNWSSessionSignedClientToken extends MNWSGenericItem
 {
  public String getClientTokenBody ()
   {
    return getValueByName("client_token_body");
   }

  public String getClientTokenSign ()
   {
    return getValueByName("client_token_sign");
   }
 }
