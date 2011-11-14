package com.playphone.sdk.example;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.providers.MNServerInfoProvider;
import com.playphone.multinet.providers.MNServerInfoProvider.IEventHandler;

public class ServerInfoActivity extends CustomTitleActivity implements Callback {
	private Handler handler = new Handler(this);
	TextView serverInfoText;

	MNServerInfoProvider.IEventHandler eh = new IEventHandler() {

		@Override
		public void onServerInfoItemRequestFailedWithError(int key, String error) {
			Message msg = new Message();
			msg.what = SERVER_INFO_FAILED;
			msg.obj = error;

			handler.sendMessage(msg);
		}

		@Override
		public void onServerInfoItemReceived(int key, String value) {

			if (key == MNServerInfoProvider.SERVER_TIME_INFO_KEY) {
				Message msg = new Message();
				msg.what = SERVER_TIME_INFO;
				msg.obj = value;
				handler.sendMessage(msg);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_info);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Server info");

		serverInfoText = (TextView) findViewById(R.id.serverInfoText);
		if (!MNDirect.isOnline()) {
			serverInfoText.setText("User is not logged in");
		}
		((Button) findViewById(R.id.updateButton))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!MNDirect.isOnline()) {
							serverInfoText.setText("User is not logged in");
						} else {
							MNDirect.getServerInfoProvider()
									.requestServerInfoItem(
											MNServerInfoProvider.SERVER_TIME_INFO_KEY);
						}
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
		MNDirect.getServerInfoProvider().addEventHandler(eh);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
		MNDirect.getServerInfoProvider().removeEventHandler(eh);
	}

	private final static int SERVER_INFO_FAILED = 0;
	private final static int SERVER_TIME_INFO = 1;

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == SERVER_INFO_FAILED) {
			String error = (String) msg.obj;
			serverInfoText.setText("Error : " + error);

		} else if (msg.what == SERVER_TIME_INFO) {
			String result = (String) msg.obj;
			Long timeStamp = Long.parseLong(result);
			StringBuilder info = new StringBuilder();

			info.append("   Server Info :\n\n");
			info.append("Server time: ").append(timeStamp);
			SimpleDateFormat dateformatter = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
			Date d = new Date(timeStamp*1000);
			info.append(" (").append(dateformatter.format(d)).append(")");
			serverInfoText.setText(info.toString());

		} else {
			return false;
		}

		return true;
	}
}
