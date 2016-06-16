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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAudio;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiOperateRef;

/**
 * POI音频查询
 * @author luyao
 *
 */
public class IxPoiOperateRefSelector implements ISelector {

	private static Logger logger = Logger.getLogger(IxPoiOperateRefSelector.class);

	private Connection conn;
	
	public IxPoiOperateRefSelector(Connection conn) {
		super();
		this.conn = conn;
	}
	
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiOperateRef ixPoiOperateRef = new IxPoiOperateRef();

		String sql = "select * from " + ixPoiOperateRef.tableName() + " where row_id=hextoraw(:1)";

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

				ixPoiOperateRef.setPoiPid(resultSet.getInt("poi_pid"));
				ixPoiOperateRef.setFreshVerified(resultSet.getInt("fresh_verified"));
				ixPoiOperateRef.setRawFileds(resultSet.getString(""));
				ixPoiOperateRef.setRowId(resultSet.getString("raw_fileds"));
				ixPoiOperateRef.setuDate(resultSet.getString("u_date"));
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

		return ixPoiOperateRef;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_operate_ref where poi_pid=:1 and u_record!=:2";

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

				IxPoiOperateRef ixPoiOperateRef = new IxPoiOperateRef();

				ixPoiOperateRef.setPoiPid(resultSet.getInt("poi_pid"));
				ixPoiOperateRef.setFreshVerified(resultSet.getInt("fresh_verified"));
				ixPoiOperateRef.setRawFileds(resultSet.getString("raw_fileds"));
				ixPoiOperateRef.setRowId(resultSet.getString("row_id"));
				ixPoiOperateRef.setuDate(resultSet.getString("u_date"));
				rows.add(ixPoiOperateRef);
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
