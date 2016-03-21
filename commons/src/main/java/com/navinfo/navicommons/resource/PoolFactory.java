package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class PoolFactory
{
    public static ResourcePool getInnerPool(ResourcePool resourcePool)
    {
        if(resourcePool == null)
            return null;
        String resourceType = resourcePool.getResourceType();
        if(PoolType.dbSchema.name().equals(resourceType))
            return new SchemaPool(resourcePool);
        else if(PoolType.table.name().equals(resourceType))
            return new TablePool(resourcePool);
        else if(PoolType.db.name().equals(resourceType))
            return new DbPool(resourcePool);
        return resourcePool;
    }
}
