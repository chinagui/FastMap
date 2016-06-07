package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * POI基础信息表 selector
 * 
 * @author zhangxiaolong
 *
 */
public class IxPoiSelector implements ISelector{
	
	private Connection conn;

	public IxPoiSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoi ixPoi = new IxPoi();

		StringBuilder sb = new StringBuilder(
				 "select * from " + ixPoi.tableName() + " WHERE pid = :1 and  u_record !=2");

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
				ixPoi.setPid(id);

				ixPoi.setKindCode(resultSet.getString("kind_code"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				ixPoi.setGeometry(geometry);
				
				ixPoi.setxGuide(resultSet.getDouble("x_guide"));
				
				ixPoi.setyGuide(resultSet.getDouble("y_guide"));
				
				ixPoi.setLinkPid(resultSet.getInt("link_pid"));
				
				ixPoi.setSide(resultSet.getInt("side"));
				
				ixPoi.setNameGroupid(resultSet.getInt("name_groupid"));
				
				ixPoi.setRoadFlag(resultSet.getInt("road_flag"));
				
				ixPoi.setPmeshId(resultSet.getInt("pmesh_id"));
				
				ixPoi.setAdminReal(resultSet.getInt("admin_real"));
				
				ixPoi.setImportance(resultSet.getInt("importance"));
				
				ixPoi.setChain(resultSet.getString("chain"));
				
				ixPoi.setAirportCode(resultSet.getString("airport_code"));
				
				ixPoi.setAccessFlag(resultSet.getInt("access_flag"));
				
				ixPoi.setOpen24h(resultSet.getInt("open_24h"));
				
				ixPoi.setMeshId5k(resultSet.getString("mesh_id_5k"));
				
				ixPoi.setMeshId(resultSet.getInt("mesh_id"));
				
				ixPoi.setRegionId(resultSet.getInt("region_id"));
				
				ixPoi.setPostCode(resultSet.getString("post_code"));
				
				ixPoi.setEditFlag(resultSet.getInt("edit_flag"));
				
				ixPoi.setDifGroupid(resultSet.getString("dif_groupid"));
				
				ixPoi.setReserved(resultSet.getString("reserved"));
				
				ixPoi.setState(resultSet.getInt("state"));
				
				ixPoi.setFieldState(resultSet.getString("field_state"));
				
				ixPoi.setLabel(resultSet.getString("label"));
				
				ixPoi.setType(resultSet.getInt("type"));
				
				ixPoi.setAddressFlag(resultSet.getInt("address_flag"));
				
				ixPoi.setExPriority(resultSet.getString("ex_priority"));
				
				ixPoi.setEditFlag(resultSet.getInt("edition_flag"));
				
				ixPoi.setPoiMemo(resultSet.getString("poi_memo"));
				
				ixPoi.setOldBlockcode(resultSet.getString("old_blockcode"));
				
				ixPoi.setOldName(resultSet.getString("old_name"));
				
				ixPoi.setOldAddress(resultSet.getString("old_address"));
				
				ixPoi.setOldKind(resultSet.getString("old_kind"));
				
				ixPoi.setPoiNum(resultSet.getString("poi_num"));
				
				ixPoi.setLog(resultSet.getString("log"));
				
				ixPoi.setTaskId(resultSet.getInt("task_id"));
				
				ixPoi.setDataVersion(resultSet.getString("data_version"));
				
				ixPoi.setFieldTaskId(resultSet.getInt("field_task_id"));
				
				ixPoi.setVerifiedFlag(resultSet.getInt("verified_flag"));

				ixPoi.setCollectTime(resultSet.getString("collect_time"));

				ixPoi.setGeoAdjustFlag(resultSet.getInt("geo_adjust_flag"));
				
				ixPoi.setFullAttrFlag(resultSet.getInt("full_attr_flag"));
				
				ixPoi.setOldXGuide(resultSet.getDouble("old_x_guide"));
				
				ixPoi.setOldYGuide(resultSet.getDouble("old_y_guide"));
				
				
				
				ixPoi.setRowId(resultSet.getString("row_id"));

				return ixPoi;
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
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}
}
