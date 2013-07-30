package com.anfengde.epub.test;

import java.util.List;

import junit.framework.Assert;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.anfengde.epub.DownloadFile;
import com.anfengde.epub.XmlReader;

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
