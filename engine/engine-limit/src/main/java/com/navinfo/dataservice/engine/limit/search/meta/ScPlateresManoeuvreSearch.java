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


    public List<IRow> searchDataByCondition(JSONObject condition) throws Exception {

        String sql = "";

//        if (isLock) {
//            sql += " for update nowait";
//        }
        List<IRow> rows = new ArrayList<>();
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

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

        return rows;
    }
}
