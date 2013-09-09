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
