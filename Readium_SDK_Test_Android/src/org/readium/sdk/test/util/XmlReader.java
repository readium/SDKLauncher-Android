package org.readium.sdk.test.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.readium.sdk.test.ReadiumTestCase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XmlReader {
    private static final String TAG = "Config";

    private List<ReadiumTestCase> tests = new ArrayList<ReadiumTestCase>();

    public final List<ReadiumTestCase> getTests() {
        return tests;
    }

    public XmlReader(String file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new File(file), new ConfigHandler());
        } catch (Exception e) {
            Log.i(TAG, "Parse config xml file failed.");
            e.printStackTrace();
        }
    }

    class ConfigHandler extends DefaultHandler {
        private ReadiumTestCase test;

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            Log.i(TAG, "start element:" + qName + "value:" + attributes.getValue("name"));

            if ("testcase".equals(qName)) {
                test = new ReadiumTestCase();
                test.setName(attributes.getValue("name"));
            }

            if ("function".equals(qName)) {
                test.addFunction(attributes.getValue("name"));
            }

            if ("file".equals(qName)) {
                test.setFile(attributes.getValue("name"));
                test.setUrl(attributes.getValue("url"));
            }

            if ("assert".equals(qName)) {
                test.addAssertExpression(attributes.getValue("expression"));
                test.addAssertMessag(attributes.getValue("msg"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {

            Log.i(TAG, "end element:" + qName);

            if ("testcase".equals(qName)) {
                tests.add(test);
            }
        }
    }
}
