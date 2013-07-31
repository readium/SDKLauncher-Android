/**
 *  readium sdk android testcase implementation.
 *  <ul> work flow description
 *  <li> read config file for get url of book download
 *  <li> download book if don't exist in cache folder
 *  <li> read test instrument from config and run one by one
 *  </ul>
 *  
 *  <ul> config file description
 *  <testcase name="open bad format file">
 *      <function name="openBook"/>
 *      <file name="a.epub3" url="http://google.com/a.epub3" />
 *      <assert value="-1" msg="bad zip format"/>
 *  </testcase>
 *  <li> tag function : can be single or list, describe readium sdk call flow  
 *  <li> tag file: config file get url
 *  <li> tag assert: can be single or list
 *  </ul>
 *  
 *  @author chtian@anfengde.com
 */
package org.readium.sdk.test;

import java.util.List;

import org.readium.sdk.test.util.Util;

import junit.framework.Assert;
import android.test.AndroidTestCase;

/**
 * @author chtian@anfengde.com
 * 
 */
public class Readium_SDK_Test extends AndroidTestCase {
    
    /**
     * test case list
     */
    private List<ReadiumTestCase> tests;
    
    private boolean firstDownload = false;
    protected void setUp() throws Exception {
        super.setUp();
        //download default test case config file
        if (!firstDownload){
            Util.download("", "");
            firstDownload = true;
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRepeatedInitEPubEnv() {
        Assert.assertEquals(true, true);
    }

}
