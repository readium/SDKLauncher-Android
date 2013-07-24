package org.readium.sdk.android.launcher;

import java.io.ByteArrayInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;

public class WebViewActivity extends Activity {

	private static final String TAG = "WebViewActivity";
	private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
	private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";
	
	private WebView webview;
	private Container container;
	private Package pckg;
	private String openPageRequestData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		
		webview = (WebView) findViewById(R.id.webview);
		initWebView();

        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                container = ContainerHolder.getInstance().get(extras.getLong(Constants.CONTAINER_ID));
                if (container == null) {
                	finish();
                	return;
                }
                pckg = container.getDefaultPackage();
                openPageRequestData = extras.getString(Constants.OPEN_PAGE_REQUEST_DATA);
            }
        }

        // Load the page skeleton
        webview.loadUrl(READER_SKELETON);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		webview.getSettings().setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
		}
		webview.getSettings().setLightTouchEnabled(true);
		webview.getSettings().setPluginState(WebSettings.PluginState.ON);
		webview.setWebViewClient(new EpubWebViewClient());
		webview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    if (item.getItemId() == R.id.add_bookmark) {
			Log.i(TAG, "Add a bookmark");
			bookmarkCurrentPage();
			return true;
	    }
	    return false;
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.left) {
			openPageLeft();
		} else if (v.getId() == R.id.right) {
			openPageRight();
		}
	}
	
	private void bookmarkCurrentPage() {
		loadJS("window.LauncherUI.getBookmarkData(ReadiumSDK.reader.bookmarkCurrentPage());");
	}
	
	private void openPageLeft() {
		loadJS("ReadiumSDK.reader.openPageLeft();");
	}
	
	private void openPageRight() {
		loadJS("ReadiumSDK.reader.openPageRight();");
	}
	
	private void openBook(String packageData, String openPageRequest) {
		Log.i(TAG, "packageData: "+packageData);
		loadJSOnReady("ReadiumSDK.reader.openBook("+packageData+", "+openPageRequest+");");
	}
	
	private void openContentUrl(String href, String baseUrl) {
		loadJSOnReady("ReadiumSDK.reader.openContentUrl(\""+href+"\", \""+baseUrl+"\");");
	}
	
	private void openSpineItemPage(String idRef, int page) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemPage(\""+idRef+"\", "+page+");");
	}

	private void openSpineItemElementCfi(String idRef, String elementCfi) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemElementCfi(\""+idRef+"\",\""+elementCfi+"\");");
	}

    private void loadJSOnReady(String jScript) {
        loadJS("$(document).ready(function () {" + jScript + "});");
    }

    private void loadJS(String jScript) {
        webview.loadUrl("javascript:(function(){" + jScript + "})()");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}

    public final class EpubWebViewClient extends WebViewClient {
    	
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	Log.i(TAG, "onPageStarted: "+url);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	Log.i(TAG, "onPageFinished: "+url);
        	if (url.equals(READER_SKELETON)) {
        		openBook(pckg.toJSON(), openPageRequestData);
        	}
        }
        
        @Override
        public void onLoadResource(WebView view, String url) {
        	Log.i(TAG, "onLoadResource: "+url);
        	byte[] data = pckg.getContent(cleanResourceUrl(url));
            if (data.length > 0) {
                // TODO Pass the correct mimetype
            	webview.loadDataWithBaseURL(url, new String(data), null, "utf-8", null);
            }
        }
        
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        	byte[] data = pckg.getContent(cleanResourceUrl(url));
//        	return (data.length > 0);
//        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        	Log.i(TAG, "shouldInterceptRequest ? "+url);

            byte[] data = pckg.getContent(cleanResourceUrl(url));
        	//Log.i(TAG, "data : "+new String(data));
            // TODO Pass the correct mimetype
        	return new WebResourceResponse(null, "utf-8", new ByteArrayInputStream(data));
        }
        
        private String cleanResourceUrl(String url) {
        	String cleanUrl = url.replace(ASSET_PREFIX, "");
        	return (cleanUrl.startsWith(pckg.getBasePath())) ? 
        			cleanUrl.replaceFirst(pckg.getBasePath(), "") : cleanUrl;
        }
    }
    
	public class EpubInterface {

		@JavascriptInterface
		public void onOpenPage(String currentPagesInfo) {
			Log.i(TAG, "currentPagesInfo: "+currentPagesInfo);
		}
		
		@JavascriptInterface
		public void getBookmarkData(final String bookmarkData) {
			Log.i(TAG, "bookmarkData: "+bookmarkData);
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
							BookmarkDatabase.getInstance().addBookmark(container.getName(), title,
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
