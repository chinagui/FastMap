/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: RdNameSelector 
* @author Zhang Xiaolong
* @date 2016年10月9日 上午11:26:01 
* @Description: TODO
*/
public class RdNameSelector {

	public JSONObject searchByName(String name, int pageSize, int pageNum,int dbId)
			throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;
		
		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {

			if (name.length() == 0) {
				result.put("total", total);

				result.put("data", array);

				return result;
			}

			String sql = "SELECT * FROM (SELECT C.*, ROWNUM RN FROM (SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, A.NAME_GROUPID, A.NAME, c.name as PROVINCE FROM RD_NAME A, AD_ADMIN B,ad_admin_name c WHERE A.NAME LIKE :1 AND a.ADMIN_ID = b.ADMIN_ID and b.REGION_ID = c.region_id and c.NAME_CLASS =1 and c.LANG_CODE = 'CHI') C WHERE ROWNUM <= :2) WHERE RN >= :3";

			int startRow = pageNum * pageSize + 1;

			int endRow = (pageNum + 1) * pageSize;
			
			conn = DBConnector.getInstance().getConnectionById(dbId);

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, name + "%");

			pstmt.setInt(2, endRow);

			pstmt.setInt(3, startRow);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				if (total == 0) {
					total = resultSet.getInt("total");
				}

				int nameId = resultSet.getInt("name_groupid");

				String nameStr = resultSet.getString("name");
				
				String province = resultSet.getString("province");

				JSONObject json = new JSONObject();

				json.put("nameId", nameId);

				json.put("name", nameStr);
				
				json.put("province", province);

				array.add(json);
			}

			result.put("total", total);

			result.put("data", array);

			return result;
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}
}
