//
//  MNInfoPanelHighScore.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.playphone.multinet.MNDirectUIHelper.IEventHandler;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.providers.MNMyHiScoresProvider;

class MNInfoPanelHighScore {
	protected WeakReference<View> binderViewRef = new WeakReference<View>(null);
	private Helper helper = new Helper();
	protected boolean isAnimationState = false;
	
	private Thread animThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				if (helper.panelView != null) {
					if (binderViewRef.get() != null) {
						binderViewRef.get().post(new Runnable() {
							@Override
							public void run() {
								helper.panelView.setVisibility(View.VISIBLE);
							}
						});
					}
				}
				isAnimationState = true;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				isAnimationState = false;
				if (helper.panelView != null) {
					if (binderViewRef.get() != null) {
						binderViewRef.get().post(new Runnable() {
							@Override
							public void run() {
								helper.panelView.setVisibility(View.GONE);
							}
						});
					}
				}
			}
		});
	}
	
	class  Helper {
		private Context context = null;
		private View panelView = null;
		
		public synchronized View getMyHiScorePanel () {
			if (context == null) {
				return null;
			}
			
			if (panelView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				Resources res = context.getApplicationContext().getResources();
				int panelId = res.getIdentifier("mninfopanelhighscore", "layout",
						context.getPackageName());
				panelView = inflater.inflate(panelId, null, false);
				panelView.setVisibility(View.GONE);
			}
			
			return panelView;
		}

		private synchronized void bindTo(ViewGroup vg) {
			if (panelView != null) {
				ViewGroup parentVG = (ViewGroup) (panelView.getParent());

				if (parentVG != null) {
					parentVG.removeView(panelView);
				}
				
				if (isAnimationState) {
					panelView.setVisibility(View.VISIBLE);
				} else {
					panelView.setVisibility(View.GONE);
				}
			}
			
			vg.addView(getMyHiScorePanel());
		}

		public void setContext(Context context) {
			helper.context = context;
			if (context == null){
                panelView = null;
            }
		}
	}
	
	protected void animate() {
		animThread().start();
	}
	
	protected MNMyHiScoresProvider.IEventHandler hiScoreEvent = new MNMyHiScoresProvider.IEventHandler() {
		@Override
		public void onNewHiScore(long newScore, int gameSetId, int periodMask) {
			animate();
		}
	};
	
	protected void install() {
		MNMyHiScoresProvider.IEventHandler hsEventHandler;
		MNMyHiScoresProvider hiScoreProvider = MNDirect.getMyHiScoresProvider();
		
		if (MNDirectPopup.isOldShowMode()) {
			hsEventHandler = hiScoreEvent;
		} else {
			hsEventHandler = hiToastScoreEvent;
		}
		
		hiScoreProvider.removeEventHandler(hsEventHandler);
		hiScoreProvider.addEventHandler(hsEventHandler);
		
		binderViewRef.get().post(new Runnable() {
			@Override
			public void run() {
				ViewGroup vg = (ViewGroup) binderViewRef.get();
				if (vg != null) {
					helper.bindTo(vg);
					vg = null;
				}
			}
		});
	}
	
	public void bind(View v) {
		MNSession session = MNDirect.getSession();
		
		if (session == null) {
			Log.w("MNInfoPanelHighScore","unexpected MNSession is null");
			helper.setContext(null);
			return;
		}
		
		binderViewRef = new WeakReference<View>(v);
		if (v == null) {
			helper.setContext(null);
		} else {
			helper.setContext(v.getContext());
			install();
		}
	}

	public void bind(Activity activity) {
		if (activity == null) {
			bind((View)null);
		} else {
			Window w = activity.getWindow(); 
			
			if (w == null) {
				Log.w("MNInfoPanelHighScore",
					"unexpected calling bind panel to invisible activity");
				bind((View) null);
			}else {
				bind(w.peekDecorView());
			}
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
	
	private void showNotification() {
		try {
			Resources res = helper.context.getApplicationContext().getResources();
			int panelId = res.getIdentifier("mninfopanelhighscore","layout", helper.context.getPackageName());
	        LayoutInflater li = (LayoutInflater) helper.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	        View tv = li.inflate(panelId, null);

			Toast t = Toast.makeText(helper.context, "High score notification",
					Toast.LENGTH_SHORT);
			t.setView(tv);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showToast() {
		try {
			binderViewRef.get().post(new Runnable() {
				@Override
				public void run() {
					showNotification();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected MNMyHiScoresProvider.IEventHandler hiToastScoreEvent = new MNMyHiScoresProvider.IEventHandler() {
		@Override
		public void onNewHiScore(long newScore, int gameSetId, int periodMask) {
			showToast();
		}
	};
}
