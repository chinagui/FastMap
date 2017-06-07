package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Set;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class RegionIndexExporter {

	/**
	 * @Title: run
	 * @Description: 记录当前省份的id和外接矩形坐标
	 * @param sqliteConn
	 * @param stmt
	 * @param meshes
	 * @param regionIdStr
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月6日 下午6:13:41 
	 */
	public static void run(Connection sqliteConn, Statement stmt,
			 Set<Integer> meshes,String regionIdStr)
			throws Exception {

		// creating a LINESTRING table
		stmt.execute("create table region_index(id integer primary key)");
		stmt.execute("alter table region_index add minlon integer;");
		stmt.execute("alter table region_index add maxlon integer;");
		stmt.execute("alter table region_index add minlat integer;");
		stmt.execute("alter table region_index add maxlat integer;");
		

		String insertSql = "insert into region_index values("
				+ "?, ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		double[] xyArr = MeshUtils.meshs2Rect(meshes);
		
		if(regionIdStr.contains("/")){
			String[] strs = regionIdStr.split("/");
			regionIdStr =  strs[strs.length -1];
		}else{
			if(regionIdStr.length() > 2){
				regionIdStr = regionIdStr.substring(regionIdStr.length()-2);
			}
		}
		
		
		int regionId = 0;
		if(isStr2Num(regionIdStr)){
			regionId = Integer.parseInt(regionIdStr);
		}
			prep.setInt(1, regionId);

			prep.setDouble(2, xyArr[0]);

			prep.setDouble(3, xyArr[1]);

			prep.setDouble(4, xyArr[2]);

			prep.setDouble(5, xyArr[3]);

			prep.executeUpdate();
		

		sqliteConn.commit();
	}

	 public static boolean isStr2Num(String str) {   
		 try {  
			    Integer.parseInt(str);  
			    return true;  
			} catch (NumberFormatException e) {  
			    return false;  
			} 
	 } 

	 public static void main(String[] args) {
		String regionIdStr = "f:\\gdb\\53";
		
		/*String[] strs = dir.split("/");
		String str = strs[strs.length -1];*/
		
		if(regionIdStr.contains("/")){
			String[] strs = regionIdStr.split("/");
			regionIdStr =  strs[strs.length -1];
		}else{
			if(regionIdStr.length() > 2){
				regionIdStr = regionIdStr.substring(regionIdStr.length()-2);
			}
		}
		
		System.out.println(regionIdStr);
		
	}
}
