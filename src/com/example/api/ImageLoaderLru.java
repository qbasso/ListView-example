/*
 * taken from Android developer blog on asynchronous loading and slightly changed
 */
package com.example.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * same as Image downloader from Google Developer Blog but different caching
 * mechanism.
 */
public class ImageLoaderLru {

	private static final String LOG_TAG = "ImageLoaderLRU";

	/**
	 * Instantiates a new image loader lru.
	 */
	public ImageLoaderLru() {

	}

	/** The s cache. */
	private BitmapCache mCache = new BitmapCache();

	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the image to.
	 * @param thumbnail
	 *            the thumbnail
	 * @param local
	 *            the local
	 */
	public void download(String url, ImageView imageView) {
		Bitmap bitmap = getBitmapFromCache(url);
		if (bitmap == null) {
			forceDownload(url, imageView);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	/*
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear. private
	 * void forceDownload(String url, ImageView view) { forceDownload(url, view,
	 * null); }
	 */

	/**
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear.
	 * 
	 * @param url
	 *            the url
	 * @param imageView
	 *            the image view
	 * @param thumbnail
	 *            the subsample rate
	 * @param local
	 *            the local
	 */
	private void forceDownload(String url, ImageView imageView) {
		// State sanity: url is guaranteed to never be null in
		// DownloadedDrawable and cache keys.
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}
		if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			imageView.setMinimumHeight(156);
			task.execute(url);
		}
	}

	/**
	 * Returns true if the current download has been canceled or if there was no
	 * download in progress on this image view. Returns false if the download in
	 * progress deals with the same url. The download is not stopped in that
	 * case.
	 * 
	 * @param url
	 *            the url
	 * @param imageView
	 *            the image view
	 * @return true, if successful
	 */
	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the bitmap downloader task.
	 * 
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * Download bitmap.
	 * 
	 * @param url
	 *            the url
	 * @param thumbnail
	 *            the thumbnail
	 * @param local
	 *            the local
	 * @return the bitmap
	 */
	Bitmap downloadBitmap(String url) {
		Bitmap b = null;
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					b = BitmapFactory.decodeStream(inputStream);
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
		}
		return b;
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

		/** The url. */
		private String url;

		/** The image view reference. */
		private final WeakReference<ImageView> imageViewReference;

		/**
		 * Instantiates a new bitmap downloader task.
		 * 
		 * @param imageView
		 *            the image view
		 */
		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Actual download method.
		 * 
		 * @param params
		 *            the params
		 * @return the bitmap
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downloadBitmap(url);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView.
		 * 
		 * @param bitmap
		 *            the bitmap
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			addBitmapToCache(url, bitmap);

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				// Or if we don't use any bitmap to task association
				// (NO_DOWNLOADED_DRAWABLE mode)
				if (this == bitmapDownloaderTask) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	static class DownloadedDrawable extends ColorDrawable {

		/** The bitmap downloader task reference. */
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		/**
		 * Instantiates a new downloaded drawable.
		 * 
		 * @param bitmapDownloaderTask
		 *            the bitmap downloader task
		 */
		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
			super(Color.BLACK);
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		/**
		 * Gets the bitmap downloader task.
		 * 
		 * @return the bitmap downloader task
		 */
		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	/**
	 * Adds this bitmap to the cache.
	 * 
	 * @param url
	 *            the url
	 * @param bitmap
	 *            The newly downloaded bitmap.
	 */
	private void addBitmapToCache(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (mCache) {
				mCache.put(url, bitmap);
			}
		}
	}

	/**
	 * Gets the bitmap from cache.
	 * 
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	private Bitmap getBitmapFromCache(String url) {
		// First try the hard reference cache
		synchronized (mCache) {
			final Bitmap bitmap = mCache.get(url);
			if (bitmap != null) {
				return bitmap;
			}
		}
		return null;
	}

}
