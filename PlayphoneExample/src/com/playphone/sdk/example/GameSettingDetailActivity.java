package com.playphone.sdk.example;

import com.playphone.multinet.MNDirectUIHelper;

import android.os.Bundle;
import android.widget.TextView;

public class GameSettingDetailActivity extends CustomTitleActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_setting_detail);
		
		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
		txtBreadCrumbs.setText("Home > Game Settings > Detail");
		
		if (getIntent().getBooleanExtra("gamesetting",false)) {
			((TextView) findViewById(R.id.title)).setText(String.valueOf(getIntent().getIntExtra("id",0))+" : " + getIntent().getStringExtra("name"));
			((TextView) findViewById(R.id.params)).setText(getIntent().getStringExtra("params"));
			((TextView) findViewById(R.id.sysparams)).setText(getIntent().getStringExtra("sysparams"));
		}
	}
	
	@Override
	protected void onResume() {
		MNDirectUIHelper.setHostActivity(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		MNDirectUIHelper.setHostActivity(null);
		super.onPause();
	}
}
