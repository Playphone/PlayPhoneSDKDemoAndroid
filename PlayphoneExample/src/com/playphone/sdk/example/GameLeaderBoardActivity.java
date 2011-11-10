package com.playphone.sdk.example;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.core.ws.MNWSRequestContent;

public class GameLeaderBoardActivity extends CustomTitleActivity implements
		OnCheckedChangeListener {
	RadioButton customGame;
	RadioButton currentGame;
	RadioButton allTime;
	RadioButton week;
	RadioButton month;

	EditText gameId;
	EditText gameSetId;
	Button sendRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_game);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs
				.setText("Home > Leaderboards > Details > Game leaderboard");

		customGame = (RadioButton) findViewById(R.id.customGameRadio);
		currentGame = (RadioButton) findViewById(R.id.currentGameRadio);
		allTime = (RadioButton) findViewById(R.id.allTimeRadio);
		week = (RadioButton) findViewById(R.id.weekRadio);
		month = (RadioButton) findViewById(R.id.monthRadio);

		customGame.setOnCheckedChangeListener(this);
		currentGame.setOnCheckedChangeListener(this);

		int gameIdValue = 0;
		int gameSetValue = 0;
		if (MNDirect.isOnline()) {
			gameIdValue = MNDirect.getSession().getGameId();
			gameSetValue = MNDirect.getDefaultGameSetId();
		}
		gameId = (EditText) findViewById(R.id.gameIdEdit);
		gameId.setText(String.valueOf(gameIdValue));
		gameSetId = (EditText) findViewById(R.id.gameSetEdit);
		gameSetId.setText(String.valueOf(gameSetValue));

		sendRequest = (Button) findViewById(R.id.sendRequestButton);

		sendRequest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GameLeaderBoardActivity.this,
						LeaderboardDetailShowActivity.class);

				intent.putExtra(
						"request",
						LeaderboardDetailShowActivity.ANYGAME_LEADERBOARD_REQUEST);
				intent.putExtra("scope",
						MNWSRequestContent.LEADERBOARD_SCOPE_GLOBAL);

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

		updateState();
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

	private void updateState() {
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		updateState();
	}
}
