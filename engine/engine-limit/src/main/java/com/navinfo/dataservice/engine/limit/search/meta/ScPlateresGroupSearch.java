package com.navinfo.dataservice.engine.limit.search.meta;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ScPlateresGroupSearch {

    private Connection conn;

    public ScPlateresGroupSearch(Connection conn) {
        this.conn = conn;
    }

    public ScPlateresGroup loadById(String groupId) throws Exception {

        ScPlateresGroup group = new ScPlateresGroup();

        String sqlstr = "SELECT * FROM SC_PLATERES_GROUP WHERE GROUP_ID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(group, resultSet);
            }
        } catch (Exception e) {

            throw new Exception("查询的ID为：" + groupId + "的" + group.tableName().toUpperCase() + "不存在");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return group;
    }

	public String loadMaxGroupId(String key) throws Exception {

        String sqlstr = "SELECT MAX(GROUP_ID) FROM SC_PLATERES_GROUP WHERE GROUP_ID LIKE '" + key + "%'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		String groupId = "";

		try {
			pstmt = this.conn.prepareStatement(sqlstr);



			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				groupId = resultSet.getString(1);
			}
		} catch (Exception e) {

			throw new Exception("查询GROUP信息异常");

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return groupId;
	}


    public int searchDataByCondition(JSONObject condition,List<IRow> rows) throws Exception {
    	
    	StringBuilder sqlstr = new StringBuilder();
    	sqlstr.append("SELECT * FROM SC_PLATERES_GROUP WHERE");
        componentSql(condition,sqlstr);
        
        StringBuilder sql = new StringBuilder();
        sql.append("WITH query AS (" + sqlstr + ")");
        sql.append(" SELECT query.*,(SELECT COUNT(1) FROM query) AS TOTAL_ROW_NUM FROM query");

        if (condition.containsKey("pageSize") && condition.containsKey("pageNum")) {
            int pageSize = condition.getInt("pageSize");
            int pageNum = condition.getInt("pageNum");

            sql.append(" WHERE rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + " FOR UPDATE NOWAIT");
        }
    	
        PreparedStatement pstmt = null;
        int total = 0;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresGroup group = new ScPlateresGroup();

                ReflectionAttrUtils.executeResultSet(group, resultSet);
                
                total = resultSet.getInt("TOTAL_ROW_NUM");

                rows.add(group);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }
    
	private void componentSql(JSONObject obj, StringBuilder sql) {

		if (obj.containsKey("infoIntelId")) {
			String infoIntelId = obj.getString("infoIntelId");

			if (infoIntelId != null && !infoIntelId.isEmpty()) {
				sql.append(" INFO_INTEL_ID = ");
				sql.append("'" + infoIntelId + "'");
			}
		}

		if (obj.containsKey("adminArea")) {
			String admin = obj.getString("adminArea");

			if (!sql.toString().endsWith("WHERE")) {
				sql.append(" AND");
			}

			if (admin != null && !admin.isEmpty()) {
				sql.append(" AD_ADMIN = ");
				sql.append(admin);
			}
		}

		if (obj.containsKey("groupId")) {
			String groupId = obj.getString("groupId");

			if (groupId != null && !groupId.isEmpty()) {
				sql.append(" AND GROUP_ID = ");
				sql.append("'" + groupId + "'");
			}
		}

		if (obj.containsKey("groupType")) {
			JSONArray groupType = obj.getJSONArray("groupType");

			if (groupType != null && groupType.size() != 0) {
				sql.append(" AND GROUP_TYPE IN ");
				sql.append("(" + groupType.toString().replace("[", "").replace("]", "") + ")");
			}
		}
	}
}