package com.marcodinacci.android.pim;

public final class KeyValue<K,V> {
	public K key;
	public V value;
	
	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}
}
