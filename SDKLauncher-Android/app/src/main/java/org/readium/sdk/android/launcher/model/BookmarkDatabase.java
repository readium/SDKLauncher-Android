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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class BookmarkDatabase {

	private static final String TAG = "BookmarkDatabase";
	private static final String BOOKMARKS_KEY = "bookmarks";
	private static BookmarkDatabase INSTANCE;
	private final Context mContext;
	
	private BookmarkDatabase(Context context) {
		mContext = context;
	}
	
	public static void initInstance(Context context) {
		INSTANCE = new BookmarkDatabase(context);
	}
	
	public static BookmarkDatabase getInstance() {
		return INSTANCE;
	}
	
	public void addBookmark(String container, String title, String idref, String contentCfi) {
		List<Bookmark> bookmarks = getBookmarks(container);
		bookmarks.add(new Bookmark(title, idref, contentCfi));
		setBookmarks(container, bookmarks);
	}
	
	public void setBookmarks(String container, List<Bookmark> bookmarks) {
		SharedPreferences pref = getPreference(container);
		JSONArray json = new JSONArray();
		for (Bookmark bookmark : bookmarks) {
			json.put(bookmark.toJSON());
		}
		pref.edit().putString(BOOKMARKS_KEY, json.toString()).commit();
	}
	
	public List<Bookmark> getBookmarks(String container) {
		SharedPreferences pref = getPreference(container);
		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		
		try {
			JSONArray json = new JSONArray(pref.getString(BOOKMARKS_KEY, new JSONArray().toString()));
			for (int i = 0; i < json.length(); i++) {
				Bookmark bookmark = Bookmark.fromJSON(json.getJSONObject(i));
				bookmarks.add(bookmark);
			}
		} catch (JSONException e) {
			Log.e(TAG, ""+e.getMessage(), e);
		}
		return bookmarks;
	}

	private SharedPreferences getPreference(String container) {
		return mContext.getSharedPreferences(container, Context.MODE_PRIVATE);
	}
}
