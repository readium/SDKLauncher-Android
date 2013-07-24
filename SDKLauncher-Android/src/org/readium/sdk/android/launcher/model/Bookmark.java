package org.readium.sdk.android.launcher.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Bookmark {

	private static final String TAG = "Bookmark";
	private final String title;
	private final String idref;
	private final String contentCfi;

	public Bookmark(String title, String idref, String contentCfi) {
		this.title = title;
		this.idref = idref;
		this.contentCfi = contentCfi;
	}

	public String getTitle() {
		return title;
	}

	public String getIdref() {
		return idref;
	}

	public String getContentCfi() {
		return contentCfi;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("title", title);
			json.put("idref", idref);
			json.put("contentCFI", contentCfi);
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
