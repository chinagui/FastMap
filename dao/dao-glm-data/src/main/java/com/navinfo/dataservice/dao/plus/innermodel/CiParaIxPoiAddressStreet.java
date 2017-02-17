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
	
	public List<String> queryStreet(long adminCode, String addressFull) throws Exception{
		List<String> res = new ArrayList<String>();
		
		String sql = "select street from " + addressStreetTableName +  " where admin_id=:1 and INSTR(:2, street)>0 and street not in (select pre_key from sc_point_addrck t where t.type=11) order by street_len desc  ";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
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
