package org.readium.sdk.android.launcher.model;

import org.json.JSONException;
import org.readium.sdk.android.Package;

import android.util.Log;

public class ReadiumJSApi {

	public interface JSLoader {
		public void loadJS(String javascript);
	}

	private static final String TAG = "ReadiumJSApi";

	private JSLoader mJSLoader;
	
	public ReadiumJSApi(JSLoader jsLoader) {
		mJSLoader = jsLoader;
	}

	public void bookmarkCurrentPage() {
		loadJS("window.LauncherUI.getBookmarkData(ReadiumSDK.reader.bookmarkCurrentPage());");
	}
	
	public void openPageLeft() {
		loadJS("ReadiumSDK.reader.openPageLeft();");
	}
	
	public void openPageRight() {
		loadJS("ReadiumSDK.reader.openPageRight();");
	}
	
	public void openBook(Package pckg, String openPageRequest) {
		loadJSOnReady("ReadiumSDK.reader.openBook("+pckg.toJSON().toString()+", "+openPageRequest+");");
	}
	
	public void updateSettings(ViewerSettings viewerSettings) {
		try {
			loadJSOnReady("ReadiumSDK.reader.updateSettings("+viewerSettings.toJSON().toString()+");");
		} catch (JSONException e) {
			Log.e(TAG, ""+e.getMessage(), e);
		}
	}
	
	public void openContentUrl(String href, String baseUrl) {
		loadJSOnReady("ReadiumSDK.reader.openContentUrl(\""+href+"\", \""+baseUrl+"\");");
	}
	
	public void openSpineItemPage(String idRef, int page) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemPage(\""+idRef+"\", "+page+");");
	}

	public void openSpineItemElementCfi(String idRef, String elementCfi) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemElementCfi(\""+idRef+"\",\""+elementCfi+"\");");
	}

    private void loadJSOnReady(String jScript) {
        loadJS("$(document).ready(function () {" + jScript + "});");
    }

    private void loadJS(String jScript) {
		Log.i(TAG, "loadJS: "+jScript);
		mJSLoader.loadJS("javascript:(function(){" + jScript + "})()");
    }
}
