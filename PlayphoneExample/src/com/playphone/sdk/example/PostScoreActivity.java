package com.playphone.sdk.example;

import com.playphone.multinet.MNDirect;
import com.playphone.multinet.MNDirectUIHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PostScoreActivity extends Activity implements OnClickListener{
	
	EditText editInput;
	TextView txtResult;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_score);
        
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);
        
        editInput = (EditText) findViewById(R.id.editInput);
        txtResult = (TextView) findViewById(R.id.txtResult);
        
        editInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        
        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
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


	@Override
	public void onClick(View arg0) {
		MNDirect.postGameScore(Long.valueOf(editInput.getText().toString()));
		txtResult.setText("Updated your score " + editInput.getText().toString() + " to the Leaderboards");
		
	}

}