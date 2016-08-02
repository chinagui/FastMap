package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEditStatus;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class IxPoiEditStatusSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiEditStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiEditStatus.class);
	}

	public int loadStatusByRowId(String rowId, boolean isLock) throws Exception {
		int status = -1;

		String sql = "select status from poi_edit_status where row_id=hextoraw(:1)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				status = resultSet.getInt("status");
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
		return status;
	}
}
