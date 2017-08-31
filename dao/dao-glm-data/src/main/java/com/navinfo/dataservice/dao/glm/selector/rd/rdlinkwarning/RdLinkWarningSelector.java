package com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        String sql = "SELECT A.* FROM RD_LINK_WARNING A WHERE A.U_RECORD <>2 AND A.LINK_PID=:1 ";

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

    public List<RdLinkWarning> loadByLinks(List<Integer> linkPids, boolean isLock) throws Exception {

        List<RdLinkWarning> rows = new ArrayList<>();

        if (null == linkPids || linkPids.size() == 0) {

            return rows;
        }

        String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

        Clob pidClod = null;

        StringBuilder sb = new StringBuilder("SELECT * FROM  RD_LINK_WARNING WHERE U_RECORD !=2 AND LINK_PID IN ");

        if (linkPids.size() > 1000) {
            pidClod = ConnectionUtil.createClob(getConn());
            pidClod.setString(1, ids);
            sb.append(" (select to_number(column_value) from table(clob_to_table(?))) ");
        } else {
            sb.append(" (");
            sb.append(ids);
            sb.append(") ");
        }

        if (isLock) {
            sb.append(" for update nowait");
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = getConn().prepareStatement(sb.toString());

            if (linkPids.size() > 1000) {

                pstmt.setClob(1, pidClod);
            }

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