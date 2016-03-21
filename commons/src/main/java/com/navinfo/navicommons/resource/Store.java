package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class Store
{
    private String storeName;
    private SchemaPool schemaPool;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public SchemaPool getSchemaPool() {
        return schemaPool;
    }

    public void setSchemaPool(SchemaPool schemaPool) {
        this.schemaPool = schemaPool;
    }
}
