package com.playphone.sdk.example;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.playphone.multinet.MNConst;
import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.data.MNWSLeaderboardListItem;
import com.playphone.multinet.providers.MNWSInfoRequestLeaderboard;

public class LeaderboardDetailShowActivity extends CustomTitleListActivity
		implements Callback, OnClickListener {

	public static final String CURRUSER_LEADERBOARD_REQUEST = "currUser";
	public static final String ANYGAME_LEADERBOARD_REQUEST = "anyGame";
	public static final String ANYUSERGLOBAL_LEADERBOARD_REQUEST = "anyUserGlobal";
	public static final String CURRUSERLOCAL_LEADERBOARD_REQUEST = "currUserLocal";

	ArrayAdapter<MNWSLeaderboardListItem> aa = null;
	Handler handler = new Handler(this);
	ArrayList<MNWSLeaderboardListItem> leaderboard = new ArrayList<MNWSLeaderboardListItem>();
	int scope;
	int period;
	int gameId;
	int gameSetId;
	long userId;
	Button backBtn;
	Button refreshBtn;
	String request;

	private void listUpdate() {
		aa.notifyDataSetChanged();
		handler.sendEmptyMessage(UPDATE_OK_COMMAND);
	}

	private void leaderBoardRequest() {
          MNWSInfoRequestLeaderboard.LeaderboardMode mode = null;

          if (request.equalsIgnoreCase(CURRUSER_LEADERBOARD_REQUEST)) {
            mode = new MNWSInfoRequestLeaderboard.LeaderboardModeCurrentUser(scope,period);
          }
          else if (request.equalsIgnoreCase(ANYGAME_LEADERBOARD_REQUEST)) {
            mode = new MNWSInfoRequestLeaderboard.LeaderboardModeAnyGameGlobal(gameId,gameSetId,period);
          }
          else if (request.equalsIgnoreCase(ANYUSERGLOBAL_LEADERBOARD_REQUEST)) {
            mode = new MNWSInfoRequestLeaderboard.LeaderboardModeAnyUserAnyGameGlobal(userId,gameId,gameSetId,period);
          }
          else if (request.equalsIgnoreCase(CURRUSERLOCAL_LEADERBOARD_REQUEST)) {
            mode = new MNWSInfoRequestLeaderboard.LeaderboardModeCurrUserAnyGameLocal(gameId,gameSetId,period);
          }
          else {
            handler.sendEmptyMessage(INVALID_REQUEST);
            return;
          }

          MNDirect.getWSProvider().send
           (new MNWSInfoRequestLeaderboard
             (mode,new MyLeaderboardResponseHandler()));
          handler.sendEmptyMessage(UPDATE_BEGIN_COMMAND);
        }

	class MyLeaderboardResponseHandler implements MNWSInfoRequestLeaderboard.IEventHandler {
          public void onCompleted (MNWSInfoRequestLeaderboard.RequestResult result) {
            if (!result.hadError()) {
              leaderboard.clear();

              for (MNWSLeaderboardListItem item : result.getDataEntry()) {
                leaderboard.add(item);
              }

              handler.sendEmptyMessage(LIST_UPDATE_COMMAND);
            }
            else {
              Log.e("PlayerLeaderBoardActivity",result.getErrorMessage());
            }
          }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_info);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Leaderboard info");

		int defaultGameId = 0;
		int defaultGameSetId = 0;
		long defaultUserId = MNConst.MN_USER_ID_UNDEFINED;
		if (MNDirect.isOnline()) {
			defaultGameId = MNDirect.getSession().getGameId();
			defaultGameSetId = MNDirect.getDefaultGameSetId();
			defaultUserId = MNDirect.getSession().getMyUserId();
		}

		request = getIntent().getStringExtra("request");

		scope = getIntent().getIntExtra("scope",
				MNWSRequestContent.LEADERBOARD_SCOPE_GLOBAL);
		period = getIntent().getIntExtra("period",
				MNWSRequestContent.LEADERBOARD_PERIOD_ALL_TIME);
		gameId = getIntent().getIntExtra("gameid", defaultGameId);
		gameSetId = getIntent().getIntExtra("gamesetid", defaultGameSetId);
		userId = getIntent().getLongExtra("userid", defaultUserId);

		aa = new ArrayAdapter<MNWSLeaderboardListItem>(this,
				R.layout.leaderboard_item, leaderboard) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;

				if (row == null) {
					LayoutInflater li = getLayoutInflater();
					row = li.inflate(R.layout.leaderboard_item, null);
				}

				final MNWSLeaderboardListItem item = leaderboard.get(position);

				((TextView) row.findViewById(R.id.place)).setText(String
						.valueOf(item.getOutUserPlace()));
				((TextView) row.findViewById(R.id.nickname)).setText(String
						.valueOf(item.getUserNickName()));
				((TextView) row.findViewById(R.id.score)).setText(String
						.valueOf(item.getOutHiScore()));
				return row;
			}

		};

		setListAdapter(aa);

		backBtn = (Button) findViewById(R.id.returnButton);
		backBtn.setOnClickListener(this);
		refreshBtn = (Button) findViewById(R.id.refreshButton);
		refreshBtn.setOnClickListener(this);

		leaderBoardRequest();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
	}

	@Override
	public void onClick(View v) {
		if (v == refreshBtn) {
			leaderBoardRequest();
		} else if (v == backBtn) {
			finish();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == LIST_UPDATE_COMMAND) {
			listUpdate();
		} else if (msg.what == UPDATE_BEGIN_COMMAND) {
			Toast.makeText(this, "Scoreboard request sended",
					Toast.LENGTH_SHORT).show();
		} else if (msg.what == UPDATE_OK_COMMAND) {
			Toast.makeText(this, "Scoreboard request is ok", Toast.LENGTH_SHORT)
					.show();
		} else if (msg.what == INVALID_REQUEST) {
			Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
		} else {
			return false;
		}
		return true;
	}

	private static final int LIST_UPDATE_COMMAND = 0;
	private static final int UPDATE_BEGIN_COMMAND = 1;
	private static final int UPDATE_OK_COMMAND = 2;
	private static final int INVALID_REQUEST = 3;

}
