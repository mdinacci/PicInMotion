package com.marcodinacci.android.pim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.marcodinacci.android.commons.io.DataSink;
import com.marcodinacci.android.commons.io.DataWriter;
import com.marcodinacci.android.commons.io.FileUtils;

import com.marcodinacci.android.pim.io.DataSink;
import com.marcodinacci.android.pim.io.DataWriter;
import com.marcodinacci.android.pim.io.FileUtils;

/**
 * Extends SurfaceView to preview the Camera frames.
 * 
 * @author Marco Dinacci <marco.dinacci@gmail.com>
 * @see http
 *      ://developer.android.com/resources/samples/ApiDemos/src/com/example/android
 *      /apis/graphics/CameraPreview.html
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraView";
	private static final String PREFS_NAME = "prefs_camera";
	private final static String MOTION_DETECTION_KEY = "motion_detection_active";

	// Available from API level 9
	private static final String FOCUS_MODE_CONTINUOS_VIDEO = "continuos-video";

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private Context mContext;

	private CameraCallback mCameraCallback;
	private boolean mMotionDetectionActive;

	public Preview(Context context) {
		super(context);

		mContext = context;

		// Install a SurfaceHolder.Callback in order to receive notifications
		// when the underlying surface is created and destroyed
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		@SuppressWarnings("static-access")
		SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME,
				mContext.MODE_PRIVATE);
		mMotionDetectionActive = prefs.getBoolean(MOTION_DETECTION_KEY, true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(width, height);
		mCamera.setParameters(parameters);
		
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		if(mCamera == null) // TODO show Toast
			throw new RuntimeException("Camera is null");
		
		configure(mCamera);
		
		if(mMotionDetectionActive) {
			mCameraCallback = new CameraCallback(mContext, mCamera);
			mCamera.setPreviewCallback(mCameraCallback);
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException exception) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
				closeCamera();
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		closeCamera();
	}

	private void closeCamera() {
		Log.i(TAG, "Closing camera and freeing its resources");

		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private void configure(Camera camera) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, mContext.MODE_PRIVATE);
		Camera.Parameters params = camera.getParameters();

		params.set("jpeg-quality", prefs.getInt("pim.image-quality", 75));

		// Configure image format
		List<Integer> formats = params.getSupportedPictureFormats();
		if (formats.contains(PixelFormat.RGB_565))
			params.setPictureFormat(PixelFormat.RGB_565);
		else
			params.setPictureFormat(PixelFormat.JPEG);

		// FIXME Configure picture size, choose the smallest supported for now
		List<Size> sizes = params.getSupportedPictureSizes();
		Camera.Size size = sizes.get(0);// sizes.get(sizes.size()-1);
		params.setPictureSize(size.width, size.height);

		/*
		 * FIXME A wrong config cause the screen to go black on a Milestone so I
		 * just leave the default one. 
		 * sizes = params.getSupportedPreviewSizes(); 
		 * smallestSize = sizes.get(0);
		 * params.setPreviewSize(smallestSize.width, smallestSize.height);
		 */

		List<String> flashModes = params.getSupportedFlashModes();
		// Camera has flash, all flash modes are supported since API level 5
		if (flashModes.size() > 0)
			params.setFlashMode(prefs.getString("pim.camera.flash",
					Camera.Parameters.FLASH_MODE_AUTO));

		// Action mode take pictures of fast moving objects
		List<String> sceneModes = params.getSupportedSceneModes();
		if (sceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION))
			params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
		else
			params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

		// TODO test FOCUS_MODE_INFINITY against FOCUS_MODE_FIXED
		if (params.getSupportedFocusModes()
				.contains(FOCUS_MODE_CONTINUOS_VIDEO))
			params.setFocusMode(prefs.getString("pim.camera.focus_mode",
					FOCUS_MODE_CONTINUOS_VIDEO));
		else
			params.setFocusMode(prefs.getString("pim.camera.focus_mode",
					Camera.Parameters.FOCUS_MODE_INFINITY));

		camera.setParameters(params);

		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Picture format: " + params.getPictureFormat());
			Log.d(TAG, "Picture size: " + params.getPictureSize().width + " - "
					+ params.getPictureSize().height);
			Log.d(TAG, "Preview size: " + params.getPreviewSize().width + " - "
					+ params.getPreviewSize().height);
			Log.d(TAG, "Flash mode: " + params.getFlashMode());
			Log.d(TAG, "Scene mode: " + params.getSceneMode());
			Log.d(TAG, "Focus mode: " + params.getFocusMode());
		}
	}
}

final class CameraCallback implements Camera.PreviewCallback, 
	Camera.PictureCallback {

	private final String PICTURE_PREFIX = "/Pictures/pim/";
	private static final int PICTURE_DELAY = 4000;
	
	private static final String TAG = "CameraCallback";
	private MotionDetection mMotionDetection;
	private Camera mCamera;
	
	private long mReferenceTime;
	private DataWriter mDataWriter;

	public CameraCallback(Context ct, Camera camera) {
		mDataWriter = new DataWriter();
		
		mCamera = camera;

		mMotionDetection = new MotionDetection(ct.getSharedPreferences(
				MotionDetection.PREFS_NAME, Context.MODE_PRIVATE));
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Picture Taken");

		String pictureName = PICTURE_PREFIX+System.currentTimeMillis()+".jpg";
		File f = new File(
				Environment.getExternalStorageDirectory(),pictureName);
		FileOutputStream fos = null;
		try {
			FileUtils.touch(f);
			fos = new FileOutputStream(f);
		} catch (IOException e) {
			Log.e(TAG, "Cannot write picture to disk");
			e.printStackTrace();
		}
		
		DataSink<FileOutputStream>df = new DataSink<FileOutputStream>(data,fos);
		mDataWriter.writeAsync(df);

		mCamera.startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mMotionDetection.detect(data)) {
			// the delay is necessary to avoid taking a picture while in the 
			// middle of taking another. This problem causes a Motorola 
			// Milestone to reboot.
			long now = System.currentTimeMillis();
			if (now > mReferenceTime + PICTURE_DELAY) {
				mReferenceTime = now + PICTURE_DELAY;
				Log.i(TAG, "Taking picture");
				camera.takePicture(null, null, this);
			} else {
				Log.i(TAG, "Not taking picture because not enough time has "
						+ "passed since the creation of the Surface");
			}
		}
	}
}