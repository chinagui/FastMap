package com.navinfo.navicommons.pid;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 11-2-10
 */
public class PidConfig
{
    private static final transient Logger log = Logger.getLogger(PidConfig.class);
    private static Map<String,Pid> pidMap = new HashMap<String, Pid>();

    public static String getPidCol(String tableName)
    {
        Pid pid = pidMap.get(tableName); 
        return pid == null ? null : pid.getPid();
    }

    public static boolean validate(String tableName)
    {
        return pidMap.containsKey(tableName);
    }

    public static void main(String[] args) {
         PidConfig.getPidCol("RD_LINK") ;
    }
    

    static class Pid
    {
        private String tableName;
        private String pid;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }
    }
    
    private static String getResourceAsString(String fileName)
    {
        String content = null;
        InputStream is = PidConfig.class.getClassLoader().getResourceAsStream(fileName);
        if(is == null)
        {
            log.error("未找到pid配置文件：" + fileName);
            throw new RuntimeException("未找到pid配置文件：" + fileName);
        }
        try
        {
            content = IOUtils.toString(is);
        } catch (IOException e)
        {
            log.error("读取pid配置文件时出错：" + fileName,e);
            throw new RuntimeException("读取pid配置文件时出错：" + fileName,e);
        } finally
        {
            IOUtils.closeQuietly(is);
        }
        return content;
    }

    private static Element parseRoot(String xmlStr) throws DocumentException
    {
        Element e = null;
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(xmlStr);
        Document document = reader.read(sr);
        e = document.getRootElement();
        return e;
    }

    
    static
    {
        String xmlStrig = getResourceAsString("pid_config.xml");
        try
        {
            Element root = parseRoot(xmlStrig);
            List<Element> tableElements = root.elements("table");
            for(Element tableElement : tableElements)
            {
                Pid pid = new Pid();
                String name = tableElement.attributeValue("name");
                pid.setTableName(name);
                String pid1 = tableElement.attributeValue("pid");
                pid.setPid(pid1);
                log.debug("insert into PID_MAX_PER_TABLE (TABLE_NAME,PID_NAME,PID_MAX_VALUE) values('"+name+"','"+pid1+"',0);");

                pidMap.put(pid.getTableName(),pid);
            }
        } catch (DocumentException e)
        {
            log.error("解析PID配置文件时出错",e);
            throw new RuntimeException("解析PID配置文件时出错",e);
        }
    }
}
