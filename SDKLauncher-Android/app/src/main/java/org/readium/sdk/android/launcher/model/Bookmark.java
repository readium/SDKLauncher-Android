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

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Bookmark {

	private static final String TAG = "Bookmark";
	private final String mTitle;
	private final String mIdref;
	private final String mContentCfi;

	public Bookmark(String title, String idref, String contentCfi) {
		mTitle = title;
		mIdref = idref;
		mContentCfi = contentCfi;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getIdref() {
		return mIdref;
	}

	public String getContentCfi() {
		return mContentCfi;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("title", mTitle);
			json.put("idref", mIdref);
			json.put("contentCFI", mContentCfi);
		} catch (JSONException e) {
			Log.e(TAG, ""+e.getMessage(), e);
		}
		return json;
	}
	
	public static Bookmark fromJSON(String data) throws JSONException {
		return fromJSON(new JSONObject(data));
	}
	
	public static Bookmark fromJSON(JSONObject json) throws JSONException {
		return new Bookmark(json.getString("title"), json.getString("idref"), json.getString("contentCFI"));
	}
}
