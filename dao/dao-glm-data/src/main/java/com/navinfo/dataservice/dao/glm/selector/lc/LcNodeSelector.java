package com.navinfo.dataservice.dao.glm.selector.lc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @author zhangyt
 * @Title: LcNode.java
 * @Description: TODO
 * @date: 2016年7月27日 下午1:52:29
 * @version: v1.0
 */
public class LcNodeSelector extends AbstractSelector {

    private Connection conn;

    public LcNodeSelector(Connection conn) throws Exception {
        super(LcNode.class, conn);
        this.conn = conn;
    }

    // 加载盲端节点
    public List<LcNode> loadEndLcNodeByLinkPid(int linkPid, boolean isLock) throws Exception {
        List<LcNode> nodes = new ArrayList<LcNode>();
        String sql = "with tmp1 as (select s_node_pid, e_node_pid from lc_link where link_pid = :1), tmp2 as (select " +
                "b.link_pid, s_node_pid from lc_link b where exists (select null from tmp1 a where a.s_node_pid = b" +
                ".s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from lc_link b where exists " +
                "(select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record != 2), tmp3 as (select b" +
                ".link_pid, s_node_pid as e_node_pid from lc_link b where exists (select null from tmp1 a where a" +
                ".e_node_pid = b.s_node_pid) and b.u_record != 2 union select b.link_pid, e_node_pid from lc_link b " +
                "where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record != 2), tmp4 " +
                "as (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as (select " +
                "e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as (select pid from tmp4 " +
                "union select pid from tmp5) select * from lc_node a where exists (select null from tmp6 b where a" +
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
                LcNode node = new LcNode();
                ReflectionAttrUtils.executeResultSet(node, resultSet);
                LcNodeMeshSelector mesh = new LcNodeMeshSelector(conn);
                node.setMeshes(mesh.loadRowsByParentId(node.getPid(), isLock));
                nodes.add(node);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
        }
        return nodes;
    }
}
