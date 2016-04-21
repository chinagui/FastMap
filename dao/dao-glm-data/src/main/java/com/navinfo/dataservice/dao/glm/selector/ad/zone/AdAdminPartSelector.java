package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;

/**
 * @Title: AdAdminPartSelector.java
 * @Description: AdAdminPart的查询类
 * @author 张小龙
 * @date 2016年4月18日 下午5:30:06
 * @version V1.0
 */
public class AdAdminPartSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdAdminPartSelector(Connection conn) {
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

		String sql = "select * from ad_admin_part where group_id=:1 and u_record!=:2";

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

				AdAdminPart part = new AdAdminPart();

				part.setGroupId(resultSet.getInt("group_id"));

				part.setRegionIdDown(resultSet.getInt("region_id_down"));

				part.setRowId(resultSet.getString("row_id"));

				rows.add(part);
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
