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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiNameTone;

/**
 * POI名称表selector
 * @author zhangxiaolong
 *
 */
public class IxPoiNameSelector implements ISelector {
	
	private Connection conn;

	public IxPoiNameSelector(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoiName name = new IxPoiName();

		String sql = "select * from "+name.tableName()+" where name_id=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				//设置主表信息
				setAttr(name, resultSet);
				
				//设置子表IxPoiNameTone
				IxPoiNameToneSelector ixPoiNameToneSelector = new IxPoiNameToneSelector(
						conn);

				name.setNameTones(ixPoiNameToneSelector.loadRowsByParentId(id, isLock));

				for (IRow row : name.getNameTones()) {
					
					IxPoiNameTone obj = (IxPoiNameTone) row;

					name.nameToneMap.put(obj.getRowId(), obj);
				}
				
				//设置子表IxPoiNameFlag
				IxPoiNameFlagSelector ixPoiNameFlagSelector = new IxPoiNameFlagSelector(
						conn);

				name.setNameFlags(ixPoiNameFlagSelector.loadRowsByParentId(id, isLock));

				for (IRow row : name.getNameFlags()) {
					
					IxPoiNameFlag obj = (IxPoiNameFlag) row;

					name.nameFlagMap.put(obj.getRowId(), obj);
				}
				
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

		return name;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		IxPoiName ixPoiName = new IxPoiName();

		String sql = "select * from " + ixPoiName.tableName() + " where row_id=hextoraw(:1) and u_record!=2";

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
				setAttr(ixPoiName, resultSet);
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
		return ixPoiName;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_name where poi_pid=:1 and u_record!=:2";

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

				IxPoiName ixPoiName = new IxPoiName();

				//设置主表name属性
				setAttr(ixPoiName, resultSet);
				
				// 设置子表ix_poi_name_tone
				IxPoiNameToneSelector poiNameToneSelector = new IxPoiNameToneSelector(conn);

				ixPoiName.setNameTones(poiNameToneSelector.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoiName.getNameTones()) {

					IxPoiNameTone obj = (IxPoiNameTone) row;

					ixPoiName.nameToneMap.put(obj.getRowId(), obj);
				}
				
				// 设置子表ix_poi_name_flag
				IxPoiNameFlagSelector poiNameFlagSelector = new IxPoiNameFlagSelector(conn);

				ixPoiName.setNameFlags(poiNameFlagSelector.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoiName.getNameFlags()) {

					IxPoiNameFlag obj = (IxPoiNameFlag) row;

					ixPoiName.nameFlagMap.put(obj.getRowId(), obj);
				}
				
				
				rows.add(ixPoiName);
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
	 * 设置主表属性值
	 * @param obj 主表对象
	 * @param resultSet 结果集
	 * @throws SQLException
	 */
	private void setAttr(IxPoiName obj,ResultSet resultSet) throws SQLException
	{
		obj.setPid(resultSet.getInt("name_id"));
		
		obj.setPoiPid(resultSet.getInt("poi_pid"));

		obj.setNameGroupid(resultSet.getInt("name_groupid"));
		
		obj.setNameClass(resultSet.getInt("name_class"));
		
		obj.setNameType(resultSet.getInt("name_type"));

		obj.setLangCode(resultSet.getString("lang_code"));
		
		obj.setName(resultSet.getString("name"));
		
		obj.setNamePhonetic(resultSet.getString("name_phonetic"));
		
		obj.setKeywords(resultSet.getString("keywords"));
		
		obj.setNidePid(resultSet.getString("nidb_pid"));

		obj.setRowId(resultSet.getString("row_id"));
		
		obj.setuDate(resultSet.getString("u_date"));
	}
	
	/**
	 * add by wangdongbin
	 * for android download
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public List<IRow> loadByIdForAndroid(int id) throws Exception{
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiName ixPoiName = new IxPoiName();
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			String sql = "SELECT name FROM " + ixPoiName.tableName() + " where poi_pid=:1 AND name_class=1 AND name_type=2 AND lang_code='CHI' AND u_record!=2";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()){
				ixPoiName.setName(resultSet.getString("name"));
			}
			rows.add(ixPoiName);
			return rows;
		} catch (Exception e) {
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
		
	}
	
}
