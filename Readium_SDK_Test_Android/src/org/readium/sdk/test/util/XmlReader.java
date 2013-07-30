package org.readium.sdk.test.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlReader {

	public List<String> getValue(String fileName, String selector)
			throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(fileName));

		List<String> valueList = new ArrayList<String>();
		List list = document.selectNodes(selector);
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			Element element = (Element) iter.next();
			valueList.add(element.getText());
		}
		return valueList;
	}
}