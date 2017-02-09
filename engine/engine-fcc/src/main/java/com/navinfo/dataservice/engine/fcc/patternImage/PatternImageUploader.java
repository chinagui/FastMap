package com.navinfo.dataservice.engine.fcc.patternImage;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;

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

		try {
			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);

			int counter = 0;
			
			String seasonVersion=SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
			
			for (PatternImage image : images) {
				
				pstmt.setLong(1, getFileId(conn));//FILE_ID

				pstmt.setString(2, "NIDB-G");//PRODUCT_LINE  :NIDB-G

				pstmt.setString(3,seasonVersion);//VERSION  获取sys服务下sys_config表中的当前版本号

				pstmt.setString(4, "博士"); //PROJECT_NM :获取sys服务下sys_config表中的当前版本号

				pstmt.setNull(5, Types.VARCHAR); //SPECIFICATION

				pstmt.setString(6, image.getbType());//B_TYPE

				pstmt.setString(7, image.getmType());//M_TYPE

				pstmt.setNull(8, Types.VARCHAR); //S_TYPE

				pstmt.setString(9, image.getName());//FILE_NAME
				
				pstmt.setNull(10, Types.VARCHAR); //SIZE 空

				pstmt.setString(11, image.getFormat());//  FORMAT

				pstmt.setString(12, String.valueOf(image.getUserId())); //IMP_WORKER   原值导入

				//pstmt.setString(13, ""); //IMP_DATE 在语句中已经赋值
				
				pstmt.setString(13,"/multimedia/data/3D/pattern/"+image.getName()+"."+image.getFormat());//URL_DB

				pstmt.setString(14, "D:\\2.模式图\\3D\\pattern\\"+image.getName()+"."+image.getFormat());//URL_FILE
				
				pstmt.setString(15, "");//MEMO 空

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
	
	/**
	 * 
	 * @Description:获取fileId
	 * 原则：
	 * 1. 前4位：当前年份
	 * 2. 第5位：0
	 * 3. 后8位：顺序编号 ：max（数据中后8位）+1
	 * @param conn
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-2-8 上午9:40:02
	 */
	private long getFileId(Connection conn) throws Exception{
		PreparedStatement pstmt=null;
		
		ResultSet rs=null;
		
		try{
		
		//1. 前4位：当前年份
		Calendar c = Calendar.getInstance();
		
		c.setTime(new Date());
		
		String year=String.valueOf((c.get(Calendar.YEAR))) ;
		
		//max（数据中后8位）+1
		String sql="SELECT  max(SUBSTR(file_id,-7,7))+1  file_id FROM SC_MODEL_MATCH_G ";
		
		pstmt = conn.prepareStatement(sql);
		
		rs= pstmt.executeQuery();
		
		if(rs.next()){
			
			return Long.valueOf(year+"0"+String.valueOf(rs.getLong("file_id")));
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
