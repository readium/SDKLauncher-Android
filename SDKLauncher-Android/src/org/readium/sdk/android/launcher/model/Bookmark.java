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
