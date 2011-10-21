package com.playphone.sdk.example;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CustomTitleListActivity extends ListActivity {
	private boolean titleAttached = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (titleAttached) {
			return;
		}
		titleAttached = true;

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		Button btnHome = (Button) findViewById(R.id.btnHome);
		btnHome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("playphone", "Home button has been pressed");
				// Need to start the Home activity but clear all the rest of the
				// stack
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		});

	}
}
