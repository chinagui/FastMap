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
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.DoubleUtil;

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
			Set<String> results = CompGridUtil.intersectGeometryGrid(jg,"595674");
			for(String s:results){
				System.out.println(s);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void compare_001(){
		double[] xyArr = new double[20000000];
		for(int i=0;i<10000;i++){
			double lat = 0.1+i*0.00501;
			for(int j=0;j<1000;j++){
				xyArr[i*2000+(2*j)]=60.1+j*0.08001;
				xyArr[i*2000+(2*j+1)]=lat;
			}
		}

		List<String[]> results = new LinkedList<String[]>();
		long t1 = System.currentTimeMillis();
		for(int k=0;k<20000000;k+=2){
			results.add(CompGridUtil.point2GridsUsingNM(xyArr[k],xyArr[k+1]));
			if(k%1000000==0){
				System.out.println(k);
			}
		}
		System.out.println("time consumed:"+(System.currentTimeMillis()-t1)+"ms.");
		List<String[]> results2 = new LinkedList<String[]>();
		long t2 = System.currentTimeMillis();
		for(int k=0;k<20000000;k+=2){
			results2.add( CompGridUtil.point2Grids(xyArr[k],xyArr[k+1]));
			if(k%1000000==0){
				System.out.println(k);
			}
		}
		System.out.println("time consumed:"+(System.currentTimeMillis()-t2)+"ms.");
		Iterator<String[]> it = results.iterator();
		Iterator<String[]> it2 = results2.iterator();
		for(int k = 0;k<10000000;k++){
			String[] grids = it.next();
			int size = grids.length;
			String[] grids2 = it2.next();
			List<String> grids2Set = Arrays.asList(grids2);
			if(grids2.length!=size){
				System.out.println(xyArr[2*k]+" "+xyArr[2*k+1]+":"+StringUtils.join(grids,",")+"|"+StringUtils.join(grids2,","));
				continue;
			}
			for(String s:grids){
				if(!grids2Set.contains(s)){
					System.out.println(xyArr[2*k]+" "+xyArr[2*k+1]+":"+StringUtils.join(grids,",")+"|"+StringUtils.join(grids2,","));
					break;
				}
			}
		}
	}
	@Test
	public void compare_002(){
		double[] p = new double[]{116.00596,40.33333};
		List<String> r0 = MeshUtils.lonlat2MeshIds(p[0],p[1]);
		System.out.println(StringUtils.join(r0,","));
		String[] r1 = CompGridUtil.point2GridsUsingNM(p[0],p[1]);
		System.out.println(StringUtils.join(r1,","));
		String[] r2 = CompGridUtil.point2Grids(p[0],p[1]);
		System.out.println(StringUtils.join(r2,","));
		
	}
	@Test
	public void compare_003(){
		double[] p = new double[]{69.62119,34.33333};
		List<String> r0 = MeshUtils.lonlat2MeshIds(p[0],p[1]);
		System.out.println(StringUtils.join(r0,","));
		String[] r1 = CompGridUtil.point2GridsUsingNM(p[0],p[1]);
		System.out.println(StringUtils.join(r1,","));
		String[] r2 = CompGridUtil.point2Grids(p[0],p[1]);
		System.out.println(StringUtils.join(r2,","));
		
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
}
