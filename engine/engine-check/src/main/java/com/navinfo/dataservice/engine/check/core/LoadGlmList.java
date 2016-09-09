package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.SelectorFactory;



public class LoadGlmList {

	public LoadGlmList() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * SELECT TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID FROM LOG_DETAIL log WHERE OP_TP!=2
	 * @return 
	 * @throws Exception 
	 */
	public static List<IRow> loadByLogSql(Connection conn,String tableName,List<String> changeFields,String logSql) throws Exception{
		String dataSql="WITH T AS("+logSql+") "
				+ "SELECT *"
				+ "  FROM "+tableName
				+ " WHERE "+tableName+".U_RECORD != 2"
				+ "   AND EXISTS (SELECT 1"
				+ "          FROM T"
				+ "         WHERE T.TB_NM = '"+tableName+"'"
				+ "           AND T.TB_ROW_ID = "+tableName+".ROW_ID)";
		SelectorFactory factory=new SelectorFactory(conn);
		AbstractSelector selector=factory.createSelector(ObjType.valueOf(tableName.replace("_", "")));
		List<IRow> iRows=selector.loadBySql(dataSql, false, false);
		return iRows;
	}
	
	/**
	 * 
	 * @return 
	 * @throws Exception 
	 */
	public static List<IRow> loadByWkt(Connection conn,String tableName,String wkt) throws Exception{
		String dataSql="";
		SelectorFactory factory=new SelectorFactory(conn);
		AbstractSelector selector=factory.createSelector(ObjType.valueOf(tableName.replace("_", "")));
		List<IRow> iRows=selector.loadBySql(dataSql, false, false);
		return iRows;
	}

}
