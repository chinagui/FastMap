package com.navinfo.dataservice.engine.limit.search.mate;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.mate.ScPlateresManoeuvre;
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
public class ScPlateresManoeuvreSearch {

    private Connection conn;

    public ScPlateresManoeuvreSearch(Connection conn) {
        this.conn = conn;
    }


    public int searchDataByCondition(JSONObject condition, List<IRow> rows) throws Exception {

    	if(!condition.containsKey("groupId")){
    		throw new Exception("为给定GROUP_ID,无法查询SC_PLATERES_MANOEUVRE信息");
    	}
    	
    	String groupId = condition.getString("groupId");
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT *, (SELECT COUNT(1) FROM SC_PLATERES_MANOEUVRE) AS TOTAL_NUM_ROW FROM SC_PLATERES_MANOEUVRE WHERE GROUP_ID = ");
        sql.append("'" + groupId + "'");        

        int total = 0;
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresManoeuvre manoeuvre = new ScPlateresManoeuvre();

                ReflectionAttrUtils.executeResultSet(manoeuvre, resultSet);

                rows.add(manoeuvre);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }
}
