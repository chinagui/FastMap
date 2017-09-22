package com.navinfo.dataservice.engine.limit.search.meta;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
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

    public List<IRow> searchDataByCondition(JSONObject condition) throws Exception {

    	if(!condition.containsKey("adminArea") && !condition.containsKey("infoCode")){
            throw new Exception("筛选GROUP参数不完善，请重新输入！");
        }
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT * FROM SC_PLATERES_GROUP WHERE");
        componentSql(condition,sql);
    	
        List<IRow> rows = new ArrayList<>();
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresGroup group = new ScPlateresGroup();

                ReflectionAttrUtils.executeResultSet(group, resultSet);

                rows.add(group);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return rows;
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

            if (sql.toString().endsWith("WHERE")){
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

        if (obj.containsKey("pageSize")&&obj.containsKey("pageNum")) {
            int pageSize = obj.getInt("pageSize");
            int pageNum = obj.getInt("pageNum");

            sql.append(" AND rownum BETWEEN "+ ((pageNum - 1) * pageSize + 1) + " AND " + (pageNum * pageSize));
        }

        sql.append(" for update nowait");
    }
}
