package com.navinfo.dataservice.commons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGridUtilTest {
	private static void t1(){
		try{
			//59567003
			double[] rect = CompGridUtil.grid2Rect("59567012");
			for(double o:rect){
				System.out.println(o);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t1_1(){
		try{
			double[] line = new double[]{116.0625,39.9379,116.0625,39.958};
			Set<String> res = CompGridUtil.intersectLineGrid(line, "595670");
			for(String o:res){
				System.out.println(o);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t1_2(){
		try{
			CompGridUtil.point2Grid(116.0625, 39.9379);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t2(){
		try{
			//59567003
			String s = CompGridUtil.point2Grid(116.09375
					,39.916667);
			System.out.println(s);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 验证double类型的精度
	 */
	public static void t3(){//		double x1=15.01;
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

	private static void t0(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			QueryRunner runn = new QueryRunner();
			String sql = "SELECT GEOMETRY FROM RD_link WHERE ROWNUM=1";
			JGeometry geo = runn.query(conn, sql, new ResultSetHandler<JGeometry>(){

				@Override
				public JGeometry handle(ResultSet rs) throws SQLException {
					rs.next();
					try{
						JGeometry geo = JGeometry.load(rs.getBytes("GEOMETRY"));
						return  geo;
					}catch(Exception e){
						throw new SQLException(e.getMessage(),e);
					}
				}
				
			});
			System.out.println(geo.getPoint());
			System.out.println(geo.getOrdinatesArray());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
//		t1();
//		t1_1();
		t3();
	}
}
