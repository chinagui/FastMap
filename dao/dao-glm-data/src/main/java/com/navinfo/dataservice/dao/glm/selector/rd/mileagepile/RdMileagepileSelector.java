package com.navinfo.dataservice.dao.glm.selector.rd.mileagepile;

import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class RdMileagepileSelector extends AbstractSelector {
    public RdMileagepileSelector(Connection conn) {
        super(RdMileagepile.class, conn);
    }

    public RdMileagepileSelector(Class<?> cls, Connection conn) {
        super(cls, conn);
    }

    public List<RdMileagepile> loadByLinkPid(int linkPid, boolean isLock) throws Exception {
        List<RdMileagepile> mileagepiles = new ArrayList<>();
        String sql = "select * from rd_mileagepile t where t.link_pid = :1 and t.u_record != 2";
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
                RdMileagepile mileagepile = new RdMileagepile();
                ReflectionAttrUtils.executeResultSet(mileagepile, resultSet);
                mileagepiles.add(mileagepile);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return mileagepiles;
    }
}
