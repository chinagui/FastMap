package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScPointNominganList {
	/**
	 * select pid,name from sc_point_nomingan_list
	 * @return List<String>: pid|name 所拼字符串列表
	 * @throws Exception
	 */
	public static boolean scPointNominganListPidNameList(String pidname) throws Exception{
		try {
			String sql = "select pid,name from sc_point_nomingan_list where pid||'|'||name='"+pidname+"'";
				
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getMetaConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					return true;} 
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				DbUtils.close(conn);
			}
		} catch (Exception e) {
			throw new SQLException("加载pidNameList失败："+ e.getMessage(), e);
		}
		return false;
	}
}
