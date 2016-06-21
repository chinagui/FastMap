package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Check {

	

	// 路口组成link不允许打断
	public void checkIsCrossLink(Connection conn, int linkPid) throws Exception {

		String sql = "select null from rd_cross_link where link_pid = :1 and rownum =1";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}
		
		resultSet.close();

		pstmt.close();

		if (flag) {
			
			throwException("路口组成link不允许打断");
		}
	}
	
	

	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
