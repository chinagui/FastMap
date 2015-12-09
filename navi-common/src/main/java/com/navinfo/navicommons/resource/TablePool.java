package com.navinfo.navicommons.resource;

import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class TablePool extends ResourcePool
{
    private SchemaPool parent;
    private String tableName;

    public TablePool(){}

    public TablePool(ResourcePool resourcePool)
    {
        init(resourcePool);
        ResourcePool schemaPool = ResourceManager.getInstance().getResourcePool(this.getParentResourceId());
        parent = new SchemaPool(schemaPool);
        buid();
    }

    public void buid()
    {
        try
        {
            Element root = parseRoot(this.getMetaStr());
            Element e = root.element("tableName");
            if(e != null)
                tableName = e.getText();
        } catch (DocumentException e)
        {
            throw new ResourceLockException(e);
        }
    }


    public SchemaPool getParent() {
        return parent;
    }

    public void setParent(SchemaPool parent) {
        this.parent = parent;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
