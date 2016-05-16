package com.navinfo.dataservice.dao.glm.selector.rd.link;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;

public class RdLinkFormSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkFormSelector.class);

	private Connection conn;

	public RdLinkFormSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		StringBuilder sb = new StringBuilder(
				"select * from rd_link_form where link_pid =:1 and u_record != 2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<IRow> list = new ArrayList<IRow>();

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLinkForm form = new RdLinkForm();

				form.setFormOfWay(resultSet.getInt("form_of_way"));

				form.setLinkPid(id);
				
				form.setAuxiFlag(resultSet.getInt("auxi_flag"));
				
				form.setExtendedForm(resultSet.getInt("extended_form"));
				
				form.setKgFlag(resultSet.getInt("kg_flag"));

				form.setRowId(resultSet.getString("row_id"));

				list.add(form);
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

		return list;
	}
}
