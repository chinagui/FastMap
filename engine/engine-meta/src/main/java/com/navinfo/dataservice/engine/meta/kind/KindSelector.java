package com.navinfo.dataservice.engine.meta.kind;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by chaixin on 2016/9/26 0026.
 */
public class KindSelector {
    private Logger log = LoggerRepos.getLogger(this.getClass());

    private Connection conn;

    public KindSelector() {

    }

    public KindSelector(Connection conn) {
        this.conn = conn;
    }

    public JSONObject getKinkMap() throws Exception {
        String sql = "select distinct poikind,r_kind from sc_point_kind_new where type = ?";
        ResultSet resultSet = null;
        PreparedStatement pstmt = null;
        JSONObject kindMap = new JSONObject();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 5);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                String poiKind = resultSet.getString("poikind");
                String rKind = resultSet.getString("r_kind");
                if (kindMap.containsKey(poiKind)) {
                    String value = kindMap.getString(poiKind);
                    value = value + "," + rKind;
                    kindMap.element(poiKind, value);
                } else {
                    kindMap.put(poiKind, rKind);
                }
            }

            return kindMap;
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }
}
