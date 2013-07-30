package org.readium.sdk.android.launcher.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PaginationInfo {

	private static final String TAG = "PaginationInfo";
	private final String pageProgressionDirection;
	private final boolean isFixedLayout;
	private final int spineItemCount;
	private final List<Page> openPages;
	
	public PaginationInfo(String pageProgressionDirection,
			boolean isFixedLayout, int spineItemCount) {
		this.pageProgressionDirection = pageProgressionDirection;
		this.isFixedLayout = isFixedLayout;
		this.spineItemCount = spineItemCount;
		this.openPages = new ArrayList<Page>();
	}

	public String getPageProgressionDirection() {
		return pageProgressionDirection;
	}

	public boolean isFixedLayout() {
		return isFixedLayout;
	}

	public int getSpineItemCount() {
		return spineItemCount;
	}

	public List<Page> getOpenPages() {
		return openPages;
	}
	
	public static PaginationInfo fromJson(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		PaginationInfo paginationInfo = new PaginationInfo(json.optString("pageProgressionDirection", "ltr"), 
				json.optBoolean("isFixedLayout"), 
				json.optInt("spineItemCount"));
		JSONArray openPages = json.getJSONArray("openPages");
		for (int i = 0; i < openPages.length(); i++) {
			JSONObject p = openPages.getJSONObject(i);
			Page page = new Page(p.optInt("spineItemPageIndex"), p.optInt("spineItemPageCount"),
					p.optString("idref"), p.optInt("spineItemIndex"));
			paginationInfo.openPages.add(page);
		}
		return paginationInfo;
	}
}
