package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;

public class AdAdminNameSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdAdminNameSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdAdminName adAdminName = new AdAdminName();

		String sql = "select * from " + adAdminName.tableName() + " where name_id =:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				adAdminName.setPid(resultSet.getInt("name_id"));

				adAdminName.setNameGroupId(resultSet.getInt("name_groupid"));

				adAdminName.setRegionId(resultSet.getInt("region_id"));

				adAdminName.setLangCode(resultSet.getString("lang_code"));

				adAdminName.setNameClass(resultSet.getInt("name_class"));

				adAdminName.setName(resultSet.getString("name"));

				adAdminName.setPhonetic(resultSet.getString("phonetic"));

				adAdminName.setSrcFlag(resultSet.getInt("src_flag"));

				adAdminName.setRowId(resultSet.getString("row_id"));

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

		return adAdminName;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	/**
	 * id:代表的parentId对应的是adadmin的region_id
	 */
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ad_admin_name where region_id=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			// 非删除状态
			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdAdminName adAdminName = new AdAdminName();

				adAdminName.setPid(resultSet.getInt("name_id"));

				adAdminName.setNameGroupId(resultSet.getInt("name_groupid"));

				adAdminName.setRegionId(resultSet.getInt("region_id"));

				adAdminName.setLangCode(resultSet.getString("lang_code"));

				adAdminName.setNameClass(resultSet.getInt("name_class"));

				adAdminName.setName(resultSet.getString("name"));

				adAdminName.setPhonetic(resultSet.getString("phonetic"));

				adAdminName.setSrcFlag(resultSet.getInt("src_flag"));

				adAdminName.setRowId(resultSet.getString("row_id"));

				rows.add(adAdminName);
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

	/**
	 * 根据adadmin的region_id查询对应的AdadminName
	 * @param id 代表的parentId对应的是adadmin的region_id
	 * @param langCode CHI或者ENG
	 * @param nameClass 1代表标准化 2代表原始 3代表简称 4代表别名
	 * @param isLock
	 * @return List<IRow>
	 * @throws Exception
	 */
	public List<IRow> loadRowsByParentId(int id, String langCode, Integer nameClass, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		StringBuffer sqlBuf = new StringBuffer("select * from ad_admin_name where region_id=:1 and u_record!=:2 ");

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

			pstmt.setString(3, langCode);

			// 非删除状态
			pstmt.setInt(4, nameClass);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdAdminName adAdminName = new AdAdminName();

				adAdminName.setPid(resultSet.getInt("name_id"));

				adAdminName.setNameGroupId(resultSet.getInt("name_groupid"));

				adAdminName.setRegionId(resultSet.getInt("region_id"));

				adAdminName.setLangCode(resultSet.getString("lang_code"));

				adAdminName.setNameClass(resultSet.getInt("name_class"));

				adAdminName.setName(resultSet.getString("name"));

				adAdminName.setPhonetic(resultSet.getString("phonetic"));

				adAdminName.setSrcFlag(resultSet.getInt("src_flag"));

				adAdminName.setRowId(resultSet.getString("row_id"));

				rows.add(adAdminName);
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
