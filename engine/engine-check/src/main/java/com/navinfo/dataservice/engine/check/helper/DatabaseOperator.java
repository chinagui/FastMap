package com.navinfo.dataservice.engine.check.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class DatabaseOperator {

	public DatabaseOperator() {
		// TODO Auto-generated constructor stub
	}
	
	public List<Object> exeSelect(Connection conn,String sql) throws Exception{
		PreparedStatement pstmt = conn.prepareStatement(sql);		
		ResultSet resultSet = pstmt.executeQuery();
		List<Object> resultList=new ArrayList<Object>();
		resultList=settleResultSet(resultSet);
		releaseSource(pstmt,resultSet);
		return resultList;
		}
	
	/**
	 * 通过查询SQL直接返回对应的NiValException（loc+targets+meshid）
	 * @param conn
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<NiValException> getNiValExceptionFromSql(Connection conn,String sql) throws Exception{
		PreparedStatement pstmt = conn.prepareStatement(sql);		
		ResultSet resultSet = pstmt.executeQuery();
		List<NiValException> resultList=new ArrayList<NiValException>();
		while (resultSet.next()){
			String pointWkt ="";
			try{
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);			
				Geometry pointGeo=GeoHelper.getPointFromGeo(geometry);
				pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
			}catch(Exception e){}
			
			String targets=resultSet.getString(2);
			int meshId=resultSet.getInt(3);
			
			NiValException checkResult=new NiValException();
			checkResult.setLoc(pointWkt);
			checkResult.setTargets(targets);
			checkResult.setMeshId(meshId);
			resultList.add(checkResult);
		} 
		return resultList;
	}
	
	public List<Object> settleResultSet(ResultSet resultSet) throws Exception{
		List<Object> resultList=new ArrayList<Object>();
		while (resultSet.next()){
			resultList.add(resultSet.getString(1));
		} 
		return resultList;
	}
	
	private void releaseSource(Statement stmt,ResultSet resultSet) throws SQLException{
		resultSet.close();
		stmt.close(); 
	}
	
	public static void main(String[] args) throws Exception{
		String sql="select a.link_pid from rd_link a";
		Connection conn=DBConnector.getInstance().getConnectionById(11);
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(conn, sql);
		System.out.println("end");
	}

}
