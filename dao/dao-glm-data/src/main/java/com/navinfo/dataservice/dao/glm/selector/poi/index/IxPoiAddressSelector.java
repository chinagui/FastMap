package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * POI地址表selector
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiAddressSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiAddressSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiAddress.class);
	}

	/**
	 * add by wangdongbin for android download
	 * 
	 * @param id
	 * @return IxPoiAddress
	 * @throws Exception
	 */
	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiAddress ixPoiAddress = new IxPoiAddress();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			String sql = "SELECT fullname,floor FROM "
					+ ixPoiAddress.tableName()
					+ " where poi_pid=:1 AND name_groupid=1 AND lang_code='CHI' AND u_record!=2";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				ixPoiAddress.setFullname(resultSet.getString("fullname"));
				ixPoiAddress.setFloor(resultSet.getString("floor"));
			}
			rows.add(ixPoiAddress);
			return rows;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

	}
}
