package com.navinfo.navicommons.resource;

import javax.sql.DataSource;

import com.navinfo.navicommons.database.DBConnectionFactory;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public class ResourceLock
{
    private ResourcePool resourcePool;
    private ResourcePool pysicalPool;

    public ResourceLock(ResourcePool resourcePool) {
        this.pysicalPool = resourcePool;
        this.resourcePool = PoolFactory.getInnerPool(resourcePool);
    }

    public ResourcePool getResourcePool() {
        return resourcePool;
    }

    public ResourcePool getPysicalPool() {
        return pysicalPool;
    }

    public DataSource getDataSource()
    {
        if(resourcePool instanceof SchemaPool)//todo 放到子类中去实现，或移到工具类中
            return DBConnectionFactory.getInstance().getDataSoure((SchemaPool)resourcePool);
        else if(resourcePool instanceof TablePool)
            return DBConnectionFactory.getInstance().getDataSoure(((TablePool)resourcePool).getParent());
        else if(resourcePool instanceof DbPool)
            return null;
        return null;
    }

    public GeoserverPool getBindGeoserverInfo()
    {
        if(resourcePool instanceof SchemaPool)
            return  ((SchemaPool)resourcePool).getGeoserverPool();
        else if(resourcePool instanceof TablePool)
            return  ((TablePool)resourcePool).getParent().getGeoserverPool();
        else if(resourcePool instanceof DbPool)
            return null;
        return null;
    }

    public String getResourceId()
    {
        return pysicalPool.getResourceId();
    }
    public String getResourceName()
    {
        return pysicalPool.getResourceName();
    }

}
