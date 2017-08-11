package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class AdAdminNameSelector extends AbstractSelector {
	private Connection conn;

	public AdAdminNameSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdAdminName.class);
	}

	/**
	 * 根据adadmin的region_id查询对应的AdadminName
	 * 
	 * @param id
	 *            代表的parentId对应的是adadmin的region_id
	 * @param langCode
	 *            CHI或者ENG
	 * @param nameClass
	 *            1代表标准化 2代表原始 3代表简称 4代表别名
	 * @param isLock
	 * @return List<IRow>
	 * @throws Exception
	 */
	public List<IRow> loadRowsByParentId(int id, String langCode,
			Integer nameClass, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		StringBuffer sqlBuf = new StringBuffer(
				"select * from ad_admin_name where region_id=:1 and u_record!=:2 ");

		if (langCode != null) {
			sqlBuf.append(" AND lang_code = :3 ");
		}

		if (nameClass != null) {
			sqlBuf.append(" AND name_class = :4 ");
		}
		if (isLock) {
			sqlBuf.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sqlBuf.toString());

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			if (langCode != null) {
				pstmt.setString(3, langCode);
			}
			if (nameClass != null) {
				// 非删除状态
				pstmt.setInt(4, nameClass);
			}
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdAdminName adAdminName = new AdAdminName();

				ReflectionAttrUtils.executeResultSet(adAdminName, resultSet);

				rows.add(adAdminName);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return rows;
	}

}
