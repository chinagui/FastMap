package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

public class CHECK_CROSS_NOT_CONNECTED_NODE extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdCross) {
				RdCross rdCross = (RdCross) row;
				if (rdCross.status().equals(ObjStatus.INSERT)) {
					List<IRow> nodes = rdCross.getNodes();
					checkRdCross(nodes,rdCross.getPid());
				}
			} else if (row instanceof RdCrossNode) {
				RdCrossNode rdCrossNode = (RdCrossNode) row;
				 if (rdCrossNode.status().equals(ObjStatus.INSERT)) {
						// 通过node查出路口
					 	RdCrossSelector selector = new RdCrossSelector(this.getConn());
					 	RdCross rdCross = (RdCross) selector.loadById(rdCrossNode.getPid(), false, false);
					 	List<IRow> nodes = rdCross.getNodes();
						checkRdCross(nodes,rdCross.getPid());
					}
			}
		}

	}

	private void checkRdCross(List<IRow> nodes, int crossPid) throws Exception {
		List<Integer> nodePidList = new ArrayList<Integer>();
		for (IRow node:nodes) {
			RdCrossNode rdCrossNode = (RdCrossNode) node;
			nodePidList.add(rdCrossNode.getNodePid());
		}
		if (nodePidList.size()<2) {
			return;
		}
		Set<Integer> hasNodePidSet = new HashSet<Integer>();
		hasNodePidSet = getAllNodePid(nodePidList.get(0),hasNodePidSet);
		for (int nodePid:nodePidList) {
			if (!hasNodePidSet.contains(nodePid)) {
				String target = "[RD_CROSS," + crossPid + "]";
				this.setCheckResult("", target, 0);
				break;
			}
		}
	}

	private Set<Integer> getAllNodePid(Integer nodePid, Set<Integer> hasNodePidSet) throws Exception {
		hasNodePidSet.add(nodePid);
		List<RdLink> rdLinkList = loadByNodePidOnlyRdLink(nodePid);
		for (RdLink rdLink:rdLinkList) {
			int sNodepid = rdLink.getsNodePid();
			int eNodepid = rdLink.geteNodePid();
			if (!hasNodePidSet.contains(sNodepid)) {
				getAllNodePid(sNodepid, hasNodePidSet);
			}else if (!hasNodePidSet.contains(eNodepid)) {
				getAllNodePid(eNodepid, hasNodePidSet);
			}
		}
		return hasNodePidSet;
	}
	
	
	
	/*
     * 查询关联的交叉点内link
     */
    public List<RdLink> loadByNodePidOnlyRdLink(int nodePid) throws Exception {

        List<RdLink> links = new ArrayList<RdLink>();

        StringBuilder sb = new StringBuilder();
        sb.append("select l.* ");
        sb.append(" from rd_link l,rd_link_form f");
        sb.append(" where (l.s_node_pid = :1 or l.e_node_pid = :2)");
        sb.append(" and l.link_pid = f.link_pid");
        sb.append(" and f.form_of_way = 50");
        sb.append(" and l.u_record != 2");
        sb.append(" and f.u_record != 2");

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = this.getConn().prepareStatement(sb.toString());
            pstmt.setInt(1, nodePid);
            pstmt.setInt(2, nodePid);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                RdLink rdLink = new RdLink();
                ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
                links.add(rdLink);
            }
        }catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
        return links;
    }


}
