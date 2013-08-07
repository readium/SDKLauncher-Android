/**
 * The AssertActivity used to inject js in webview for test
 */
package org.readium.sdk.test;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author chtian@anfengde.com
 * 
 */
public class AssertActivity extends Activity {

    WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assert_engine);
        web = (WebView)findViewById(R.id.assert_view);
        web.setWebViewClient(new WebViewClient());
        
        web.getSettings().setJavaScriptEnabled(true);
    }
    
    /**
     * 
     */
    public AssertActivity() {
        // TODO Auto-generated constructor stub
    }

}
