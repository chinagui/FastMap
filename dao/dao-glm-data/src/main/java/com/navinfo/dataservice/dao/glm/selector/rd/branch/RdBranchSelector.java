package com.navinfo.dataservice.dao.glm.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdBranchSelector extends AbstractSelector {

	private Connection conn;

	public RdBranchSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdBranch.class);
	}

	@Override
	public IRow loadById(int id, boolean isLock, boolean... loadChild)
			throws Exception {

		RdBranch branch = new RdBranch();

		String sql = "select * from " + branch.tableName()
				+ " where branch_pid=:1 and u_record!=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);
			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return branch;
	}

	public IRow loadByDetailId(int detailId, int branchType, String rowId,
			boolean isLock) throws Exception {

		String tableName = "";
		String condition = "";
		if (branchType >= 0 && branchType <= 4) {
			tableName = " rd_branch_detail ";
			condition = " detail_id=:1 ";
		}
		if (branchType == 5) {
			tableName = " rd_branch_realimage ";
			condition = " row_id=hextoraw(:1)";

		}
		if (branchType == 6) {
			tableName = " rd_signasreal ";
			condition = " signboard_id =:1 ";
		}
		if (branchType == 7) {
			tableName = " rd_seriesbranch ";
			condition = " row_id=hextoraw(:1)";
		}
		if (branchType == 8) {
			tableName = " rd_branch_schematic ";
			condition = " schematic_id =:1 ";

		}
		if (branchType == 9) {
			tableName = " rd_signboard ";
			condition = " signboard_id =:1 ";

		}
		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b,"
				+ tableName
				+ " c where a.u_record!=2 and a.in_link_pid = b.link_pid  and a.branch_pid=c.branch_pid and c."
				+ condition + "";

		if (isLock) {
			sql += " for update nowait";
		}
		RdBranch branch = new RdBranch();
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			if (branchType == 5 || branchType == 7) {
				pstmt.setString(1, rowId);
			} else {
				pstmt.setInt(1, detailId);
			}

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				if (branchType >= 0 && branchType <= 4) {
					IRow detail = new AbstractSelector(RdBranchDetail.class,
							conn).loadById(detailId, isLock);

					List<IRow> details = new ArrayList<IRow>();

					details.add(detail);

					branch.setDetails(details);
				}
				if (branchType == 5) {
					IRow image = new AbstractSelector(RdBranchRealimage.class,
							conn).loadByRowId(rowId, isLock);
					List<IRow> images = new ArrayList<IRow>();
					images.add(image);
					branch.setRealimages(images);
				}
				if (branchType == 6) {
					IRow signasreal = new AbstractSelector(RdSignasreal.class,
							conn).loadById(detailId, isLock);
					List<IRow> signasreals = new ArrayList<IRow>();
					signasreals.add(signasreal);
					branch.setSignasreals(signasreals);
				}
				if (branchType == 7) {
					IRow seriesbranch = new AbstractSelector(
							RdSeriesbranch.class, conn).loadByRowId(rowId,
							isLock);
					List<IRow> seriesbranches = new ArrayList<IRow>();
					seriesbranches.add(seriesbranch);
					branch.setSeriesbranches(seriesbranches);
				}
				if (branchType == 8) {
					IRow schematic = new AbstractSelector(
							RdBranchSchematic.class, conn).loadById(detailId,
							isLock);
					List<IRow> schematics = new ArrayList<IRow>();
					schematics.add(schematic);
					branch.setSchematics(schematics);
				}
				if (branchType == 9) {
					IRow signboard = new AbstractSelector(RdSignboard.class,
							conn).loadById(detailId, isLock);
					List<IRow> signboards = new ArrayList<IRow>();
					signboards.add(signboard);
					branch.setSignboards(signboards);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(),
						isLock));

			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return branch;
	}

	public List<RdBranch> loadRdBranchByInLinkPid(int linkPid, boolean isLock)
			throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "select * from rd_branch where in_link_pid = :1 and u_record!=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

	public List<RdBranch> loadRdBranchByOutLinkPid(int linkPid, boolean isLock)
			throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "SELECT a.*, a.node_pid out_node_pid FROM rd_branch a WHERE a.relationship_type = 1 AND a.out_link_pid = :1 AND a.u_record != 2 UNION ALL SELECT a.*, CASE WHEN b.s_node_pid IN (d.s_node_pid, d.e_node_pid) THEN b. s_node_pid ELSE b.e_node_pid END out_node_pid FROM rd_branch a, rd_link b, rd_branch_via C, rd_link d WHERE a.relationship_type = 2 AND a.branch_pid = C.branch_pid AND a.out_link_pid = b.link_pid AND C.link_pid = d.link_pid AND (b.s_node_pid IN (d.s_node_pid, d.e_node_pid) OR b.e_node_pid IN (d.s_node_pid, d.e_node_pid)) and b.link_pid = :2  AND a.u_record != 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdBranch branch = new RdBranch();
				ReflectionAttrUtils.executeResultSet(branch, resultSet);
				branch.isetOutNodePid(resultSet.getInt("out_node_pid"));
				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

	public RdBranch loadByLinkNodeLink(int inLinkPid, int nodePid,
			int outLinkPid, boolean isLock) throws Exception {

		RdBranch branch = new RdBranch();

		String sql = "select a.*,b.mesh_id from rd_branch a,rd_link b where a.in_link_pid=:1 and a.node_pid=:2 and a.out_link_pid=:3 and a.u_record!=2 and a.in_link_pid = b.link_pid ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);

			} else {
				return null;
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branch;

	}

	public List<RdBranch> loadRdBranchByNodePid(int nodePid, boolean isLock)
			throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "select * from rd_branch where node_pid = :1 and u_record!=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

	public List<RdBranch> loadRdBranchByLinkNode(int linkPid, int nodePid1,
			int nodePid2, boolean isLock) throws Exception {

		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "select * from rd_branch where node_pid in (:1,:2) and in_link_pid=:3 and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid1);

			pstmt.setInt(2, nodePid2);

			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return branchs;
	}

	/**
	 * 设置子表
	 * 
	 * @param branch
	 * @param isLock
	 * @throws Exception
	 */
	private void setChild(RdBranch branch, boolean isLock) throws Exception {
		branch.setDetails(new AbstractSelector(RdBranchDetail.class, conn)
				.loadRowsByParentId(branch.getPid(), isLock));

		AbstractSelector branchNameSelector = new AbstractSelector(
				RdBranchName.class, conn);

		for (IRow row : branch.getDetails()) {

			RdBranchDetail detail = (RdBranchDetail) row;

			detail.setNames(branchNameSelector.loadRowsByParentId(
					detail.getPid(), isLock));

			for (IRow nameRow : detail.getNames()) {
				RdBranchName name = (RdBranchName) nameRow;

				detail.nameMap.put(name.pid(), name);
			}

			branch.detailMap.put(detail.getPid(), detail);
		}

		branch.setSignboards(new AbstractSelector(RdSignboard.class, conn)
				.loadRowsByParentId(branch.getPid(), isLock));

		AbstractSelector signboardNameSelector = new AbstractSelector(
				RdSignboardName.class, conn);

		for (IRow row : branch.getSignboards()) {

			RdSignboard obj = (RdSignboard) row;

			obj.setNames(signboardNameSelector.loadRowsByParentId(obj.getPid(),
					isLock));

			for (IRow nameRow : obj.getNames()) {

				RdSignboardName name = (RdSignboardName) nameRow;

				obj.nameMap.put(name.pid(), name);
			}

			branch.signboardMap.put(obj.getPid(), obj);
		}

		branch.setSignasreals(new AbstractSelector(RdSignasreal.class, conn)
				.loadRowsByParentId(branch.getPid(), isLock));

		for (IRow row : branch.getSignasreals()) {
			RdSignasreal obj = (RdSignasreal) row;

			branch.signasrealMap.put(obj.getPid(), obj);
		}

		branch.setSeriesbranches(new AbstractSelector(RdSeriesbranch.class,
				conn).loadRowsByParentId(branch.getPid(), isLock));

		for (IRow row : branch.getSeriesbranches()) {
			RdSeriesbranch obj = (RdSeriesbranch) row;

			branch.seriesbranchMap.put(obj.rowId(), obj);
		}

		branch.setRealimages(new AbstractSelector(RdBranchRealimage.class, conn)
				.loadRowsByParentId(branch.getPid(), isLock));

		for (IRow row : branch.getRealimages()) {
			RdBranchRealimage obj = (RdBranchRealimage) row;

			branch.realimageMap.put(obj.rowId(), obj);
		}

		branch.setSchematics(new AbstractSelector(RdBranchSchematic.class, conn)
				.loadRowsByParentId(branch.getPid(), isLock));

		for (IRow row : branch.getSchematics()) {
			RdBranchSchematic obj = (RdBranchSchematic) row;

			branch.schematicMap.put(obj.getPid(), obj);
		}

		RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

		branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), isLock));

		for (IRow row : branch.getVias()) {
			RdBranchVia obj = (RdBranchVia) row;

			branch.viaMap.put(obj.rowId(), obj);
		}
	}

	/**
	 * 根据经过线查询分歧
	 * 
	 * @param linkPid
	 *            进入线
	 * @param isLock
	 *            是否锁
	 * @return 经过线的分歧集
	 * @throws Exception
	 */
	public List<RdBranch> loadRdBranchByViaLinkPid(int linkPid, boolean isLock)
			throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "select a.* from rd_branch a,rd_branch_via b where a.branch_pid = b.branch_pid and b.link_pid = :1 and a.U_RECORD !=2 and b.U_RECORD !=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

	public List<RdBranch> loadByLinkPid(int linkPid, int linkType,
			boolean isLock) throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		String sql = "";

		if (linkType == 1) {

			sql = "SELECT * FROM RD_BRANCH WHERE IN_LINK_PID = :1 AND U_RECORD!=2 ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_BRANCH WHERE OUT_LINK_PID = :1 AND U_RECORD!=2 ";
		} else if (linkType == 3) {

			sql = "SELECT * FROM RD_BRANCH WHERE U_RECORD != 2 AND BRANCH_PID IN (SELECT DISTINCT (BRANCH_PID) FROM RD_BRANCH_VIA WHERE U_RECORD != 2 AND LINK_PID = :1)";
		} else {
			return branchs;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

	/**
	 * 根据路口pid查询分歧
	 * 
	 * @param crossPid
	 *            路口pid
	 * @param isLock
	 *            是否加锁
	 * @return 分歧集合
	 * @throws Exception
	 */
	public List<RdBranch> getRdBranchByCrossPid(int crossPid, boolean isLock)
			throws Exception {

		List<RdBranch> result = new ArrayList<RdBranch>();

		String sql = "select * from rd_branch a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, crossPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				branch.setDetails(new AbstractSelector(RdBranchDetail.class,
						getConn()).loadRowsByParentId(branch.getPid(), true));

				branch.setSignboards(new AbstractSelector(RdSignboard.class,
						getConn()).loadRowsByParentId(branch.getPid(), true));

				branch.setSignasreals(new AbstractSelector(RdSignasreal.class,
						getConn()).loadRowsByParentId(branch.getPid(), true));

				branch.setSeriesbranches(new AbstractSelector(
						RdSeriesbranch.class, getConn()).loadRowsByParentId(
						branch.getPid(), true));

				branch.setRealimages(new AbstractSelector(
						RdBranchRealimage.class, getConn()).loadRowsByParentId(
						branch.getPid(), true));

				branch.setSchematics(new AbstractSelector(
						RdBranchSchematic.class, getConn()).loadRowsByParentId(
						branch.getPid(), true));

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(
						getConn());

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(),
						true));

				result.add(branch);
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
	 * 根据link类型获取分歧
	 * 
	 * @param linkPids
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdBranch> loadByLinks(List<Integer> linkPids, int linkType,
			boolean isLock) throws Exception {

		List<RdBranch> rows = new ArrayList<RdBranch>();

		if (linkPids == null || linkPids.size() == 0) {
			return rows;
		}

		List<Integer> pidTemp = new ArrayList<Integer>();

		pidTemp.addAll(linkPids);

		int dataLimit = 100;

		while (pidTemp.size() >= dataLimit) {

			List<Integer> listPid = pidTemp.subList(0, dataLimit);

			rows.addAll(loadByLinkPids(listPid, linkType, isLock));

			pidTemp.subList(0, dataLimit).clear();
		}

		if (!pidTemp.isEmpty()) {
			rows.addAll(loadByLinkPids(pidTemp, linkType, isLock));
		}

		return rows;
	}

	public List<RdBranch> loadByLinkPids(List<Integer> linkPids, int linkType,
			boolean isLock) throws Exception {
		List<RdBranch> branchs = new ArrayList<RdBranch>();

		if (linkPids == null || linkPids.isEmpty()) {

			return branchs;
		}

		String pids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "";

		if (linkType == 1) {

			sql = "SELECT * FROM RD_BRANCH  WHERE U_RECORD !=2 AND IN_LINK_PID IN ("
					+ pids + ")  ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_BRANCH WHERE U_RECORD !=2 AND OUT_LINK_PID IN ("
					+ pids + ")  ";
			
		} else if (linkType == 3) {

			sql = "SELECT * FROM RD_BRANCH WHERE U_RECORD != 2 AND BRANCH_PID IN (SELECT DISTINCT (BRANCH_PID) FROM RD_BRANCH_VIA WHERE U_RECORD != 2 AND LINK_PID IN ("
					+ pids + "))";
		} else {
			return branchs;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				ReflectionAttrUtils.executeResultSet(branch, resultSet);

				setChild(branch, isLock);

				branchs.add(branch);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return branchs;
	}

}
