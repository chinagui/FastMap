package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;

/**
 * POI联系方式表查询selector
 * @author zhangxiaolong
 *
 */
public class IxPoiContactSelector implements ISelector {

	private Connection conn;

	public IxPoiContactSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiContact ixPoiContact = new IxPoiContact();

		String sql = "select * from " + ixPoiContact.tableName() + " where row_id=hextoraw(:1) and u_record!=2";

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
				setAttr(ixPoiContact, resultSet);
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
		return ixPoiContact;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_contact where poi_pid=:1 and u_record!=:2";

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

				IxPoiContact ixPoiContact = new IxPoiContact();

				setAttr(ixPoiContact, resultSet);

				rows.add(ixPoiContact);
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

	private void setAttr(IxPoiContact ixPoiContact,ResultSet resultSet) throws SQLException
	{
		ixPoiContact.setPoiPid(resultSet.getInt("poi_pid"));

		ixPoiContact.setContactType(resultSet.getInt("contact_type"));

		ixPoiContact.setContact(resultSet.getString("contact"));

		ixPoiContact.setContactDepart(resultSet.getInt("contact_depart"));

		ixPoiContact.setPriority(resultSet.getInt("priority"));

		ixPoiContact.setRowId(resultSet.getString("row_id"));
		
		ixPoiContact.setuDate(resultSet.getString("u_date"));
	}
	
	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select CONTACT_TYPE,CONTACT,CONTACT_DEPART,PRIORITY,ROW_ID from ix_poi_contact where poi_pid=:1 and u_record!=:2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoiContact ixPoiContact = new IxPoiContact();

				ixPoiContact.setContactType(resultSet.getInt("contact_type"));

				ixPoiContact.setContact(resultSet.getString("contact"));

				ixPoiContact.setContactDepart(resultSet.getInt("contact_depart"));

				ixPoiContact.setPriority(resultSet.getInt("priority"));

				ixPoiContact.setRowId(resultSet.getString("row_id"));

				rows.add(ixPoiContact);
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
