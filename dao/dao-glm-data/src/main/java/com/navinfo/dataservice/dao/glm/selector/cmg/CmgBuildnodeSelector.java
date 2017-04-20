package com.navinfo.dataservice.dao.glm.selector.cmg;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.exception.DAOException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CmgBuildnodeSelector
 * @Package: com.navinfo.dataservice.dao.glm.selector.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class CmgBuildnodeSelector extends AbstractSelector {

    /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(CmgBuildnodeSelector.class);

    public CmgBuildnodeSelector(Connection conn) {
        super(CmgBuildnode.class, conn);
    }

    /**
     * 根据CMG-FACE查询组成CMG-LINK
     *
     * @param facePid CMG-FACE主键
     * @param isLock 是否加锁
     * @return 组成面的CMG-LINK
     * @throws Exception 查询组成面的CMG-LINK出错
     */
    public List<CmgBuildnode> listTheAssociatedNodeOfTheFace(int facePid, boolean isLock) throws Exception {
        List<CmgBuildnode> result = new ArrayList<>();

        String sql = "with tmp as(select distinct cmn.node_pid from cmg_buildnode cmn, cmg_buildlink cml, cmg_buildface_topo cmf where ("
                + "cmn.node_pid = cml.s_node_pid or cmn.node_pid = cml.e_node_pid) and cml.link_pid = cmf.link_pid and cmf.face_pid = :1 "
                + "and cmn.u_record <> 2 and cml.u_record <> 2 and cmf.u_record <> 2) select * from cmg_buildnode t, tmp t1 where "
                + "t.node_pid = t1.node_pid";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, facePid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildnode(result, resultSet);
        } catch (SQLException e) {
            logger.error("method listTheAssociatedNodeOfTheFace error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }

    /**
     * 查询CMG-LINK的盲端节点
     *
     * @param linkPid CMG-LINK主键
     * @param isLock 是否加锁
     * @return 盲端节点
     * @throws Exception 查询盲端节点出错
     */
    public List<CmgBuildnode> listTheBlindNodeOfTheLink(int linkPid, boolean isLock) throws Exception {
        List<CmgBuildnode> result = new ArrayList<>();

        String sql = "with tmp1 as (select s_node_pid, e_node_pid from cmg_buildlink where link_pid = :1), tmp2 as (select b.link_pid, "
                + "s_node_pid from cmg_buildlink b where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record"
                + " != 2 union select b.link_pid, e_node_pid from cmg_buildlink b where exists (select null from tmp1 a where a.s_node_pid"
                + " = b.e_node_pid) and b.u_record != 2), tmp3 as (select b.link_pid, s_node_pid as e_node_pid from cmg_buildlink b where "
                + "exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record != 2 union select b.link_pid, "
                + "e_node_pid from cmg_buildlink b where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record"
                + " != 2), tmp4 as (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as (select e_node_pid "
                + "pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as (select pid from tmp4 union select pid from tmp5) "
                + "select * from cmg_buildnode a where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record != 2";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, linkPid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildnode(result, resultSet);
        } catch (SQLException e) {
            logger.error("method listTheBlindNodeOfTheLink error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }

    /**
     * 根据结果组装CMG-NODE对象
     * @param result 结果集
     * @param resultSet 查询结果
     * @throws Exception 组装CMG-NODE对象出错
     */
    private void generateCmgBuildnode(List<CmgBuildnode> result, ResultSet resultSet) throws Exception {
        while (resultSet.next()) {
            CmgBuildnode cmgBuildnode = new CmgBuildnode();
            ReflectionAttrUtils.executeResultSet(cmgBuildnode, resultSet);

            List<IRow> meshes = new AbstractSelector(CmgBuildnodeMesh.class, getConn()).loadRowsByParentId(cmgBuildnode.pid(), false);
            cmgBuildnode.setMeshes(meshes);

            result.add(cmgBuildnode);
        }
    }
}