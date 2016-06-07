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

/**
 * POI音频查询
 * @author luyao
 *
 */
public class IxPoiAudioSelector implements ISelector {

	private static Logger logger = Logger.getLogger(IxPoiAudioSelector.class);

	private Connection conn;
	
	public IxPoiAudioSelector(Connection conn) {
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
		IxPoiAudio audio = new IxPoiAudio();

		String sql = "select * from " + audio.tableName() + " where row_id=hextoraw(:1)";

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

				audio.setPoiPid(resultSet.getInt("poi_pid"));

				audio.setAudioId(resultSet.getInt("audio_id"));
				
				audio.setStatus(resultSet.getString("status"));

				audio.setMemo(resultSet.getString("memo"));

				audio.setRowId(resultSet.getString("row_id"));
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

		return audio;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_audio where poi_pid=:1 and u_record!=:2";

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

				IxPoiAudio audio = new IxPoiAudio();

				audio.setPoiPid(resultSet.getInt("poi_pid"));

				audio.setAudioId(resultSet.getInt("audio_id"));
				
				audio.setStatus(resultSet.getString("status"));

				audio.setMemo(resultSet.getString("memo"));

				audio.setRowId(resultSet.getString("row_id"));

				rows.add(audio);
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
