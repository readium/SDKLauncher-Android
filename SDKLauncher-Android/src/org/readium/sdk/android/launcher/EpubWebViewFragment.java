package org.readium.sdk.android.launcher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.SpineItem;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;
import org.readium.sdk.android.launcher.model.OpenPageRequest;
import org.readium.sdk.android.launcher.model.Page;
import org.readium.sdk.android.launcher.model.PaginationInfo;
import org.readium.sdk.android.launcher.model.ReadiumJSApi;
import org.readium.sdk.android.launcher.model.ViewerSettings;
import org.readium.sdk.android.launcher.util.EpubServer;
import org.readium.sdk.android.launcher.util.HTMLUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;


public class EpubWebViewFragment extends WebViewFragment {

    private final boolean quiet = false;

    private static final String TAG = "EpubWebViewFragment";
    private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
    private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";

    // Installs "hook" function so that top-level window (application) can later
    // inject the window.navigator.epubReadingSystem into this HTML document's
    // iframe
    private static final String INJECT_EPUB_RSO_SCRIPT_1 = ""
            + "window.readium_set_epubReadingSystem = function (obj) {"
            + "\nwindow.navigator.epubReadingSystem = obj;"
            + "\nwindow.readium_set_epubReadingSystem = undefined;"
            + "\nvar el1 = document.getElementById(\"readium_epubReadingSystem_inject1\");"
            + "\nif (el1 && el1.parentNode) { el1.parentNode.removeChild(el1); }"
            + "\nvar el2 = document.getElementById(\"readium_epubReadingSystem_inject2\");"
            + "\nif (el2 && el2.parentNode) { el2.parentNode.removeChild(el2); }"
            + "\n};";

    // Iterate top-level iframes, inject global
    // window.navigator.epubReadingSystem if the expected hook function exists (
    // readium_set_epubReadingSystem() ).
    private static final String INJECT_EPUB_RSO_SCRIPT_2 = ""
            + "var epubRSInject =\nfunction(win) {"
            + "\nvar ret = '';"
            + "\nret += win.location.href;"
            + "\nret += ' ---- ';"
            +
            // "\nret += JSON.stringify(win.navigator.epubReadingSystem);" +
            // "\nret += ' ---- ';" +
            "\nif (win.frames)"
            + "\n{"
            + "\nfor (var i = 0; i < win.frames.length; i++)"
            + "\n{"
            + "\nvar iframe = win.frames[i];"
            + "\nret += ' IFRAME ';"
            + "\nif (iframe.readium_set_epubReadingSystem)"
            + "\n{"
            + "\nret += ' EPBRS ';"
            + "\niframe.readium_set_epubReadingSystem(window.navigator.epubReadingSystem);"
            + "\n}" + "\nret += epubRSInject(iframe);" + "\n}" + "\n}"
            + "\nreturn ret;" + "\n};" + "\nepubRSInject(window);";

    // Script tag to inject the "hook" function installer script, added to the
    // head of every epub iframe document
    private static final String INJECT_HEAD_EPUB_RSO_1 = ""
            + "<script id=\"readium_epubReadingSystem_inject1\" type=\"text/javascript\">\n"
            + "//<![CDATA[\n" + INJECT_EPUB_RSO_SCRIPT_1 + "\n" + "//]]>\n"
            + "</script>";
    // Script tag that generates an HTTP request to a fake script => triggers
    // push of window.navigator.epubReadingSystem into this HTML document's
    // iframe
    private static final String INJECT_HEAD_EPUB_RSO_2 = ""
            + "<script id=\"readium_epubReadingSystem_inject2\" type=\"text/javascript\" "
            + "src=\"/%d/readium_epubReadingSystem_inject.js\"> </script>";
    // Script tag to load the mathjax script payload, added to the head of epub
    // iframe documents, only if <math> tags are detected
    private static final String INJECT_HEAD_MATHJAX = "<script type=\"text/javascript\" src=\"/readium_MathJax.js\"> </script>";

    // Location of payloads in the asset folder
    private static final String PAYLOAD_MATHJAX_ASSET = "reader-payloads/MathJax.js";
    private static final String PAYLOAD_ANNOTATIONS_CSS_ASSET = "reader-payloads/annotations.css";

    private final EpubServer.DataPreProcessor dataPreProcessor = new EpubServer.DataPreProcessor() {

        @Override
        public byte[] handle(byte[] data, String mime, String uriPath,
                             ManifestItem item) {
            if (mime == null
                    || (mime != "text/html" && mime != "application/xhtml+xml")) {
                return null;
            }

            if (!quiet)
                Log.d(TAG, "PRE-PROCESSED HTML: " + uriPath);

            String htmlText = new String(data, Charset.forName("UTF-8"));

            // String uuid = mPackage.getUrlSafeUniqueID();
            String newHtml = htmlText; // HTMLUtil.htmlByReplacingMediaURLsInHTML(htmlText,
            // cleanedUrl, uuid);
            // //"PackageUUID"

            // Set up the script tags to add to the head
            String tagsToInjectToHead = INJECT_HEAD_EPUB_RSO_1
                    // Slightly change fake script src url with an
                    // increasing count to prevent caching of the
                    // request
                    + String.format(INJECT_HEAD_EPUB_RSO_2,
                    ++mEpubRsoInjectCounter);
            // Checks for the existance of MathML => request
            // MathJax payload
            if (newHtml.contains("<math") || newHtml.contains("<m:math")) {
                tagsToInjectToHead += INJECT_HEAD_MATHJAX;
            }

            newHtml = HTMLUtil.htmlByInjectingIntoHead(newHtml,
                    tagsToInjectToHead);

            // Log.d(TAG, "HTML head inject: " + newHtml);

            return newHtml.getBytes();
        }
    };

    private WebView mWebview;
    private Container mContainer;
    private org.readium.sdk.android.Package mPackage;
    private OpenPageRequest mOpenPageRequestData;
    private TextView mPageInfo;
    private ViewerSettings mViewerSettings;
    public ReadiumJSApi readiumJSApi;
    private EpubServer mServer;

    private int mEpubRsoInjectCounter = 0;



    @SuppressLint({"SetJavaScriptEnabled", "NewApi", "AddJavascriptInterface"})
    private void initWebView() {
        mWebview.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        mWebview.setWebViewClient(new EpubWebViewClient());
        mWebview.setWebChromeClient(new EpubWebChromeClient());

        mWebview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mWebview = getWebView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && 0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mPageInfo = (TextView) getActivity().findViewById(R.id.page_info);
        initWebView();


        Bundle extras = getArguments();
        if (extras != null) {
            mContainer = ContainerHolder.getInstance().get(
                    extras.getLong(Constants.CONTAINER_ID));
            if (mContainer == null) {
                getActivity().finish();
                return view;
            }
            mPackage = mContainer.getDefaultPackage();

            String rootUrl = "http://" + EpubServer.HTTP_HOST + ":"
                    + EpubServer.HTTP_PORT + "/";
            mPackage.setRootUrls(rootUrl, null);

            try {
                mOpenPageRequestData = OpenPageRequest.fromJSON(extras
                        .getString(Constants.OPEN_PAGE_REQUEST_DATA));
            } catch (JSONException e) {
                Log.e(TAG,
                        "Constants.OPEN_PAGE_REQUEST_DATA must be a valid JSON object: "
                                + e.getMessage(), e);
            }
        }


        // No need, EpubServer already launchers its own thread
        // new AsyncTask<Void, Void, Void>() {
        // @Override
        // protected Void doInBackground(Void... params) {
        // //xxx
        // return null;
        // }
        // }.execute();

        mServer = new EpubServer(EpubServer.HTTP_HOST, EpubServer.HTTP_PORT,
                mPackage, quiet, dataPreProcessor);
        mServer.startServer();

        // Load the page skeleton
        mWebview.loadUrl(READER_SKELETON);
        mViewerSettings = new ViewerSettings(
                ViewerSettings.SyntheticSpreadMode.AUTO,
                ViewerSettings.ScrollMode.AUTO, 100, 20);

        readiumJSApi = new ReadiumJSApi(new ReadiumJSApi.JSLoader() {
            @Override
            public void loadJS(String javascript) {
                mWebview.loadUrl(javascript);
            }
        });

        return view;
    }

    public final class EpubWebViewClient extends WebViewClient {

        private static final String HTTP = "http";
        private static final String UTF_8 = "utf-8";
        private boolean skeletonPageLoaded = false;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!quiet)
                Log.d(TAG, "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!quiet)
                Log.d(TAG, "onPageFinished: " + url);

            if (!skeletonPageLoaded && url.equals(READER_SKELETON)) {
                skeletonPageLoaded = true;

                if (!quiet)
                    Log.d(TAG, "openPageRequestData: " + mOpenPageRequestData);

                readiumJSApi.openBook(mPackage, mViewerSettings,
                        mOpenPageRequestData);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (!quiet)
                Log.d(TAG, "onLoadResource: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!quiet)
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            return false;
        }

        public class SyncronizeObj {

            public void doWait() {
                doWait(0);
            }

            public void doWait(long l) {
                synchronized (this) {
                    try {
                        this.wait(l);
                    } catch (InterruptedException e) {
                    }
                }
            }

            public void doNotify() {
                synchronized (this) {
                    this.notify();
                }
            }
        }

        private final SyncronizeObj syncObj = new SyncronizeObj();

        private void evaluateJavascript(final String script) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!quiet)
                        Log.d(TAG, "WebView evaluateJavascript: " + script + "");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWebview.evaluateJavascript(script,
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String str) {
                                        if (!quiet)
                                            Log.d(TAG,
                                                    "WebView evaluateJavascript RETURN: "
                                                            + str);
                                        syncObj.doNotify();
                                    }
                                });
                    } else {
                        mWebview.loadUrl("javascript:" + script);
                        syncObj.doNotify();
                    }
                }
            });
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            if (!quiet)
                Log.d(TAG, "shouldInterceptRequest: " + url);

            if (url != null && url != "undefined") {

                String localHttpUrlPrefix = "http://" + EpubServer.HTTP_HOST
                        + ":" + EpubServer.HTTP_PORT;
                boolean isLocalHttp = url.startsWith(localHttpUrlPrefix);

                // Uri uri = Uri.parse(url);
                // uri.getScheme()

                if (url.startsWith("http") && !isLocalHttp) {
                    if (!quiet)
                        Log.d(TAG, "HTTP (NOT LOCAL): " + url);
                    return super.shouldInterceptRequest(view, url);
                }

                String cleanedUrl = cleanResourceUrl(url, false);
                if (!quiet)
                    Log.d(TAG, url + " => " + cleanedUrl);

                if (cleanedUrl
                        .matches("\\/?\\d*\\/readium_epubReadingSystem_inject.js")) {
                    if (!quiet)
                        Log.d(TAG, "navigator.epubReadingSystem inject ...");

                    // Fake script requested, this is immediately invoked after
                    // epubReadingSystem hook is in place,
                    // => execute js on the reader.html context to push the
                    // global window.navigator.epubReadingSystem into the
                    // iframe(s)

                    evaluateJavascript(INJECT_EPUB_RSO_SCRIPT_2);
                    syncObj.doWait(1000);

                    return new WebResourceResponse("text/javascript", UTF_8,
                            new ByteArrayInputStream(
                                    "(function(){})()".getBytes()));
                }

                if (cleanedUrl.matches("\\/?readium_MathJax.js")) {
                    if (!quiet)
                        Log.d(TAG, "MathJax.js inject ...");

                    InputStream is = null;
                    try {
                        is = getActivity().getAssets().open(PAYLOAD_MATHJAX_ASSET);
                    } catch (IOException e) {

                        Log.e(TAG, "MathJax.js asset fail!");

                        return new WebResourceResponse(null, UTF_8,
                                new ByteArrayInputStream("".getBytes()));
                    }

                    return new WebResourceResponse("text/javascript", UTF_8, is);
                }

                if (cleanedUrl.matches("\\/?readium_Annotations.css")) {
                    if (!quiet)
                        Log.d(TAG, "annotations.css inject ...");

                    InputStream is = null;
                    try {
                        is = getActivity().getAssets().open(PAYLOAD_ANNOTATIONS_CSS_ASSET);
                    } catch (IOException e) {

                        Log.e(TAG, "annotations.css asset fail!");

                        return new WebResourceResponse(null, UTF_8,
                                new ByteArrayInputStream("".getBytes()));
                    }

                    return new WebResourceResponse("text/css", UTF_8, is);
                }

                String mime = null;
                int dot = cleanedUrl.lastIndexOf('.');
                if (dot >= 0) {
                    mime = EpubServer.MIME_TYPES.get(cleanedUrl.substring(
                            dot + 1).toLowerCase());
                }
                if (mime == null) {
                    mime = "application/octet-stream";
                }

                ManifestItem item = mPackage.getManifestItem(cleanedUrl);
                String contentType = item != null ? item.getMediaType() : null;
                if (mime != "application/xhtml+xml"
                        && mime != "application/xml" // FORCE
                        && contentType != null && contentType.length() > 0) {
                    mime = contentType;
                }

                if (url.startsWith("file:")) {
                    if (item == null) {
                        Log.e(TAG, "NO MANIFEST ITEM ... " + url);
                        return super.shouldInterceptRequest(view, url);
                    }

                    String cleanedUrlWithQueryFragment = cleanResourceUrl(url,
                            true);
                    String httpUrl = "http://" + EpubServer.HTTP_HOST + ":"
                            + EpubServer.HTTP_PORT + "/"
                            + cleanedUrlWithQueryFragment;
                    Log.e(TAG, "FILE to HTTP REDIRECT: " + httpUrl);

                    try {
                        URLConnection c = new URL(httpUrl).openConnection();
                        ((HttpURLConnection) c).setUseCaches(false);
                        if (mime == "application/xhtml+xml"
                                || mime == "text/html") {
                            ((HttpURLConnection) c).setRequestProperty(
                                    "Accept-Ranges", "none");
                        }
                        InputStream is = c.getInputStream();
                        return new WebResourceResponse(mime, null, is);
                    } catch (Exception ex) {
                        Log.e(TAG,
                                "FAIL: " + httpUrl + " -- " + ex.getMessage(),
                                ex);
                    }
                }
                if (!quiet)
                    Log.d(TAG, "RESOURCE FETCH ... " + url);
                return super.shouldInterceptRequest(view, url);
            }

            Log.e(TAG, "NULL URL RESPONSE: " + url);
            return new WebResourceResponse(null, UTF_8,
                    new ByteArrayInputStream("".getBytes()));
        }
    }

    private String cleanResourceUrl(String url, boolean preserveQueryFragment) {

        String cleanUrl = null;

        String httpUrl = "http://" + EpubServer.HTTP_HOST + ":"
                + EpubServer.HTTP_PORT;
        if (url.startsWith(httpUrl)) {
            cleanUrl = url.replaceFirst(httpUrl, "");
        } else {
            cleanUrl = (url.startsWith(ASSET_PREFIX)) ? url.replaceFirst(
                    ASSET_PREFIX, "") : url.replaceFirst("file://", "");
        }

        String basePath = mPackage.getBasePath();
        if (basePath.charAt(0) != '/') {
            basePath = '/' + basePath;
        }
        if (cleanUrl.charAt(0) != '/') {
            cleanUrl = '/' + cleanUrl;
        }
        cleanUrl = (cleanUrl.startsWith(basePath)) ? cleanUrl.replaceFirst(
                basePath, "") : cleanUrl;

        if (cleanUrl.charAt(0) == '/') {
            cleanUrl = cleanUrl.substring(1);
        }

        if (!preserveQueryFragment) {
            int indexOfQ = cleanUrl.indexOf('?');
            if (indexOfQ >= 0) {
                cleanUrl = cleanUrl.substring(0, indexOfQ);
            }

            int indexOfSharp = cleanUrl.indexOf('#');
            if (indexOfSharp >= 0) {
                cleanUrl = cleanUrl.substring(0, indexOfSharp);
            }
        }

        return cleanUrl;
    }

    public class EpubWebChromeClient extends WebChromeClient implements
            MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (!quiet)
                Log.d(TAG, "here in on ShowCustomView: " + view);
            super.onShowCustomView(view, callback);
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                if (!quiet)
                    Log.d(TAG,
                            "frame.getFocusedChild(): "
                                    + frame.getFocusedChild());
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
            if (!quiet)
                Log.d(TAG, "Video completed");

            // a.setContentView(R.layout.main);
            // WebView wb = (WebView) a.findViewById(R.id.webview);
            // a.initWebView();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            Log.e(TAG, "MediaPlayer onError: " + what + ", " + extra);
            return false;
        }
    }

    public class EpubInterface {

        @JavascriptInterface
        public void onPaginationChanged(String currentPagesInfo) {
            if (!quiet)
                Log.d(TAG, "onPaginationChanged: " + currentPagesInfo);
            try {
                PaginationInfo paginationInfo = PaginationInfo
                        .fromJson(currentPagesInfo);
                List<Page> openPages = paginationInfo.getOpenPages();
                if (!openPages.isEmpty()) {
                    final Page page = openPages.get(0);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mPageInfo.setText(getString(R.string.page_x_of_y,
                                    page.getSpineItemPageIndex() + 1,
                                    page.getSpineItemPageCount()));
                            SpineItem spineItem = mPackage.getSpineItem(page
                                    .getIdref());
                            boolean isFixedLayout = spineItem
                                    .isFixedLayout(mPackage);
                            mWebview.getSettings().setBuiltInZoomControls(
                                    isFixedLayout);
                            mWebview.getSettings()
                                    .setDisplayZoomControls(false);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }

        @JavascriptInterface
        public void onSettingsApplied() {
            if (!quiet)
                Log.d(TAG, "onSettingsApplied");
        }

        @JavascriptInterface
        public void onReaderInitialized() {
            if (!quiet)
                Log.d(TAG, "onReaderInitialized");
        }

        @JavascriptInterface
        public void onContentLoaded() {
            if (!quiet)
                Log.d(TAG, "onContentLoaded");
        }

        @JavascriptInterface
        public void onPageLoaded() {
            if (!quiet)
                Log.d(TAG, "onPageLoaded");
        }

        @JavascriptInterface
        public void onIsMediaOverlayAvailable(String available) {
            if (!quiet)
                Log.d(TAG, "onIsMediaOverlayAvailable:" + available);



        }

        @JavascriptInterface
        public void onMediaOverlayStatusChanged(String status) {
            if (!quiet)
                Log.d(TAG, "onMediaOverlayStatusChanged:" + status);
            // this should be real json parsing if there will be more data that
            // needs to be extracted


        }

        //
        // @JavascriptInterface
        // public void onMediaOverlayTTSSpeak() {
        // Log.d(TAG, "onMediaOverlayTTSSpeak");
        // }
        //
        // @JavascriptInterface
        // public void onMediaOverlayTTSStop() {
        // Log.d(TAG, "onMediaOverlayTTSStop");
        // }

        @JavascriptInterface
        public void getBookmarkData(final String bookmarkData) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    EpubWebViewFragment.this.getActivity()).setTitle(R.string.add_bookmark);

            final EditText editText = new EditText(EpubWebViewFragment.this.getActivity());
            editText.setId(android.R.id.edit);
            editText.setHint(R.string.title);
            builder.setView(editText);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                String title = editText.getText().toString();
                                try {
                                    JSONObject bookmarkJson = new JSONObject(
                                            bookmarkData);
                                    BookmarkDatabase.getInstance().addBookmark(
                                            mContainer.getName(),
                                            title,
                                            bookmarkJson.getString("idref"),
                                            bookmarkJson
                                                    .getString("contentCFI"));
                                } catch (JSONException e) {
                                    Log.e(TAG, "" + e.getMessage(), e);
                                }
                            }
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }
}