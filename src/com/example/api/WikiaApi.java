package com.example.api;

import com.example.listview.WikiaListResponse;
import com.loopj.android.http.RequestParams;

public class WikiaApi extends BaseAPI {
	
	private static final String API_URL = "http://www.wikia.com/wikia.php?controller=WikisApi";

	private WikiaApi() {

	}

	public void getWikiList(String language, int limit, int batch, ApiListener l) {
		RequestParams p = new RequestParams();
		p.put("method", "getList");
		p.put("lang", language);
		p.put("limit", String.valueOf(limit));
		p.put("batch", String.valueOf(batch));
		p.put("expand", "yes");
		get(API_URL, p, WikiaListResponse.class, l);
	}
}
