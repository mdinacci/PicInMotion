package com.marcodinacci.android.pim.io;

/**
 * Associate an array of bytes to a sink, which can be any kind of class
 * though normally an OutputStream.
 *
 * @param <T> Sink type
 */
public class DataSink<T> {

	private byte[] mData;
	private T mSink;
	
	public DataSink(byte[] data, T sink) {
		mData = data;
		mSink = sink;
	}
	
	public byte[] getData() { return mData; }
	public T getSink() { return mSink; }
}
