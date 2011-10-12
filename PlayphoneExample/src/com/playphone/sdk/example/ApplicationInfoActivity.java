package com.playphone.sdk.example;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;
import com.playphone.multinet.core.MNSession;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class ApplicationInfoActivity extends CustomTitleActivity {
	
	TextView appInfoText;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.applicationinfo);
        
        appInfoText = (TextView) findViewById(R.id.appinfotext);
        
        PackageManager pm = getPackageManager();
        StringBuffer resultInfo = new StringBuffer();
        
        resultInfo.append("Application  name : ").append(pm.getApplicationLabel(getApplicationInfo())).append("\n");
        
        try {
			PackageInfo packInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        resultInfo.append("Application  version: ").append(packInfo.versionName).append("\n");
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
        resultInfo.append("MN SDK version :  ").append(MNSession.CLIENT_API_VERSION.replace('_', '.')).append("\n");
        resultInfo.append("Android library version :  ").append(getApplicationInfo().targetSdkVersion).append("\n");
        resultInfo.append("Configuration url : ").append("\n");
        resultInfo.append(MNDirect.getSession().getPlatform().getMultiNetConfigURL()).append("\n");
        
        appInfoText.setText(resultInfo.toString());
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

}
