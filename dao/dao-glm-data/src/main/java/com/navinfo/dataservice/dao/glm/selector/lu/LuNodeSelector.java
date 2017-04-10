package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class LuNodeSelector extends AbstractSelector {

    private Connection conn;

    public LuNodeSelector(Connection conn) throws Exception {
        super(LuNode.class, conn);
        this.conn = conn;
    }

    private void setChildren(ResultSet resultSet, LuNode luNode, boolean isLock) throws Exception {
        List<IRow> forms = new LuNodeMeshSelector(conn).loadRowsByParentId(luNode.pid(), isLock);
        luNode.setMeshes(forms);
        for (IRow row : luNode.getMeshes()) {
            LuNodeMesh mesh = (LuNodeMesh) row;
            luNode.meshMap.put(mesh.rowId(), mesh);
        }
    }

    // 加载盲端节点
    public List<LuNode> loadEndLuNodeByLinkPid(int linkPid, boolean isLock) throws Exception {
        List<LuNode> nodes = new ArrayList<LuNode>();
        String sql = "with tmp1 as (select s_node_pid, e_node_pid from lu_link where link_pid = :1), tmp2 as (select " +
                "b.link_pid, s_node_pid from lu_link b where exists (select null from tmp1 a where a.s_node_pid = b" +
                ".s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from lu_link b where exists " +
                "(select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record != 2), tmp3 as (select b" +
                ".link_pid, s_node_pid as e_node_pid from lu_link b where exists (select null from tmp1 a where a" +
                ".e_node_pid = b.s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from lu_link b " +
                "where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record != 2), tmp4 " +
                "as (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as (select " +
                "e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as (select pid from tmp4 " +
                "union select pid from tmp5) select * from lu_node a where exists (select null from tmp6 b where a" +
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
                LuNode luNode = new LuNode();
                ReflectionAttrUtils.executeResultSet(luNode, resultSet);
                this.setChildren(resultSet, luNode, isLock);
                nodes.add(luNode);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return nodes;
    }

}
