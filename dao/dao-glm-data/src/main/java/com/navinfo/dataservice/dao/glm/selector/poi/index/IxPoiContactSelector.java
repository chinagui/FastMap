package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * POI联系方式表查询selector
 * @author zhangxiaolong
 *
 */
public class IxPoiContactSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiContactSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiContact.class);
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

			DbUtils.closeQuietly(resultSet);

			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}
	
}
