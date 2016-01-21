package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchName;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdBranchDetailSelector implements ISelector {


	private Connection conn;

	public RdBranchDetailSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranchDetail detail = new RdBranchDetail();

//		String sql = "select * from " + detail.tableName()
//				+ " where detail_id=:1 and u_record!=2";
		
		String sql = "select a.*,c.mesh_id from " + detail.tableName()
				+ " a,rd_branch b,rd_link c where a.detail_id=:1 and a.u_record!=2 and a.branch_pid = b.branch_pid and b.in_link_pid = c.link_pid";

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

				detail.setPid(resultSet.getInt("detail_id"));

				detail.setBranchPid(resultSet.getInt("branch_pid"));

				detail.setVoiceDir(resultSet.getInt("voice_dir"));

				detail.setEstabType(resultSet.getInt("estab_type"));

				detail.setNameKind(resultSet.getInt("name_kind"));

				detail.setExitNum(resultSet.getString("exit_num"));

				detail.setBranchType(resultSet.getInt("branch_type"));

				detail.setPatternCode(resultSet.getString("pattern_code"));

				detail.setArrowCode(resultSet.getString("arrow_code"));

				detail.setArrowFlag(resultSet.getInt("arrow_flag"));

				detail.setGuideCode(resultSet.getInt("guide_code"));

				detail.setRowId(resultSet.getString("row_id"));
				
				detail.setMesh(resultSet.getInt("mesh_id"));

				RdBranchNameSelector nameSelector = new RdBranchNameSelector(
						conn);

				detail.setNames(nameSelector.loadRowsByParentId(id, isLock));

				for (IRow row : detail.getNames()) {
					RdBranchName name = (RdBranchName) row;
					
					name.setMesh(detail.mesh());

					detail.nameMap.put(name.getPid(), name);
				}
			} else {

				throw new DataNotFoundException("数据不存在");
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

		return detail;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_branch_detail where branch_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranchDetail detail = new RdBranchDetail();

				detail.setPid(resultSet.getInt("detail_id"));

				detail.setBranchPid(resultSet.getInt("branch_pid"));

				detail.setVoiceDir(resultSet.getInt("voice_dir"));

				detail.setEstabType(resultSet.getInt("estab_type"));

				detail.setNameKind(resultSet.getInt("name_kind"));

				detail.setExitNum(resultSet.getString("exit_num"));

				detail.setBranchType(resultSet.getInt("branch_type"));

				detail.setPatternCode(resultSet.getString("pattern_code"));

				detail.setArrowCode(resultSet.getString("arrow_code"));

				detail.setArrowFlag(resultSet.getInt("arrow_flag"));

				detail.setGuideCode(resultSet.getInt("guide_code"));

				detail.setRowId(resultSet.getString("row_id"));

				RdBranchNameSelector nameSelector = new RdBranchNameSelector(
						conn);

				detail.setNames(nameSelector.loadRowsByParentId(detail.getPid(), isLock));

				for (IRow row : detail.getNames()) {
					RdBranchName name = (RdBranchName) row;

					detail.nameMap.put(name.getPid(), name);
				}

				rows.add(detail);
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

		return rows;
	}
	
	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}
}
