package com.example.listview;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class ListViewAdapter extends ArrayAdapter<WikiModel> {

	public ListViewAdapter(Context context, int textViewResourceId,
			List<WikiModel> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

}
