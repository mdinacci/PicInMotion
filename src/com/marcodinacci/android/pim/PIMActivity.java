package com.marcodinacci.android.pim;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PIMActivity extends Activity {
	private static final String TAG = "PIMActivity";
	private Button launchButton;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        launchButton = (Button) findViewById(R.id.launch_button);
        
        launchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Starting CameraActivity");
				Intent i = new Intent(PIMActivity.this, CameraActivity.class);
				startActivity(i);
			}
		});
    }
}