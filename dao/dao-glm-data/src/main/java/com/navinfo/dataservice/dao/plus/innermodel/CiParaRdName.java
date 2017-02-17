package com.navinfo.dataservice.dao.plus.innermodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

public class CiParaRdName {
	private Connection conn;
	private String CI_PARA_RD_NAMETable = "CI_PARA_RD_NAME";
	
	public CiParaRdName(Connection conn){
		this.conn = conn;
	}
	
	public List<String> queryRdNameInAddress(long adminCode, String addressFull) throws Exception{
		List<String> res = new ArrayList<String>();

		String sql = "select name from " + CI_PARA_RD_NAMETable +  " where admin_id=:1 and INSTR(:2, name)>0 order by name_len desc  ";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		String leve1AdminCode = String.valueOf(adminCode).substring(0, 2) + "0000";
		adminCode = Long.parseLong(leve1AdminCode);

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setLong(1, adminCode);
			pstmt.setString(2, addressFull);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				String name = resultSet.getString("name");
				res.add(name);
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
