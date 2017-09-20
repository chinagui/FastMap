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
import java.util.List;

public class ScPlateresInfoSearch  {

    private Connection conn;

    public ScPlateresInfoSearch(Connection conn) {
        this.conn = conn;
    }

    public List<IRow> searchDataByCondition(JSONObject condition) throws Exception {

        if(!condition.containsKey("adminArea")||!condition.containsKey("pageSize")||!condition.containsKey("pageNum")){
            throw new Exception("筛选情报参数不完善，请重新输入！");
        }

        String adminCode = condition.getString("adminArea");
        int pageSize = condition.getInt("pageSize");
        int pageNum = condition.getInt("pageNum");

        StringBuilder sql = new StringBuilder();

        List<Object> params = new ArrayList<>();

        sql.append("SELECT * FROM SC_PLATERES_INFO WHERE ADMIN_CODE = ?");
        params.add(adminCode);
        componentSql(condition,sql,params,pageSize,pageNum);

//        if (isLock) {
//            sql += " for update nowait";
//        }
        List<IRow> rows = new ArrayList<>();
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresInfo info = new ScPlateresInfo();

                ReflectionAttrUtils.executeResultSet(info, resultSet);

                rows.add(info);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return rows;
    }

    private void componentSql(JSONObject obj,StringBuilder sql,List<Object> params,int pageSize,int pageNum){

        if (obj.containsKey("infoCode")) {
            String infoCode = obj.getString("infoCode");

            if (infoCode != null && !infoCode.isEmpty()) {
                sql.append(" AND INFO_CODE = ?");
                params.add(infoCode);
            }
        }

        if (obj.containsKey("newsTime")) {
            String newsTime = obj.getString("newsTime");

            if (newsTime != null && !newsTime.isEmpty()) {
                sql.append(" AND NEWS_TIME = ?");
                params.add(newsTime);
            }
        }

        if (obj.containsKey("complete")) {
            String complete = obj.getString("complete");

            if (complete != null && !complete.isEmpty()) {
                sql.append(" AND COMPLETE IN ?");
                params.add("(" + complete + ")");
            }
        }

        if (obj.containsKey("condition")) {
            String condition = obj.getString("condition");

            if (condition != null && !condition.isEmpty()) {
                sql.append(" AND CONDITION = ?");
                params.add("(" + condition + ")");
            }
        }

        sql.append(" AND rownum BETWEEN ? AND ?");
        params.add((pageNum - 1) * pageSize + 1);
        params.add(pageNum * pageSize);
    }

}
