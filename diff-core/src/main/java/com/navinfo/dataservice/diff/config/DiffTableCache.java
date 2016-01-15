package com.navinfo.dataservice.diff.config;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: DiffTableCache 
 * @author Xiao Xiaowen 
 * @date 2016-1-14 下午3:18:44 
 * @Description: TODO
 */
public class DiffTableCache {
	private static List<Table> allTables;
	public static List<Table> getAllTables(){
		if(allTables==null){
			synchronized(DiffTableCache.class){
				if(allTables==null){
					allTables = new ArrayList<Table>();
					Table table = new Table();
					table.setName("IX_POI");
					List<Column> cols = new ArrayList<Column>();
					Column col1=new Column();
					col1.setName("ROW_ID");
					col1.setType(Column.TYPE_VARCHAR2);
					col1.setPk(true);
					Column col2=new Column();
					col2.setName("PID");
					col2.setType(Column.TYPE_NUMBER);
					Column col3=new Column();
					col3.setName("KIND_CODE");
					col3.setType(Column.TYPE_VARCHAR2);
					cols.add(col1);
					cols.add(col2);
					cols.add(col3);
					allTables.add(table);
				}
			}
		}
        return allTables;
	}
}
