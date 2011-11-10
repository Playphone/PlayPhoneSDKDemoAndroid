package com.playphone.sdk.example;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.core.ws.MNWSRequestContent;

public class PlayerLeaderBoardActivity extends CustomTitleActivity implements
OnCheckedChangeListener {
	
	private RadioButton customGame;
	private RadioButton currentGame;
	private RadioButton customPlayer;
	private RadioButton currentPlayer;
	@SuppressWarnings("unused")
	private RadioButton allTime;
	private RadioButton week;
	private RadioButton month;

	private EditText gameId;
	private EditText gameSetId;
	private EditText playerId;
	private Button sendRequest;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_player);
		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs
				.setText("Home > Leaderboards > Details > Player leaderboard");

		customGame = (RadioButton) findViewById(R.id.customGameRadio);
		currentGame = (RadioButton) findViewById(R.id.currentGameRadio);
		customPlayer = (RadioButton) findViewById(R.id.customPlayerRadio);
		currentPlayer = (RadioButton) findViewById(R.id.currentPlayerRadio);
		allTime = (RadioButton) findViewById(R.id.allTimeRadio);
		week = (RadioButton) findViewById(R.id.weekRadio);
		month = (RadioButton) findViewById(R.id.monthRadio);

		customGame.setOnCheckedChangeListener(this);
		currentGame.setOnCheckedChangeListener(this);
		customPlayer.setOnCheckedChangeListener(this);
		currentPlayer.setOnCheckedChangeListener(this);

		int gameIdValue = 0;
		int gameSetValue = 0;
		long playerIdValue = 0;
		if (MNDirect.isOnline()) {
			gameIdValue = MNDirect.getSession().getGameId();
			gameSetValue = MNDirect.getDefaultGameSetId();
			playerIdValue = MNDirect.getSession().getMyUserId();
		}
		gameId = (EditText) findViewById(R.id.gameIdEdit);
		gameId.setText(String.valueOf(gameIdValue));
		gameSetId = (EditText) findViewById(R.id.gameSetEdit);
		gameSetId.setText(String.valueOf(gameSetValue));
		
		
		playerId = (EditText) findViewById(R.id.playerIdEdit);
		playerId.setText(String.valueOf(playerIdValue));

		sendRequest = (Button) findViewById(R.id.sendRequestButton);

		sendRequest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PlayerLeaderBoardActivity.this,
						LeaderboardDetailShowActivity.class);
				
				if (customPlayer.isChecked()) {
					intent.putExtra(
							"request",
							LeaderboardDetailShowActivity.ANYUSERGLOBAL_LEADERBOARD_REQUEST);
				} else {
					intent.putExtra(
							"request",
							LeaderboardDetailShowActivity.CURRUSERLOCAL_LEADERBOARD_REQUEST);
				}
					
				intent.putExtra("scope",
						MNWSRequestContent.LEADERBOARD_SCOPE_GLOBAL);

				intent.putExtra("userid",
						Integer.parseInt(playerId.getText().toString()));
				intent.putExtra("gameid",
						Integer.parseInt(gameId.getText().toString()));
				intent.putExtra("gamesetid",
						Integer.parseInt(gameSetId.getText().toString()));

				if (week.isChecked()) {
					intent.putExtra("period",
							MNWSRequestContent.LEADERBOARD_PERIOD_THIS_WEEK);
				} else if (month.isChecked()) {
					intent.putExtra("period",
							MNWSRequestContent.LEADERBOARD_PERIOD_THIS_MONTH);
				} else /* if (allTime.isChecked()) */{
					intent.putExtra("period",
							MNWSRequestContent.LEADERBOARD_PERIOD_ALL_TIME);
				}

				startActivity(intent);
			}
		});

		updateGameState();
		updatePlayerState();
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
	
	private void updateGameState() {
		// workaround edit on disable text form field 
		final boolean flag = customGame.isChecked();
		gameId.setEnabled(flag);
		gameId.setFocusable(flag);
		gameId.setFocusableInTouchMode(flag);
		if (flag) {
			gameId.requestFocus();
		}
		gameId.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (!flag) {
					return source.length() < 1 ? dest.subSequence(dstart, dend)
							: "";
				}
				return null;
			}
		} });
	}
	
	private void updatePlayerState() {
		// workaround edit on disable text form field 
		final boolean flag = customPlayer.isChecked();
		playerId.setEnabled(flag);
		playerId.setFocusable(flag);
		playerId.setFocusableInTouchMode(flag);
		if (flag) {
			playerId.requestFocus();
		}
		playerId.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (!flag) {
					return source.length() < 1 ? dest.subSequence(dstart, dend)
							: "";
				}
				return null;
			}
		} });
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		updateGameState();
		updatePlayerState();
	}
}
