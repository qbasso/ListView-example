package com.example.api;

import com.loopj.android.http.RequestParams;

public class WikiaApi extends BaseAPI {
	
	private static final String API_URL = "http://www.wikia.com/wikia.php";	

	public WikiaApi() {

	}

	public void getWikiList(String language, int limit, int batch, ApiListener l) {
		RequestParams p = new RequestParams();
		p.put("controller", "WikisApi");
		p.put("method", "getList");
		p.put("lang", language);
		p.put("limit", String.valueOf(limit));
		p.put("batch", String.valueOf(batch));
		p.put("expand", "yes");
		p.put("width", "150");
		p.put("height", "150");
		get(API_URL, p, WikiaListResponse.class, l);
	}
}
