package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/** 
* @ClassName: IxPoiDeepStatusSelector 
* @author: zhangpengpeng 
* @date: 2016年11月16日
* @Desc: 深度信息状态表查询类
*/
public class IxPoiDeepStatusSelector extends AbstractSelector{
	private Connection conn;
	
	public IxPoiDeepStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	/**
	 * 查询 作业员名下 已申请未提交的数据量
	 * @param userId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public int queryHandlerCount(long userId, int type) throws Exception {
		int count = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(1) num");
		sb.append(" FROM poi_deep_status s");
		sb.append(" WHERE s.handler=:1");
		sb.append(" AND s.TYPE=:2");
		sb.append(" AND s.STATUS != 3");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, userId);
			pstmt.setInt(2, type);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()){
				count = resultSet.getInt("num");
			}
			
			return count;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	

	/**
	 * 根据subtask获取 可申请的数据rowIds
	 * @param subtask
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIds(Subtask subtask, int type) throws Exception {
		List<String> rowIds = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.row_id ");
		sb.append(" FROM IX_POI p,POI_DEEP_STATUS s ");
		sb.append(" WHERE sdo_within_distance(p.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE'");
		sb.append(" AND s.TYPE=:2");
		sb.append(" AND s.handler is null");
		sb.append(" AND s.STATUS = 1");
		sb.append(" AND p.row_id = s.row_id");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, subtask.getGeometry());
			pstmt.setInt(2, type);
			
			resultSet = pstmt.executeQuery();
			int count = 0;
			//获取100条rowId
			while (resultSet.next()) {
				rowIds.add(resultSet.getString("row_id"));
				count++;
				if (count == 100){
					break;
				}
			}
			
			return rowIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
}
