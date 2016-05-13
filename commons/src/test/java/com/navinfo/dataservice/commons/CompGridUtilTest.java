package com.navinfo.dataservice.commons;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
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
			run = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_prjgdb250_bj01", "fm_prjgdb250_bj01").getConnection();
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
	/**
	 * [116.53116 39.91667],在595664和595674的图廓线上
	 * 这条图廓线是四舍五入
	 */
	@Test
	public void point2Grid_001(){
		try{
			String grid = CompGridUtil.point2Grid(116.0625, 39.9379);
			System.out.println(grid);
			Assert.assertEquals("59567032", grid);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void point2Grid_002(){
		try{
			//59567003
			String s = CompGridUtil.point2Grid(116.09375
					,39.916667);
			System.out.println(s);
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
	 * 验证double类型的精度
	 */
	@Test
	public void other_001(){
//		double x1=15.01;
//		double x2 = 16.01;
//		System.out.println(x1%1);
//		System.out.println(x2%1);
//		double y1 = x1%1;
//		double y2 = x2%1;
//		if(y1==y2){
//			System.out.println("YES!!!");
//		}else{
//			System.out.println("NO!!!");
//		}
//		System.out.println((int)(y1*100));
//		System.out.println((int)(y2*100));

//		System.out.println(29*1.5);
//		System.out.println(1.7*1.5);
//		System.out.println(4.015*1000);
//		System.out.println((int)(4.015*1000));
//		System.out.println(4.015-4);
//		System.out.println(Math.floor(4014.9999999999995));
//		System.out.println(4.115*1000);
//		for(double i = 0.0;i<60;i++){
//			System.out.println(i*1.5);
//		}
		System.out.println(1571064264264199999L/785532132132100000L);
		
//		System.out.println(15.01%1);
//		System.out.println(16.01%1);
//		System.out.println(1.01%1);
//		System.out.println(1.0%1);
//		System.out.println(2.0%1);
//		System.out.println(Double.valueOf(1%1));
//		System.out.println(5.0000000%1);
//		long t1 = System.currentTimeMillis();
//		double t;
//		for(int i=0;i<1000000000;i++){
//			t = 15.01-(int)15.01;
//			if(i%100000000==0){
//				System.out.println(t);
//			}
//		}
	}
}
