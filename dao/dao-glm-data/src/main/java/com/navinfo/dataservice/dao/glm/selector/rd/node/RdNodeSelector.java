package com.navinfo.dataservice.dao.glm.selector.rd.node;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.StringUtil;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdNodeSelector extends AbstractSelector {

    private static Logger logger = Logger.getLogger(RdNodeSelector.class);

    private Connection conn;

    public RdNodeSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(RdNode.class);
    }

    // 加载盲端节点
    public List<RdNode> loadEndRdNodeByLinkPid(int linkPid, boolean isLock)
            throws Exception {

        List<RdNode> nodes = new ArrayList<RdNode>();

        String sql = "with tmp1 as  (select s_node_pid, e_node_pid from rd_link where link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from rd_link b    where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid as e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from tmp5) select *   from rd_node a  where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record!=2";

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

                RdNode node = new RdNode();

                ReflectionAttrUtils.executeResultSet(node, resultSet);

                setChildData(node, isLock);

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

    public List<RdNode> loadRdNodeByLinkPid(int linkPid, boolean isLock)
            throws Exception {

        List<RdNode> nodes = new ArrayList<RdNode>();

        String sql = "select a.* from rd_node a where exists(select null from rd_link b where (b.e_node_pid=a.node_pid or b.s_node_pid=a.node_pid) and b.link_pid=:1)";

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

                RdNode node = new RdNode();

                ReflectionAttrUtils.executeResultSet(node, resultSet);

                setChildData(node, isLock);

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


    public int loadRdLinkCountOnNode(int nodePid)
            throws Exception {

        String sql = "select count(1) count from rd_link a where a.s_node_pid=:1 or a.e_node_pid=:2";

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
     * 根据nodepid查询node形态
     *
     * @param nodePids nodepid组成的sql中in中字符串
     * @return
     * @throws Exception
     */
    public List<Integer> loadRdNodeWays(String nodePids)
            throws Exception {

        String sql = "select FORM_OF_WAY from RD_NODE_FORM where node_pid in(" + nodePids + ") and u_record !=2";

        List<Integer> result = new ArrayList<>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                result.add(resultSet.getInt("FORM_OF_WAY"));
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return result;
    }

    /**
     * 设置子表数据
     *
     * @param node
     * @param isLock
     * @throws Exception
     */
    private void setChildData(RdNode node, boolean isLock) throws Exception {
        // 获取Node对应的关联数据
        node.setMeshes(new AbstractSelector(RdNodeMesh.class, conn).loadRowsByParentId(node.getPid(), isLock));

        List<IRow> names = new AbstractSelector(RdNodeName.class, conn).loadRowsByParentId(node.getPid(),
                isLock);

        for (IRow row : names) {
            row.setMesh(node.mesh());
        }

        node.setNames(names);

        List<IRow> forms = new AbstractSelector(RdNodeForm.class, conn).loadRowsByParentId(node.getPid(),
                isLock);

        for (IRow row : forms) {
            row.setMesh(node.mesh());
        }

        node.setForms(forms);
    }

    /*
     * 仅加载rdnode表，其他子表若有需要，请单独加载
     */
    public List<RdNode> loadBySql(String sql, boolean isLock) throws Exception {

        List<RdNode> nodes = new ArrayList<RdNode>();

        StringBuilder sb = new StringBuilder(sql);

        if (isLock) {
            sb.append(" for update nowait");
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sb.toString());

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                RdNode rdNode = new RdNode();

                ReflectionAttrUtils.executeResultSet(rdNode, resultSet);

                nodes.add(rdNode);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return nodes;

    }

    public Map<Integer, Integer> calRdLinkCountOnNodes(List<Integer> nodePids, boolean isLock) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        if (nodePids.isEmpty())
            return result;
        StringBuffer sb = new StringBuffer();
        sb.append("with tmp1 as (select rl.s_node_pid node_pid,count(rl.s_node_pid) num from rd_link rl where rl.s_node_pid in (");
        sb.append(StringUtils.getInteStr(nodePids));
        sb.append(")group by rl.s_node_pid),");
        sb.append("tmp2 as(select rl.e_node_pid node_pid,count(rl.e_node_pid) num from rd_link rl where rl.e_node_pid in (");
        sb.append(StringUtils.getInteStr(nodePids));
        sb.append(")group by rl.e_node_pid) ");
        sb.append("select tmp1.node_pid node_pid,(tmp1.num + tmp2.num) num from tmp1 ,tmp2 where tmp1.node_pid = tmp2.node_pid ");
        if (isLock) {
            sb.append("for update nowait");
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                result.put(resultSet.getInt("node_pid"), resultSet.getInt("num"));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return result;
    }
}