package com.playphone.sdk.example;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.playphone.multinet.MNDirect;

import com.playphone.multinet.core.ws.data.MNWSLeaderboardListItem;
import com.playphone.multinet.providers.MNWSInfoRequestLeaderboard;

public class LeaderboardsDetailsActivity extends CustomTitleActivity implements
		Callback {
	private EditText editInput;
	private Button btnLeaderboard;
	private Handler handler = new Handler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_details);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Leaderboards > Details");

		// LinearLayout layout = (LinearLayout)
		// findViewById(R.id.layoutLeaderboards);

		// get intent and set the gamesetid
		int gamesetId = getIntent().getExtras().getInt("leaderboardID");
		MNDirect.setDefaultGameSetId(gamesetId);
		TextView txtLeaderboardName = (TextView) findViewById(R.id.txtLeaderboardName);
		editInput = (EditText) findViewById(R.id.editInput);
		txtLeaderboardName.setText(getLeaderboardName(gamesetId));

		btnLeaderboard = (Button) findViewById(R.id.btnUpdateScore);
		btnLeaderboard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				editInput.clearFocus();
				MNDirect.postGameScore(Long.valueOf(editInput.getText()
						.toString()));
				Toast.makeText(
						LeaderboardsDetailsActivity.this,
						"Updated your score of "
								+ editInput.getText().toString()
								+ " on the Leaderboards", Toast.LENGTH_LONG)
						.show();
				leaderBoardRequest();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		leaderBoardRequest();
	}

	private String getLeaderboardName(int gamesetId) {
		if (gamesetId == 1)
			return "Simple";
		if (gamesetId == 2)
			return "Advanced";
		return "Default";
	}

	protected class MyLeaderboardResponseHandler implements
			MNWSInfoRequestLeaderboard.IEventHandler {
          public void onCompleted (MNWSInfoRequestLeaderboard.RequestResult result) {
            if (!result.hadError()) {
              MNWSLeaderboardListItem[] leaderboard = result.getDataEntry();

              // Iterate over the returned list and print the name of the player
              // and his/her highest score
              ArrayList<String> usernames = new ArrayList<String>();
              ArrayList<String> scores = new ArrayList<String>();

              for (MNWSLeaderboardListItem item : leaderboard) {
                Log.d("playphone","Player : " + item.getUserNickName()
                                  + " gamesetid: " + String.valueOf(item.getGamesetId())
                                  + " score: " + item.getOutHiScoreText());
                usernames.add(item.getUserNickName());
                scores.add(item.getOutHiScoreText());
               }

              Message msg = Message.obtain();
              Bundle bundle = new Bundle();
              bundle.putStringArrayList("usernames", usernames);
              bundle.putStringArrayList("scores", scores);
              msg.setData(bundle);
              LeaderboardsDetailsActivity.this.handler.sendMessage(msg);
            }
            else {
              Toast.makeText(LeaderboardsDetailsActivity.this,
                             result.getErrorMessage(),Toast.LENGTH_LONG).show();
              // log error message
              Log.e("LeaderboardResponse",result.getErrorMessage());
            }
          }
	}

	private void leaderBoardRequest() {
          MNDirect.getWSProvider()
           .send(new MNWSInfoRequestLeaderboard
             (new MNWSInfoRequestLeaderboard.LeaderboardModeCurrentUser
                  (MNWSInfoRequestLeaderboard.LEADERBOARD_SCOPE_GLOBAL,
                   MNWSInfoRequestLeaderboard.LEADERBOARD_PERIOD_ALL_TIME),
              new MyLeaderboardResponseHandler()));
	}

	@Override
	public boolean handleMessage(Message msg) {
		ArrayList<String> usernames = msg.getData().getStringArrayList(
				"usernames");
		ArrayList<String> scores = msg.getData().getStringArrayList("scores");

		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutLeaderboards);
		layout.removeAllViews();

		// for each item create a button and add it to the layout
		for (int i = 0; i < usernames.size(); i++) {
			String place = String.valueOf(i + 1);
			TextView txtLeaderboardItem = new TextView(layout.getContext());
			txtLeaderboardItem.setText(place + ". " + usernames.get(i)
					+ "                  " + scores.get(i));
			layout.addView(txtLeaderboardItem);
			Log.d("playphone", "added user" + usernames.get(i));
		}

		layout.invalidate();
		return false;
	}
}
