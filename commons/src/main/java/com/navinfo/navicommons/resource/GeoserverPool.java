package com.navinfo.navicommons.resource;

import org.dom4j.Element;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class GeoserverPool
{
    private String ip;
    private String port;
    private String user;
    private String password;
    private String serviceName;
    private String contextPath;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }


    public void build(Element e)
    {
        if(e == null)
            return;
        ip = getStringNodeText(e,"ip");
        port = getStringNodeText(e,"port");
        user = getStringNodeText(e,"user");
        password = getStringNodeText(e,"password");
        serviceName = getStringNodeText(e,"serviceName");
        contextPath = getStringNodeText(e,"contextPath");        
    }

    protected String getStringNodeText(Element e,String childName)
    {
        Element node = e.element(childName);
        if(node != null)
            return node.getText();
        else
            return null;
    }

}
