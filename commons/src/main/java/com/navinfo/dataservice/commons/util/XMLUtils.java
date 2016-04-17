package com.navinfo.dataservice.commons.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-10
 */
public class XMLUtils
{
    public static String element2str(Element root)
    {
        String xmlStr = null;
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        document.setRootElement(root);
        xmlStr = document.asXML();
        return xmlStr;
    }

    public static Element parseRoot(String xmlStr) throws DocumentException
    {
        Element e = null;
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(xmlStr);
        Document document = reader.read(sr);
        e = document.getRootElement();
        return e;
    }
}
