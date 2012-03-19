package com.playphone.sdk.example;

//import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.playphone.multinet.MNConst;
import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.MNGameParams;
import com.playphone.multinet.MNUserInfo;
import com.playphone.multinet.MNScoreProgressHelper;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MultiPlayerActivity extends CustomTitleActivity implements
		OnClickListener, Callback {
	Handler handler = new Handler(this);

	MNScoreProgressHelper scoreProgress;
	FrameLayout scoreProgressLayout;

	Button minusScoreButton;
	Button plusScoreButton;
	View gameControlsLayout;
	Button postScoreButton;
	TextView gameScoreText;
	EditText editInput;
	Button btnUpload;
	TextView txtResult;
	Button btnBack;
        Button btnPing;

        private static final String PingRequestPrefix  = "PING:";
        private static final String PingResponsePrefix = "PONG:";

	long currentScore = 0;
	
    protected TimerTask getNewTimerTask () {
    	return new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(ON_GAME_TIMER_EVENT);
			}
		};
    }
    
    Timer gameTimer = null;

	protected class SessionEventHandler extends MNSessionEventHandlerAbstract {

		@Override
		public void mnSessionStatusChanged(int newStatus, int oldStatus) {
			handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
		}

		@Override
		public void mnSessionRoomUserStatusChanged(int userStatus) {
			handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
		}

		@Override
		public void mnSessionDoStartGameWithParams(MNGameParams gameParams) {
			handler.sendEmptyMessage(ON_START_GAME_EVENT);
			handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
		}

		@Override
		public void mnSessionDoFinishGame() {
			handler.sendEmptyMessage(ON_FINISH_GAME_EVENT);
			handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
		}

		@Override
		public void mnSessionDoCancelGame() {
			handler.sendEmptyMessage(ON_CANCEL_GAME_EVENT);
			handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
		}

                @Override
                public void mnSessionGameMessageReceived (String      message,
                                                          MNUserInfo sender) {
                        if (message.startsWith(PingRequestPrefix)) {
                                MNDirect.sendGameMessage(PingResponsePrefix + message.substring(PingRequestPrefix.length()));
                        }
                        else if (message.startsWith(PingResponsePrefix)) {
                                long currentTime = System.currentTimeMillis();

                                try {
                                     long sendTime = Long.parseLong(message.substring(PingResponsePrefix.length()));
                                     final long roundTrip = currentTime - sendTime;

                                     final String senderInfo = (sender != null && sender.userName != null) ? sender.userName : "???";

                                     runOnUiThread(new Runnable() {
                                             public void run () {
                                                     long halfTime  = roundTrip / 2;
                                                     String message = String.format("Ping resp. from %s\nDevice-Device time: %d",
                                                                                     senderInfo,halfTime);

                                                     Toast.makeText(MultiPlayerActivity.this,message,Toast.LENGTH_LONG).show();
                                             }
                                     });
                                }
                                catch (Exception e) {
				        Log.d("MultiPlayerActivity","ping response has invalid format: " + message);
                                }
                        }
                }
	}

	SessionEventHandler eh = new SessionEventHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_multiplayer);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Multiplayer");

		scoreProgressLayout = (FrameLayout) findViewById(R.id.scoreProgressLayout);

		minusScoreButton = (Button) findViewById(R.id.minusScoreButton);
		plusScoreButton = (Button) findViewById(R.id.plusScoreButton);
		gameControlsLayout = findViewById(R.id.gameControlsLayout);
		postScoreButton = (Button) findViewById(R.id.postScoreButton);
		gameScoreText = (TextView) findViewById(R.id.gameScoreText);
		editInput = (EditText) findViewById(R.id.editInput);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		txtResult = (TextView) findViewById(R.id.txtResult);
		btnBack = (Button) findViewById(R.id.btnBack);
		btnPing = (Button) findViewById(R.id.btnPing);

                btnPing.setVisibility(View.GONE);

		minusScoreButton.setOnClickListener(this);
		plusScoreButton.setOnClickListener(this);
		btnUpload.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		postScoreButton.setOnClickListener(this);

                btnPing.setOnClickListener(new View.OnClickListener()
                 {
                  public void onClick (View v)
                   {
                    MNDirect.sendGameMessage("PING:" + Long.toString(System.currentTimeMillis()));
                   }
                 });
 
		if (MNDirect.isUserLoggedIn()) {
			handler.sendEmptyMessage(ON_START_GAME_EVENT);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
		MNDirect.getSession().addEventHandler(eh);
		handler.sendEmptyMessage(ON_GAME_STATUS_CHANGED_EVENT);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
		MNDirect.getSession().removeEventHandler(eh);
	}

	@Override
	public void onClick(View v) {
		if (v == minusScoreButton) {
			currentScore -= 10;
			handler.sendEmptyMessage(ON_SCORE_UPDATED_EVENT);
		} else if (v == plusScoreButton) {
			currentScore += 10;
			handler.sendEmptyMessage(ON_SCORE_UPDATED_EVENT);
		} else if (v == btnUpload) {
			EditText textContentView = (EditText) findViewById(R.id.editInput);
			String textContent = textContentView.getText().toString();
			if (!"".equalsIgnoreCase(textContent)) {
				MNDirect.sendGameMessage(textContent);
				Log.d("playphone", "Sending game message: " + textContent);
			}
		} else if (v == postScoreButton) {
			MNDirect.postGameScore(currentScore);
			MNDirectUIHelper.showDashboard();
		} else if (v == btnBack) {
			MNDirect.getSession().leaveRoom();
			finish();
		}
	}

	protected void onStartGame() {
		System.gc();
		
		if (scoreProgress == null) {
			scoreProgress = new MNScoreProgressHelper();

//			if (new Random().nextBoolean()) {
//				scoreProgress.initWithFrame(scoreProgressLayout,
//						R.layout.mnscoreprogresssidebyside);
//			} else {
//				scoreProgress.initWithFrame(scoreProgressLayout,
//						R.layout.mnscoreprogresshorisontal);
//			}
			scoreProgress.initWithFrame(scoreProgressLayout,
					R.layout.mnscoreprogresssidebyside);
		}

		scoreProgress.start();
		
		gameTimer = new Timer("gameTimer");
		gameTimer.scheduleAtFixedRate(getNewTimerTask(), 0, 1000);
	}

	protected void onCancelGame() {
		scoreProgress.stop();
		gameTimer.cancel();
		gameTimer = null;
		System.gc();
	}

	protected void onFinishGame() {
		scoreProgress.stop();
		gameTimer.cancel();
		gameTimer = null;
		System.gc();
	}

	protected void onScoreUpdated() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Score : ").append(currentScore);
		gameScoreText.setText(stringBuffer.toString());
		stringBuffer = null;
	}

	public void startGame() {
		handler.sendEmptyMessage(ON_START_GAME_EVENT);
	}

	protected void updateUIByState() {
		final MNSession session = MNDirect.getSession();

		if (session == null) {
			return;
		}

		switch (session.getStatus()) {
		case MNConst.MN_IN_GAME_WAIT: {
			gameControlsLayout.setVisibility(View.INVISIBLE);
			postScoreButton.setVisibility(View.GONE);
		}
			break;
		case MNConst.MN_IN_GAME_START: {
			gameControlsLayout.setVisibility(View.INVISIBLE);
			postScoreButton.setVisibility(View.GONE);
		}
			break;

		case MNConst.MN_IN_GAME_PLAY: {
			if (session.getRoomUserStatus() == MNConst.MN_USER_PLAYER) {
				gameControlsLayout.setVisibility(View.VISIBLE);
				postScoreButton.setVisibility(View.GONE);
			} else {
				gameControlsLayout.setVisibility(View.INVISIBLE);
				postScoreButton.setVisibility(View.GONE);
			}
		}
			break;

		case MNConst.MN_IN_GAME_END: {
			gameControlsLayout.setVisibility(View.GONE);
			postScoreButton.setVisibility(View.VISIBLE);
		}
			break;
		}
	}

	protected static final int ON_START_GAME_EVENT = 1;
	protected static final int ON_FINISH_GAME_EVENT = 2;
	protected static final int ON_SCORE_UPDATED_EVENT = 3;
	protected static final int ON_CANCEL_GAME_EVENT = 4;
	protected static final int ON_GAME_STATUS_CHANGED_EVENT = 5;
	protected static final int ON_GAME_TIMER_EVENT = 6;

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == ON_START_GAME_EVENT) {
			onStartGame();
		} else if (msg.what == ON_FINISH_GAME_EVENT) {
			onFinishGame();
		} else if (msg.what == ON_CANCEL_GAME_EVENT) {
			onCancelGame();
		} else if (msg.what == ON_SCORE_UPDATED_EVENT) {
			onScoreUpdated();
		} else if (msg.what == ON_GAME_STATUS_CHANGED_EVENT) {
			updateUIByState();
		} else if (msg.what == ON_GAME_TIMER_EVENT) {
			scoreProgress.postScore(currentScore);
		} else {
			return false;
		}

		return true;
	}
}

