//
//  MNDirectUIHelper.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.playphone.multinet.core.IMNUserProfileViewEventHandler;
import com.playphone.multinet.core.MNEventHandlerArray;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;
import com.playphone.multinet.core.MNUserProfileView;

public class MNDirectUIHelper {
	public static final int DASHBOARD_STYLE_FULLSCREEN = 1;
	public static final int DASHBOARD_STYLE_POPUP      = 2;
	public static final int DASHBOARD_STYLE_POPUP_MINI = 3; // 300x220 dpx
	
	protected static boolean showOnBind = false;
	protected static int dashboardStyle = DASHBOARD_STYLE_POPUP; // default style   
	

	public static void setDashboardStyle(int newStyle) {
		switch (newStyle) {
		case DASHBOARD_STYLE_FULLSCREEN :
			dashboardStyle = DASHBOARD_STYLE_FULLSCREEN; 
			break;
		case DASHBOARD_STYLE_POPUP_MINI :
			dashboardStyle = DASHBOARD_STYLE_POPUP_MINI;
			break;
		case DASHBOARD_STYLE_POPUP :
		default :
			dashboardStyle = DASHBOARD_STYLE_POPUP;
			break;
		}
	}
	
	public static interface IEventHandler {
		public void onSetHostActivity(Activity newHostActivity);

		public void onShowDashboard();

		public void onHideDashboard();
	}

	public static class EventHandlerAbstract implements IEventHandler {
		@Override
		public void onSetHostActivity(Activity newHostActivity) {
		}

		@Override
		public void onShowDashboard() {
		}

		@Override
		public void onHideDashboard() {
		}
	}

	private static MNEventHandlerArray<IEventHandler> eventHandlers = new MNEventHandlerArray<IEventHandler>();

	/**
	 * Adds event handler
	 * 
	 * @param eventHandler
	 *            an object that implements
	 *            {@link IEventHandler IEventHandler} interface
	 */
	public static synchronized void addEventHandler(IEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	/**
	 * Removes event handler
	 * 
	 * @param eventHandler
	 *            an object that implements
	 *            {@link IEventHandler IEventHandler} interface
	 */
	public static synchronized void removeEventHandler(IEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}
		
    protected static Dashboard dashboard = null;
    protected static WeakReference<Activity> currHostActivity = new WeakReference<Activity>(null);  

	public static synchronized Activity getHostActivity() {
		return (currHostActivity.get());
	}

	/**
	 * Set current acytive activity for UI components
	 * 
	 * @param newHostActivity
	 *            current host activity (call in Activity.onResume)
	 *            optional: you can provide null as a newHostActivity parameter
	 *            when you activity is destroying (call in Activity.onDestroy)
	 */

	public static synchronized void setHostActivity(Activity newHostActivity) {

		// Assign activity here, to allow
		currHostActivity = new WeakReference<Activity>(newHostActivity);

		MNUserProfileView view = MNDirect.getView();

		if (view != null)
		{
			view.setHostActivity(newHostActivity);
		}

		eventHandlers.beginCall();

		try {
			int index;
			int count = eventHandlers.size();

			for (index = 0; index < count; index++) {
				eventHandlers.get(index).onSetHostActivity(newHostActivity);
			}
		} finally {
			eventHandlers.endCall();
		}
	}

	protected synchronized static void onShowDashboard() {
		eventHandlers.beginCall();
		try {
			int index;
			int count = eventHandlers.size();

			for (index = 0; index < count; index++) {
				eventHandlers.get(index).onShowDashboard();
			}
		} finally {
			eventHandlers.endCall();
		}
	}
	
	protected synchronized static void onHideDashboard() {
		eventHandlers.beginCall();
		try {
			int count = eventHandlers.size();

			for (int index = 0; index < count; index++) {
				eventHandlers.get(index).onHideDashboard();
			}
		} finally {
			eventHandlers.endCall();
		}
	}

	public synchronized static void showDashboard() {
		showOnBind = true;
		
		if (dashboard != null) {
			dashboard.restoreState();
		}
	}
	
	public synchronized static void hideDashboard() {
		showOnBind = false;
		
		if (dashboard != null) {
			dashboard.restoreState();
		}
	}

	public synchronized static boolean isDashboardHidden() {
		return !isDashboardVisible();
	}

	public synchronized static boolean isDashboardVisible() {
		return (dashboard == null) ? false : dashboard.isShowing();
	}

	protected static class EventHandler extends MNSessionEventHandlerAbstract {
		@Override
		public void mnSessionDoStartGameWithParams(MNGameParams gameParams) {
			Activity a = currHostActivity.get();
			if (a != null) {
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideDashboard();
					}
				});
			}
		}
	}

	protected final static EventHandler eh = new EventHandler();
	protected final static IMNUserProfileViewEventHandler viewEventHandler = new IMNUserProfileViewEventHandler() {
		@Override
		public void mnUserProfileViewDoGoBack() {
			Activity a = currHostActivity.get();
			if (a != null) {
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideDashboard();
					}
				});
			}
		}
	};

	protected synchronized static void bindDashboard(Activity a) {
		final MNUserProfileView upv = MNDirect.getView();
		final MNSession s = MNDirect.getSession();
		
		if (s == null) {
			// on case if session already destroyed
			return;
		}
		
		if (a != null) {
			dashboard = new Dashboard(a);
			s.addEventHandler(eh);
			upv.addEventHandler(viewEventHandler);
			dashboard.restoreState();
		} else {
			if (upv != null) {
				upv.removeEventHandler(viewEventHandler);
			}
			if (s != null) {
				s.removeEventHandler(eh);
			}
			if (dashboard != null) {
				dashboard.dismiss();
			}
		}
	}

	protected static class Dashboard extends Dialog {
		
		private boolean showNowFlag = false;
		
		public boolean isShowing () {
			return (showNowFlag);
		}

		static final int PADDING_SIZE = 10;

		public void restoreState() {
			if (showOnBind) {
				showNowFlag = true;
				dashboard.show();
				onShowDashboard();
			} else {
				showNowFlag = false;
				dashboard.hide();
				onHideDashboard();
			}
		}

		protected Dashboard(Context context) {
			super(context);
		}

		protected WindowManager.LayoutParams getLayout(int padding) {
			final Window w = getWindow();
			final Display display = w.getWindowManager().getDefaultDisplay();
			final float scale = getContext().getResources().getDisplayMetrics().density;
			int Padding = (int) (padding * scale + 0.5f);
			int wMax = (int) (display.getWidth());
			int hMax = (int) (display.getHeight());
			int wReal = (wMax - (2 * Padding));
			int hReal = (hMax - (2 * Padding));

			return new WindowManager.LayoutParams(wReal, hReal);
		}

		protected int getMNDashboardTheme() {
			int resultThemeId = android.R.style.Theme_Dialog;
			switch (dashboardStyle) {
			case DASHBOARD_STYLE_FULLSCREEN :	
				resultThemeId = getContext().getResources().getIdentifier(
						"Theme_Dashboard_Fullscreen", "style",
						getContext().getPackageName());
				break;
			case DASHBOARD_STYLE_POPUP :
			case DASHBOARD_STYLE_POPUP_MINI :	
			default:
				resultThemeId = getContext().getResources().getIdentifier(
						"Theme_Dashboard", "style",
						getContext().getPackageName());
				break;

			}

			return resultThemeId;
		}
		
		static final int POPUP_PADDING = 5; 
		static final int POPUP_MARGIN = 5;
		
		private View getDashboardView (View contentView) {
			final Resources res = getContext().getResources();
			final String packageName = getContext().getPackageName();
			final LayoutInflater li = (LayoutInflater)getContext().getSystemService
		      (Context.LAYOUT_INFLATER_SERVICE);
			final int resultDashboardId = res.getIdentifier("mndashboard", "layout", packageName);
			View mnDashboard = null;
			mnDashboard = li.inflate(resultDashboardId, null);
			int bgFrameId =  res.getIdentifier("mnwfview_bg", "id", packageName);
			int viewFrameId =  res.getIdentifier("mnwfview", "id", packageName);
			RelativeLayout bgFrame = (RelativeLayout) mnDashboard.findViewById(bgFrameId);
			RelativeLayout viewFrame = (RelativeLayout) mnDashboard.findViewById(viewFrameId);
			
			switch (dashboardStyle) {
			case DASHBOARD_STYLE_FULLSCREEN :
				bgFrame.setVisibility(View.GONE);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
				viewFrame.setLayoutParams(lp);
				break;
			case DASHBOARD_STYLE_POPUP_MINI : {
				int widthBG = (int) (res.getDisplayMetrics().density * 310 + 0.5);
				int heightBG = (int) (res.getDisplayMetrics().density * 230 + 0.5);
				int width = (int) (res.getDisplayMetrics().density * 300 + 0.5);
				int height = (int) (res.getDisplayMetrics().density * 220 + 0.5);
				RelativeLayout.LayoutParams bgLP = new RelativeLayout.LayoutParams(
						widthBG, heightBG);
				bgLP.addRule(RelativeLayout.CENTER_IN_PARENT,
						RelativeLayout.TRUE);
				bgFrame.setLayoutParams(bgLP);

				RelativeLayout.LayoutParams viewLP = new RelativeLayout.LayoutParams(
						width, height);
				viewLP.addRule(RelativeLayout.CENTER_IN_PARENT,
						RelativeLayout.TRUE);
				viewFrame.setLayoutParams(viewLP);
			}
				break;
			case DASHBOARD_STYLE_POPUP: {
				int padding = (int) (res.getDisplayMetrics().density
						* POPUP_PADDING + 0.5);
				mnDashboard.setPadding(padding, padding, padding, padding);
				RelativeLayout.LayoutParams bgLP = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.FILL_PARENT);
				bgLP.addRule(RelativeLayout.CENTER_IN_PARENT,
						RelativeLayout.TRUE);
				bgFrame.setLayoutParams(bgLP);

				RelativeLayout.LayoutParams vlp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.FILL_PARENT);
				vlp.addRule(RelativeLayout.CENTER_IN_PARENT,
						RelativeLayout.TRUE);
				int margin = (int) (res.getDisplayMetrics().density
						* POPUP_MARGIN + 0.5);
				vlp.setMargins(margin, margin, margin, margin);
				viewFrame.setLayoutParams(vlp);
				
			}
				break;
			}
			
			// install content view into view frame
			ViewGroup parentView = (ViewGroup) (contentView.getParent());

			if (parentView != null) {
				parentView.removeView(contentView);
			}
			
			RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
			contentView.setLayoutParams(clp);
			viewFrame.addView(contentView);
			
			return mnDashboard;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getContext().setTheme(getMNDashboardTheme ());
			final Window w = getWindow();
			w.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			w.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
			
			View mnDashboard = getDashboardView (MNDirect.getView());
			addContentView(mnDashboard, getLayout(0));

			setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface paramDialogInterface,
						int paramInt, KeyEvent paramKeyEvent) {
					if (paramKeyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
						hideDashboard();
						return true;
					}
					return false;
				}
			});

			setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					onHideDashboard();
					dashboard = null;
				}
			});

			setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					showOnBind = false;
				}
			});
		}
	}
}
