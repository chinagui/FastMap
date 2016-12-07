package com.navinfo.dataservice.bizcommons;

import java.sql.Connection;

import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

/** 
* @ClassName: GlmGridCalculatorTest 
* @author Xiao Xiaowen 
* @date 2016年4月18日 下午7:01:35 
* @Description: TODO
*/
public class GlmGridCalculatorTest {
	private static void t1(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			GlmGridCalculator calc = GlmGridCalculatorFactory.getInstance().create("250+");
			
			long t1 = System.currentTimeMillis();
			String[] grids = calc.calc("RD_NODE", "29C263F79929BA8EE050A8C08304FE8E", conn).getGrids();
			System.out.println("Time consuming:"+(System.currentTimeMillis()-t1)+"ms");
			for(String s:grids){
				System.out.println(s);
			}
		}catch(Exception e){
			try{
				if(conn!=null)conn.close();
			}catch(Exception err){
				err.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	public static void t2(){
		String str = "RD_NODE:XXX:NULL";
		String[] arr = str.split(":");
		System.out.println(arr.length);
	}
	public static void main(String[] args){
		t1();
		System.out.println("Over...");
	}
}
