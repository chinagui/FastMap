package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;

public class RwLinkNameSelector implements ISelector {
	
	private Connection conn;

	public RwLinkNameSelector(Connection conn) {
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
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select a.*,b.name from rw_link_name a,rd_name b where link_pid =:1 and a.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' and a.u_record!=2";

		/*if (isLock) {
			sql += " for update nowait";
		}*/

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RwLinkName rwLinkName = new RwLinkName();

				rwLinkName.setLinkPid(id);
				
				rwLinkName.setNameGroupid(resultSet.getInt("name_groupid"));

				rwLinkName.setRowId(resultSet.getString("row_id"));
				
				rwLinkName.setName(resultSet.getString("name"));

				rows.add(rwLinkName);
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
