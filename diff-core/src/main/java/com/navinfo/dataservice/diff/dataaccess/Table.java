package com.navinfo.dataservice.diff.dataaccess;

import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 上午10:30
 */
public class Table
{
    private String tableName;//表名
    private List<Column> diffCols;//差分字段，需要排除主键

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public List<Column> getDiffCols() {
        return diffCols;
    }

    public void setDiffCols(List<Column> diffCols) {
        this.diffCols = diffCols;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Table table = (Table) o;
        return !(tableName != null ? !tableName.equals(table.tableName) : table.tableName != null);

    }

    @Override
    public int hashCode()
    {
        return tableName != null ? tableName.hashCode() : 0;
    }
    
    public Table clone(){
    	Table t = new Table();
    	t.setTableName(tableName);
        t.setDiffCols(diffCols);
        return t;
    }
}
