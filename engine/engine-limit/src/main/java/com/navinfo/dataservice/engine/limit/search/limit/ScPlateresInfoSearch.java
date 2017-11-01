package com.navinfo.dataservice.engine.limit.search.limit;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScPlateresInfoSearch  {

    private Connection conn;

    public ScPlateresInfoSearch(Connection conn) {
        this.conn = conn;
    }


    public ScPlateresInfo loadById(String infoId) throws Exception {

        ScPlateresInfo info = new ScPlateresInfo();

        String sqlstr = "SELECT * FROM SC_PLATERES_INFO WHERE INFO_INTEL_ID=? ";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, infoId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(info, resultSet);
            }
        } catch (Exception e) {

            throw new Exception("查询的ID为：" + infoId + "的" + info.tableName().toUpperCase() + "不存在");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return info;
    }

    public int searchDataByCondition(JSONObject condition, List<IRow> objList) throws Exception {

        if(!condition.containsKey("adminArea")||!condition.containsKey("pageSize")||!condition.containsKey("pageNum")){
            throw new Exception("筛选情报参数不完善，请重新输入！");
        }

        String adminCode = condition.getString("adminArea");
        int pageSize = condition.getInt("pageSize");
        int pageNum = condition.getInt("pageNum");

        StringBuilder sqlstr = new StringBuilder();

        sqlstr.append("SELECT t.*, row_number() over(order by INFO_INTEL_ID) as row_num FROM SC_PLATERES_INFO t WHERE t.ADMIN_CODE in ");
               
        Map<String,String> adminCodeAndName = getBeyondDirstrict(adminCode);
        
        if(adminCodeAndName.size() == 0){
        	throw new Exception("输入的行政区划，无法获取下级区县信息，确认后请重新输入！");
        }
        
        sqlstr.append(componentInSql(adminCodeAndName));

        componentSql(condition,sqlstr);
        
        StringBuilder sql = new StringBuilder();
        sql.append("WITH query AS (" + sqlstr + ") SELECT query.*,(SELECT count(1) FROM query) AS TOTAL_ROW_NUM FROM query");
        sql.append(" WHERE row_num BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize));

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        int total = 0;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());
            
            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresInfo info = new ScPlateresInfo();

                ReflectionAttrUtils.executeResultSet(info, resultSet);
                
                //web界面显示区县级行政区划只需要文字标识，后续操作，依据市级行政区划编码
                info.setAdminCode(adminCodeAndName.get(info.getAdminCode()));
                
                total = resultSet.getInt("TOTAL_ROW_NUM");

                objList.add(info);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }
    
	private String componentInSql(Map<String, String> adminCodeAndName) {
		String result = "(";

		for (Map.Entry<String, String> entry : adminCodeAndName.entrySet()) {
			result += "'" + entry.getKey() + "',";
		}

		if(adminCodeAndName.size() != 0){
		result = result.substring(0, result.lastIndexOf(","));}
		result += ")";

		return result;
	}
    
	private Map<String, String> getBeyondDirstrict(String adminCodeMK) throws Exception {
		
		//母库与情报库admincode不一致，转换成情报库admincode进行情报查询
		String adminCode = SearchHelp.updateInfoAdminCode(adminCodeMK);
		
		Connection mkconn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Map<String, String> result = new HashMap<>();

		StringBuilder sql = new StringBuilder();

		try {
			mkconn = DBConnector.getInstance().getMkConnection();

			if (adminCode.length() == 4 || (adminCode.length() == 6 && adminCode.substring(4).equals("00"))) {

				String cityCode = adminCode.substring(0, 4);

				sql.append(
						"select a.ADMIN_ID,b.NAME FROM AD_ADMIN a,AD_ADMIN_NAME b where a.REGION_ID = b.REGION_ID and b.LANG_CODE='CHI' AND b.NAME_CLASS = 1 and a.ADMIN_TYPE < 5 and a.ADMIN_ID between :1 and :2");

				pstmt = mkconn.prepareStatement(sql.toString());

				pstmt.setInt(1, Integer.valueOf(cityCode) * 100);

				pstmt.setInt(2, Integer.valueOf(cityCode) * 100 + 99);

				resultSet = pstmt.executeQuery();

				while (resultSet.next()) {
					result.put(String.valueOf(resultSet.getInt(1)), resultSet.getString(2));
				}
			} else {

				sql.append(
						"select a.ADMIN_ID,b.NAME FROM AD_ADMIN a,AD_ADMIN_NAME b where a.REGION_ID = b.REGION_ID and b.LANG_CODE='CHI' AND b.NAME_CLASS = 1 and a.ADMIN_ID = :1");

				pstmt = mkconn.prepareStatement(sql.toString());

				pstmt.setInt(1, Integer.valueOf(adminCode));

				resultSet = pstmt.executeQuery();

				while (resultSet.next()) {
					result.put(String.valueOf(resultSet.getInt(1)), resultSet.getString(2));
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeConnection(mkconn);
		}
		return result;
	}

	
	
    private void componentSql(JSONObject obj,StringBuilder sql){

        if (obj.containsKey("infoCode")) {
            String infoCode = obj.getString("infoCode");

            if (infoCode != null && !infoCode.isEmpty()) {
                sql.append(" AND t.INFO_CODE = ");
                sql.append("'" + infoCode + "'");
            }
        }
        
        if(obj.containsKey("infoIntelId")){
        	String infoIntelId = obj.getString("infoIntelId");
        	
        	if(infoIntelId != null && !infoIntelId.isEmpty()){
        		sql.append(" AND t.INFO_INTEL_ID = ");
        		sql.append("'" + infoIntelId + "'");
        	}
        }

        if (obj.containsKey("startTime")) {
            String startTime = obj.getString("startTime");

            if (startTime != null && !startTime.isEmpty()) {
                sql.append(" AND t.NEWS_TIME >= ");
                sql.append("'" + startTime + "'");
            }
        }
        
        if (obj.containsKey("endTime")) {
            String endTime = obj.getString("endTime");

            if (endTime != null && !endTime.isEmpty()) {
                sql.append(" AND t.NEWS_TIME <= ");
                sql.append("'" + endTime + "'");
            }
        }

        if (obj.containsKey("complete")) {
            JSONArray complete = obj.getJSONArray("complete");

			if (complete != null && complete.size() != 0) {
				sql.append(" AND t.COMPLETE IN (");
				sql.append(complete.toString().replace("[", "").replace("]", "")+ ")");
			}
        }

		if (obj.containsKey("condition")) {
			JSONArray condition = obj.getJSONArray("condition");

			if (condition != null && condition.size() != 0) {
				sql.append(" AND t.CONDITION IN (");

				for (int i = 0; i < condition.size(); i++) {
					if (i > 0) {
						sql.append(",");
					}
					sql.append("'" + condition.getString(i) + "'");
				}

				sql.append(")");
			}
		}

        //sql.append(" AND rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + " for update nowait");
    }

}
