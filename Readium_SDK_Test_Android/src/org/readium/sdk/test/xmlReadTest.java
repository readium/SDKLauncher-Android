package org.readium.sdk.test;

import java.util.List;

import org.readium.sdk.test.util.DownloadFile;
import org.readium.sdk.test.util.XmlReader;

import junit.framework.Assert;
import android.os.Environment;
import android.test.AndroidTestCase;


public class xmlReadTest extends AndroidTestCase {
	private final static String testPath = Environment
			.getExternalStorageDirectory().getPath() + "/readiumtest/";

	public void testReadXml() throws Exception {
		XmlReader read = new XmlReader();
		List<String> list = read
				.getValue(testPath + "bookName.xml", "epubBook/books/bookname");
		
		Assert.assertEquals(14, list.size());
		Assert.assertEquals("Creative Commons - A Shared Culture.epub",
				list.get(0));
		
		DownloadFile.downLoad(list.get(0));
	}
}
