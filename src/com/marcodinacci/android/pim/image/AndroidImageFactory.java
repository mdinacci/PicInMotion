package com.marcodinacci.android.pim.image;

import android.graphics.PixelFormat;
import android.util.Log;

public final class AndroidImageFactory {

	/* These constants have been defined API level 8 and the corresponding
	 * in PixelFormat have been deprecated */ 
	
	// PixelFormat.YCbCr_420_SP
	// This is the standard format used for the preview frame
	public static final int IMAGE_FORMAT_NV21 = 0x00000011;
	
	// PixelFormat.YCbCr_422_I
	public static final int IMAGE_FORMAT_YUV2 = 0x00000014;
	
	// PixelFormat.YCbCr_422_SP
	public static final int IMAGE_FORMAT_NV16 = 0x00000010;

	private static final String TAG = "AndroidImageFactory";
			
	/** Create an image depending on the format type 
	 * 
	 * @see AndroidImage_JPEG
	 * @see AndroidImage_RGB_565
	 * @see AbstractAndroidImage
	 * 
	 * */
	public static AndroidImage createImage(byte[] data, Size<Integer, Integer> size, int format) {
		AndroidImage im = null;
		
		String imgType = "";
		
		switch (format) {
		case IMAGE_FORMAT_NV21:
			im = new AndroidImage_NV21(data,size);
			imgType = "NV_21";
			break;
		case IMAGE_FORMAT_YUV2:
			imgType = "YUV2";
			break;
		case IMAGE_FORMAT_NV16:
			imgType = "NV_16";
			break;
		case PixelFormat.RGB_565:
			//im = new AndroidImage_RGB_565(data, size);
			imgType = "RGB_565";
			break;
		case PixelFormat.JPEG:
			imgType = "JPEG";
			//im = new AndroidImage_JPEG(data, size);
			break;
		case PixelFormat.RGBA_5551:
			imgType = "RGBA_5551";
			break;
		case PixelFormat.RGB_888:
			imgType = "RGB_888";
			break;
		default:
			imgType = "default";
			break;
		}
		
		Log.i(TAG, "Creating an image of type: " + imgType );
		
		return im;
	}
}