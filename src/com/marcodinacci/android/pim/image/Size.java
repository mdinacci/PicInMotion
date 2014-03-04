package com.marcodinacci.android.pim.image;

/**
 * Simple Size class.
 *
 * @param <T>
 * @param <V>
 */
public final class Size<T extends Number,V extends Number> {
	public T mWidth;
	public V mHeight;

	public Size(T width, V height) {
		mWidth = width;
		mHeight = height;
	}
}