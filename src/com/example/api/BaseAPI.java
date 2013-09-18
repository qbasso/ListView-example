package com.example.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * The Class BaseAPI. Uses modified android async client library, as in wikia
 * API parameters order apparently matters. (by default parameters were sorted
 * alphabeticaly in async http client library)
 */
public class BaseAPI {

	/** The client. */
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	private Handler mHandler = new Handler();

	private ExecutorService mThreadPool = Executors.newCachedThreadPool();

	/**
	 * Gets the.
	 * 
	 * @param url
	 *            the url
	 * @param params
	 *            the params
	 * @param type
	 *            the type
	 * @param taskCallback
	 *            the task callback
	 */
	protected void get(String url, RequestParams params,
			final Class<? extends WikiaListResponse> type,
			final ApiListener taskCallback) {
		request(RequestType.GET, url, params, type, taskCallback);
	}

	/**
	 * Request.
	 * 
	 * @param requestType
	 *            the request type
	 * @param url
	 *            the url
	 * @param params
	 *            the params
	 * @param type
	 *            the type
	 * @param taskCallback
	 *            the task callback
	 */
	protected void request(RequestType requestType, String url,
			RequestParams params,
			final Class<? extends WikiaListResponse> type,
			final ApiListener taskCallback) {
		final ApiListener activityCallback = taskCallback;
		AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				activityCallback.onApiStart();
			}

			@Override
			public void onSuccess(final String response) {
				mThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						final WikiaListResponse jsonObj = deserialize(response, type);
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								activityCallback.onApiSuccess(jsonObj);
							}
						});
					}
				});
			}

			@Override
			public void onFailure(Throwable e, String response) {
				activityCallback.onApiError(RestErrorType.HTTP, e, response,
						null);
			}

			@Override
			public void onFinish() {
				activityCallback.onApiFinish();
			}
		};

		switch (requestType) {
		case GET:
			client.get(url, params, handler);
			break;
		case POST:
			client.post(url, params, handler);
			break;
		}
	}

	/**
	 * Deserialize.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param jsonString
	 *            the json string
	 * @param type
	 *            the type
	 * @return the t
	 * @throws JsonSyntaxException
	 *             the json syntax exception
	 */
	private <T> T deserialize(String jsonString, Class<T> type)
			throws JsonSyntaxException {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, type);
	}

	/**
	 * The Enum RequestType.
	 */
	public enum RequestType {

		/** The get. */
		GET,
		/** The post. */
		POST
	}

	/**
	 * The Enum RestErrorType.
	 */
	public enum RestErrorType {

		/** The http. */
		HTTP,
		/** The json. */
		JSON
	}

	// Must be implemented at a root level, normally on the Base activity or
	// Base fragment
	// in order to receive basics notifications.
	/**
	 * The listener interface for receiving api events. The class that is
	 * interested in processing a api event implements this interface, and the
	 * object created with that class is registered with a component using the
	 * component's <code>addApiListener<code> method. When
	 * the api event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ApiEvent
	 */
	public interface ApiListener {

		/**
		 * On api start.
		 */
		void onApiStart();

		/**
		 * On api success.
		 * 
		 * @param response
		 *            the response
		 */
		void onApiSuccess(WikiaListResponse response);

		/**
		 * On api error.
		 * 
		 * @param type
		 *            the type
		 * @param error
		 *            the error
		 * @param response
		 *            the response
		 * @param jsonResponse
		 *            the json response
		 */
		void onApiError(RestErrorType type, Throwable error, String response,
				WikiModel jsonResponse);

		/**
		 * On api finish.
		 */
		void onApiFinish();
	}

	/**
	 * The Interface StringResponseCallback.
	 */
	public interface StringResponseCallback {

		/**
		 * On api start.
		 */
		void onApiStart();

		/**
		 * On success.
		 * 
		 * @param response
		 *            the response
		 */
		void onSuccess(String response);

		/**
		 * On failure.
		 * 
		 * @param response
		 *            the response
		 */
		void onFailure(String response);
	}

}
