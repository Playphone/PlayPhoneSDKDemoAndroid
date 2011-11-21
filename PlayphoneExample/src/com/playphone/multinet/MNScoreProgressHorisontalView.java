package com.playphone.multinet;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.providers.MNScoreProgressProvider.IEventHandler;
import com.playphone.multinet.providers.MNScoreProgressProvider.ScoreItem;

/**
 *
 */
class MNScoreProgressHorisontalView extends LinearLayout implements
		IEventHandler, Callback {

	/* in message object ScoreItem[] array */
	private static final int SCOREBOARD_UPDATED_EVENT = 3;

	private Handler handler = new Handler(this);

	private final Resources res;

	private LinearLayout mMyLayout;
	private TextView mMyPlace;
	private TextView mMyName;
	private TextView mMyProgress;
	private ImageView mMyProgressImage;

	private LinearLayout mOppLayout;
	private TextView mOppPlace;
	private TextView mOppName;

	private final int upArrowImageId;
	private final int downArrowImageId;
	private final int backgroundMyId;
	private final int backgroundWinId;

	private static long TRANSLATE_ANIMATION_DURATION = 1000;
	private float animaterLineHeight = 20;

	private TranslateAnimation animationMyToUp;
	private TranslateAnimation animationOppToUp;
	private TranslateAnimation animationMyToDown;
	private TranslateAnimation animationOppToDown;

	/**
	 * @param context
	 * @param attrs
	 */
	public MNScoreProgressHorisontalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		res = context.getApplicationContext().getResources();
		upArrowImageId = res.getIdentifier(
				"mnprogresindicatorhorizontal_arrow_up", "drawable",
				context.getPackageName());
		downArrowImageId = res.getIdentifier(
				"mnprogresindicatorhorizontal_arrow_down", "drawable",
				context.getPackageName());
		backgroundMyId = res.getIdentifier(
				"mnprogresindicatorhorizontal_bgr_my", "drawable",
				context.getPackageName());
		backgroundWinId = res.getIdentifier(
				"mnprogresindicatorhorizontal_bgr_win", "drawable",
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

	protected long oldScoreDiff = 0;
	protected boolean isIWinState;

	protected void setNewYPos(View view, int yPos) {
		final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		lp.topMargin = yPos;
		view.setLayoutParams(lp);
	}

	protected void animationForward() {
		Log.i(this.getClass().getSimpleName(), "animationForward");
		mMyLayout.startAnimation(animationMyToUp);
		mOppLayout.startAnimation(animationOppToDown);
	}

	protected void animationBackward() {
		Log.i(this.getClass().getSimpleName(), "animationBackward");
		mMyLayout.startAnimation(animationMyToDown);
		mOppLayout.startAnimation(animationOppToUp);
	}

		public void scoreUpdate(final ScoreItem[] scoreBoard) {
			final ScoreItem myLocalScoreItem = getMyScore(scoreBoard);
			final ScoreItem oppLocalScoreItem = getBestOpponentScore(scoreBoard);

			// TODO : optimization GC need (strings)
			if (myLocalScoreItem != null) {
				mMyPlace.setText(String.valueOf(myLocalScoreItem.place));
				mMyName.setText(myLocalScoreItem.userInfo.userName);
			}

			if (oppLocalScoreItem != null) {
				mOppPlace.setText(String.valueOf(oppLocalScoreItem.place));
				mOppName.setText(oppLocalScoreItem.userInfo.userName);
			}

			if ((myLocalScoreItem != null) && (oppLocalScoreItem != null)) {
				final long currentScoreDiff = myLocalScoreItem.score
						- oppLocalScoreItem.score;

				mMyProgress.setText(String.valueOf(currentScoreDiff));
				if (currentScoreDiff < oldScoreDiff) {
					mMyProgressImage.setBackgroundResource(downArrowImageId);
				} else if (currentScoreDiff > oldScoreDiff) {
					mMyProgressImage.setBackgroundResource(upArrowImageId);
				}

				if (currentScoreDiff > 0) {
					mMyLayout.setBackgroundResource(backgroundWinId);
				} else {
					mMyLayout.setBackgroundResource(backgroundMyId);
				}

				Log.i(this.getClass().getSimpleName(),
						"isIWinState:" + String.valueOf(isIWinState)
								+ "    currentScoreDiff:"
								+ String.valueOf(currentScoreDiff));

				if (isIWinState) {
					if (currentScoreDiff < 0) {
						animationBackward();
						isIWinState = false;
					}
				} else {
					if (currentScoreDiff > 0) {
						animationForward();
						isIWinState = true;
					}
				}

				oldScoreDiff = currentScoreDiff;
			}
		}

	@Override
	public void onScoresUpdated(ScoreItem[] scoreBoard) {
		Message msg = handler.obtainMessage(SCOREBOARD_UPDATED_EVENT, scoreBoard);
		msg.sendToTarget();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final Context context = getContext();

		mMyLayout = (LinearLayout) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_layout_top", "id",
				context.getPackageName()));
		mMyPlace = (TextView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_my_number", "id",
				context.getPackageName()));
		mMyName = (TextView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_my_name", "id",
				context.getPackageName()));
		mMyProgress = (TextView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_my_progress", "id",
				context.getPackageName()));
		mMyProgressImage = (ImageView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_my_arrow", "id",
				context.getPackageName()));
		mOppLayout = (LinearLayout) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_layout_bottom", "id",
				context.getPackageName()));
		mOppPlace = (TextView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_opp_number", "id",
				context.getPackageName()));
		mOppName = (TextView) findViewById(res.getIdentifier(
				"mnprogresindicatorhorizontal_opp_name", "id",
				context.getPackageName()));

		animaterLineHeight = 20; // mMyLayout.getHeight();
		Log.i(this.getClass().getSimpleName(),
				"Height " + String.valueOf(animaterLineHeight));

		animationMyToDown = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, animaterLineHeight);
		animationMyToUp = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, animaterLineHeight,
				Animation.ABSOLUTE, 0);
		animationOppToDown = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -animaterLineHeight,
				Animation.ABSOLUTE, 0);
		animationOppToUp = new TranslateAnimation(Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, -animaterLineHeight);

		animationMyToDown.setDuration(TRANSLATE_ANIMATION_DURATION);
		animationOppToDown.setDuration(TRANSLATE_ANIMATION_DURATION);
		animationMyToUp.setDuration(TRANSLATE_ANIMATION_DURATION);
		animationOppToUp.setDuration(TRANSLATE_ANIMATION_DURATION);

		animationMyToDown.setFillAfter(true);
		animationOppToDown.setFillAfter(true);
		animationMyToUp.setFillAfter(true);
		animationOppToUp.setFillAfter(true);

		isIWinState = true;

		mMyProgress.setText("0");
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == SCOREBOARD_UPDATED_EVENT) {
			final ScoreItem[] scoreBoard = (ScoreItem[]) msg.obj;
			scoreUpdate(scoreBoard);
			msg.obj = null;
		} else {
			return false;
		}

		return true;
	}
}
