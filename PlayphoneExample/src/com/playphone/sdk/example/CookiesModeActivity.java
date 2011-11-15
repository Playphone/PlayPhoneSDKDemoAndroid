package com.playphone.sdk.example;

import com.playphone.multinet.MNDirectUIHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CookiesModeActivity extends CustomTitleActivity  implements OnClickListener {
	Button anyGameButton;
	Button currentGameButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cookies_mode);
		// set the breadcrumbs text
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Cookies");
		
		anyGameButton = (Button) findViewById(R.id.anyRoomButton);
		currentGameButton = (Button) findViewById(R.id.currentRoomButton);
		anyGameButton.setOnClickListener(this);
		currentGameButton.setOnClickListener(this);
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

	@Override
	public void onClick(View v) {
		if (v == anyGameButton) {
			Intent intent = new Intent(this, CookiesAnyRoomActivity.class);
			startActivity(intent);
		} else if (v == currentGameButton) {
			Intent intent = new Intent(this, CookiesCurrentRoomActivity.class);
			startActivity(intent);
		}
	}
}
