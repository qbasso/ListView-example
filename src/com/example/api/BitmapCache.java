package com.example.api;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Simple bitmap cache using LRUCache.
 */
@SuppressLint("NewApi")
public class BitmapCache extends LruCache<String, Bitmap> {

	/** The Constant sCacheSize. One fifth available application memory*/
	private static final int sCacheSize = (int) (Runtime.getRuntime()
			.maxMemory() / 1024 / 10);

	/**
	 * Instantiates a new bitmap cache.
	 */
	public BitmapCache() {
		super(sCacheSize);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.util.LruCache#sizeOf(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected int sizeOf(String key, Bitmap value) {
		// here we return exact bitmap size for API > 12 and approximate size
		// for lower API
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
			return value.getByteCount() /1024;
		} else {
			return value.getWidth() * value.getHeight() * 4 / 1024;
		}
	}

}
