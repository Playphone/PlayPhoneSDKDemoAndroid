//
//  MNAppBeaconResponse.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

/**
 * A class representing beacon response.
 */
public class MNAppBeaconResponse
 {
  /**
   * Constructs new <code>MNAppBeaconResponse</code>.
   *
   * @param responseText  text of the response
   * @param callSeqNumber beacon request number
   */
  public MNAppBeaconResponse (String responseText, long callSeqNumber)
   {
    this.responseText  = responseText;
    this.callSeqNumber = callSeqNumber;
   }

  /**
   * Returns beacon response text
   * @return beacon response text
   */
  public String getResponseText ()
   {
    return responseText;
   }

  /**
   * Returns beacon request number
   * @return beacon request number
   */
  public long getCallSeqNumber ()
   {
    return callSeqNumber;
   }

  private String responseText;
  private long   callSeqNumber;
 }

