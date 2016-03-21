package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public enum PoolType
{
    dbSchema("数据库资源"),table("表资源"),geoserver("webgis服务"),db("数据库实例");

    private String desc;

    private PoolType(String desc)
    {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
