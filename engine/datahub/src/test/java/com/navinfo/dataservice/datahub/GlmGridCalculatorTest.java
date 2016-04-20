package com.navinfo.dataservice.datahub;

import java.sql.Connection;

import org.junit.Test;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.glm.GlmGridCalculator;
import com.navinfo.dataservice.datahub.glm.GlmGridCalculatorFactory;

/** 
* @ClassName: GlmGridCalculatorTest 
* @author Xiao Xiaowen 
* @date 2016年4月18日 下午7:01:35 
* @Description: TODO
*/
public class GlmGridCalculatorTest {
	
	@Test
	public void t1(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			GlmGridCalculator calc = GlmGridCalculatorFactory.getInstance().create("250+");
			
			long t1 = System.currentTimeMillis();
			String[] grids = calc.calc("RD_RESTRICTION_CONDITION", "29C264B75E41BA8EE050A8C08304FE8E", conn);
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
	public void t2(){
		String str = "RD_NODE:XXX:NULL";
		String[] arr = str.split(":");
		System.out.println(arr.length);
	}
}
