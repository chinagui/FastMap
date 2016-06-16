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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEntryimage;

/**
 * POI入口概略图表 selector
 * @author zhangxiaolong
 *
 */
public class IxPoiEntryImageSelector implements ISelector {
	
	private Connection conn;

	public IxPoiEntryImageSelector(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiEntryimage ixPoiEntryimage = new IxPoiEntryimage();

		String sql = "select * from " + ixPoiEntryimage.tableName() + " where row_id=hextoraw(:1) and u_record!=2";

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
				setAttr(ixPoiEntryimage, resultSet);
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
		return ixPoiEntryimage;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_entryimage where poi_pid=:1 and u_record!=:2";

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

				IxPoiEntryimage ixPoiEntryimage = new IxPoiEntryimage();

				setAttr(ixPoiEntryimage, resultSet);

				rows.add(ixPoiEntryimage);
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
	
	private void setAttr(IxPoiEntryimage ixPoiEntryimage,ResultSet resultSet) throws SQLException
	{
		ixPoiEntryimage.setPoiPid(resultSet.getInt("poi_pid"));
		
		ixPoiEntryimage.setImageCode(resultSet.getString("image_code"));
		
		ixPoiEntryimage.setxPixelR4(resultSet.getInt("x_pixel_r4"));
		
		ixPoiEntryimage.setyPixelR4(resultSet.getInt("y_pixel_r4"));
		
		ixPoiEntryimage.setxPixelR5(resultSet.getInt("x_pixel_r5"));
		
		ixPoiEntryimage.setyPixelR5(resultSet.getInt("y_pixel_r5"));
		
		ixPoiEntryimage.setxPixel35(resultSet.getInt("x_pixel_35"));
		
		ixPoiEntryimage.setyPixel35(resultSet.getInt("y_pixel_35"));
		
		ixPoiEntryimage.setMemo(resultSet.getString("memo"));
		
		ixPoiEntryimage.setMainPoiPid(resultSet.getInt("main_poi_pid"));
		
		ixPoiEntryimage.setRowId(resultSet.getString("row_id"));
		
		ixPoiEntryimage.setuDate(resultSet.getString("u_date"));
	}
}
