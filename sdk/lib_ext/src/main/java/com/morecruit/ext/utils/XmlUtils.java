package com.morecruit.ext.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author markzhai on 16/2/29
 * @version 1.0.0
 */
public final class XmlUtils {
    private static final String LOG_TAG = "XmlUtils";
    private static final String ENCODE = "utf-8";
    private static final String XML_TAG_ITEM_TAG = "string";

    /**
     * 解析xml
     *
     * @param xmlFilePath 文件路径
     * @return map
     * @throws XmlPullParserException XmlPullParserException
     * @throws IOException            IOException
     */
    public static Map<String, String> parse(String xmlFilePath) throws XmlPullParserException, IOException {
        return parse(new FileInputStream(xmlFilePath));
    }

    /**
     * 解析xml
     *
     * @param inputStream 输入流
     * @return map
     * @throws XmlPullParserException XmlPullParserException
     * @throws IOException            IOException
     */
    public static Map<String, String> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        HashMap<String, String> map = new HashMap<String, String>();
        XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
        xmlParser.setInput(inputStream, ENCODE);

        int eventType = xmlParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    String name = xmlParser.getName();
                    if (name.equals(XML_TAG_ITEM_TAG)) {
                        String attributeValue = xmlParser.getAttributeValue(0);
                        String text = xmlParser.nextText();

                        if (attributeValue != null) {
                            map.put(attributeValue, text);
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    break;

                default:
                    break;
            }

            eventType = xmlParser.next();
        }

        return map;
    }
}
