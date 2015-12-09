package com.navinfo.navicommons.resource;

import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 11-3-29
 */
public class DbPool extends ResourcePool
{
    private String driveClassName;
    private String url;
    private String sys;
    private String password;

    public DbPool(){}

    public DbPool(ResourcePool resourcePool)
    {
        init(resourcePool);
        build();
    }

    private void build()
    {
        try
        {
            Element root = parseRoot(this.getMetaStr());
            driveClassName = fetchStringNodeText(root,"driveClassName");
            url = fetchStringNodeText(root,"url");
            sys = fetchStringNodeText(root,"sys");
            password = fetchStringNodeText(root,"password");
        } catch (DocumentException e)
        {
            throw new ResourceLockException(e);
        }
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

    public String getSys() {
        return sys;
    }

    public void setSys(String sys) {
        this.sys = sys;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
