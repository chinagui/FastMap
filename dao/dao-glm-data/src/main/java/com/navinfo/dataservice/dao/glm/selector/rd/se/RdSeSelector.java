package com.navinfo.dataservice.dao.glm.selector.rd.se;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @author zhangyt
 * @Title: RdSeSelector.java
 * @Description: TODO
 * @date: 2016年8月1日 上午10:55:25
 * @version: v1.0
 */
public class RdSeSelector extends AbstractSelector {

    private Connection conn;

    public RdSeSelector(Connection conn) throws Exception {
        super(RdSe.class, conn);
        this.conn = conn;
    }

    /**
     * 根据线的Pid找出所有使用到该线的分叉口提示
     *
     * @param linkPid 关联线PID
     * @param isLock  是否锁定数据
     * @return 返回关联的分叉口提示集合（集合中的数据仅有Pid,RowId,NodePid属性）
     * @throws Exception
     */
    public List<RdSe> loadRdSesWithLinkPid(int linkPid, boolean isLock) throws Exception {
        List<RdSe> rdSes = new ArrayList<RdSe>();
        StringBuilder sb = new StringBuilder("select t.pid, t.row_id, t.node_pid, t.in_link_pid, t.out_link_pid from " +
                "" + "" + "rd_se t where (in_link_pid = :1 or out_link_pid = :2) and u_record != 2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            pstmt.setInt(1, linkPid);
            pstmt.setInt(2, linkPid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdSe rdSe = new RdSe();
                ReflectionAttrUtils.executeResultSet(rdSe, resultSet);
                rdSes.add(rdSe);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.close(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return rdSes;
    }

    /**
     * 根据点的Pid找出所有使用到该点的分叉口提示
     *
     * @param nodePids node点集合
     * @param isLock   是否锁定数据
     * @return 返回关联的分叉口提示集合（集合中的数据仅有Pid,RowId,NodePid属性）
     * @throws Exception
     */
    public List<RdSe> loadRdSesWithNodePids(List<Integer> nodePids, boolean isLock) throws Exception {
        List<RdSe> rdSes = new ArrayList<RdSe>();
        StringBuilder sb = new StringBuilder("select t.pid, t.row_id, t.node_pid, t.in_link_pid, t.out_link_pid from " +
                "" + "" + "rd_se t where node_pid in(" + StringUtils.getInteStr(nodePids) + ") and u_record != 2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdSe rdSe = new RdSe();
                ReflectionAttrUtils.executeResultSet(rdSe, resultSet);
                rdSes.add(rdSe);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.close(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return rdSes;
    }

}
