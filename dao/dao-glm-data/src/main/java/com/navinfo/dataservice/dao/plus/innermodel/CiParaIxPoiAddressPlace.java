package com.navinfo.dataservice.dao.plus.innermodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

public class CiParaIxPoiAddressPlace {
	private Connection conn;
	private String addressStreetPlaceTableName = "CI_PARA_IX_POI_ADDRESS_PLACE";
	
	public CiParaIxPoiAddressPlace(Connection conn){
		this.conn = conn;
	}
	
	public List<String> queryPlace(long adminCode, String addressFull) throws Exception{
		List<String> res = new ArrayList<String>();
		
		String sql = "select place from " + addressStreetPlaceTableName +  "  where src=:1 and admin_id=:2 and INSTR(:3, place)>0  and place not in (select pre_key from sc_point_addrck t where t.type=12) order by place_len desc  ";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, "ix_poi_address");
			pstmt.setLong(2, adminCode);
			pstmt.setString(3, addressFull);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				res.add(resultSet.getString("place"));
			}

			if (res.size() == 0){
				return queryPlaceFromIxPoiName(adminCode, addressFull);
			}
			return res;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public List<String> queryPlaceFromIxPoiName(long adminCode, String addressFull) throws Exception{
		List<String> res = new ArrayList<String>();
		
		String sql = "select place from " + addressStreetPlaceTableName +  " where src=:1 and  admin_id=:2 and INSTR(:3, place)>0  order by place_len desc  ";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, "ix_poi_name");
			pstmt.setLong(2, adminCode);
			pstmt.setString(3, addressFull);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				res.add(resultSet.getString("place"));
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
