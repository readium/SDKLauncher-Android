/*
 * WebViewActivity.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-10.
 * Copyright (c) 2012-2013 The Readium Foundation and contributors.
 * 
 * The Readium SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.readium.sdk.android.launcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.SpineItem;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;
import org.readium.sdk.android.launcher.model.OpenPageRequest;
import org.readium.sdk.android.launcher.model.Page;
import org.readium.sdk.android.launcher.model.PaginationInfo;
import org.readium.sdk.android.launcher.model.ReadiumJSApi;
import org.readium.sdk.android.launcher.model.ViewerSettings;
import org.readium.sdk.android.launcher.util.EpubServer;
import org.readium.sdk.android.launcher.util.HTMLUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class WebViewActivity extends FragmentActivity implements ViewerSettingsDialog.OnViewerSettingsChange {

	private static final String TAG = "WebViewActivity";
	private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
	private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";
	
	private WebView mWebview;
	private Container mContainer;
	private Package mPackage;
	private OpenPageRequest mOpenPageRequestData;
	private TextView mPageInfo;
	private ViewerSettings mViewerSettings;
	private ReadiumJSApi mReadiumJSApi;
	private EpubServer mServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		
		mWebview = (WebView) findViewById(R.id.webview);
		mPageInfo = (TextView) findViewById(R.id.page_info);
		initWebView();

        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mContainer = ContainerHolder.getInstance().get(extras.getLong(Constants.CONTAINER_ID));
                if (mContainer == null) {
                	finish();
                	return;
                }
                mPackage = mContainer.getDefaultPackage();
                try {
					mOpenPageRequestData = OpenPageRequest.fromJSON(extras.getString(Constants.OPEN_PAGE_REQUEST_DATA));
				} catch (JSONException e) {
					Log.e(TAG, "Constants.OPEN_PAGE_REQUEST_DATA must be a valid JSON object: "+e.getMessage(), e);
				}
            }
        }
        new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
        		mServer = new EpubServer(EpubServer.HTTP_HOST, EpubServer.HTTP_PORT, mPackage, true);
    			mServer.startServer();
    			return null;
        	}
        }.execute();

        // Load the page skeleton
        mWebview.loadUrl(READER_SKELETON);
        mViewerSettings = new ViewerSettings(false, 100, 20);
        mReadiumJSApi = new ReadiumJSApi(new ReadiumJSApi.JSLoader() {
			
			@Override
			public void loadJS(String javascript) {
				mWebview.loadUrl(javascript);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mServer.stop();
        mWebview.loadUrl(READER_SKELETON);
		((ViewGroup) mWebview.getParent()).removeView(mWebview);
		mWebview.removeAllViews();
		mWebview.clearCache(true);
		mWebview.clearHistory();
		mWebview.destroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mWebview.onPause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mWebview.onResume();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		mWebview.getSettings().setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mWebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
		}
		mWebview.setWebViewClient(new EpubWebViewClient());
		mWebview.setWebChromeClient(new EpubWebChromeClient());

		mWebview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    int itemId = item.getItemId();
	    switch (itemId) {
	    case R.id.add_bookmark:
			Log.d(TAG, "Add a bookmark");
			mReadiumJSApi.bookmarkCurrentPage();
			return true;
	    case R.id.settings:
			Log.d(TAG, "Show settings");
			showSettings();
			return true;
	    }
	    return false;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.left) {
			mReadiumJSApi.openPageLeft();
		} else if (v.getId() == R.id.right) {
			mReadiumJSApi.openPageRight();
		}
	}
	
	private void showSettings() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		DialogFragment dialog = new ViewerSettingsDialog(this, mViewerSettings);
        dialog.show(fm, "dialog");
		fragmentTransaction.commit();
	}

	@Override
	public void onViewerSettingsChange(ViewerSettings viewerSettings) {
		updateSettings(viewerSettings);
	}
	
	private void updateSettings(ViewerSettings viewerSettings) {
		mViewerSettings = viewerSettings;
		mReadiumJSApi.updateSettings(viewerSettings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}

    public final class EpubWebViewClient extends WebViewClient {
    	
        private static final String UTF_8 = "utf-8";

		@Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	Log.d(TAG, "onPageStarted: "+url);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	Log.d(TAG, "onPageFinished: "+url);
        	if (url.equals(READER_SKELETON)) {
        		Log.d(TAG, "openPageRequestData: "+mOpenPageRequestData);
        		mReadiumJSApi.openBook(mPackage, mViewerSettings, mOpenPageRequestData);
        	}
        }
        
        @Override
        public void onLoadResource(WebView view, String url) {
        	String cleanedUrl = cleanResourceUrl(url);
        	byte[] data = mPackage.getContent(cleanedUrl);
            if (data.length > 0) {
            	ManifestItem item = mPackage.getManifestItem(cleanedUrl);
            	String mimetype = (item != null) ? item.getMediaType() : null;
            	mWebview.loadData(new String(data), mimetype, UTF_8);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        	String cleanedUrl = cleanResourceUrl(url);
        	InputStream data = mPackage.getInputStream(cleanedUrl);
        	ManifestItem item = mPackage.getManifestItem(cleanedUrl);
        	if (item != null && item.isHtml()) {
            	byte[] binary;
				try {
					binary = new byte[data.available()];
	            	data.read(binary);
	            	data.close();
		            data = new ByteArrayInputStream(HTMLUtil.htmlByReplacingMediaURLsInHTML(new String(binary), 
		            		cleanedUrl, "PackageUUID").getBytes());
				} catch (IOException e) {
					Log.e(TAG, ""+e.getMessage(), e);
				}
        	}
        	String mimetype = (item != null) ? item.getMediaType() : null;
        	return new WebResourceResponse(mimetype, UTF_8, data);
        }
    }
    
    private String cleanResourceUrl(String url) {
        String cleanUrl = url.replace(ASSET_PREFIX, "");
        cleanUrl = (cleanUrl.startsWith(mPackage.getBasePath())) ? cleanUrl.replaceFirst(mPackage.getBasePath(), "") : cleanUrl;
        int indexOfSharp = cleanUrl.indexOf('#');
        if (indexOfSharp >= 0) {
            cleanUrl = cleanUrl.substring(0, indexOfSharp);
        }
        return cleanUrl;
    }

	public class EpubWebChromeClient extends WebChromeClient implements
			MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			Log.d(TAG, "here in on ShowCustomView: " + view);
			super.onShowCustomView(view, callback);
			if (view instanceof FrameLayout) {
				FrameLayout frame = (FrameLayout) view;
				Log.d(TAG, "frame.getFocusedChild(): " + frame.getFocusedChild());
				if (frame.getFocusedChild() instanceof VideoView) {
					VideoView video = (VideoView) frame.getFocusedChild();
					// frame.removeView(video);
					// a.setContentView(video);
					video.setOnCompletionListener(this);
					video.setOnErrorListener(this);
					video.start();
				}
			}
		}

		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, "Video completed");

			// a.setContentView(R.layout.main);
			// WebView wb = (WebView) a.findViewById(R.id.webview);
			// a.initWebView();
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.d(TAG, "MediaPlayer onError: " + what + ", " + extra);
			return false;
		}
	}
    
	public class EpubInterface {
		
		@JavascriptInterface
		public void onPaginationChanged(String currentPagesInfo) {
			try {
				PaginationInfo paginationInfo = PaginationInfo.fromJson(currentPagesInfo);
				List<Page> openPages = paginationInfo.getOpenPages();
				if (!openPages.isEmpty()) {
					final Page page = openPages.get(0);
					runOnUiThread(new Runnable() {
						public void run() {
							mPageInfo.setText(getString(R.string.page_x_of_y,
									page.getSpineItemPageIndex() + 1,
									page.getSpineItemPageCount()));
							SpineItem spineItem = mPackage.getSpineItem(page.getIdref());
							boolean isFixedLayout = spineItem.isFixedLayout();
				            mWebview.getSettings().setBuiltInZoomControls(isFixedLayout);
				            mWebview.getSettings().setDisplayZoomControls(false);
						}
					});
				}
			} catch (JSONException e) {
				Log.e(TAG, ""+e.getMessage(), e);
			}
		}
		
		@JavascriptInterface
		public void onSettingsApplied() {
			Log.d(TAG, "onSettingsApplied");
		}
		
		@JavascriptInterface
		public void onReaderInitialized() {
			Log.d(TAG, "onReaderInitialized");
		}
		
		@JavascriptInterface
		public void getBookmarkData(final String bookmarkData) {
			AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this).
					setTitle(R.string.add_bookmark);
	        
	        final EditText editText = new EditText(WebViewActivity.this);
	        editText.setId(android.R.id.edit);
	        editText.setHint(R.string.title);
	        builder.setView(editText);
	        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						String title = editText.getText().toString();
						try {
							JSONObject bookmarkJson = new JSONObject(bookmarkData);
							BookmarkDatabase.getInstance().addBookmark(mContainer.getName(), title,
									bookmarkJson.getString("idref"), bookmarkJson.getString("contentCFI"));
						} catch (JSONException e) {
							Log.e(TAG, ""+e.getMessage(), e);
						}
					}
				}
			});
	        builder.setNegativeButton(android.R.string.cancel, null);
	        builder.show();
		}
	}
}
