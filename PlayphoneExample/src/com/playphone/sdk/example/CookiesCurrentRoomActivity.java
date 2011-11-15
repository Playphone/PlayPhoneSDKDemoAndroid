package com.playphone.sdk.example;

import java.util.HashMap;
import java.util.Random;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CookiesCurrentRoomActivity extends CustomTitleActivity implements
		OnClickListener {

	TextView roomText;
	EditText cookieValueEdit;
	Button storeCookieButton;
	Button readCookieButton;
	TextView resultText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cookies_current_room);

		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Cookies > Current Room");

		roomText = (TextView) findViewById(R.id.roomText);
		cookieValueEdit = (EditText) findViewById(R.id.cookieValueEdit);
		storeCookieButton = (Button) findViewById(R.id.storeCookieButton);
		readCookieButton = (Button) findViewById(R.id.readCookieButton);
		resultText = (TextView) findViewById(R.id.resultText);

		storeCookieButton.setOnClickListener(this);
		readCookieButton.setOnClickListener(this);
		if (MNDirect.isUserLoggedIn()) {
			roomText.setText(" Room id : "
					+ String.valueOf(MNDirect.getSession().getCurrentRoomId()));
		}
	}

	HashMap<Integer, String> cookiesList = new HashMap<Integer, String>();

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

	private void storeCookie() {
		int key = new Random().nextInt(5);
		String cookie = cookieValueEdit.getText().toString();
		if (cookie.length() <= 0) {
			cookie = null;
		}
		MNDirect.getGameRoomCookiesProvider().setCurrentGameRoomCookie(key,
				cookie);
		
		Toast.makeText(this, "Cookie " + key + " stored (" + cookie + ")", Toast.LENGTH_SHORT)
		.show();
		
		readCookies();
	}

	private void readCookies() {
		for (int index = 0; index < 5; index++) {
			cookiesList.put(index, MNDirect.getGameRoomCookiesProvider()
					.getCurrentGameRoomCookie(index));
		}

		updateResult();
	}

	@Override
	public void onClick(View v) {
		if (!MNDirect.isUserLoggedIn()) {
			Toast.makeText(this, "You need be logged in", Toast.LENGTH_LONG)
					.show();
			return;
		}

		if (v == storeCookieButton) {
			storeCookie();
		} else if (v == readCookieButton) {
			readCookies();
		}
	}

	private void updateResult() {
		if (MNDirect.isUserLoggedIn()) {
			roomText.setText(" Room id : "
					+ String.valueOf(MNDirect.getSession().getCurrentRoomId()));

			StringBuilder text = new StringBuilder();
			for (int index = 0; index < 5; index++) {

				text.append("Id : ").append(index).append(",");
				text.append(" Value : ");

				if (cookiesList.containsKey(index)) {
					text.append(cookiesList.get(index));
				} else {
					text.append("(null)");
				}
				text.append("\n");
			}

			resultText.setText(text.toString());
		}
	}

}
