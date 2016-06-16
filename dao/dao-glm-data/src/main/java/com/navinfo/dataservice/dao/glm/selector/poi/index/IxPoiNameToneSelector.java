package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameTone;

/**
 * POI名称语音语调表
 * @author zhangxiaolong
 *
 */
public class IxPoiNameToneSelector implements ISelector {

	private Connection conn;

	public IxPoiNameToneSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiNameTone ixPoiNameTone = new IxPoiNameTone();

		String sql = "select * from " + ixPoiNameTone.tableName() + " where row_id=hextoraw(:1) and u_record!=2";

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

				ixPoiNameTone.setNameId(resultSet.getInt("name_id"));

				ixPoiNameTone.setToneA(resultSet.getString("tone_a"));

				ixPoiNameTone.setToneB(resultSet.getString("tone_b"));

				ixPoiNameTone.setLhA(resultSet.getString("lh_a"));

				ixPoiNameTone.setLhB(resultSet.getString("lh_b"));

				ixPoiNameTone.setJyutp(resultSet.getString("jyutp"));

				ixPoiNameTone.setMemo(resultSet.getString("memo"));

				ixPoiNameTone.setRowId(resultSet.getString("row_id"));
				
				ixPoiNameTone.setuDate(resultSet.getString("u_date"));
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
		return ixPoiNameTone;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_name_tone where name_id=:1 and u_record!=:2";

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

				IxPoiNameTone ixPoiNameTone = new IxPoiNameTone();

				ixPoiNameTone.setNameId(resultSet.getInt("name_id"));

				ixPoiNameTone.setToneA(resultSet.getString("tone_a"));

				ixPoiNameTone.setToneB(resultSet.getString("tone_b"));

				ixPoiNameTone.setLhA(resultSet.getString("lh_a"));

				ixPoiNameTone.setLhB(resultSet.getString("lh_b"));

				ixPoiNameTone.setJyutp(resultSet.getString("jyutp"));

				ixPoiNameTone.setMemo(resultSet.getString("memo"));

				ixPoiNameTone.setRowId(resultSet.getString("row_id"));
				
				ixPoiNameTone.setuDate(resultSet.getString("u_date"));

				rows.add(ixPoiNameTone);
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
