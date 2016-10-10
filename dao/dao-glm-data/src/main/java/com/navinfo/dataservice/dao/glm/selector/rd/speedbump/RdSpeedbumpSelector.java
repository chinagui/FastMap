package com.navinfo.dataservice.dao.glm.selector.rd.speedbump;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @author zhangyt
 * @Title: RdSpeedbumpSelector.java
 * @Description: 减速带查询
 * @date: 2016年8月5日 下午1:58:34
 * @version: v1.0
 */
public class RdSpeedbumpSelector extends AbstractSelector {

    private Connection conn;

    public RdSpeedbumpSelector(Connection conn) throws Exception {
        super(RdSpeedbump.class, conn);
        this.conn = conn;
    }

    public List<RdSpeedbump> loadByLinkPid(int linkPid, boolean isLock) throws Exception {
        List<RdSpeedbump> speedbumps = new ArrayList<RdSpeedbump>();
        String sql = "select * from rd_speedbump where link_pid = :1 and u_record != 2";
        if (isLock) {
            sql += " for update nowait";
        }
        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, linkPid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdSpeedbump speedbump = new RdSpeedbump();
                ReflectionAttrUtils.executeResultSet(speedbump, resultSet);
                speedbumps.add(speedbump);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return speedbumps;
    }

    public List<RdSpeedbump> loadByLinkPids(Collection<Integer> pids, boolean isLock) throws Exception {
        List<RdSpeedbump> speedbumps = new ArrayList<RdSpeedbump>();
        StringBuffer sb = new StringBuffer();
        Iterator<Integer> it = pids.iterator();
        while (it.hasNext()) {
            sb.append(it.next()).append(",");
        }
        String inter = "";
        if (sb.length() > 0)
            inter = sb.substring(0, sb.length() - 1);
        String sql = "select * from rd_speedbump where link_pid in ( " + inter + ") and u_record != 2";
        if (isLock) {
            sql += " for update nowait";
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdSpeedbump speedbump = new RdSpeedbump();
                ReflectionAttrUtils.executeResultSet(speedbump, resultSet);
                speedbumps.add(speedbump);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return speedbumps;
    }

    public List<RdSpeedbump> loadByNodePids(Collection<Integer> pids, boolean isLock) throws Exception {
        List<RdSpeedbump> speedbumps = new ArrayList<RdSpeedbump>();
        if (pids.isEmpty())
            return speedbumps;
        StringBuffer sb = new StringBuffer();
        Iterator<Integer> it = pids.iterator();
        while (it.hasNext()) {
            sb.append(it.next()).append(",");
        }
        String inter = "''";
        if (sb.length() > 0)
            inter = sb.substring(0, sb.length() - 1);

        String sql = "select * from rd_speedbump where node_pid in ( " + inter + " ) and u_record != 2";
        if (isLock) {
            sql += " for update nowait";
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdSpeedbump speedbump = new RdSpeedbump();
                ReflectionAttrUtils.executeResultSet(speedbump, resultSet);
                speedbumps.add(speedbump);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return speedbumps;
    }
}
