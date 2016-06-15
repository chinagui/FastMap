package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;

/**
 * POI照片查询
 * @author luyao
 *
 */
public class IxPoiPhotoSelector implements ISelector {
	
	private static Logger logger = Logger.getLogger(IxPoiPhotoSelector.class);

	private Connection conn;

	public IxPoiPhotoSelector(Connection conn) {
		this.conn = conn;
	}
	
	
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiPhoto photo = new IxPoiPhoto();

		String sql = "select * from " + photo.tableName() + " where row_id=hextoraw(:1)";

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

				photo.setPoiPid(resultSet.getInt("poi_pid"));

				photo.setPhotoId(resultSet.getString("photo_id"));				
				
				photo.setStatus(resultSet.getString("status"));
					
				photo.setMemo(resultSet.getString("memo"));	

				photo.setRowId(resultSet.getString("row_id"));
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

		return photo;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_photo where poi_pid=:1 and u_record!=:2";

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

				IxPoiPhoto photo = new IxPoiPhoto();

				photo.setPoiPid(resultSet.getInt("poi_pid"));

				photo.setPhotoId(resultSet.getString("photo_id"));				
				
				photo.setStatus(resultSet.getString("status"));
					
				photo.setMemo(resultSet.getString("memo"));	

				photo.setRowId(resultSet.getString("row_id"));

				rows.add(photo);
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
