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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildrenForAndroid;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;

/**
 * POI父子关系子表 查询
 * @author luyao
 *
 */
public class IxPoiChildrenSelector implements ISelector {

	private static Logger logger = Logger.getLogger(IxPoiChildrenSelector.class);

	private Connection conn;
	
	public IxPoiChildrenSelector(Connection conn) {
		super();
		this.conn = conn;
	}
	
	
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		
		IxPoiChildren poiChildren = new IxPoiChildren();

		String sql = "select * from " + poiChildren.tableName() + " where row_id=hextoraw(:1)";

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

				poiChildren.setGroupId (resultSet.getInt("group_id"));

				poiChildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				
				poiChildren.setRelationType(resultSet.getInt("relation_type"));

				poiChildren.setRowId(resultSet.getString("row_id"));
				
				poiChildren.setuDate(resultSet.getString("u_date"));
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

		return poiChildren;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_children where group_id=:1 and u_record!=:2";

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
				
				poiChildren.setGroupId (resultSet.getInt("group_id"));

				poiChildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				
				poiChildren.setRelationType(resultSet.getInt("relation_type"));

				poiChildren.setRowId(resultSet.getString("row_id"));

				rows.add(poiChildren);
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
				
				poiChildren.setGroupId (resultSet.getInt("group_id"));

				poiChildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				
				poiChildren.setRelationType(resultSet.getInt("relation_type"));

				poiChildren.setRowId(resultSet.getString("row_id"));

				rows.add(poiChildren);
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
		IxPoiChildrenForAndroid poiCheildre = new IxPoiChildrenForAndroid();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			//直接进行联结查询，对结果进行判断
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT c.child_poi_pid,c.relation_type,c.row_id,");
			sb.append("(select poi_num from ix_poi where pid=c.child_poi_pid) poi_num");
			sb.append(" FROM "+poiParent.tableName()+" p");
			sb.append(" ,"+poiCheildre.tableName()+" c");
			sb.append(" WHERE p.group_id=c.group_id");
			sb.append(" AND p.parent_poi_pid = :1");
			sb.append(" AND c.u_record !=2");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				poiCheildre.setRelationType(resultSet.getInt("relation_type"));
				poiCheildre.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				poiCheildre.setPoiNum(resultSet.getString("poi_num"));
				poiCheildre.setRowId(resultSet.getString("row_id"));
				rows.add(poiCheildre);
			}
		}catch(Exception e){
			throw e;
		}finally {
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
