package org.readium.sdk.android.launcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Font {

    private final String mDisplayName;
    private final String mFontFamily;
    private final String mUrl;

    public Font(String displayName, String fontFamily, String url) {
        this.mDisplayName = displayName;
        this.mFontFamily = fontFamily;
        this.mUrl = url;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public String getUrl() {
        return mUrl;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("displayName", mDisplayName);
        json.put("fontFamily", mFontFamily);
        json.put("url", mUrl);
        return json;
    }

    @Override
    public String toString() {
        return "Font{" +
                "mDisplayName=" + mDisplayName +
                ", mFontFamily=" + mFontFamily +
                ", mUrl=" + mUrl +
                '}';
    }
}
