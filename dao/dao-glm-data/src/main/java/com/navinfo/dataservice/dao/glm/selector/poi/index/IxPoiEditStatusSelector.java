package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEditStatus;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class IxPoiEditStatusSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiEditStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiEditStatus.class);
	}

	public int loadStatusByRowId(String rowId, boolean isLock) throws Exception {
		int status = 0;

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
			} 
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
		return status;
	}
}
