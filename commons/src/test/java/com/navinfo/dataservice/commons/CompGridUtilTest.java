package com.navinfo.dataservice.commons;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.JGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;

import junit.framework.Assert;
import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGridUtilTest{
	private Connection conn = null;
	private QueryRunner run = null;
	@Before
	public void prepare(){
		try{
			//run = new QueryRunner();
			//conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
			//		"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_prjgdb250_bj01", "fm_prjgdb250_bj01").getConnection();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@After
	public void after(){
		DbUtils.closeQuietly(conn);
	}
	
	/**
	 * 
	 */
	@Test
	public void grid2Rect_0001(){
		//59567003
		double[] rect = CompGridUtil.grid2Rect("59567023");
		for(double o:rect){
			System.out.println(o);
		}
		Assert.assertNotNull(rect);
	}
	/**
	 * 场景：
	 * 
	 */
	@Test
	public void intersectLineGrid_0001(){
		try{
			double[] line = new double[]{116.11024, 39.93096, 116.11037, 39.93097};
			Set<String> res = CompGridUtil.intersectLineGrid(line, "595670");
			for(String o:res){
				System.out.println(o);
			}
			Assert.assertNotNull(res);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void intersectRectGrid_001(){
		try{
			CompGridUtil.intersectRectGrid(new double[]{116.5018, 39.99997, 116.50183, 40.0}, "595674");
			System.out.println("Yes...");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void intersectGeometryGrid_001(){
		try{
			//59567003
			String sql = "SELECT P.ROW_ID,R1.GEOMETRY,R1.MESH_ID FROM RD_LINK_FORM P,RD_LINK R1 WHERE P.LINK_PID=R1.LINK_PID AND P.ROW_ID = HEXTORAW('326515AC8C0924B4E050A8C08304598C') ";
			JGeometry jg = run.query(conn, sql, new ResultSetHandler<JGeometry>(){

				@Override
				public JGeometry handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						try{
							return JGeometry.load(rs.getBytes("GEOMETRY"));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return null;
				}
				
			});
			Set<String> results = JGeometryUtil.intersectGeometryGrid(jg,"595674");
			for(String s:results){
				System.out.println(s);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 生成图幅线
	 */
	@Test
	public void other_001(){
		double sLon = 116.0;
		double eLon = 120.0;
		double sLat = 36.0;
		double eLat = 40.0;
		double initLon = sLon;
		for(int i=0;i<=32;i++){//lon
			initLon=DoubleUtil.keepSpecDecimal(sLon+i/8.0);
			System.out.println(String.format("%s%s%s%s%s%s%s%s%s", "LINESTRING (",String.valueOf(initLon)," ",String.valueOf(sLat),",",String.valueOf(initLon)," ",String.valueOf(eLat),")"));
		}
		double initLat = sLat;
		for(int j=0;j<=48;j++){//lat递增
			initLat=DoubleUtil.keepSpecDecimal(sLat+j/12.0);
			System.out.println(String.format("%s%s%s%s%s%s%s%s%s", "LINESTRING (",String.valueOf(sLon)," ",String.valueOf(initLat),",",String.valueOf(eLon)," ",String.valueOf(initLat),")"));
		}
	}
	
	@Test
	public void gridInMesh_01(){
		System.out.println(CompGridUtil.gridInMesh("35671", "3567100"));
	}
}
