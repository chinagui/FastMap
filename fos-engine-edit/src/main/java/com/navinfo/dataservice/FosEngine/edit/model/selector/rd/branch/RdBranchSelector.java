package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignasreal;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboard;

public class RdBranchSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdBranchSelector.class);

	private Connection conn;

	public RdBranchSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranch branch = new RdBranch();

		String sql = "select * from " + branch.tableName()
				+ " where branch_pid=:1 and u_record!=2";

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

				branch.setPid(resultSet.getInt("branch_pid"));

				branch.setInLinkPid(resultSet.getInt("in_link_pid"));

				branch.setNodePid(resultSet.getInt("node_pid"));

				branch.setOutLinkPid(resultSet.getInt("out_link_pid"));

				branch.setRelationshipType(resultSet
						.getInt("relationship_type"));

				branch.setRowId(resultSet.getString("row_id"));

				RdBranchDetailSelector detailSelector = new RdBranchDetailSelector(
						conn);

				branch.setDetails(detailSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getDetails()) {
					RdBranchDetail obj = (RdBranchDetail) row;

					branch.detailMap.put(obj.getPid(), obj);
				}

				RdSignboardSelector signboardSelector = new RdSignboardSelector(
						conn);

				branch.setSignboards(signboardSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignboards()) {
					RdSignboard obj = (RdSignboard) row;

					branch.signboardMap.put(obj.getPid(), obj);
				}

				RdSignasrealSelector signasrealSelector = new RdSignasrealSelector(
						conn);

				branch.setSignasreals(signasrealSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSignasreals()) {
					RdSignasreal obj = (RdSignasreal) row;

					branch.signasrealMap.put(obj.getPid(), obj);
				}

				RdSeriesbranchSelector seriesbranchSelector = new RdSeriesbranchSelector(
						conn);

				branch.setSeriesbranches(seriesbranchSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getSeriesbranches()) {
					RdSeriesbranch obj = (RdSeriesbranch) row;

					branch.seriesbranchMap.put(obj.rowId(), obj);
				}

				RdBranchRealimageSelector realimageSelector = new RdBranchRealimageSelector(
						conn);

				branch.setRealimages(realimageSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getRealimages()) {
					RdBranchRealimage obj = (RdBranchRealimage) row;

					branch.realimageMap.put(obj.rowId(), obj);
				}

				RdBranchSchematicSelector schematicSelector = new RdBranchSchematicSelector(
						conn);

				branch.setSchematics(schematicSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : branch.getSchematics()) {
					RdBranchSchematic obj = (RdBranchSchematic) row;

					branch.schematicMap.put(obj.getPid(), obj);
				}

				RdBranchViaSelector viaSelector = new RdBranchViaSelector(conn);

				branch.setVias(viaSelector.loadRowsByParentId(id, isLock));

				for (IRow row : branch.getVias()) {
					RdBranchVia obj = (RdBranchVia) row;

					branch.viaMap.put(obj.rowId(), obj);
				}
			} else {

				throw new DataNotFoundException(null);
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

		return branch;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}

}
