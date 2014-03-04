package com.marcodinacci.android.pim;

/**
 * Very simple class to represent a key, value pair.
 *
 * @param <K>
 * @param <V>
 */
public final class KeyValue<K,V> {
	public K key;
	public V value;
	
	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}
}
