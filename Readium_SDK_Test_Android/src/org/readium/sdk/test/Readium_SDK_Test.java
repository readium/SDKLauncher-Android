package org.readium.sdk.test;

import java.util.List;

import org.readium.sdk.test.util.Util;
import org.readium.sdk.test.util.XmlReader;

import android.test.ActivityInstrumentationTestCase2;

public class Readium_SDK_Test extends
        ActivityInstrumentationTestCase2<AssertActivity> {

    /**
     * test case list
     */
    private List<ReadiumTestCase> tests;

    private static boolean firstDownload = false;

    private AssertActivity web;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        this.setActivityInitialTouchMode(true);
        web = getActivity();

        if (!firstDownload) {
            Util.download(Util.getConfig_url(), Util.getConfig_file());
            firstDownload = true;
            XmlReader read = new XmlReader(Util.getConfigFullName());
            tests = read.getTests();
        }

    }

    public Readium_SDK_Test() {
        super(AssertActivity.class);
    }

    public void testTestShow() {
        assertTrue(web != null);
    }
}
