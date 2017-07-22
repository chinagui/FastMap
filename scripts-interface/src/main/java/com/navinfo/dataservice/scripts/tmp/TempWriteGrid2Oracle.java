package com.navinfo.dataservice.scripts.tmp;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;

/** 
 * @ClassName: TempWriteGrid2Oracle
 * @author xiaoxiaowen4127
 * @date 2017年6月12日
 * @Description: TempWriteGrid2Oracle.java
 */
public class TempWriteGrid2Oracle {
	
	public static void exe(int i){

    	Connection conn = null;
    	PreparedStatement stmt = null;
    	QueryRunner run = new QueryRunner();
    	try{
    		conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1").getConnection();
    		String meshSql = "SELECT MESH FROM CP_MESHLIST@DBLINK_RMS WHERE to_number(mesh)<"+i*100000+" AND TO_NUMBER(MESH)>="+(i-1)*100000+" AND SCALE='2.5'";
    		Set<String> meshes = run.query(conn, meshSql, new ResultSetHandler<Set<String>>(){

				@Override
				public Set<String> handle(ResultSet rs) throws SQLException {
					Set<String> mes = new HashSet<String>();
					while(rs.next()){
						mes.add(rs.getString("MESH"));
					}
					return mes;
				}
    			
    		});
    		//INSERT INTO GRID_MESH TABLE
    		
    		String sql = "INSERT INTO TEMP_GRID_MESH VALUES(?,?,SDO_GEOMETRY(?,8307))";
    		stmt = conn.prepareStatement(sql);
    		int count = 0;
    		for(String mesh:meshes){
    			Set<String> grids = CompGridUtil.mesh2Grid(mesh);
    			for(String g:grids){
    				stmt.setString(1, g);
    				stmt.setString(2, mesh);
    				stmt.setString(3, GridUtils.grid2Wkt(g));
    				stmt.addBatch();
    				count++;
    				if(count%1000==0){
    					stmt.executeBatch();
						stmt.clearBatch();
    					System.out.println("thread:"+Thread.currentThread().getName()+",inserted "+count);
    				}
    			}
    		}
    		if(count%1000!=0){
        		stmt.executeBatch();
    			stmt.clearBatch();
				System.out.println("inserted "+count);
    		}
    		long t1 = System.currentTimeMillis();
    		System.out.println((System.currentTimeMillis()-t1)/1000);
    	}catch(Exception e){
    		e.printStackTrace();
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
    public static void main( String[] args )
    {
		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(1);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(2);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(3);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(4);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(5);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(6);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(7);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(8);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteGrid2Oracle.exe(9);
			}
		}.start();
    }
}
