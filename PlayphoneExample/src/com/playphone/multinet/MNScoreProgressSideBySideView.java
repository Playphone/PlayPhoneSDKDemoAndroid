package com.playphone.multinet;

import com.playphone.multinet.MNConst;
import com.playphone.multinet.MNUserInfo;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.providers.MNScoreProgressProvider.ScoreItem;
import com.playphone.multinet.providers.MNScoreProgressProvider.IEventHandler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MNScoreProgressView class
 */
class MNScoreProgressSideBySideView extends LinearLayout implements
		IEventHandler, Callback {
	boolean debugLog = true;

	/* in message object our Bitmap waiting */
	private static final int OUR_AVATAR_UPDATED_EVENT = 1;
	/* in message object waiting opponent Bitmap */
	private static final int OPPONENT_AVATAR_UPDATED_EVENT = 2;
	/* in message object ScoreItem[] array */
	private static final int SCOREBOARD_UPDATED_EVENT = 3;

	private Handler handler = new Handler(this);

	View left_user_layout;
	ImageView left_avatar;
	TextView left_user_name;
	TextView left_user_score;
	TextView left_user_status;

	View right_user_layout;
	ImageView right_avatar;
	TextView right_user_name;
	TextView right_user_score;
	TextView right_user_status;

	private final Resources res;
	private final int fieldBlueId;
	private final int fieldGreenId;
	private final int fieldRedId;
	private final int defaultAvatarId;

	/**
	 * Construct ScoreProgressView class
	 * 
	 * @param context
	 * @param attrs
	 */
	public MNScoreProgressSideBySideView(Context context, AttributeSet attrs) {
		super(context, attrs);

		res = context.getApplicationContext().getResources();
		fieldBlueId = res.getIdentifier("mnprogressindicator_sticker_blue",
				"drawable", context.getPackageName());
		fieldGreenId = res.getIdentifier("mnprogressindicator_sticker_green",
				"drawable", context.getPackageName());
		fieldRedId = res.getIdentifier("mnprogressindicator_sticker_red",
				"drawable", context.getPackageName());
		defaultAvatarId = res.getIdentifier(
				"mnprogressindicator_sticker_avatar", "drawable",
				context.getPackageName());
	}

	private ScoreItem getMyScore(ScoreItem[] scoreBoard) {
		final MNSession session = MNDirect.getSession();
		if (session != null) {
			for (ScoreItem item : scoreBoard) {
				if (item.userInfo.userId == session.getMyUserId())
					return item;
			}
		}
		return null;
	}

	private ScoreItem getBestOpponentScore(ScoreItem[] scoreBoard) {
		// TODO : search real best opponent
		final MNSession session = MNDirect.getSession();
		if (session != null) {
			for (ScoreItem item : scoreBoard) {
				if (item.userInfo.userId != session.getMyUserId())
					return item;
			}
		}
		return null;
	}

	// <begin implementationOf="MNScoreProgress.IProgressHandler">

	private static final int MAX_INFO_LENGTH = 10;

	private final char[] ourScore = new char[MAX_INFO_LENGTH];
	private final char[] ourStatus = new char[MAX_INFO_LENGTH + 2];
	private long ourAvatarId = MNConst.MN_USER_ID_UNDEFINED;

	private final char[] oppScore = new char[MAX_INFO_LENGTH];
	private final char[] oppStatus = new char[MAX_INFO_LENGTH + 2];
	private long oppAvatarId = MNConst.MN_USER_ID_UNDEFINED;

	private final int fillNumberBuffer(final char[] buffer, final long value) {
		if (value <= 0) {
			buffer[MAX_INFO_LENGTH - 1] = '0';
			return 1;
		}

		int len = 0;
		long tmp = value;

		while (tmp > 0) {
			long dig = tmp % 10;
			tmp /= 10;
			len++;
			buffer[MAX_INFO_LENGTH - len] = (char) ('0' + dig);
		}

		return len;
	}

	private static final String POS_FIRST = "1st";
	private static final String POS_FIRST_POSTFIX = "st";
	private static final String POS_SECOND_POSTFIX = "nd";
	private static final String POS_THIRD_POSTFIX = "rd";
	private static final String POS_TH_POSTFIX = "th";

	private final int fillStatusBuffer(final char[] buffer, final int value) {
		if (value <= 0) {
			buffer[MAX_INFO_LENGTH - 1] = '0';
			buffer[MAX_INFO_LENGTH] = 't';
			buffer[MAX_INFO_LENGTH + 1] = 'h';
			return 1;
		}

		int len = fillNumberBuffer(buffer, value);

		int mod10 = value % 10;
		int mod100 = value % 100;

		final String postfix;

		if ((mod10 == 1) && (mod100 != 11)) {
			postfix = POS_FIRST_POSTFIX;
		} else if ((mod10 == 2) && (mod100 != 12)) {
			postfix = POS_SECOND_POSTFIX;
		} else if ((mod10 == 3) && (mod100 != 13)) {
			postfix = POS_THIRD_POSTFIX;
		} else {
			postfix = POS_TH_POSTFIX;
		}

		buffer[MAX_INFO_LENGTH] = (char) postfix.charAt(0);
		buffer[MAX_INFO_LENGTH + 1] = (char) postfix.charAt(1);

		return len;
	}

	private void colorScoreBoardFields(int userPlace, int opponentPlace) {
		if ((userPlace == 1) && (userPlace == opponentPlace)) {
			left_user_layout.setBackgroundResource(fieldGreenId);
			right_user_layout.setBackgroundResource(fieldGreenId);
		} else if (userPlace == 1) {
			left_user_layout.setBackgroundResource(fieldGreenId);
			right_user_layout.setBackgroundResource(fieldRedId);
		} else if (opponentPlace == 1) {
			left_user_layout.setBackgroundResource(fieldRedId);
			right_user_layout.setBackgroundResource(fieldGreenId);
		} else {
			left_user_layout.setBackgroundResource(fieldGreenId);
			right_user_layout.setBackgroundResource(fieldGreenId);
		}
	}

	protected void scoreUpdate(final ScoreItem[] scoreBoard) {
		final ScoreItem ourItemScore = getMyScore(scoreBoard);
		final ScoreItem oppItemScore = getBestOpponentScore(scoreBoard);

		int len;

		if (ourItemScore != null) {
			updateOurAvatar(ourItemScore.userInfo);
			left_user_name.setText(ourItemScore.userInfo.userName);
			len = fillNumberBuffer(ourScore, ourItemScore.score);
			left_user_score.setText(ourScore, MAX_INFO_LENGTH - len, len);
			len = fillStatusBuffer(ourStatus, ourItemScore.place);
			left_user_status.setText(ourStatus, MAX_INFO_LENGTH - len, len + 2);
		}

		if (oppItemScore != null) {
			updateOppAvatar(oppItemScore.userInfo);
			right_user_name.setText(oppItemScore.userInfo.userName);
			len = fillNumberBuffer(oppScore, oppItemScore.score);
			right_user_score.setText(oppScore, MAX_INFO_LENGTH - len, len);
			len = fillStatusBuffer(oppStatus, oppItemScore.place);
			right_user_status
					.setText(oppStatus, MAX_INFO_LENGTH - len, len + 2);
		}

		if ((ourItemScore != null) && (oppItemScore != null)) {
			colorScoreBoardFields(ourItemScore.place, oppItemScore.place);
		}

		if (debugLog) {
			StringBuffer logMsg = new StringBuffer();

			logMsg.append(" * scoresUpdate our:");

			if (ourItemScore == null) {
				logMsg.append("(null) null");
			} else {
				logMsg.append("(").append(ourItemScore.place).append(") ")
						.append(ourItemScore.score);
			}

			logMsg.append(" opp:");

			if (oppItemScore == null) {
				logMsg.append("(null) null");
			} else {
				logMsg.append("(").append(oppItemScore.place).append(") ")
						.append(oppItemScore.score);
			}

			Log.d("MNScoreProgressSideBySideView", logMsg.toString());
			logMsg = null;
		}
	}

	// <end implementationOf="MNScoreProgress.IProgressHandler">

	@Override
	public void onScoresUpdated(final ScoreItem[] scoreBoard) {
		Message msg = handler.obtainMessage(SCOREBOARD_UPDATED_EVENT,
				scoreBoard);
		msg.sendToTarget();
	}

	private void updateOppAvatar(final MNUserInfo userInfo) {

		if (oppAvatarId == userInfo.userId)
			return;
		Message oppAvatarMsg = handler
				.obtainMessage(OPPONENT_AVATAR_UPDATED_EVENT);

		oppAvatarId = userInfo.userId;

		MNScoreProgressUtil.downloadImageAssinc(oppAvatarMsg, userInfo
				.getAvatarUrl(), MNScoreProgressUtil.getBitmapImageById(
				getResources(), defaultAvatarId));
	}

	private void updateOurAvatar(final MNUserInfo userInfo) {

		if (ourAvatarId == userInfo.userId)
			return;

		ourAvatarId = userInfo.userId;

		Message ourAvatarMsg = handler.obtainMessage(OUR_AVATAR_UPDATED_EVENT);

		MNScoreProgressUtil.downloadImageAssinc(ourAvatarMsg, userInfo
				.getAvatarUrl(), MNScoreProgressUtil.getBitmapImageById(
				getResources(), defaultAvatarId));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final Context context = getContext();

		left_user_layout = findViewById(res.getIdentifier("fld_left_user",
				"id", context.getPackageName()));
		left_avatar = (ImageView) findViewById(res.getIdentifier(
				"fld_left_avatar", "id", context.getPackageName()));
		left_user_name = (TextView) findViewById(res.getIdentifier(
				"lbl_left_user_name", "id", context.getPackageName()));
		left_user_score = (TextView) findViewById(res.getIdentifier(
				"lbl_left_user_score", "id", context.getPackageName()));
		left_user_status = (TextView) findViewById(res.getIdentifier(
				"lbl_left_user_status", "id", context.getPackageName()));

		right_user_layout = findViewById(res.getIdentifier("fld_right_user",
				"id", context.getPackageName()));
		right_avatar = (ImageView) findViewById(res.getIdentifier(
				"fld_right_avatar", "id", context.getPackageName()));
		right_user_name = (TextView) findViewById(res.getIdentifier(
				"lbl_right_user_name", "id", context.getPackageName()));
		right_user_score = (TextView) findViewById(res.getIdentifier(
				"lbl_right_user_score", "id", context.getPackageName()));
		right_user_status = (TextView) findViewById(res.getIdentifier(
				"lbl_right_user_status", "id", context.getPackageName()));

		post(new Runnable() {
			@Override
			public void run() {
				left_user_layout.setBackgroundResource(fieldBlueId);
				right_user_layout.setBackgroundResource(fieldBlueId);

				left_user_name.setText("");
				right_user_name.setText("");

				left_user_score.setText("0");
				right_user_score.setText("0");

				left_user_status.setText(POS_FIRST);
				right_user_status.setText(POS_FIRST);

				Log.d(this.getClass().getName(), "onFinishInflate");
			}
		});

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == OUR_AVATAR_UPDATED_EVENT) {
			if (msg.obj != null) {
				left_avatar.setImageBitmap((Bitmap) msg.obj);
				msg.obj = null;
			}
		} else if (msg.what == OPPONENT_AVATAR_UPDATED_EVENT) {
			if (msg.obj != null) {
				right_avatar.setImageBitmap((Bitmap) msg.obj);
				msg.obj = null;
			}
		} else if (msg.what == SCOREBOARD_UPDATED_EVENT) {
			final ScoreItem[] scoreBoard = (ScoreItem[]) msg.obj;
			scoreUpdate(scoreBoard);
			msg.obj = null;
		} else {
			return false;
		}

		return true;
	}
}
