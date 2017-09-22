package com.navinfo.dataservice.engine.limit.search.limit;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.navicommons.database.sql.DBUtils;

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

    public int searchDataByCondition(JSONObject condition, List<IRow> objList) throws Exception {

        if(!condition.containsKey("adminArea")||!condition.containsKey("pageSize")||!condition.containsKey("pageNum")){
            throw new Exception("筛选情报参数不完善，请重新输入！");
        }

        String adminCode = condition.getString("adminArea");
        int pageSize = condition.getInt("pageSize");
        int pageNum = condition.getInt("pageNum");

        StringBuilder sqlstr = new StringBuilder();

        sqlstr.append("SELECT * FROM SC_PLATERES_INFO WHERE ADMIN_CODE = ");
        sqlstr.append("'" + adminCode +"'");

        componentSql(condition,sqlstr);
        
        StringBuilder sql = new StringBuilder();
        sql.append("WITH query AS (" + sqlstr + ") SELECT query.*,(SELECT count(1) FROM query) AS TOTAL_ROW_NUM FROM query");
        sql.append(" WHERE rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + " for update nowait");

        //List<IRow> rows = new ArrayList<>();
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        int total = 0;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());
            
            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresInfo info = new ScPlateresInfo();

                ReflectionAttrUtils.executeResultSet(info, resultSet);
                
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

    private void componentSql(JSONObject obj,StringBuilder sql){

        if (obj.containsKey("infoCode")) {
            String infoCode = obj.getString("infoCode");

            if (infoCode != null && !infoCode.isEmpty()) {
                sql.append(" AND INFO_CODE = ");
                sql.append("'" + infoCode + "'");
            }
        }

        if (obj.containsKey("startTime")) {
            String startTime = obj.getString("startTime");

            if (startTime != null && !startTime.isEmpty()) {
                sql.append(" AND NEWS_TIME >= ");
                sql.append("'" + startTime + "'");
            }
        }
        
        if (obj.containsKey("endTime")) {
            String endTime = obj.getString("endTime");

            if (endTime != null && !endTime.isEmpty()) {
                sql.append(" AND NEWS_TIME <= ");
                sql.append("'" + endTime + "'");
            }
        }

        if (obj.containsKey("complete")) {
            String complete = obj.getString("complete");

            if (complete != null && !complete.isEmpty()) {
                sql.append(" AND COMPLETE IN ");
                sql.append("(" + complete + ")");
            }
        }

        if (obj.containsKey("condition")) {
            String condition = obj.getString("condition");

            if (condition != null && !condition.isEmpty()) {
                sql.append(" AND CONDITION IN ");
                sql.append("(" + condition + ")");
            }
        }

        //sql.append(" AND rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + " for update nowait");
    }

}
