package com.anfengde.epub.test;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.anfengde.epub.XmlReader;

public class xmlReadTest extends AndroidTestCase {
	public void testReadXml() throws Exception {
		XmlReader read = new XmlReader();
		Assert.assertEquals(14, read.getValue("bookName.xml", "bookname")
				.size());
		Assert.assertEquals("Creative Commons - A Shared Culture.epub", read
				.getValue("bookName.xml", "bookname").get(0));
	}
}
