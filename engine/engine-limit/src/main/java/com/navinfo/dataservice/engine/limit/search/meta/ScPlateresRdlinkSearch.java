
package com.navinfo.dataservice.engine.limit.search.meta;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
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
public class ScPlateresRdlinkSearch {

    private Connection conn;

    public ScPlateresRdlinkSearch(Connection conn) {
        this.conn = conn;
    }


    public int searchDataByCondition(JSONObject condition,List<IRow> rows) throws Exception {
    	if(condition==null||condition.isNullObject()){
    		throw new Exception("输入信息为空，无法查询SC_PLATERES_RDLINK信息");
    	}
    	
        StringBuilder sqlstr = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        
        int total = 0;
        
        sqlstr.append(" FROM SC_PLATERES_RDLINK WHERE");
        componentSql(condition,sqlstr);
        
        sql.append("SELECT *, (SELECT COUNT(*) " + sqlstr + ") AS TOTAL_ROW_NUM");
        sql.append(sqlstr);

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresRdLink link = new ScPlateresRdLink();

                ReflectionAttrUtils.executeResultSet(link, resultSet);

                rows.add(link);
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

        if (obj.containsKey("geoId")) {
            String geoId = obj.getString("geoId");

            if (geoId != null && !geoId.isEmpty()) {
                sql.append(" GEOMETRY_ID = ");
                sql.append("'" + geoId + "'");
            }
        }

        if (obj.containsKey("linkPid")) {
            int linkPid = obj.getInt("linkPid");
            
            if(!sql.toString().endsWith("WHERE")){
            	sql.append(" AND");
            }

            if (linkPid != 0) {
                sql.append(" LINK_PID = ");
                sql.append(linkPid);
            }
        }
    }

}