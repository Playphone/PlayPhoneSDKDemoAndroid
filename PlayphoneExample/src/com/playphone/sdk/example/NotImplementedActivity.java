package com.playphone.sdk.example;

import com.playphone.multinet.MNDirectUIHelper;

import android.os.Bundle;

public class NotImplementedActivity extends CustomTitleActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		MNDirectUIHelper.setHostActivity(null);
	}
 
	@Override
	protected void onResume() {
		super.onResume();
		MNDirectUIHelper.setHostActivity(this);
	}
}