package com.navinfo.navicommons.database.sql;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.vividsolutions.jts.geom.Geometry;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/** 
* @ClassName: RunnableSQL 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午6:24:28 
* @Description: TODO
*/
public class RunnableSQL {
	protected Logger log = Logger.getLogger(RunnableSQL.class);
    private String sql;
    private List<Object> args = new ArrayList<Object>();
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<Object> getArgs() {
		return args;
	}
	public void setArgs(List<Object> args) {
		this.args = args;
	}
	/**
	 * 非线程安全
	 * @param arg
	 */
	public void addArg(Object arg){
		args.add(arg);
	}
	
	/**
	 * 再优化
	 * @throws Exception
	 */
	public void run(Connection conn)throws Exception{
		PreparedStatement pst = null;
		try{
			pst = conn.prepareStatement(sql);
			for(int i=0;i<args.size();i++){
				Object arg = args.get(i);
				if(arg instanceof Geometry){
					Clob c = ConnectionUtil.createClob(conn);
					c.setString(1, GeoTranslator.jts2Wkt((Geometry)arg,1,5));
					pst.setClob(i+1, c);
				}else{
					pst.setObject(i+1, arg);
				}
			}
			pst.executeQuery();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(pst);
		}
	}
	public static void main(String[] args) {
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.103:1521/orcl", "fm260_region_16win_d_1", "fm260_region_16win_d_1").getConnection();
			RunnableSQL sql = new RunnableSQL();
			sql.setSql("INSERT INTO TEMP_XXW_01 VALUES (TO_DATE(?,'yyyymmdd hh24:mi:ss'),?,SDO_GEOMETRY(?,8307))");
			sql.addArg("20161111 00:00:00");
			sql.addArg(null);
			sql.addArg(JtsGeometryFactory.read("LINESTRING(129.789321823 34.18782666,129.34455656 34.898776)"));
			sql.run(conn);
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
