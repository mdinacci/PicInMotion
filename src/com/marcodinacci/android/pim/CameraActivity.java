package com.marcodinacci.android.pim;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

/**
 * {@link} http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/graphics/CameraPreview.html
 */
public class CameraActivity extends Activity {

	/* The surface where the preview is drawn */
	private SurfaceView mCameraView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// FIXME force screen in landscape mode as showing a video in 
		// potrait mode is not possible on all devices
		// http://groups.google.com/group/android-developers/browse_thread/thread/24dfa452ffc0e049?pli=1
		// http://groups.google.com/group/android-developers/msg/ca87bb60c52f0cac
		// http://groups.google.com/group/android-developers/browse_thread/thread/24dfa452ffc0e049?pli=1
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		// Hide window title and go fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mCameraView= new Preview(this);
		setContentView(mCameraView);
	}
}