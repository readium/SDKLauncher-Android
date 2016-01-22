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
	
	public void openBook(Package pckg, ViewerSettings viewerSettings,
			OpenPageRequest openPageRequestData) {
		JSONObject openBookData = new JSONObject();
		try {
			openBookData.put("package", pckg.toJSON());
			openBookData.put("settings", viewerSettings.toJSON());
			openBookData.put("openPageRequest", openPageRequestData.toJSON());
		} catch (JSONException e) {
			Log.e(TAG, ""+e.getMessage(), e);
		}
		loadJSOnReady("ReadiumSDK.reader.openBook("+openBookData.toString()+");");
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
	
	public void nextMediaOverlay(){
		loadJSOnReady("ReadiumSDK.reader.nextMediaOverlay();");
	}
	
	public void previousMediaOverlay(){
		loadJSOnReady("ReadiumSDK.reader.previousMediaOverlay();");
	}
	
	public void toggleMediaOverlay(){
		loadJSOnReady("ReadiumSDK.reader.toggleMediaOverlay();");
	}
	
	
	
    private void loadJSOnReady(String jScript) {
        loadJS("$(document).ready(function () {" + jScript + "});");
    }

    private void loadJS(String jScript) {
		//Log.i(TAG, "loadJS: "+jScript);
		mJSLoader.loadJS("javascript:(function(){" + jScript + "})()");
    }
}
