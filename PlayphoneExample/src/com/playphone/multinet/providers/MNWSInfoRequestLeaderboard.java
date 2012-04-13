//
//  MNWSInfoRequestLeaderboard.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import java.util.ArrayList;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSLeaderboardListItem;

public class MNWSInfoRequestLeaderboard extends MNWSInfoRequest
 {
  public static final int LEADERBOARD_PERIOD_ALL_TIME   = MNWSRequestContent.LEADERBOARD_PERIOD_ALL_TIME;
  public static final int LEADERBOARD_PERIOD_THIS_WEEK  = MNWSRequestContent.LEADERBOARD_PERIOD_THIS_WEEK;
  public static final int LEADERBOARD_PERIOD_THIS_MONTH = MNWSRequestContent.LEADERBOARD_PERIOD_THIS_MONTH;

  public static final int LEADERBOARD_SCOPE_GLOBAL = MNWSRequestContent.LEADERBOARD_SCOPE_GLOBAL;
  public static final int LEADERBOARD_SCOPE_LOCAL  = MNWSRequestContent.LEADERBOARD_SCOPE_LOCAL;

  public static class LeaderboardModeCurrentUser extends LeaderboardMode
   {
    public LeaderboardModeCurrentUser (int scope, int period)
     {
      this.scope  = scope;
      this.period = period;
     }

    protected String addContent (MNWSRequestContent content)
     {
      return content.addCurrUserLeaderboard(scope,period);
     }

    private final int scope;
    private final int period;
   }

  public static class LeaderboardModeAnyGameGlobal extends LeaderboardMode
   {
    public LeaderboardModeAnyGameGlobal (int gameId, int gameSetId, int period)
     {
      this.gameId    = gameId;
      this.gameSetId = gameSetId;
      this.period    = period;
     }

    protected String addContent (MNWSRequestContent content)
     {
      return content.addAnyGameLeaderboardGlobal(gameId,gameSetId,period);
     }

    private final int gameId;
    private final int gameSetId;
    private final int period;
   }

  public static class LeaderboardModeAnyUserAnyGameGlobal extends LeaderboardMode
   {
    public LeaderboardModeAnyUserAnyGameGlobal (long userId, int gameId, int gameSetId, int period)
     {
      this.userId    = userId;
      this.gameId    = gameId;
      this.gameSetId = gameSetId;
      this.period    = period;
     }

    protected String addContent (MNWSRequestContent content)
     {
      return content.addAnyUserAnyGameLeaderboardGlobal(userId,gameId,gameSetId,period);
     }

    private final long userId;
    private final int  gameId;
    private final int  gameSetId;
    private final int  period;
   }

  public static class LeaderboardModeCurrUserAnyGameLocal extends LeaderboardMode
   {
    public LeaderboardModeCurrUserAnyGameLocal (int gameId, int gameSetId, int period)
     {
      this.gameId    = gameId;
      this.gameSetId = gameSetId;
      this.period    = period;
     }

    protected String addContent (MNWSRequestContent content)
     {
      return content.addCurrUserAnyGameLeaderboardLocal(gameId,gameSetId,period);
     }

    private final int  gameId;
    private final int  gameSetId;
    private final int  period;
   }

  public abstract static class LeaderboardMode
   {
    protected abstract String addContent (MNWSRequestContent content);
   }

  public MNWSInfoRequestLeaderboard (LeaderboardMode mode, IEventHandler eventHandler)
   {
    this.leaderboardMode = mode;
    this.eventHandler    = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = leaderboardMode.addContent(content);
   }

  @SuppressWarnings("unchecked")
  void handleRequestCompleted (MNWSResponse       response)
   {
    ArrayList<MNWSLeaderboardListItem> data = (ArrayList<MNWSLeaderboardListItem>)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult(data != null ?
                                             data.toArray(new MNWSLeaderboardListItem[data.size()]) :
                                             new MNWSLeaderboardListItem[0]);

    eventHandler.onCompleted(result);
   }

  void handleRequestError (MNWSRequestError   error)
   {
    RequestResult result = new RequestResult(null);

    result.setError(error.getMessage());

    eventHandler.onCompleted(result);
   }

  public interface IEventHandler
   {
    public void onCompleted (RequestResult result);
   }

  public static class EventHandlerAbstract implements IEventHandler
   {
    public void onCompleted (RequestResult result)
     {
     }
   }

  public static class RequestResult extends MNWSInfoRequest.RequestResult
   {
    public MNWSLeaderboardListItem[] getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSLeaderboardListItem[] data)
     {
      super();

      this.data = data;
     }

    private MNWSLeaderboardListItem[] data;
   }

  private final LeaderboardMode leaderboardMode;
  private final IEventHandler   eventHandler;
  private String                blockName;
 }

