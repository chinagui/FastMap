
package com.navinfo.dataservice.engine.limit.search.meta;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.model.ReflectionAttrUtils;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
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

    public ScPlateresManoeuvre loadById(int manoeuvreId,String groupId) throws Exception {

    	ScPlateresManoeuvre manoeuvre = new ScPlateresManoeuvre();

        String sqlstr = "SELECT * FROM SC_PLATERES_MANOEUVRE WHERE MANOEUVRE_ID = ? AND GROUP_ID = ?";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setInt(1, manoeuvreId);
            pstmt.setString(2, groupId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(manoeuvre, resultSet);
            }
        } catch (Exception e) {

            throw new Exception("查询的ID为：" + manoeuvreId + ",GROUP_ID为" + groupId + "的" + manoeuvre.tableName().toUpperCase() + "不存在");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return manoeuvre;
    }

    public int loadMaxManoeuvreId(String groupId) throws Exception {

        String sqlstr = "SELECT MAX(MANOEUVRE_ID) FROM SC_PLATERES_MANOEUVRE WHERE GROUP_ID = ?";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        
        int manoeuvreId = 0;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

               manoeuvreId = resultSet.getInt(1);
            }
        } catch (Exception e) {

            throw new Exception("查询GROUP_ID为：" + groupId + "的最大MANOEUVRE_ID异常");

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return manoeuvreId;
    }

    public int searchDataByCondition(JSONObject condition, List<IRow> rows) throws Exception {

    	if(!condition.containsKey("groupId")){
    		throw new Exception("未给定GROUP_ID,无法查询SC_PLATERES_MANOEUVRE信息");
    	}
    	
    	String groupId = condition.getString("groupId");
        StringBuilder sql = new StringBuilder();
        
        sql.append("WITH query AS (SELECT * FROM SC_PLATERES_MANOEUVRE WHERE GROUP_ID = ?)");
        sql.append(" SELECT query.*, (SELECT COUNT(1) FROM query) AS TOTAL_NUM_ROW FROM query FOR UPDATE NOWAIT");       

        int total = 0;
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql.toString());
            
            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresManoeuvre manoeuvre = new ScPlateresManoeuvre();

                ReflectionAttrUtils.executeResultSet(manoeuvre, resultSet);

                rows.add(manoeuvre);
                
                total = resultSet.getInt("TOTAL_NUM_ROW");
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }

    public List<ScPlateresManoeuvre> loadByGroupId(String groupId) throws Exception {

        List<ScPlateresManoeuvre> manoeuvres = new ArrayList<>();

        String sqlstr = "SELECT * FROM SC_PLATERES_MANOEUVRE WHERE  GROUP_ID = ?";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sqlstr);

            pstmt.setString(1, groupId);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                ScPlateresManoeuvre manoeuvre = new ScPlateresManoeuvre();

                ReflectionAttrUtils.executeResultSet(manoeuvre, resultSet);

                manoeuvres.add(manoeuvre);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return manoeuvres;
    }
}