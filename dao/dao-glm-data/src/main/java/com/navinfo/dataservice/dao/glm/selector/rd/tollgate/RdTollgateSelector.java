package com.navinfo.dataservice.dao.glm.selector.rd.tollgate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.datahub.api.Db;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgatePassage;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @author zhangyt
 * @Title: RdTollgateSelector.java
 * @Description: TODO
 * @date: 2016年8月10日 下午2:18:52
 * @version: v1.0
 */
public class RdTollgateSelector extends AbstractSelector {

    private Connection conn;

    public RdTollgateSelector(Connection conn) throws Exception {
        super(RdTollgate.class, conn);
        this.conn = conn;
    }

    /**
     * 根据线的Pid找出所有使用到该线的收费站
     *
     * @param linkPid 关联线PID
     * @param isLock  是否锁定数据
     * @return 返回关联的分叉口提示集合（集合中的数据仅有Pid,RowId,NodePid属性）
     * @throws Exception
     */
    public List<RdTollgate> loadRdTollgatesWithLinkPid(int linkPid, boolean isLock) throws Exception {
        List<RdTollgate> rdTollgates = new ArrayList<RdTollgate>();
        StringBuilder sb = new StringBuilder("select t.pid ss, t.row_id, t.node_pid, t.in_link_pid, t.out_link_pid from rd_tollgate t where (in_link_pid = :1 or out_link_pid = :2) and u_record != 2");
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
                RdTollgate rdTollgate = new RdTollgate();
                ReflectionAttrUtils.executeResultSet(rdTollgate, resultSet);

                RdTollgateNameSelector tollgateNameSelector = new RdTollgateNameSelector(this.conn);
                List<IRow> tollgateNames = tollgateNameSelector.loadRowsByParentId(rdTollgate.pid(), true);
                for (IRow row : tollgateNames) {
                    RdTollgateName tollgateName = (RdTollgateName) row;
                    rdTollgate.tollgateNameMap.put(tollgateName.rowId(), tollgateName);
                }
                rdTollgate.setNames(tollgateNames);
                RdTollgatePassageSelector tollgatePassageSelector = new RdTollgatePassageSelector(this.conn);
                List<IRow> tollgatePassages = tollgatePassageSelector.loadRowsByParentId(rdTollgate.pid(), true);
                for (IRow row : tollgatePassages) {
                    RdTollgatePassage tollgatePassage = (RdTollgatePassage) row;
                    rdTollgate.tollgatePassageMap.put(tollgatePassage.rowId(), tollgatePassage);
                }
                rdTollgate.setPassages(tollgatePassages);

                rdTollgates.add(rdTollgate);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return rdTollgates;
    }

    public IRow loadByIdOrderBySeqnum(int pid, boolean isLock) throws Exception {
        RdTollgate rdTollgate = new RdTollgate();
        StringBuilder sb = new StringBuilder("select * from rd_tollgate where pid = ? and u_record != 2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sb.toString());
            pstmt.setInt(1, pid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                ReflectionAttrUtils.executeResultSet(rdTollgate, resultSet);

                RdTollgateNameSelector tollgateNameSelector = new RdTollgateNameSelector(this.conn);
                List<IRow> tollgateNames = tollgateNameSelector.loadRowsByParentId(rdTollgate.pid(), true);
                for (IRow row : tollgateNames) {
                    RdTollgateName tollgateName = (RdTollgateName) row;
                    rdTollgate.tollgateNameMap.put(tollgateName.rowId(), tollgateName);
                }
                rdTollgate.setNames(tollgateNames);
                RdTollgatePassageSelector tollgatePassageSelector = new RdTollgatePassageSelector(this.conn);
                List<IRow> tollgatePassages = tollgatePassageSelector.loadRowsByClassParentId(RdTollgatePassage.class, rdTollgate.pid(), true, "seq_num");
                for (IRow row : tollgatePassages) {
                    RdTollgatePassage tollgatePassage = (RdTollgatePassage) row;
                    rdTollgate.tollgatePassageMap.put(tollgatePassage.rowId(), tollgatePassage);
                }
                rdTollgate.setPassages(tollgatePassages);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

        return rdTollgate;
    }

    /**
     * 根据线的Pid找出所有使用到该线的收费站
     *
     * @param nodePids 点PID
     * @param isLock   是否锁定数据
     * @return 返回关联的收费站集合
     * @throws Exception
     */
    public List<RdTollgate> loadRdTollgatesWithNodePids(List<Integer> nodePids, boolean isLock) throws Exception {
        List<RdTollgate> rdTollgates = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select t.* from rd_tollgate t where node_pid in ("
                + StringUtils.getInteStr(nodePids) + ") and u_record != 2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdTollgate rdTollgate = new RdTollgate();
                ReflectionAttrUtils.executeResultSet(rdTollgate, resultSet);

                RdTollgateNameSelector tollgateNameSelector = new RdTollgateNameSelector(this.conn);
                List<IRow> tollgateNames = tollgateNameSelector.loadRowsByParentId(rdTollgate.pid(), true);
                for (IRow row : tollgateNames) {
                    RdTollgateName tollgateName = (RdTollgateName) row;
                    rdTollgate.tollgateNameMap.put(tollgateName.rowId(), tollgateName);
                }
                rdTollgate.setNames(tollgateNames);
                RdTollgatePassageSelector tollgatePassageSelector = new RdTollgatePassageSelector(this.conn);
                List<IRow> tollgatePassages = tollgatePassageSelector.loadRowsByParentId(rdTollgate.pid(), true);
                for (IRow row : tollgatePassages) {
                    RdTollgatePassage tollgatePassage = (RdTollgatePassage) row;
                    rdTollgate.tollgatePassageMap.put(tollgatePassage.rowId(), tollgatePassage);
                }
                rdTollgate.setPassages(tollgatePassages);

                rdTollgates.add(rdTollgate);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return rdTollgates;
    }
}
