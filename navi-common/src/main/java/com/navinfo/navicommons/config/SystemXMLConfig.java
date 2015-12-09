package com.navinfo.navicommons.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.navicommons.exception.DMSException;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-4-24
 * Time: 下午2:07
 * To change this template use File | Settings | File Templates.
 */
public class SystemXMLConfig {
    private static Logger log = Logger.getLogger(SystemXMLConfig.class);

    private static final String defaultConfigFile = "/SystemConfig.xml";

    public static MavenConfigMap getSystemConfig() {
        return getSystemConfig(defaultConfigFile);
    }

    public static MavenConfigMap getSystemConfig(String configFile) {
        MavenConfigMap mavenConfigMap = allConfigMap.get(configFile);
        if (mavenConfigMap == null) {
            mavenConfigMap = parseConfig(configFile);
            allConfigMap.put(configFile, mavenConfigMap);
        }

        return mavenConfigMap;
    }

    private static Map<String, MavenConfigMap> allConfigMap = new HashMap<String, MavenConfigMap>();


    private static MavenConfigMap parseConfig(String configFile) {
        InputStream is = null;
        MavenConfigMap mavenConfigMap = new MavenConfigMap();
        log.debug("parse file " + configFile);
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                is = SystemXMLConfig.class.getResourceAsStream(configFile);
            }

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String key = element.getName();
                String value = element.getTextTrim();
                mavenConfigMap.put(key, value);
            }
        } catch (Exception e) {
        	log.error(e.getMessage());
            throw new DMSException("读取文件" + configFile + "错误", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mavenConfigMap;
    }

}
