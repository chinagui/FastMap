package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
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

			branch = this.createBranch();
		}

		switch (command.getBranchType()) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			createBranchDetail(branch, flag, result);
			break;
		case 5:
			createBranchRealimage(branch, flag, result);
			break;
		case 6:
			createSignasreal(branch, flag, result);
			break;
		case 7:
			createSeriesbranch(branch, flag, result);
			break;
		case 8:
			createSchematic(branch, flag, result);
			break;
		case 9:
			createSignboard(branch, flag, result);
			break;
		default:
			break;
		}

		return msg;
	}

	private RdBranch createBranch() throws Exception {
		int meshId = new RdLinkSelector(conn).loadById(command.getInLinkPid(),
				true).mesh();

		RdBranch branch = new RdBranch();

		branch.setPid(PidUtil.getInstance().applyBranchPid());

		branch.setInLinkPid(command.getInLinkPid());

		branch.setNodePid(command.getNodePid());

		branch.setOutLinkPid(command.getOutLinkPid());

		branch.setRelationshipType(getRelationShipType(command.getNodePid(),
				command.getOutLinkPid()));

		branch.setMesh(meshId);

		List<Integer> viaLinks = this.calViaLinks(command.getInLinkPid(),
				command.getNodePid(), command.getOutLinkPid());

		int seqNum = 1;

		//路口关系的分歧不记录经过线
		if(branch.getRelationshipType() != 1)
		{
			if(viaLinks.size() <= 32)
			{
				List<IRow> vias = new ArrayList<IRow>();
				
				for (Integer linkPid : viaLinks) {
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
			else
			{
				throw new Exception("分歧经过线数目不能超过32条:"+viaLinks.size());
			}
		}

		return branch;
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

							if (viaPid == inLinkPid || viaPid == outLinkPid) {
								continue;
							}

							viaLinks.add(viaPid);
						}
					}

				}

				return viaLinks;
			}

		} catch (Exception e) {
			if(e.getMessage().contains("value too large"))
			{
				throw new Exception("经过线长度超过最大长度限制");
			}
			else
			{
				throw e;
			}
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

	private void createBranchDetail(RdBranch branch, boolean flag, Result result)
			throws Exception {
		RdBranchDetail detail = new RdBranchDetail();

		detail.setPid(PidUtil.getInstance().applyBranchDetailId());

		detail.setBranchPid(branch.getPid());

		detail.setBranchType(command.getBranchType());

		detail.setMesh(branch.mesh());
		//方面分歧默认声音方向为2
		if(command.getBranchType() == 1){
			detail.setVoiceDir(2);
		}
		if(command.getBranchType() == 3||command.getBranchType() == 4){
			detail.setVoiceDir(9);
			detail.setEstabType(9);
			detail.setNameKind(9);
		}
		if (flag) {

			List<IRow> details = new ArrayList<IRow>();

			details.add(detail);

			branch.setDetails(details);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(detail, ObjStatus.INSERT, branch.pid());
		}
		// 分歧特殊处理
		result.setPrimaryPid(detail.getPid());
	}

	private void createBranchRealimage(RdBranch branch, boolean flag,
			Result result) throws Exception {
		RdBranchRealimage realimage = new RdBranchRealimage();

		realimage.setBranchPid(branch.getPid());

		realimage.setMesh(branch.mesh());

		if (flag) {

			List<IRow> realimages = new ArrayList<IRow>();

			realimages.add(realimage);

			branch.setRealimages(realimages);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(realimage, ObjStatus.INSERT, branch.pid());
		}

		// 分歧特殊处理
		result.setPrimaryPid(branch.getPid());
	}

	private void createSignasreal(RdBranch branch, boolean flag, Result result)
			throws Exception {
		RdSignasreal signasreal = new RdSignasreal();

		signasreal.setPid(PidUtil.getInstance().applyRdSignasreal());

		signasreal.setBranchPid(branch.getPid());

		signasreal.setMesh(branch.mesh());

		if (flag) {

			List<IRow> signasreals = new ArrayList<IRow>();

			signasreals.add(signasreal);

			branch.setSignasreals(signasreals);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(signasreal, ObjStatus.INSERT, branch.pid());
		}

		// 分歧特殊处理
		result.setPrimaryPid(signasreal.getPid());
	}

	private void createSeriesbranch(RdBranch branch, boolean flag, Result result)
			throws Exception {
		RdSeriesbranch seriesbranch = new RdSeriesbranch();

		seriesbranch.setBranchPid(branch.getPid());

		seriesbranch.setMesh(branch.mesh());

		if (flag) {

			List<IRow> seriesbranchs = new ArrayList<IRow>();

			seriesbranchs.add(seriesbranch);

			branch.setSeriesbranches(seriesbranchs);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(seriesbranch, ObjStatus.INSERT, branch.pid());
		}

		// 分歧特殊处理
		result.setPrimaryPid(branch.getPid());
	}

	private void createSchematic(RdBranch branch, boolean flag, Result result)
			throws Exception {
		RdBranchSchematic schematic = new RdBranchSchematic();

		schematic.setPid(PidUtil.getInstance().applyBranchSchematic());

		schematic.setBranchPid(branch.getPid());

		schematic.setMesh(branch.mesh());

		if (flag) {

			List<IRow> schematics = new ArrayList<IRow>();

			schematics.add(schematic);

			branch.setSchematics(schematics);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(schematic, ObjStatus.INSERT, branch.pid());
		}

		// 分歧特殊处理
		result.setPrimaryPid(schematic.getPid());
	}

	private void createSignboard(RdBranch branch, boolean flag, Result result)
			throws Exception {
		RdSignboard signboard = new RdSignboard();

		signboard.setPid(PidUtil.getInstance().applyRdSignboard());

		signboard.setBranchPid(branch.getPid());

		signboard.setMesh(branch.mesh());

		if (flag) {

			List<IRow> signboards = new ArrayList<IRow>();

			signboards.add(signboard);

			branch.setSignboards(signboards);

			result.insertObject(branch, ObjStatus.INSERT, branch.pid());
		} else {
			result.insertObject(signboard, ObjStatus.INSERT, branch.pid());
		}

		// 分歧特殊处理
		result.setPrimaryPid(signboard.getPid());
	}

}
