//
//  MNWSSystemGameNetStats.java
//  MultiNet client
//
//  Copyright 2011 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core.ws.data;

public class MNWSSystemGameNetStats extends MNWSGenericItem
 {
  public Long getServTotalUsers ()
   {
    return getLongValue("serv_total_users");
   }

  public Long getServTotalGames ()
   {
    return getLongValue("serv_total_games");
   }

  public Long getServOnlineUsers ()
   {
    return getLongValue("serv_online_users");
   }

  public Long getServOnlineRooms ()
   {
    return getLongValue("serv_online_rooms");
   }

  public Long getServOnlineGames ()
   {
    return getLongValue("serv_online_games");
   }

  public Long getGameOnlineUsers ()
   {
    return getLongValue("game_online_users");
   }

  public Long getGameOnlineRooms ()
   {
    return getLongValue("game_online_rooms");
   }
 }
