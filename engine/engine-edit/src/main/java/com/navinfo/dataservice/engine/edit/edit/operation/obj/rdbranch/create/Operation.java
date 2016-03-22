package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		RdBranch branch = command.getBranch();

		boolean flag = false;

		if (branch == null) {

			flag = true;
			
			int meshId = new RdLinkSelector(conn).loadById(command.getInLinkPid(), true).mesh();

			branch = new RdBranch();

			branch.setPid(PidService.getInstance().applyBranchPid());

			branch.setInLinkPid(command.getInLinkPid());

			branch.setNodePid(command.getNodePid());

			branch.setOutLinkPid(command.getOutLinkPid());

			branch.setRelationshipType(getRelationShipType(
					command.getNodePid(), command.getOutLinkPid()));
			
			branch.setMesh(meshId);

			List<Integer> viaLinks = this.calViaLinks(command.getInLinkPid(),
					command.getNodePid(), command.getOutLinkPid());

			int seqNum=1;
			
			List<IRow> vias = new ArrayList<IRow>();
			
			for(Integer linkPid: viaLinks){
				RdBranchVia via = new RdBranchVia();
				
				via.setBranchPid(branch.getPid());

				via.setLinkPid(linkPid);
				
				via.setSeqNum(seqNum);
				
				vias.add(via);
				
				via.setMesh(meshId);
				
				seqNum++;
			}
			
			branch.setVias(vias);
		}

		RdBranchDetail detail = new RdBranchDetail();

		detail.setPid(PidService.getInstance().applyBranchDetailId());

		detail.setBranchPid(branch.getPid());
		
		detail.setMesh(branch.mesh());
		
		result.setPrimaryPid(detail.getPid());

		if (flag) {

			List<IRow> details = new ArrayList<IRow>();

			details.add(detail);

			branch.setDetails(details);

			result.insertObject(branch, ObjStatus.INSERT);
		} else {
			result.insertObject(detail, ObjStatus.INSERT);
		}

		return msg;
	}

	private int getRelationShipType(int nodePid, int outLinkPid)
			throws Exception {

		String sql = "with c1 as  (select node_pid from rd_cross_node a where exists (select null from rd_cross_node b where a.pid=b.pid and b.node_pid=:1))  select count(1) count from rd_link c where c.link_pid=:2 and (c.s_node_pid=:3 or c.e_node_pid=:4 or exists(select null from c1 where c.s_node_pid=c1.node_pid or c.e_node_pid=c1.node_pid))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, outLinkPid);

			pstmt.setInt(3, nodePid);

			pstmt.setInt(4, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				int count = resultSet.getInt("count");

				if (count > 0) {
					return 1;
				} else {
					return 2;
				}
			}

		} catch (Exception e) {

			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {

			}

			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

		return 1;
	}

	private List<Integer> calViaLinks(int inLinkPid, int nodePid, int outLinkPid)
			throws Exception {

		String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setString(3, String.valueOf(outLinkPid));

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				String viaPath = resultSet.getString("via_path");

				List<Integer> viaLinks = new ArrayList<Integer>();

				if (viaPath != null) {

					String[] splits = viaPath.split(",");

					for (String s : splits) {
						if (!s.equals("")) {
							
							int viaPid = Integer.valueOf(s);
							
							if(viaPid==inLinkPid || viaPid==outLinkPid){
								continue;
							}
							
							viaLinks.add(viaPid);
						}
					}

				}

				return viaLinks;
			}

		} catch (Exception e) {
			throw e;

		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
			}

		}

		return null;
	}
}
