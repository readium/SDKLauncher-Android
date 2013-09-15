package org.readium.sdk.test;

import java.util.Iterator;
import java.util.List;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.test.util.Util;
import org.readium.sdk.test.util.XmlReader;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class Readium_SDK_Test extends
        ActivityInstrumentationTestCase2<AssertActivity> {

    private static final String TAG = "Test";
    /**
     * test case list
     */
    private List<ReadiumTestCase> tests;

    private static boolean firstDownload = false;

    private AssertActivity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        this.setActivityInitialTouchMode(true);
        activity = getActivity();

        if (!firstDownload) {
            Util.download(Util.getConfig_url(), Util.getConfig_file());
            firstDownload = true;
            XmlReader read = new XmlReader(Util.getConfigFullName());
            tests = read.getTests();
            Util.download(
                    "https://raw.github.com/readium/Launcher-Android/afd/Readium_SDK_Test_Android/assert.html",
                    "assert.html");
        }

    }

    public Readium_SDK_Test() {
        super(AssertActivity.class);
    }

    public void testTestCases() {

        boolean result = true;

        for (Iterator<ReadiumTestCase> i = tests.iterator(); i.hasNext();) {
            ReadiumTestCase test = i.next();
            activity.setDone(false);

            Util.download(test.getUrl(), test.getFile());

            // open book TODO:function mapping....
            Container container = EPub3.openBook(Util.getFullName(test
                    .getFile()));

            // get container json string
            String json = Util.getJson(test.getJson(), container);

            // assert by webview
            assertByActivity(json);

            // TODO:how to get error msg???

            // wait ui thread processing...
            waitUI();

            if (activity.getResult()) {
                Log.v(TAG,
                        "TestCase:" + test.getName() + " Result:"
                                + activity.getResult());
            } else {
                Log.v(TAG,
                        "TestCase:" + test.getName() + " Result:"
                                + activity.getResult() + "; Invalid expression:"
                                + activity.getExpression());
            }

            if (false == activity.getResult()) {
                result = false;
            }

            EPub3.closeBook(container);
        }

        assertEquals("Test failed, please open LogCat view for more detail.",
                true, result);
    }

    private void assertByActivity(final String json) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.assertTest(json);
            }
        });
    }

    private void waitUI() {
        while (!activity.getDone()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
