package com.navinfo.dataservice.dao.plus.innermodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

public class CiParaAdAdmin {
	private Connection conn;
	private String CI_PARA_AD_ADMINTable = "CI_PARA_AD_ADMIN";
	
	public CiParaAdAdmin(Connection conn){
		this.conn = conn;
	}
	
	public List<ArrayList<String>> queryAdAdmin(long adminCode, String streetPre) throws Exception{
		List<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		
		String sql = "select PROVNM,CITYNM,XIANNM,WHOLENM from " + CI_PARA_AD_ADMINTable +  "  where ADMIN_ID=:1 and  (instr(:2,provnm)>0  or instr(:3,citynm)>0  or instr(:4,xiannm)>0 ) order by wholenm_len desc ";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		String leve1AdminCode = String.valueOf(adminCode).substring(0, 2) + "0000";
		adminCode = Long.parseLong(leve1AdminCode);

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setLong(1, adminCode);
			pstmt.setString(2, streetPre);
			pstmt.setString(3, streetPre);
			pstmt.setString(4, streetPre);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ArrayList<String> resList = new ArrayList<String>();
				resList.add(resultSet.getString("PROVNM"));
				resList.add(resultSet.getString("CITYNM"));
				resList.add(resultSet.getString("XIANNM"));
				resList.add(resultSet.getString("WHOLENM"));
				
				res.add(resList);
			}

			return res;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
}
