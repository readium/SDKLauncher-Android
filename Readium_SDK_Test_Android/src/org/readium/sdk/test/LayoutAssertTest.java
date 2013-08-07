package org.readium.sdk.test;

import android.test.ActivityInstrumentationTestCase2;

public class LayoutAssertTest extends
        ActivityInstrumentationTestCase2<AssertActivity> {

    private AssertActivity mActivity;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        this.setActivityInitialTouchMode(true);
        mActivity = getActivity();
    }
    
    public LayoutAssertTest(Class<AssertActivity> activityClass) {
        super(activityClass);
    }
    
    public LayoutAssertTest() {
        super(AssertActivity.class);
    }
    
    public void testTestShow(){
        assertTrue(mActivity != null);
    }
}
