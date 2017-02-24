package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class LuLinkSelector extends AbstractSelector {

    private Connection conn;

    public LuLinkSelector(Connection conn) {
        super(LuLink.class, conn);
        this.conn = conn;
    }

    public List<LuLink> loadByNodePid(int nodePid, boolean isLock) throws Exception {
        List<LuLink> links = new ArrayList<LuLink>();
        StringBuilder sb = new StringBuilder(
                "select * from lu_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            pstmt.setInt(1, nodePid);
            pstmt.setInt(2, nodePid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                LuLink luLink = new LuLink();
                ReflectionAttrUtils.executeResultSet(luLink, resultSet);
                List<IRow> forms = new LuLinkMeshSelector(conn).loadRowsByParentId(luLink.getPid(), isLock);
                luLink.setMeshes(forms);
                for (IRow row : luLink.getMeshes()) {
                    LuLinkMesh mesh = (LuLinkMesh) row;
                    luLink.meshMap.put(mesh.rowId(), mesh);
                }
                List<IRow> kinds = new LuLinkKindSelector(conn).loadRowsByParentId(luLink.pid(), isLock);
                luLink.setLinkKinds(kinds);
                for (IRow row : luLink.getLinkKinds()) {
                    LuLinkKind kind = (LuLinkKind) row;
                    luLink.linkKindMap.put(kind.rowId(), kind);
                }
                links.add(luLink);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return links;

    }

    /**
     * 根据多个node_pid查询lu_link
     *
     * @param nodePids
     * @param isLock
     * @return
     * @throws Exception
     */
    public List<LuLink> loadByNodePids(String nodePids, boolean isLock) throws Exception {
        List<LuLink> links = new ArrayList<LuLink>();
        StringBuilder sb = new StringBuilder(
                "select * from lu_link where (s_node_pid in (" + nodePids + ") or e_node_pid in(" + nodePids + ")) and u_record!=2");
        if (isLock) {
            sb.append(" for update nowait");
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                LuLink luLink = new LuLink();
                ReflectionAttrUtils.executeResultSet(luLink, resultSet);
                this.setCls(LuLinkKind.class);
                List<IRow> kinds = loadRowsByParentId(luLink.getPid(), isLock);
                luLink.setLinkKinds(kinds);
                links.add(luLink);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return links;

    }
    
    /*
     * 仅加载LINK的pid
     */
    public List<Integer> loadLinkPidByNodePid(int nodePid, boolean isLock) throws Exception {

        List<Integer> links = new ArrayList<Integer>();

        StringBuilder sb = new StringBuilder("select link_pid from LU_LINK where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

        if (isLock) {
            sb.append(" for update nowait");
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sb.toString());

            pstmt.setInt(1, nodePid);

            pstmt.setInt(2, nodePid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                int value = resultSet.getInt("link_pid");

                links.add(value);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return links;

    }
}
