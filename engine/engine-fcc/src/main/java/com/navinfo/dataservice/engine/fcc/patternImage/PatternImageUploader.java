package com.navinfo.dataservice.engine.fcc.patternImage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;


import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class PatternImageUploader {

	public void run(List<PatternImage> images) throws Exception {

		String sql = "insert into SC_MODEL_MATCH_G (" +
				"FILE_ID," +
				"PRODUCT_LINE," +
				"\"VERSION\"," +
				"PROJECT_NM," +
				"SPECIFICATION," +
				"B_TYPE," +
				"M_TYPE," +
				"S_TYPE,"+
				"FILE_NAME,"+
				"\"SIZE\","+
				"FORMAT,"+
				"IMP_WORKER,"+
				"IMP_DATE,"+
				"URL_DB,"+
				"URL_FILE,"+
				"MEMO,"+
				"FILE_CONTENT,"+
				"FILE_TYPE,"+
				"UPDATE_TIME"+
				") values (?,?,?,?,?,?,?,?,?,?,?,?,sysdate,?,?,?,?,?,sysdate)";
		

		PreparedStatement pstmt = null;

		Connection conn = null;

		//Scanner scanner = null;

		try {
			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			int counter = 0;
			
			for (PatternImage image : images) {
				
				//pstmt.setNull(1, Types.INTEGER);
				
				pstmt.setLong(1, getFileId(conn));//FILE_ID

				pstmt.setString(2, "NIDB-G");//PRODUCT_LINE  ???

				pstmt.setString(3, "16冬");//VERSION  ???

				pstmt.setString(4, "博士"); //PROJECT_NM ???

				pstmt.setNull(5, Types.VARCHAR); //SPECIFICATION

				pstmt.setString(6, image.getbType());//B_TYPE

				pstmt.setString(7, image.getmType());//M_TYPE

				pstmt.setNull(8, Types.VARCHAR); //S_TYPE

				pstmt.setString(9, image.getName());//FILE_NAME
				
				pstmt.setNull(10, Types.VARCHAR); //SIZE

				pstmt.setString(11, image.getFormat());//  FORMAT

				pstmt.setString(12, "test"); //IMP_WORKER  ??

			//	pstmt.setString(13, ""); //IMP_DATE

				pstmt.setString(13, " ");//URL_DB

				pstmt.setString(14, " ");//URL_FILE
				
				pstmt.setString(15, "");//MEMO

				ByteArrayInputStream stream = new ByteArrayInputStream(image.getContent()); //FILE_CONTENT

				pstmt.setBlob(16, stream);

				pstmt.setInt(17, 0); //FILE_TYPE

				pstmt.executeUpdate();

				counter++;

				if (counter % 100 == 0) {
					conn.commit();
				}
			}
				


			conn.commit();

		} catch (Exception e) {

			throw e;

		} finally {

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}

		/*	if (scanner != null) {
				scanner.close();
			}*/
		}
	}
	
	
	private long getFileId(Connection conn) throws Exception{
		PreparedStatement pstmt=null;
		
		ResultSet rs=null;
		
		try{
		
		
		String sql="SELECT MAX(FILE_ID) + 1  file_id FROM SC_MODEL_MATCH_G ";
		
		pstmt = conn.prepareStatement(sql);
		
		rs= pstmt.executeQuery();
		
		if(rs.next()){
			
			return rs.getLong("file_id");
		}
		
		}catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
		
		
	}
}
