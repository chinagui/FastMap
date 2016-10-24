package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildrenForAndroid;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * POI父子关系子表 查询
 * @author luyao
 *
 */
public class IxPoiChildrenSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(IxPoiChildrenSelector.class);

	private Connection conn;
	
	public IxPoiChildrenSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiChildren.class);
	}

	public List<IRow> loadRowsByPoiId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_children where child_poi_pid=:1 and u_record!=:2";

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

				IxPoiChildren poiChildren = new IxPoiChildren();
				
				ReflectionAttrUtils.executeResultSet(poiChildren, resultSet);

				rows.add(poiChildren);
			}
		} catch (Exception e) {
			
			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			
			DBUtils.closeStatement(pstmt);
		}

		return rows;
	}

	/**
	 * add by wangdongbin
	 * for android download
	 * @param id
	 * @return IxPoiAddress
	 * @throws Exception
	 */
	public List<IRow> loadByIdForAndroid(int id)throws Exception{
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiParent poiParent = new IxPoiParent();
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			//直接进行联结查询，对结果进行判断
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT c.child_poi_pid,c.relation_type,c.row_id,");
			sb.append("(select poi_num from ix_poi where pid=c.child_poi_pid) poi_num");
			sb.append(" FROM "+poiParent.tableName()+" p");
			sb.append(" ,ix_poi_children c");
			sb.append(" WHERE p.group_id=c.group_id");
			sb.append(" AND p.parent_poi_pid = :1");
			sb.append(" AND c.u_record !=2");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				IxPoiChildrenForAndroid poiCheildren = new IxPoiChildrenForAndroid();
				poiCheildren.setRelationType(resultSet.getInt("relation_type"));
				poiCheildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				poiCheildren.setPoiNum(resultSet.getString("poi_num"));
				poiCheildren.setRowId(resultSet.getString("row_id"));
				rows.add(poiCheildren);
			}
		}catch(Exception e){
			throw e;
		}finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}		
		return rows;
	}
	
	
}
