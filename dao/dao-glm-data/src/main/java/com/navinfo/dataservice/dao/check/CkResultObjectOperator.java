package com.navinfo.dataservice.dao.check;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CkResultObjectOperator {

	private Connection conn;

	public CkResultObjectOperator(Connection conn) {
		this.conn = conn;
	}

	public void insertCkResultObject(String checkId, String targets)
			throws Exception {

		String sql = "insert into ck_result_object (md5_code,table_name,pid) values (?,?,?)";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {

			String[] splits = targets.split(";");

			for (String split : splits) {
				String s = split.substring(1, split.length() - 1);

				String[] ss = s.split(",");

				pstmt.setString(1, checkId);

				pstmt.setString(2, ss[0]);

				pstmt.setInt(3, Integer.valueOf(ss[1].trim()));

				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	public void deleteCkResultObject(String tableName, int pid)
			throws Exception {

		String sql = "delete from ck_result_object where table_name=? and pid=?";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

}
