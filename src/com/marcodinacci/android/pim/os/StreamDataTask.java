package com.marcodinacci.android.pim.os;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.marcodinacci.android.pim.io.DataSink;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Asynchronous task to write binary data to an <tt>OutputStream</tt>.
 * 
 * @author Marco Dinacci <marco.dinacci@gmail.com>
 *
 */
public class StreamDataTask extends AsyncTask<DataSink<OutputStream>, Integer, Long> {
	private static final String TAG = "StreamDataTask";
	private boolean mCloseOnWrite;
	private boolean mFlushOnWrite;
	private DataSink<OutputStream>[] mParams;
	private List<AsyncTaskListener<Integer, Long>> mTaskListeners;

	public StreamDataTask(List<AsyncTaskListener<Integer, Long>> listeners, 
			boolean flushOnWrite, boolean closeOnWrite) {
		mFlushOnWrite = flushOnWrite;
		mCloseOnWrite = closeOnWrite;
		mTaskListeners = listeners;
	}
	
	@Override
	protected Long doInBackground(DataSink<OutputStream>... params) {
		mParams = params;
		long bytesWritten = 0;
		
		int progress = 100 / params.length;
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			for (DataSink<OutputStream> dataSink : params) {
				try {
					OutputStream os = dataSink.getSink();
					byte[] data = dataSink.getData();
					os.write(data);
					bytesWritten += data.length;
					if(mFlushOnWrite)
						os.flush();
				} catch (java.io.IOException e) {
					Log.e(TAG, "Exception in doInBackground", e);
				}
				
				publishProgress(progress);
				progress+= progress;
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			String msg = "Cannot write to storage as it's mounted read only. " +
			"If the phone is connected to a PC, please disconnect it";
			Log.e(TAG, msg);
			
			// TODO rethrow exception, capture it in the UI and show a Toast
			
		} else {
			Log.e(TAG, "Cannot write to disk, probably the SD card " +
					"is not available");
			// TODO rethrow exception, capture it in the UI and show a Toast
		}

		return bytesWritten;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		
		if(mTaskListeners != null) 
			for (AsyncTaskListener<Integer, Long> listener : mTaskListeners) {
				if(listener != null)
					listener.onProgressUpdate(values);
			}
	}
	
	@Override
	protected void onPostExecute(Long result) {
		Log.d(TAG, "StreamDataTask.onPostExecute");
		
		super.onPostExecute(result);
		
		for (DataSink<OutputStream> dataSink : mParams) {
			try {
				OutputStream os = dataSink.getSink();
				if(mFlushOnWrite) os.flush();
				if(mCloseOnWrite) os.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		if(mTaskListeners != null) 
			for (AsyncTaskListener<Integer, Long> listener : mTaskListeners) {
				listener.onPostExecute(result);
			}
	}
}