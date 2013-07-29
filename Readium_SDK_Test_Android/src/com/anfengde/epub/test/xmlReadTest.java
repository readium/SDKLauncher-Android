package com.anfengde.epub.test;

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
		Assert.assertEquals("Creative Commons - A Shared Culture.epub", read
				.getValue(testPath + "bookName.xml", "epubBook/books/bookname")
				.get(0));
	}

	public void testLoadFiles() throws Exception {
		XmlReader read = new XmlReader();
		//DownloadFile down = new DownloadFile();
		String bookname = read
				.getValue(testPath + "bookName.xml", "epubBook/books/bookname")
				.get(0);
		DownloadFile.downLoad(bookname);
	}
}
