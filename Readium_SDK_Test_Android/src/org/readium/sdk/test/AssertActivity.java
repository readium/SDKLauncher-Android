/**
 * The AssertActivity used to inject js in webview for test
 */
package org.readium.sdk.test;

import org.readium.sdk.test.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author chtian@anfengde.com
 * 
 */
public class AssertActivity extends Activity {
    private boolean done = false;
    private boolean result = false;
    private String expression = "";
    private WebView web;
    //private final String ASSERT_HTML = "file:///mnt/sdcard/readium_test/assert.html";
    private final String ASSERT_HTML = "https://raw.github.com/readium/Launcher-Android/afd/Readium_SDK_Test_Android/assert.html";
    private class JavascriptAccessor {
        JavascriptAccessor() {
        }

        @JavascriptInterface
        public void getResult(String res, String expr) {
            result = "true".equals(res) ? true : false; 
            expression = expr;
            done = true;
        }
    }

    public AssertActivity() {
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assert_engine);
        web = (WebView) findViewById(R.id.assert_view);
        web.addJavascriptInterface(new JavascriptAccessor(),
                "javascriptAccessor");
        web.setWebViewClient(new WebViewClient());

        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(ASSERT_HTML);
    }

    public WebView getWeb() {
        return web;
    }

    public boolean getDone() {
        return done;
    }

    public boolean getResult() {
        return result;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public final String getExpression() {
        return expression;
    }

    public final void setExpression(String expression) {
        this.expression = expression;
    }

    public void assertTest(String json) {
        done = false;
        result = false;
        expression = "";
        String js = "javascript:data=" + json + ";";
        js += "$('#testName').text(data.test.testName);";
        js += "$('#testExpr').text(data.test.testExpr);";
        js += "var container = data.container;";
        js += "var package = container.package;";
        js += "var result = true;";
        js += "var expr = '';";
        js += "for (var i = 0; i < data.test.testExpr.length; i++) {";
        js += "    try{result = eval(data.test.testExpr[i]);}";
        js += "    catch(e){result=false;}";
        js += "    if (false === result){";
        js += "        expr = data.test.testExpr[i];break;";
        js += "    }}";
        js += "$('#testResult').text(result);";
        js += "javascriptAccessor.getResult(result, expr);";

        Util.saveEPubJson(json);
        
        web.loadUrl(js);
    }

}
