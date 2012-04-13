//
//  MNInfoPanelWebEvent.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//
package com.playphone.multinet;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.playphone.multinet.MNDirectUIHelper.IEventHandler;
import com.playphone.multinet.core.MNSession;
import com.playphone.multinet.core.MNSessionEventHandlerAbstract;
import com.playphone.multinet.core.MNUtils;

class MNInfoPanelWebEvent implements Handler.Callback {
	
	protected static final int WAIT_DELAY_IMAGE_LOAD = 10 * 1000;
	
	protected Handler handler = new Handler(this);
	protected WeakReference<View> binderViewRef = new WeakReference<View>(null);
	private IEventHandler eventHandler = null;

	public IEventHandler getDirectUIEventHandler() {
		if (eventHandler == null) {
			eventHandler = new IEventHandler() {
				@Override
				public void onSetHostActivity(Activity newHostActivity) {
					bind(newHostActivity);
				}

				@Override
				public void onShowDashboard() {
				}

				@Override
				public void onHideDashboard() {
				}
			};
		}
		return eventHandler;
	}

	private static final String POPUP_IMAGE_URL_PARAM = "popupImageUrl";
	private static final String POPUP_MESSAGE_PARAM = "popupMessage";
	private static final String CALLBACK_ID_PARAM = "callback_id";
	
	private static final String IMAGE_BUNDLE_ITEM = "Image"; 
	private static final String TEXT_BUNDLE_ITEM = "Text"; 
	private static final String CALLBACK_BUNDLE_ITEM = "Id"; 
	
	private class ToastEventHandler extends MNSessionEventHandlerAbstract {
		@Override
		public void mnSessionWebEventReceived(String eventName,
				String eventParam, String callbackId) {
			if ("web.ui.doPopupShow".contentEquals(eventName)) {
				try {
					HashMap<String, String> params = MNUtils
							.httpGetRequestParseParams(eventParam);
					final Bundle bundle = new Bundle();
					final String url = params.get(POPUP_IMAGE_URL_PARAM);
					String text = params.get(POPUP_MESSAGE_PARAM);
					String callback_id = params.get(CALLBACK_ID_PARAM);
					bundle.putString(TEXT_BUNDLE_ITEM, text);
					bundle.putString(CALLBACK_BUNDLE_ITEM, callback_id);

					new Thread(new Runnable() {
						@Override
						public void run() {
							// Load image
							bundle.putParcelable(IMAGE_BUNDLE_ITEM, MNInfoPanelUtils.loadImageWithTimeout(url,WAIT_DELAY_IMAGE_LOAD));
							Message msg = handler.obtainMessage(1);
							msg.setData(bundle);
							msg.sendToTarget();
						}
					}).start();
				} catch (UnsupportedEncodingException e) {
					Log.e(MNInfoPanelWebEvent.class.getSimpleName(),
							"Can't parse web event parameters");
				}
			}
		}
	}

	ToastEventHandler eHandler = new ToastEventHandler();

	private void bind(Activity activity) {
		final MNSession session = MNDirect.getSession();
		if (activity == null) {

			if (session != null) {
				session.removeEventHandler(eHandler);
			}
			binderViewRef.clear();
		} else if (session != null) {
			binderViewRef = new WeakReference<View>(activity.getWindow()
					.getDecorView());
			session.addEventHandler(eHandler);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == 1) {
			Bundle b = msg.getData();
			Bitmap pic = (Bitmap) b.getParcelable(IMAGE_BUNDLE_ITEM);
			String text = b.getString(TEXT_BUNDLE_ITEM);
			showPopupToast(pic, text);
			return true;
		}
		return false;
	}

	private void fillNotificationView(View v, Bitmap pic, String text) {
		final View root = binderViewRef.get();

		if (root == null) {
			return;
		}
		final Context context = root.getContext();
		final Resources res = context.getApplicationContext().getResources();

		if (text != null) {
			int textId = res.getIdentifier("mndirect_popup_text", "id",
					context.getPackageName());
			TextView tv = (TextView) v.findViewById(textId);
			tv.setText(text);
		}

		if (pic != null) {

			int imageId = res.getIdentifier("mndirect_popup_icon", "id",
					context.getPackageName());

			ImageView iv = (ImageView) v.findViewById(imageId);
			iv.setImageBitmap(pic);
		}
	}

	private void showPopupToast(final Bitmap pic, final String text) {
		final View root = binderViewRef.get();

		if (root == null) {
			return;
		}

		final Context context = root.getContext();

		try {
			Resources res = context.getApplicationContext().getResources();
			int panelId = res.getIdentifier("mninfopanelevent", "layout",
					context.getPackageName());
			LayoutInflater li = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View toastView = li.inflate(panelId, null);

			fillNotificationView(toastView, pic, text);

			Toast t = Toast.makeText(context, "Event notification",
					Toast.LENGTH_LONG);
			t.setView(toastView);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
