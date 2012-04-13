//
//  MNInfoPanelAchievement.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.playphone.multinet.MNDirectUIHelper.IEventHandler;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;
import com.playphone.multinet.providers.MNAchievementsProvider;
import com.playphone.multinet.providers.MNAchievementsProvider.GameAchievementInfo;

class MNInfoPanelAchievement {
	private WeakReference<View> binderViewRef = new WeakReference<View>(null);
	private Helper helper = new Helper(); 
	private MNAchievementsProvider getAchProvider() {
		return MNDirect.getAchievementsProvider();
	}

	protected class PanelEventHandler implements MNAchievementsProvider.IEventHandler {

		@Override
		public void onGameAchievementListUpdated() {
			Log.d("PanelEventHandler", "onGameAchievementListUpdated()");
		}

		@Override
		public void onPlayerAchievementUnlocked(int achievementId) {
			onUnlockAchievement(achievementId);
		}
	}
	
	private class SessionEventHandler extends
			MNSessionEventHandlerAbstract {
		@Override
		public void mnSessionStatusChanged(int newStatus, int oldStatus) {
			if (newStatus == MNConst.MN_LOGGEDIN) {
				if (getAchProvider().isGameAchievementListNeedUpdate()) {
					getAchProvider().doGameAchievementListUpdate();
				}
			}
		}
	}
	
	private SessionEventHandler sessionEventHandler = new SessionEventHandler();
	
	protected String getAchievementName(final int achievementId) {
		GameAchievementInfo[] list = getAchProvider().getGameAchievementsList();
		for (GameAchievementInfo info : list) {
			if (info.id == achievementId)
				return info.name;
		}
		return "";
	}
	
	protected void onUnlockAchievement(final int achievementId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final View b = binderViewRef.get();
				if (b == null) {
					return;
				}
				
				final Bitmap pic = helper.downloadImageByUrl(
						getAchProvider().getAchievementImageURL(achievementId), null);
				
				b.post(new Runnable() {
					@Override
					public void run() {
						try {
							final Context c = helper.context;
							final View v = helper.getAchievementPanel();
							final Resources r = binderViewRef.get().getResources();

							if ((v == null) || (r == null) || (c == null)) {
								return;
							}

							final int imgId = r.getIdentifier("mndirect_popup_icon", "id", c.getPackageName());
							final ImageView achImageView = (ImageView) v.findViewById(imgId);

							final int descId = r.getIdentifier("mndirect_popup_text", "id", c.getPackageName());
							final TextView achDescView = (TextView) v.findViewById(descId);

							achImageView.setImageBitmap(pic);
							achDescView.setText(getAchievementName(achievementId));
							
							animate();
							
						} catch (Exception e) {
							e.printStackTrace();
							// do nothing
						}
					}
				});
			}
		}).start();
	}

	class Helper {
		private Context context = null;
		private View panelView = null;

		protected Bitmap downloadImageByUrl(String url,
				BitmapFactory.Options opts) {
			URL ourUrl = null;
			try {
				ourUrl = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			try {
				HttpURLConnection conn = (HttpURLConnection) ourUrl
						.openConnection();
				conn.setDoInput(true);
				conn.setUseCaches(true);
				conn.connect();

				InputStream is = conn.getInputStream();

				return BitmapFactory.decodeStream(is, null, opts);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public synchronized View getAchievementPanel() {
			
			if (context == null) {
				return null;
			}
			
			if (panelView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				Resources res = context.getApplicationContext().getResources();
				int panelId = res.getIdentifier("mninfopanelachievement", "layout",
						context.getPackageName());
				panelView = inflater.inflate(panelId, null, false);
				helper.setVisible(false);
			}

			return panelView;
		}

		public synchronized void bindTo(ViewGroup vg) {
			if (panelView != null) {
				ViewGroup parentVG = (ViewGroup) (panelView.getParent());

				if (parentVG != null) {
					parentVG.removeView(panelView);
				}
			}
			
			if (context != null) {
				vg.addView(getAchievementPanel());
			}
		}

		public synchronized void setContext(Context context) {
			helper.context = context;
			if (context == null){
			    panelView = null;
			}
		}
		
		
		public void setVisible(boolean visible) {
			if (panelView != null) {
				panelView.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}
	}
	protected MNAchievementsProvider.IEventHandler panelEventHandler = null;
	
	//protected static PanelEventHandler panelEventHandler = null;
	
	public void bind(final View v) {
		MNSession session = MNDirect.getSession();
		MNAchievementsProvider provider = getAchProvider();
		
		if (session == null) {
			Log.w("MNInfoPanelAchievement","unexpected MNSession is null, please check for MNDirect initialized");
			helper.setContext(null);
			return;
		}
		
		if (provider == null) {
			Log.w("MNInfoPanelAchievement","unexpected MNAchievementProvider is null");
			helper.setContext(null);
			return;
		}
		
		binderViewRef = new WeakReference<View>(v);
		
		if (panelEventHandler != null) {
			provider.removeEventHandler(panelEventHandler);
			panelEventHandler = null; 
		}
		
		if (panelEventHandler == null) {
			if (MNDirectPopup.isOldShowMode()) {
				panelEventHandler = new PanelEventHandler();
			} else {
				panelEventHandler = new ToastEventHandler();
			}
		}
			
		if (v == null) {
			session.removeEventHandler(sessionEventHandler);
			helper.setContext(null);
		} else {
			helper.setContext(v.getContext());
			v.post(new Runnable() {
				@Override
				public void run() {
					helper.bindTo((ViewGroup) v);
					if (isAnimationState) {
						animate();
					}
				}
			});
			provider.addEventHandler(panelEventHandler);
			session.addEventHandler(sessionEventHandler);
		}
	}

	public void bind(Activity activity) {
		if (activity == null) {
			bind((View) null);
		} else{
			Window w = activity.getWindow(); 
			
			if (w == null) {
				Log.w("MNInfoPanelAchievement",
					"unexpected calling bind panel to invisible activity");
				bind((View) null);
			}else {
				bind(w.peekDecorView());
			}
		}
	}

	protected boolean isAnimationState = false;

	protected void animate() {
		if (helper.context == null) {
			return;
		}
		
		if (binderViewRef.get() == null) {
			return;
		}
		
		final Resources res = binderViewRef.get().getResources();
		
		final int animationId = res.getIdentifier("mndirectpopupanimation",
				"anim", helper.context.getPackageName());
		final Animation movingAnim = AnimationUtils.loadAnimation(
				helper.context, animationId);

		movingAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				isAnimationState = true;
				helper.setVisible(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				helper.setVisible(false);
				isAnimationState = false;
			}
		});

		binderViewRef.get().post(new Runnable() {
			@Override
			public void run() {
				if (helper.context != null) {
					helper.getAchievementPanel().startAnimation(movingAnim);
				}
			}
		});
	}
	
	public void unlockAchievement (Integer achievementId) {
		if (MNDirect.isUserLoggedIn()) {
			getAchProvider().unlockPlayerAchievement(achievementId);
		} else {
			onUnlockAchievement(achievementId);
		}
	}
	
	private IEventHandler eventHandler = null; 
	
	public IEventHandler getDirectUIEventHandler () {
		if (eventHandler == null) {
			eventHandler = new IEventHandler() {
				@Override
				public void onSetHostActivity(Activity newHostActivity) {
					bind(newHostActivity);
				}
				@Override
				public void onShowDashboard() {}
				@Override
				public void onHideDashboard() {}
			};
		}
		return eventHandler; 
	}
	
	
	private void fillNotificationView(View v, final int achievementId) {
		final Bitmap pic = helper.downloadImageByUrl(
				getAchProvider().getAchievementImageURL(achievementId), null);
		
		final Resources r = helper.context.getApplicationContext().getResources();
		final int imgId = r.getIdentifier("mndirect_popup_icon", "id", helper.context.getPackageName());
		final ImageView achImageView = (ImageView) v.findViewById(imgId);

		final int descId = r.getIdentifier("mndirect_popup_text", "id", helper.context.getPackageName());
		final TextView achDescView = (TextView) v.findViewById(descId);
		
		achImageView.setImageBitmap(pic);
		achDescView.setText(getAchievementName(achievementId));
	}
	
	private void showNotification(final int achievementId) {
		try {
			Resources res = helper.context.getApplicationContext().getResources();
			int panelId = res.getIdentifier("mninfopanelachievement","layout", helper.context.getPackageName());
	        LayoutInflater li = (LayoutInflater) helper.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// View tv = Helper.getNetworkPanel();
	        View tv = li.inflate(panelId, null);

			fillNotificationView(tv, achievementId);

			Toast t = Toast.makeText(helper.context, "Achievement notification",
					Toast.LENGTH_SHORT);
			t.setView(tv);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected class ToastEventHandler implements MNAchievementsProvider.IEventHandler {

		@Override
		public void onGameAchievementListUpdated() {
			Log.d("PanelEventHandler", "onGameAchievementListUpdated()");
		}

		@Override
		public void onPlayerAchievementUnlocked(int achievementId) {
			View v = binderViewRef.get();
			final int id = achievementId;
			if (v != null) {
			  v.post(new Runnable() {
				
				@Override
				public void run() {
					showNotification(id);
				}
			});
			}
		}
	}
}
