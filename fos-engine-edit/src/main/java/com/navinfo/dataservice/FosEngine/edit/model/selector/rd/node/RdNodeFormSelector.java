package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdNodeFormSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdNodeFormSelector.class);

	private Connection conn;

	public RdNodeFormSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdNodeForm form = new RdNodeForm();

		String sql = "select * from " + form.tableName() + " where row_id=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				form.setNodePid(resultSet.getInt("node_pid"));

				form.setFormOfWay(resultSet.getInt("form_of_way"));

				form.setAuxiFlag(resultSet.getInt("auxi_flag"));

				form.setRowId(resultSet.getString("row_id"));
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

		return form;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_node_form where node_pid=:1 and u_record!=:2";

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

				RdNodeForm form = new RdNodeForm();

				form.setNodePid(resultSet.getInt("node_pid"));

				form.setFormOfWay(resultSet.getInt("form_of_way"));

				form.setAuxiFlag(resultSet.getInt("auxi_flag"));

				form.setRowId(resultSet.getString("row_id"));

				rows.add(form);
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
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

}
