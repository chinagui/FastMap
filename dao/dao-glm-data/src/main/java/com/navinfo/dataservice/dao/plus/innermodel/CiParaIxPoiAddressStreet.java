package com.navinfo.dataservice.dao.plus.innermodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

public class CiParaIxPoiAddressStreet {
	private Connection conn;
	private String addressStreetTableName = "CI_PARA_IX_POI_ADDRESS_STREET";
	
	public CiParaIxPoiAddressStreet(Connection conn){
		this.conn = conn;
	}
	
	public List<String> queryStreet(long adminCode, String addressFull, List<String> type11Key) throws Exception{
		List<String> res = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String sql = "select street from " + addressStreetTableName +  " where admin_id=:1 and INSTR(:2, street)>0 and street not in (";
		sb.append(sql);
		if (type11Key.size() == 0){
			sb.append("''");
		}
		String temp = "";
		for (String key : type11Key) {
			sb.append(temp);
			sb.append("'" + key + "'");
			temp = ",";
		}
		sb.append(") order by street_len desc ");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setLong(1, adminCode);
			pstmt.setString(2, addressFull);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				res.add(resultSet.getString("street"));
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
