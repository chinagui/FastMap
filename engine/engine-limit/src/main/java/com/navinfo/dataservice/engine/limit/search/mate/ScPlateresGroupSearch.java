package com.navinfo.dataservice.engine.limit.search.mate;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.mate.ScPlateresGroup;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2017/9/19.
 */
public class ScPlateresGroupSearch {

    private Connection conn;

    public ScPlateresGroupSearch(Connection conn) {
        this.conn = conn;
    }

    public int searchDataByCondition(JSONObject condition,List<IRow> rows) throws Exception {

    	if(!condition.containsKey("adminArea") && !condition.containsKey("infoCode")){
            throw new Exception("筛选GROUP参数不完善，请重新输入！");
        }
    	
    	StringBuilder sqlstr = new StringBuilder();
    	sqlstr.append("SELECT * FROM SC_PLATERES_GROUP WHERE");
        componentSql(condition,sqlstr);
        
        StringBuilder sql = new StringBuilder();
        sql.append("WITH query AS (" + sql + ")");
        sql.append(" SELECT *,(SELECT COUNT(1) FROM query) AS TOTAL_ROW_NUM FROM query");

        if (condition.containsKey("pageSize") && condition.containsKey("pageNum")) {
            int pageSize = condition.getInt("pageSize");
            int pageNum = condition.getInt("pageNum");

            sql.append(" WHERE rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize) + "FOR UPDATE NOWAIT");
        }

        sql.append(" for update nowait");
    	
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
    
    private void componentSql(JSONObject obj,StringBuilder sql){

        if (obj.containsKey("infoCode")) {
            String infoCode = obj.getString("infoCode");

            if (infoCode != null && !infoCode.isEmpty()) {
                sql.append(" INFO_INTEL_ID = ");
                sql.append("'" + infoCode + "'");
            }
        }

        if (obj.containsKey("adminArea")) {
            String admin = obj.getString("adminArea");

            if (!sql.toString().endsWith("WHERE")){
            	sql.append(" AND");
            }
            
            if (admin != null && !admin.isEmpty()) {
                sql.append(" AD_ADMIN = ");
                sql.append("'" + admin + "'");
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
            String groupType = obj.getString("groupType");

            if (groupType != null && !groupType.isEmpty()) {
                sql.append(" AND GROUP_TYPE IN ");
                sql.append("(" + groupType + ")");
            }
        }
    }
}
