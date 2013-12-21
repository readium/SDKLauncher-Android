/*
 * Page.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
 * Copyright (c) 2012-2013 The Readium Foundation and contributors.
 * 
 * The Readium SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.readium.sdk.android.launcher.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaginationInfo {

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
