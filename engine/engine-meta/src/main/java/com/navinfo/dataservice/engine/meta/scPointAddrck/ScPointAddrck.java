package com.navinfo.dataservice.engine.meta.scPointAddrck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointAddrck {
	
	public List<String> getAddrckList(int type,String hmFlag) throws Exception {
		String sql = "select pre_key from SC_POINT_ADDRCK t where t.type=:1 and t.hm_flag=:2";
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			List<String> addrck = new ArrayList<String>();
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, type);
			pstmt.setString(2, hmFlag);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				addrck.add(rs.getString("pre_key"));
			}
			return addrck;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.close(rs);
			DbUtils.close(pstmt);
			DbUtils.close(conn);
		}
		
	}

}
