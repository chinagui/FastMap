package com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning;

import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RdLinkWarningSelector extends AbstractSelector {

    public RdLinkWarningSelector(Connection conn) {
        super(conn);
        this.setCls(RdLinkWarning.class);
    }

    public List<Integer> loadPidByLink(int linkPid, boolean isLock)
            throws Exception {

        List<Integer> infectList = new ArrayList<>();

        List<RdLinkWarning> warnings = loadByLink(linkPid, isLock);

        for (RdLinkWarning warning : warnings) {
            infectList.add(warning.getPid());
        }

        return infectList;
    }

    public List<RdLinkWarning> loadByLink(int linkPid, boolean isLock)
            throws Exception {
        List<RdLinkWarning> rows = new ArrayList<>();

        String sql = "SELECT A.* FROM RD_LINK_WARNING A WHERE A.U_RECORD!=<>2 AND A.LINK_PID=:1 ";

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

                RdLinkWarning obj = new RdLinkWarning();

                ReflectionAttrUtils.executeResultSet(obj, resultSet);

                rows.add(obj);
            }

        } catch (Exception e) {

            throw e;

        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);

        }

        return rows;
    }
}