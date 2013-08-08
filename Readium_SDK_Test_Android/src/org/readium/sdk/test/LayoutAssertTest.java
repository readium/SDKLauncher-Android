package org.readium.sdk.test;

import java.util.List;

import org.readium.sdk.test.util.Util;
import org.readium.sdk.test.util.XmlReader;

import android.test.ActivityInstrumentationTestCase2;

public class LayoutAssertTest extends
        ActivityInstrumentationTestCase2<AssertActivity> {

    /**
     * test case list
     */
    private List<ReadiumTestCase> tests;
    private AssertActivity webview;

    private static boolean firstDownload = false;

    private AssertActivity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        this.setActivityInitialTouchMode(true);
        mActivity = getActivity();

        if (!firstDownload) {
            Util.download(Util.getConfig_url(), Util.getConfig_file());
            firstDownload = true;
            XmlReader read = new XmlReader(Util.getConfigFullName());
            tests = read.getTests();
        }
    }

    public LayoutAssertTest(Class<AssertActivity> activityClass) {
        super(activityClass);
    }

    public LayoutAssertTest() {
        super(AssertActivity.class);
    }

    public void testTestShow() {
        assertTrue(mActivity != null);
    }
}
