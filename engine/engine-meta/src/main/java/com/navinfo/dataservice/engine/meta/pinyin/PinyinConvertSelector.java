package com.navinfo.dataservice.engine.meta.pinyin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.navicommons.database.sql.DBUtils;

public class PinyinConvertSelector {
	
	private Connection conn;
	
	public PinyinConvertSelector() {
		
	}
	
	public PinyinConvertSelector(Connection conn) {
		this.conn = conn;
	}
	
	public Map<String, List<String>> getNavicovpyMap() throws Exception{
		
		String sql = "SELECT JT, PY2 FROM TY_NAVICOVPY_PY ORDER BY JT,PYORDER";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		Map<String, List<String>> navicovpyMap = new HashMap<String, List<String>>();
		
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			String oldJt = "";
			while (resultSet.next()) {
				List<String> pyList = new ArrayList<String>();
				String jt = resultSet.getString("JT");
				if (jt.equals(oldJt)) {
					pyList.add(resultSet.getString("PY2"));
					navicovpyMap.remove(jt);
					navicovpyMap.put(jt,pyList);
				} else {
					oldJt = jt;
					pyList.add(resultSet.getString("PY2"));
					navicovpyMap.put(jt,pyList);
				}
			}
			return navicovpyMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

}
