package com.navinfo.navicommons.resource;

import java.io.StringReader;
import java.sql.Timestamp;

import org.apache.commons.beanutils.PropertyUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public class ResourcePool
{
    private String resourceId;
    private String resourceName;
    private int status;
    private String category;
    private Timestamp lockTime;
    private String descp;
    private String clientId;
    private String parentResourceId;
    private String resourceType;
    private String metaStr;
    private String verNo;
    private String moduleType;
    private String geoServerConfig;//transient myf 20110422


    public String getGeoServerConfig() {
		return geoServerConfig;
	}

	public void setGeoServerConfig(String geoServerConfig) {
		this.geoServerConfig = geoServerConfig;
	}

	public ResourcePool buildInnerPool()
    {
        return null;

    }

    public void init(ResourcePool resourcePool)
    {
        try
        {
            PropertyUtils.copyProperties(this,resourcePool);
        } catch (Exception e)
        {
            throw new IllegalArgumentException("属性复制时出错",e);
        }
    }


    protected String fetchStringNodeText(Element e,String childName)
    {
        Element node = e.element(childName);
        if(node != null)
            return node.getText();
        else
            return null;
    }
    protected int fetchIntNodeText(Element e,String childName)
    {
        String text = fetchStringNodeText(e,childName);
        if(text != null)
            return Integer.parseInt(text);
        else
            return -1;
    }

    protected  Element parseRoot(String xmlStr) throws DocumentException
    {
        Element e = null;
        SAXReader reader = new SAXReader();
        StringReader sr = new StringReader(xmlStr);
        Document document = reader.read(sr);
        e = document.getRootElement();
        return e;
    }



    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getLockTime() {
        return lockTime;
    }

    public void setLockTime(Timestamp lockTime) {
        this.lockTime = lockTime;
    }

    public String getDescp() {
        return descp;
    }

    public void setDescp(String descp) {
        this.descp = descp;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getParentResourceId() {
        return parentResourceId;
    }

    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getMetaStr() {
        return metaStr;
    }

    public void setMetaStr(String metaStr) {
        this.metaStr = metaStr;
    }

    public String getVerNo() {
        return verNo;
    }

    public void setVerNo(String verNo) {
        this.verNo = verNo;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    @Override
    public String toString() {
        return "ResourcePool{" +
                "resourceId='" + resourceId + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", status=" + status +
                ", category='" + category + '\'' +
                ", lockTime=" + lockTime +
                ", descp='" + descp + '\'' +
                ", clientId='" + clientId + '\'' +
                ", parentResourceId='" + parentResourceId + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", metaStr='" + metaStr + '\'' +
                '}';
    }
}
