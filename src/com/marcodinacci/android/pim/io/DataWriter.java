package com.marcodinacci.android.pim.io;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.marcodinacci.android.pim.os.AsyncTaskListener;
import com.marcodinacci.android.pim.os.StreamDataTask;

/**
 * Write to a DataSink asynchronously.
 * 
 * @author Marco Dinacci <marco.dinacci@gmail.com>
 */
@SuppressWarnings("rawtypes")
public class DataWriter {

	private static final String TAG = DataWriter.class.getName();

	@SuppressWarnings({ "unchecked" })
	public void writeAsync(AsyncTaskListener<Integer, Long> listener, 
			DataSink ds, boolean flushOnWrite, boolean closeOnWrite) {
		
		List<AsyncTaskListener<Integer, Long>> listeners = new ArrayList<AsyncTaskListener<Integer,Long>>(1);
		if (listener != null) {
			listeners.add(listener);
		}
		
		Log.d(TAG, "Writing asynchronously to " + ds.getSink().toString());
		
		new StreamDataTask(listeners, flushOnWrite, closeOnWrite).execute(ds);
	}
	
	public void writeAsync(DataSink ds) {
		writeAsync(null, ds, true, true);
	}
}
