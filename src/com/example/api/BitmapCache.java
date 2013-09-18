package com.example.api;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

@SuppressLint("NewApi")
public class BitmapCache extends LruCache<String, Bitmap> {

	private static final int sCacheSize = (int) (Runtime.getRuntime()
			.maxMemory() / 1024 / 8);

	public BitmapCache() {
		super(sCacheSize);
		
	}

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
