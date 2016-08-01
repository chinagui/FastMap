package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchRealimageSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSchematicSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchViaSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSeriesbranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSignasrealSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSignboardSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneTopologySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionDetailSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public void lockRdCross() throws Exception {
		// 获取该cross对象
		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		RdCross cross = (RdCross) selector.loadById(this.getCommand().getPid(), true);

		this.getCommand().setCross(cross);
	}

	public void lockRdRestriction() throws Exception {

		List<RdRestriction> result = new ArrayList<RdRestriction>();

		String sql = "select * from rd_restriction a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, this.getCommand().getPid());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));

				RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(getConn());

				restrict.setDetails(detail.loadRowsByParentId(restrict.getPid(), true));

				result.add(restrict);
			}

			this.getCommand().setRestricts(result);
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

	}

	public void lockRdLaneConnexity() throws Exception {
		List<RdLaneConnexity> result = new ArrayList<RdLaneConnexity>();

		String sql = "select * from rd_lane_connexity a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, this.getCommand().getPid());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLaneConnexity laneConn = new RdLaneConnexity();

				laneConn.setPid(resultSet.getInt("pid"));

				laneConn.setRowId(resultSet.getString("row_id"));

				laneConn.setInLinkPid(resultSet.getInt("in_link_pid"));

				laneConn.setNodePid(resultSet.getInt("node_pid"));

				laneConn.setLaneInfo(resultSet.getString("lane_info"));

				laneConn.setConflictFlag(resultSet.getInt("conflict_flag"));

				laneConn.setKgFlag(resultSet.getInt("kg_flag"));

				laneConn.setLaneNum(resultSet.getInt("lane_num"));

				laneConn.setLeftExtend(resultSet.getInt("left_extend"));

				laneConn.setRightExtend(resultSet.getInt("right_extend"));

				laneConn.setSrcFlag(resultSet.getInt("src_flag"));

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(getConn());

				laneConn.setTopos(topoSelector.loadRowsByParentId(laneConn.getPid(), true));

				result.add(laneConn);
			}

			this.getCommand().setLanes(result);
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
	}

	public void lockRdBranch() throws Exception {

		List<RdBranch> result = new ArrayList<RdBranch>();

		String sql = "select * from rd_branch a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, this.getCommand().getPid());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranch branch = new RdBranch();

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(getConn());

				branch.setDetails(detailSelector.loadRowsByParentId(branch.getPid(), true));

				RdSignboardSelector signboardSelector = new RdSignboardSelector(getConn());

				branch.setSignboards(signboardSelector.loadRowsByParentId(branch.getPid(), true));

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(getConn());

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(branch.getPid(), true));

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(getConn());

				branch.setSeriesbranches(seriesbranchSelector.loadRowsByParentId(branch.getPid(), true));

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(getConn());

				branch.setRealimages(realimageSelector.loadRowsByParentId(branch.getPid(), true));

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(getConn());

				branch.setSchematics(schematicSelector.loadRowsByParentId(branch.getPid(), true));

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(getConn());

				branch.setVias(viaSelector.loadRowsByParentId(branch.getPid(), true));

			}

			this.getCommand().setBranches(result);
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

	}

	@Override
	public boolean prepareData() throws Exception {

		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		lockRdCross();

		if (this.getCommand().getCross() == null) {

			throw new Exception("指定删除的路口不存在！");
		}

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		IOperation op = new OpTopo(this.getCommand(), getConn());

		op.run(this.getResult());

		IOperation opRefRestrict = new OpRefRdRestriction(this.getCommand());

		opRefRestrict.run(this.getResult());

		IOperation opRefLaneConnexity = new OpRefRdLaneConnexity(this.getCommand());

		opRefLaneConnexity.run(this.getResult());

		IOperation opRefBranch = new OpRefRdBranch(this.getCommand());

		return opRefBranch.run(this.getResult());

	}

}
