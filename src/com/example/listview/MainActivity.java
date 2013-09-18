package com.example.listview;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.api.BaseAPI.ApiListener;
import com.example.api.BaseAPI.RestErrorType;
import com.example.api.WikiModel;
import com.example.api.WikiaApi;
import com.example.api.WikiaListResponse;

public class MainActivity extends Activity implements ApiListener,
		OnScrollListener {

	private WikiaApi mApiClient;
	private static final int BATCH_SIZE = 25;
	private int mCurrentBatch = 1;
	private int mTotalBatches;
	private boolean mListShown = false;
	private boolean mLoading = false;
	private ArrayList<WikiModel> mItems = new ArrayList<WikiModel>();

	private ProgressBar mProgress;
	private ListView mList;
	private ListViewAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApiClient = new WikiaApi();
		setContentView(R.layout.activity_main);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mList = (ListView) findViewById(R.id.list);
		mAdapter = new ListViewAdapter(this, R.layout.list_item, R.layout.progress_item, mItems);
		mList.setAdapter(mAdapter);
		mList.setOnScrollListener(this);
		mApiClient.getWikiList("en", BATCH_SIZE, mCurrentBatch, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onApiStart() {
	}

	@Override
	public void onApiSuccess(WikiaListResponse response) {
		if (!mListShown) {
			mProgress.setVisibility(View.GONE);
			mList.setVisibility(View.VISIBLE);
		}
		mCurrentBatch = response.getCurrentBatch();
		mTotalBatches = response.getBatches();
		if (mItems.size() > 0 && mItems.get(mItems.size() - 1) == null) {
			mItems.remove(mItems.size() - 1);
		}
		mItems.addAll(Arrays.asList(response.getItems()));
		if (mCurrentBatch < mTotalBatches) {
			mItems.add(null);
		}
		mLoading = false;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onApiError(RestErrorType type, Throwable error,
			String response, WikiModel jsonResponse) {
		Toast.makeText(this, "Error occured! Try later", Toast.LENGTH_SHORT)
				.show();
		mProgress.setVisibility(View.GONE);
	}

	@Override
	public void onApiFinish() {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (totalItemCount - visibleItemCount <= firstVisibleItem
				&& mCurrentBatch < mTotalBatches) {
			if (!mLoading) {
				mApiClient.getWikiList("en", 25, mCurrentBatch + 1, this);
				mLoading = true;
			}
		}
	}

}
