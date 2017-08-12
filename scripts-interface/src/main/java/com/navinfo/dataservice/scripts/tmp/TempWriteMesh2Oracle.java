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
import com.navinfo.nirobot.common.utils.MeshUtils;

/** 
 * @ClassName: TempWriteGrid2Oracle
 * @author xiaoxiaowen4127
 * @date 2017年6月12日
 * @Description: TempWriteGrid2Oracle.java
 */
public class TempWriteMesh2Oracle {
	
	public static void exe(int i){

    	Connection conn = null;
    	PreparedStatement stmt = null;
    	QueryRunner run = new QueryRunner();
    	try{
    		conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.152:1521/orcl", "gdb250_16sum_ml_nd", "gdb250_16sum_ml_nd").getConnection();
    		String meshSql = "SELECT MESH_ID FROM SG_MESHLIST WHERE MOD(MESH_ID,10)="+i;
    		Set<Integer> meshes = run.query(conn, meshSql, new ResultSetHandler<Set<Integer>>(){

				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> mes = new HashSet<Integer>();
					while(rs.next()){
						mes.add(rs.getInt(1));
					}
					return mes;
				}
    			
    		});
    		//INSERT INTO TEMP_MESH_GEOM TABLE
    		
    		String sql = "UPDATE SG_MESHLIST SET GEOMETRY = SDO_GEOMETRY(?,8307) WHERE MESH_ID=?";
    		stmt = conn.prepareStatement(sql);
    		int count = 0;
    		for(Integer mesh:meshes){
    			stmt.setString(1, MeshUtils.mesh2WKT(String.valueOf(mesh)));
    			stmt.setInt(2, mesh);
				stmt.addBatch();
				count++;
				if(count%1000==0){
					stmt.executeBatch();
					stmt.clearBatch();
					System.out.println("thread:"+Thread.currentThread().getName()+",inserted "+count);
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
				TempWriteMesh2Oracle.exe(0);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(1);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(2);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(3);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(4);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(5);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(6);
			}
		}.start();

		new Thread(){
			@Override
			public void run(){
				TempWriteMesh2Oracle.exe(7);
			}
		}.start();
    }
}
