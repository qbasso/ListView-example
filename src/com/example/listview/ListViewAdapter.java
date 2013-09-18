package com.example.listview;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.api.ImageLoaderLru;
import com.example.api.WikiModel;

public class ListViewAdapter extends ArrayAdapter<WikiModel> {

	private ArrayList<WikiModel> mItems;
	private LayoutInflater mInflater;
	private ImageLoaderLru mLoader = new ImageLoaderLru();
	private int mResourceId;
	private int mLoadMoreResource;
	private static final int ITEM_TYPE_REGULAR = 0;
	private static final int ITEM_TYPE_LOADING_MORE = 1;

	private static class ViewHolder {
		ImageView iv;
		TextView name;
		TextView url;
	}

	public ListViewAdapter(Context context, int textViewResourceId,
			int loadingMoreResourceId, ArrayList<WikiModel> objects) {
		super(context, textViewResourceId, objects);
		mItems = objects;
		mInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mResourceId = textViewResourceId;
		mLoadMoreResource = loadingMoreResourceId;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public WikiModel getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		switch (getItemViewType(position)) {
		case ITEM_TYPE_REGULAR:
			ViewHolder viewHolder;
			if (v == null) {
				v = mInflater.inflate(mResourceId, null, false);
				viewHolder = new ViewHolder();
				viewHolder.iv = (ImageView) v.findViewById(R.id.image);
				viewHolder.name = (TextView) v.findViewById(R.id.title);
				viewHolder.url = (TextView) v.findViewById(R.id.subtitle);
				v.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) v.getTag();
			}
			setupRow(viewHolder, position);
			break;
		case ITEM_TYPE_LOADING_MORE:
			if (v == null) {
				v = mInflater.inflate(mLoadMoreResource, null, false);
			}
			break;
		default:
			break;
		}
		return v;
	}

	private void setupRow(ViewHolder viewHolder, int position) {
		WikiModel m = getItem(position);
		mLoader.download(m.getImage(), viewHolder.iv);
		viewHolder.name.setText(m.getName());
		viewHolder.url.setText(m.getUrl());
	}

	@Override
	public int getItemViewType(int position) {
		WikiModel m = mItems.get(position);
		return m == null ? ITEM_TYPE_LOADING_MORE : ITEM_TYPE_REGULAR;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

}
