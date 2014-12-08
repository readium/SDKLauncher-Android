/*
 * Page.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
 * Copyright (c) 2012-2013 The Readium Foundation and contributors.
 */

//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this 
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice, 
//  this list of conditions and the following disclaimer in the documentation and/or 
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be 
//  used to endorse or promote products derived from this software without specific 
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
//  OF THE POSSIBILITY OF SUCH DAMAGE

package org.readium.sdk.android.launcher.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaginationInfo {

	private final boolean isRightToLeft;
	private final boolean isFixedLayout;
	private final int spineItemCount;
	private final List<Page> openPages;
    private final boolean canGoLeft;
    private final boolean canGoRight;
    private final boolean canGoPrev;
    private final boolean canGoNext;

    public PaginationInfo(boolean isRightToLeft, boolean isFixedLayout, int spineItemCount, boolean canGoLeft, boolean canGoRight, boolean canGoPrev, boolean canGoNext) {

        this.isRightToLeft = isRightToLeft;
        this.isFixedLayout = isFixedLayout;
        this.spineItemCount = spineItemCount;
        this.openPages = new ArrayList<Page>();
        this.canGoLeft = canGoLeft;
        this.canGoRight = canGoRight;
        this.canGoPrev = canGoPrev;
        this.canGoNext = canGoNext;
    }

	public boolean isRightToLeft() {
		return isRightToLeft;
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

    public boolean canGoLeft() {
        return canGoLeft;
    }

    public boolean canGoRight() {
        return canGoRight;
    }

    public boolean canGoPrev() {
        return canGoPrev;
    }

    public boolean canGoNext() {
        return canGoNext;
    }

    public static PaginationInfo fromJson(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		PaginationInfo paginationInfo = new PaginationInfo(json.optBoolean("isRightToLeft"),
				json.optBoolean("isFixedLayout"),
				json.optInt("spineItemCount"),
                json.optBoolean("canGoLeft"),
                json.optBoolean("canGoRight"),
                json.optBoolean("canGoPrev"),
                json.optBoolean("canGoNext"));
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
