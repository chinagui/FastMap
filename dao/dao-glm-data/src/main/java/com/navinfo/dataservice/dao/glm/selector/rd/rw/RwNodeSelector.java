package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 铁路点查询类
 *
 * @author zhangxiaolong
 */
public class RwNodeSelector extends AbstractSelector {

    private Connection conn;

    public RwNodeSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(RwNode.class);
    }

    @Override
    public IRow loadById(int id, boolean isLock, boolean... loadChild) throws Exception {
        RwNode rwNode = new RwNode();

        StringBuilder sb = new StringBuilder("select * from " + rwNode.tableName() + " where node_pid = :1 and " +
                "u_record !=2");

        if (isLock) {
            sb.append(" for update nowait");
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sb.toString());

            pstmt.setInt(1, id);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                ReflectionAttrUtils.executeResultSet(rwNode, resultSet);
                this.setChildData(rwNode, isLock);

            } else {
                throw new Exception("对应RWNODE: " + id + " 不存在!");
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return rwNode;
    }

    // 加载盲端节点
    public List<RwNode> loadEndRdNodeByLinkPid(int linkPid, boolean isLock) throws Exception {

        List<RwNode> nodes = new ArrayList<RwNode>();

        String sql = "with tmp1 as (select s_node_pid, e_node_pid from rw_link where link_pid = :1), tmp2 as (select " +
                "b.link_pid, s_node_pid from rw_link b where exists (select null from tmp1 a where a.s_node_pid = b" +
                ".s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from rw_link b where exists " +
                "(select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record != 2), tmp3 as (select b" +
                ".link_pid, s_node_pid as e_node_pid from rw_link b where exists (select null from tmp1 a where a" +
                ".e_node_pid = b.s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from rw_link b " +
                "where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record != 2), tmp4 " +
                "as (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as (select " +
                "e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as (select pid from tmp4 " +
                "union select pid from tmp5) select * from rw_node a where exists (select null from tmp6 b where a" +
                ".node_pid = b.pid) and a.u_record != 2";

        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                RwNode node = new RwNode();

                ReflectionAttrUtils.executeResultSet(node, resultSet);
                this.setChildData(node, isLock);

                nodes.add(node);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return nodes;
    }

    public int loadRwLinkCountOnNode(int nodePid) throws Exception {

        String sql = "select count(1) count from rw_link a where a.s_node_pid=:1 or a.e_node_pid=:2";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            pstmt.setInt(1, nodePid);

            pstmt.setInt(2, nodePid);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return 0;
    }


    /**
     * 通过pid查rwnode，返回的rwnode中包含关联rwlink
     *
     * @param id
     * @param isLock
     * @return
     * @throws Exception
     */
    public RwNode GetRwNodeWithLinkById(int id, boolean isLock) throws Exception {
        RwNode rwNode = null;

        try {
            rwNode = (RwNode) loadById(id, isLock);

            List<RwLink> links = new RwLinkSelector(conn).loadByNodePid(id, isLock);

            rwNode.setTopoLink(links);

        } catch (Exception e) {

            throw e;

        } finally {

        }

        return rwNode;
    }

    private void setChildData(RwNode node, boolean isLock) throws Exception {

        List<IRow> meshes = new AbstractSelector(RwNodeMesh.class, conn).loadRowsByParentId(node.getPid(), isLock);

        node.setMeshes(meshes);

        for (IRow row : meshes) {
            RwNodeMesh mesh = (RwNodeMesh) row;

            node.meshMap.put(mesh.rowId(), mesh);
        }


    }

}
