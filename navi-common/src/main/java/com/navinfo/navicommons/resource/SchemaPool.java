package com.navinfo.navicommons.resource;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class SchemaPool extends ResourcePool
{
    private String schema;
    private String driveClassName;
    private String url;
    private String userName;
    private String password;
    private int initialSize;
    private int minIdle;
    private int maxIdle;
    private int maxWait;
    private int maxActive;
    private int accessPhysicCon;
    private int testOnBorrow;
    private int testOnReturn;
    private String validationQuery;

    private GeoserverPool geoserverPool;

    private DbPool parent;


    public SchemaPool(){}

    public SchemaPool(ResourcePool resourcePool)
    {
        init(resourcePool);
        build();
        if(StringUtils.isNotEmpty(this.getParentResourceId()))
        {
            ResourcePool schemaPool = ResourceManager.getInstance().getResourcePool(this.getParentResourceId());
            parent = new DbPool(schemaPool);
        }
    }


    private void build()
    {
        try
        {
            Element root = parseRoot(this.getMetaStr());
            schema = fetchStringNodeText(root,"schema");
            driveClassName = fetchStringNodeText(root,"driveClassName");
            url = fetchStringNodeText(root,"url");
            userName = fetchStringNodeText(root,"userName");
            password = fetchStringNodeText(root,"password");
            initialSize = fetchIntNodeText(root,"initialSize");
            minIdle = fetchIntNodeText(root,"minIdle");
            maxIdle = fetchIntNodeText(root,"maxIdle");
            maxWait = fetchIntNodeText(root,"maxWait");
            maxActive = fetchIntNodeText(root,"maxActive");
            accessPhysicCon = fetchIntNodeText(root,"accessPhysicCon");
            testOnBorrow = fetchIntNodeText(root,"testOnBorrow");
            testOnReturn = fetchIntNodeText(root,"testOnReturn");
            validationQuery = fetchStringNodeText(root,"validationQuery");
            geoserverPool = new GeoserverPool();
            geoserverPool.build(root.element("geoserverPool"));
        } catch (DocumentException e)
        {
            throw new ResourceLockException(e);
        }
    }


    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDriveClassName() {
        return driveClassName;
    }

    public void setDriveClassName(String driveClassName) {
        this.driveClassName = driveClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getAccessPhysicCon() {
        return accessPhysicCon;
    }

    public void setAccessPhysicCon(int accessPhysicCon) {
        this.accessPhysicCon = accessPhysicCon;
    }

    public int getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(int testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public int getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(int testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public GeoserverPool getGeoserverPool() {
        return geoserverPool;
    }

    public void setGeoserverPool(GeoserverPool geoserverPool) {
        this.geoserverPool = geoserverPool;
    }

    public DbPool getParent() {
        return parent;
    }

    public void setParent(DbPool parent) {
        this.parent = parent;
    }
}
