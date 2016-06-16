package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiEvent;
/**
 * 索引:POI 深度信息(重大事件类) 查询接口
 * @author zhaokk
 *
 */
public class IxPoiEventSelector implements ISelector {
	private Connection conn;

	public IxPoiEventSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiEvent  poiEvent = new IxPoiEvent();

		StringBuilder sb = new StringBuilder(
				"select * from " + poiEvent.tableName() + " WHERE event_id  = :1 and  u_record !=2");

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
				this.setAttr(poiEvent, resultSet);
				return poiEvent;
			} else {
				throw new Exception("对"+poiEvent.tableName()+"数据不存在不存在!");
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}
 
	private void setAttr(IxPoiEvent event,ResultSet resultSet) throws SQLException{
		event.setPid(resultSet.getInt("event_id"));
		event.setEventName(resultSet.getString("event_name"));
		event.setEventNameEng(resultSet.getString("event_name_eng"));
		event.setEventKind(resultSet.getString("event_kind"));
		event.setEventKindEng(resultSet.getString("event_kind_eng"));
		event.setEventDesc(resultSet.getString("event_desc"));
		event.setEventDescEng(resultSet.getString("event_desc_eng"));
		event.setStartDate(resultSet.getString("start_date"));
		event.setEndDate(resultSet.getString("end_date"));
		event.setDetailTime(resultSet.getString("detail_time"));
		event.setDetailTimeEng(resultSet.getString("detail_time_eng"));
		event.setCity(resultSet.getString("city"));
		event.setPoiPid(resultSet.getString("poi_pid"));
		event.setPhotoName(resultSet.getString("photo_name"));
		event.setReserved(resultSet.getString("reserved"));
		event.setMemo(resultSet.getString("memo"));
		event.setRowId(resultSet.getString("row_id"));
		event.setuDate(resultSet.getString("u_date"));

	}
	
}
