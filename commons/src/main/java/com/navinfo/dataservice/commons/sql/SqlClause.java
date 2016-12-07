package com.navinfo.dataservice.commons.sql;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author MaYunFei
 * 2016年6月25日
 * 描述：commonsSqlClause.java
 */
public class SqlClause {
	private String sql;
	private List<Object> values;
	public String getSql() {
		return sql;
	}
	public List<Object> getValues() {
		return values;
	}
	public SqlClause(String sql, List<Object> values) {
		super();
		this.sql = sql;
		this.values = values;
	}
	/**通过输入的int型的数据，和列名称，生成 oracle的in clause；不负责conn的关闭 
	 * @param conn 数据库连接
	 * @param inValues List<Integer> 
	 * @param column 列名称
	 * @return SqlCluase 对象，可以为空；
	 * @throws SQLException
	 */
	public static SqlClause genInClauseWithMulInt(Connection conn,List<Integer> inValues,String column) throws SQLException{
		String gridInClause=null;
		List<Object> prepareParaValues = new ArrayList<Object>();
		if(CollectionUtils.isNotEmpty(inValues)){
			if(inValues.size()>1000){
				Clob clobGrids = conn.createClob();
				clobGrids.setString(1, StringUtils.join(inValues, ","));
				gridInClause = column+"  IN (select column_value from table(clob_to_table(?)))";
				prepareParaValues.add(clobGrids);
			}else{
				gridInClause = column+"  IN ("+StringUtils.join(inValues, ",")+")";
			}
			return new SqlClause(gridInClause,prepareParaValues);
		}
		return null;
		
	}
	/**通过输入的List<String>型的数据，和列名称，生成 oracle的in clause；不负责conn的关闭 
	 * @param conn 数据库连接
	 * @param inValues List<Integer> 
	 * @param column 列名称
	 * @return SqlCluase 对象，可以为空；
	 * @throws SQLException
	 */
	public SqlClause genInClauseWithMulString(Connection conn,List<String> inValues,String column) throws SQLException{
		String ixTablesInClause = null;
		List<Object> prepareParaValues = new ArrayList<Object>();
		if(CollectionUtils.isNotEmpty(inValues)){
			if(inValues.size()>1000){
				Clob clobTables = conn.createClob();
				clobTables.setString(1, StringUtils.join(inValues, ","));
				ixTablesInClause = column+"  IN (select column_value from table(clob_to_table(?)))";
				prepareParaValues.add(clobTables);
			}else{
				ixTablesInClause = column+"  IN ('"+StringUtils.join(inValues, "','")+"')";
			}
			return new SqlClause(ixTablesInClause,prepareParaValues);
		}
		return null;
		
	}
	
	/**通过输入的String型的wkt数据，生成 oracle的in clause；不负责conn的关闭 
	 * @param conn 数据库连接
	 * @param inValues String 
	 * @return SqlCluase 对象，可以为空；
	 * @throws SQLException
	 */
	public static SqlClause genGeoClauseWithGeoString(Connection conn,String Geo) throws SQLException{
		String ixTablesInClause = null;
		List<Object> prepareParaValues = new ArrayList<Object>();
		if(StringUtils.isNotEmpty(Geo)){
			if(Geo.length()>1000){
				Clob clobTables = conn.createClob();
				clobTables.setString(1, Geo);
				ixTablesInClause = "sdo_geometry(?,8307)";
				prepareParaValues.add(clobTables);
			}else{
				Clob clobTables = conn.createClob();
				clobTables.setString(1, Geo);
				ixTablesInClause = "sdo_geometry(" + Geo + ",8307)";
				prepareParaValues.add(clobTables);
			}
			return new SqlClause(ixTablesInClause,prepareParaValues);
		}
		return null;
		
	}
	public int update(Connection conn) throws SQLException{
		if (this==null) return 0;
		QueryRunner run = new QueryRunner();
		if (this.getValues()==null || this.getValues().size()==0){
			return run.update(conn, this.getSql());
		}
		return run.update(conn, this.getSql(),this.getValues().toArray());
	}
	@Override
	public String toString() {
		return "SqlClause [sql=" + sql + ", values=" + values + "]";
	}
	
}

