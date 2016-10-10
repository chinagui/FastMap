package com.navinfo.dataservice.dao.glm.selector.rd.eleceye;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.StringUtil;

/**
 * @author zhangyt
 * @Title: RdElectroniceyeSelector.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.selector.rd.eleceye
 * @Description: 查询电子眼
 * @date: 2016年7月20日 下午5:46:19
 * @version: v1.0
 */
public class RdElectroniceyeSelector extends AbstractSelector {

    private Connection conn;

    public RdElectroniceyeSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(RdElectroniceye.class);
    }

    /**
     * 根据RdElectroniceye的Pid查询
     */
    @Override
    public IRow loadById(int id, boolean isLock, boolean... loadChild) throws Exception {
        RdElectroniceye eleceye = new RdElectroniceye();

        String sql = "select * from " + eleceye.tableName() + " where pid = :1 and u_record != 2 ";

        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            pstmt.setInt(1, id);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(eleceye, resultSet);

            } else {

                throw new DataNotFoundException("数据不存在");
            }

            List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByEleceyePid(eleceye.pid(), isLock);
            for (IRow row : parts) {
                RdEleceyePart part = (RdEleceyePart) row;
                part.setMesh(resultSet.getInt("mesh_id"));
                eleceye.partMap.put(part.rowId(), part);

                List<IRow> pairs = new ArrayList<IRow>();
                RdEleceyePair pair = (RdEleceyePair) new AbstractSelector(RdEleceyePair.class, conn)
                        .loadById(part.getGroupId(), false);
                pairs.add(pair);
                eleceye.setPairs(pairs);
                eleceye.pairMap.put(pair.pid(), pair);
            }
            eleceye.setParts(parts);
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return eleceye;
    }

    public List<RdElectroniceye> loadListByRdLinkId(int rdLinkPid, boolean isLock) throws Exception {
        List<RdElectroniceye> eleceyes = new ArrayList<RdElectroniceye>();
        RdElectroniceye eleceye = new RdElectroniceye();

        String sql = "select * from " + eleceye.tableName() + " where link_pid = :1 and u_record != 2 ";

        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            pstmt.setInt(1, rdLinkPid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                eleceye = new RdElectroniceye();
                ReflectionAttrUtils.executeResultSet(eleceye, resultSet);

                List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByEleceyePid(eleceye.pid(), isLock);
                for (IRow row : parts) {
                    RdEleceyePart part = (RdEleceyePart) row;
                    eleceye.partMap.put(part.rowId(), part);

                    List<IRow> pairs = new ArrayList<IRow>();
                    RdEleceyePair pair = (RdEleceyePair) new AbstractSelector(RdEleceyePair.class, conn)
                            .loadById(part.getGroupId(), false);
                    pairs.add(pair);
                    eleceye.setPairs(pairs);
                    eleceye.pairMap.put(pair.pid(), pair);
                }
                eleceye.setParts(parts);

                eleceyes.add(eleceye);
            }

        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return eleceyes;
    }

    public List<RdElectroniceye> loadListByRdLinkIds(List<Integer> linkPids, boolean isLock) throws Exception {
        List<RdElectroniceye> eleceyes = new ArrayList<RdElectroniceye>();
        RdElectroniceye eleceye = new RdElectroniceye();
        String ids = StringUtils.getInteStr(linkPids);
        if (ids.length() == 0) {
            ids = "''";
        }
        String sql = "select * from " + eleceye.tableName() + " where link_pid in ( " + ids + " ) and u_record != 2 ";

        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                eleceye = new RdElectroniceye();
                ReflectionAttrUtils.executeResultSet(eleceye, resultSet);

                List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByEleceyePid(eleceye.pid(), isLock);
                for (IRow row : parts) {
                    RdEleceyePart part = (RdEleceyePart) row;
                    eleceye.partMap.put(part.rowId(), part);

                    List<IRow> pairs = new ArrayList<IRow>();
                    RdEleceyePair pair = (RdEleceyePair) new AbstractSelector(RdEleceyePair.class, conn)
                            .loadById(part.getGroupId(), false);
                    pairs.add(pair);
                    eleceye.setPairs(pairs);
                    eleceye.pairMap.put(pair.pid(), pair);
                }
                eleceye.setParts(parts);

                eleceyes.add(eleceye);
            }

        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return eleceyes;
    }
}
