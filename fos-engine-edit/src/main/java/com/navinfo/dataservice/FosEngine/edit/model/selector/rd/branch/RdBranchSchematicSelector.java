package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdBranchSchematicSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdBranchSchematicSelector.class);

	private Connection conn;

	public RdBranchSchematicSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdBranchSchematic schematic = new RdBranchSchematic();

		String sql = "select * from " + schematic.tableName() +" where schematic_id=:1";

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

				schematic.setPid(resultSet.getInt("schematic_id"));
				
				schematic.setBranchPid(resultSet.getInt("branch_pid"));
				
				schematic.setSchematicCode(resultSet.getString("schematic_code"));
				
				schematic.setArrowCode(resultSet.getString("arrow_code"));
				
				schematic.setMemo(resultSet.getString("memo"));

				schematic.setRowId(resultSet.getString("row_id"));
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

		return schematic;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_branch_schematic where branch_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranchSchematic schematic = new RdBranchSchematic();

				schematic.setPid(resultSet.getInt("schematic_id"));
				
				schematic.setBranchPid(resultSet.getInt("branch_pid"));
				
				schematic.setSchematicCode(resultSet.getString("schematic_code"));
				
				schematic.setArrowCode(resultSet.getString("arrow_code"));
				
				schematic.setMemo(resultSet.getString("memo"));

				schematic.setRowId(resultSet.getString("row_id"));

				rows.add(schematic);
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

}
