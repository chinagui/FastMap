package com.navinfo.dataservice.dao.glm.selector.cmg;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CmgBuildlinkSelector
 * @Package: com.navinfo.dataservice.dao.glm.selector.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class CmgBuildlinkSelector extends AbstractSelector {

    /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(CmgBuildlinkSelector.class);

    public CmgBuildlinkSelector(Connection conn) {
        super(CmgBuildlink.class, conn);
    }

    /**
     * 根据CMG-FACE查询组成CMG-LINK
     *
     * @param facePid CMG-FACE主键
     * @param isLock 是否加锁
     * @return 组成面的CMG-LINK
     * @throws Exception 查询组成面的CMG-LINK出错
     */
    public List<CmgBuildlink> listTheAssociatedLinkOfTheFace(int facePid, boolean isLock) throws Exception {
        List<CmgBuildlink> result = new ArrayList<>();

        String sql = "select t1.* from cmg_buildlink t1, cmg_buildface_topo t2 where t1.link_pid = t2.link_pid and t2.face_pid = :1 and "
                + "t1.u_record <> 2 and t2.u_record <> 2";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet
                resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, facePid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildlink(result, resultSet);
        } catch (Exception e) {
            logger.error("method listTheAssociatedLinkOfTheFace error. [ sql : " + sql + " ] ");
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

        return result;
    }

    /**
     * 根据CMG-FACE查询组成CMG-LINK
     *
     * @param nodepid CMG-NODE主键
     * @param isLock 是否加锁
     * @return CMG-NODE关联CMG-LINK
     * @throws Exception 查询关联CMG-LINK出错
     */
    public List<CmgBuildlink> listTheAssociatedLinkOfTheNode(int nodepid, boolean isLock) throws Exception {
        List<CmgBuildlink> result = new ArrayList<>();

        String sql = "select * from cmg_buildlink t1 where (t1.s_node_pid = :1 or t1.e_node_pid = :2) and t1.u_record <> 2";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet
                resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, nodepid);
            pstmt.setInt(2, nodepid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildlink(result, resultSet);
        } catch (Exception e) {
            logger.error("method listTheAssociatedLinkOfTheNode error. [ sql : " + sql + " ] ");
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }

    /**
     * 根据结果组装CMG-LINK对象
     * @param result 结果集
     * @param resultSet 查询结果
     * @throws Exception 组装CMG-LINK对象出错
     */
    private void generateCmgBuildlink(List<CmgBuildlink> result, ResultSet resultSet) throws Exception {
        while (resultSet.next()) {
            CmgBuildlink cmgBuildlink = new CmgBuildlink();
            ReflectionAttrUtils.executeResultSet(cmgBuildlink, resultSet);

            List<IRow> meshes = new AbstractSelector(CmgBuildlinkMesh.class, getConn()).loadRowsByParentId(cmgBuildlink.pid(), false);
            cmgBuildlink.setMeshes(meshes);

            result.add(cmgBuildlink);
        }
    }
}
