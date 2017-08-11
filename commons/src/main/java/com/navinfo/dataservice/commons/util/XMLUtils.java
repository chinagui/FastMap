package com.navinfo.dataservice.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.log4j.Logger;
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
public class XMLUtils{
	protected static Logger log = Logger.getLogger(XMLUtils.class);
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

    public static Element parseXmlFile(InputStream is)throws DocumentException{
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        return document.getRootElement();
    }

    /**
     * classpath相对路径
     * @param file
     * @return
     * @throws DocumentException
     */
    public static Element parseXmlFile(String file)throws DocumentException{
    	InputStream is = null;
    	try{
    		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            if (is == null) {
                is = XMLUtils.class.getResourceAsStream(file);
            }
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            return root;
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		throw e;
    	}finally{
    		try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
    	}
    }
}
