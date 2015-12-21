package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboard;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdSignboardName;

public class RdSignboardSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdSignboardSelector.class);

	private Connection conn;

	public RdSignboardSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdSignboard signboard = new RdSignboard();

		String sql = "select * from " + signboard.tableName()
				+ " where signboard_id=:1 and u_record!=2";

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

				signboard.setPid(resultSet.getInt("signboard_id"));

				signboard.setBranchPid(resultSet.getInt("branch_pid"));
				
				signboard.setArrowCode(resultSet.getString("arrow_code"));
				
				signboard.setBackimageCode(resultSet.getString("backimage_code"));

				signboard.setRowId(resultSet.getString("row_id"));

				RdSignboardNameSelector nameSelector = new RdSignboardNameSelector(
						conn);

				signboard.setNames(nameSelector.loadRowsByParentId(id, isLock));

				for(IRow row : signboard.getNames()){
					RdSignboardName name = (RdSignboardName)row;
					
					signboard.nameMap.put(name.getPid(), name);
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

		return signboard;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_signboard where branch_pid=:1 and u_record!=:2";

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

				RdSignboard signboard = new RdSignboard();

				signboard.setPid(resultSet.getInt("signboard_id"));

				signboard.setBranchPid(resultSet.getInt("branch_pid"));
				
				signboard.setArrowCode(resultSet.getString("arrow_code"));
				
				signboard.setBackimageCode(resultSet.getString("backimage_code"));

				signboard.setRowId(resultSet.getString("row_id"));

				RdSignboardNameSelector nameSelector = new RdSignboardNameSelector(
						conn);

				signboard.setNames(nameSelector.loadRowsByParentId(id, isLock));

				for(IRow row : signboard.getNames()){
					RdSignboardName name = (RdSignboardName)row;
					
					signboard.nameMap.put(name.getPid(), name);
				}

				rows.add(signboard);
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
