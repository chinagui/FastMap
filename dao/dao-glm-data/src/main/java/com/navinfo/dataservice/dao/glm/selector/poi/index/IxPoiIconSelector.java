package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiIcon;
import com.vividsolutions.jts.geom.Geometry;

/**
 * POI图标(3DICON)表 查询
 * @author luyao
 *
 */
public class IxPoiIconSelector implements ISelector {

	
	private static Logger logger = Logger.getLogger(IxPoiIconSelector.class);

	private Connection conn;

	public IxPoiIconSelector(Connection conn) {
		super();
		this.conn = conn;
	}
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiIcon icon = new IxPoiIcon();

		StringBuilder sb = new StringBuilder(
				 "select * from " + icon.tableName() + " WHERE rel_id = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				icon.setPid(id);

				icon.setPoiPid(resultSet.getInt("poi_pid"));

				icon.setIconName(resultSet.getString("icon_name"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				icon.setGeometry(geometry);

				icon.setManageCode(resultSet.getString("manage_code"));
				
				icon.setClientFlag(resultSet.getString("client_flag"));
				
				icon.setMemo(resultSet.getString("memo"));
				
				icon.setRowId(resultSet.getString("row_id"));

				return icon;
			} else {

				throw new Exception("对应IX_POI_ICON不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiIcon icon = new IxPoiIcon();

		String sql = "select * from " + icon.tableName() + " where row_id=hextoraw(:1)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				icon.setPid(resultSet.getInt("rel_id"));

				icon.setPoiPid(resultSet.getInt("poi_pid"));

				icon.setIconName(resultSet.getString("icon_name"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				icon.setGeometry(geometry);

				icon.setManageCode(resultSet.getString("manage_code"));
				
				icon.setClientFlag(resultSet.getString("client_flag"));
				
				icon.setMemo(resultSet.getString("memo"));
				
				icon.setRowId(resultSet.getString("row_id"));

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

		return icon;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_icon where poi_pid=:1 and u_record!=:2";

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

				IxPoiIcon icon = new IxPoiIcon();

				icon.setPid(resultSet.getInt("rel_id"));

				icon.setPoiPid(resultSet.getInt("poi_pid"));

				icon.setIconName(resultSet.getString("icon_name"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				icon.setGeometry(geometry);

				icon.setManageCode(resultSet.getString("manage_code"));
				
				icon.setClientFlag(resultSet.getString("client_flag"));
				
				icon.setMemo(resultSet.getString("memo"));
				
				icon.setRowId(resultSet.getString("row_id"));

				rows.add(icon);
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
