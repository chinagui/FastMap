package com.navinfo.dataservice.dao.glm.selector.rd.hgwg;

import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/11/7 0007.
 */
public class RdHgwgLimitSelector extends AbstractSelector {
    public RdHgwgLimitSelector(Connection conn) {
        super(RdHgwgLimit.class, conn);
    }

    public List<RdHgwgLimit> loadByLinkPid(int linkPid, boolean isLock) throws Exception {
        List<RdHgwgLimit> hgwgLimits = new ArrayList<>();
        String sql = "select * from rd_hgwg_limit t where t.link_pid = :1 and t.u_record != 2";
        if (isLock) {
            sql += " for update nowait";
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, linkPid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdHgwgLimit hgwgLimit = new RdHgwgLimit();
                ReflectionAttrUtils.executeResultSet(hgwgLimit, resultSet);
                hgwgLimits.add(hgwgLimit);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return hgwgLimits;
    }
}

