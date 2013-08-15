package org.readium.sdk.android.launcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewerSettings {

    private final boolean mIsSyntheticSpread;
    private final int mFontSize;
    private final int mColumnGap;
    
	public ViewerSettings(boolean isSyntheticSpread, int fontSize, int columnGap) {
		mIsSyntheticSpread = isSyntheticSpread;
		mFontSize = fontSize;
		mColumnGap = columnGap;
	}

	public boolean isSyntheticSpread() {
		return mIsSyntheticSpread;
	}

	public int getFontSize() {
		return mFontSize;
	}

	public int getColumnGap() {
		return mColumnGap;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("isSyntheticSpread", mIsSyntheticSpread);
		json.put("fontSize", mFontSize);
		json.put("columnGap", mColumnGap);
		return json;
	}

	@Override
	public String toString() {
		return "ViewerSettings [isSyntheticSpread=" + mIsSyntheticSpread
				+ ", fontSize=" + mFontSize + ", columnGap=" + mColumnGap
				+ "]";
	}
	
	
}
