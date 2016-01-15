package com.navinfo.dataservice.diff.config;

import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 上午10:30
 */
public class Table
{
    private String name;//表名
    private List<Column> cols;//字段

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<Column> getCols() {
        return cols;
    }

    public void setCols(List<Column> cols) {
        this.cols = cols;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Table table = (Table) o;
        return !(name != null ? !name.equals(table.name) : table.name != null);

    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
    
    public Table clone(){
    	Table t = new Table();
    	t.setName(name);
        t.setCols(cols);
        return t;
    }
}
