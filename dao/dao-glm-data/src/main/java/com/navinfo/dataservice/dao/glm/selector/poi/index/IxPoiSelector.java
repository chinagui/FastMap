package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * POI基础信息表 selector
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoi.class);
	}
	
	/**
	 * @zhaokk 
	 * 查询poi列表值
	 * @param isLock
	 * @param pid
	 * @param pidName
	 * @param type
	 * @param g
	 * @param pageSize
	 * @param pageNum
	 * @return
	 * @throws Exception
	 */
	public JSONObject loadPids(boolean isLock,int pid ,String pidName,int type,String g ,int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;
		int startRow = (pageNum-1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		StringBuilder buffer = new StringBuilder();
        buffer.append(" SELECT * ");
        buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
        buffer.append(" FROM (SELECT /*+ leading(ip,ipn,ps) use_hash(ip,ipn,ps)*/  COUNT (1) OVER (PARTITION BY 1) total,");
        buffer.append(" ip.pid,ip.kind_code,ps.status, 0 as freshness_vefication,ipn.name,ip.geometry,ip.collect_time,ip.u_record ");
        buffer.append(" FROM ix_poi ip, ix_poi_name ipn, poi_edit_status ps ");
        buffer.append(" WHERE     ip.pid = ipn.poi_pid and ip.row_id = ps.row_id ");
        buffer.append(" AND lang_code = 'CHI'");
        buffer.append(" AND ipn.name_type = 2 ");
        buffer.append(" AND name_class = 1"); 
        buffer.append(" AND ps.status = "+type+"");
        buffer.append(" AND sdo_within_distance(ip.geometry, sdo_geometry(    '"+g+"'  , 8307), 'mask=anyinteract') = 'TRUE' ");
        if( pid != 0){
        	buffer.append(" AND ip.pid = "+pid+"");
        }else{
        	if(StringUtils.isNotBlank(pidName)){
        		buffer.append(" AND ipn.name like '%"+pidName+"%'");
        	}
        }
        
        buffer.append(" ) c");
        buffer.append(" WHERE ROWNUM <= :1) ");
        buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(buffer.toString());
			pstmt.setInt(1, endRow);

			pstmt.setInt(2, startRow);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 1, 0);
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("kindCode", resultSet.getString("kind_code"));
				json.put("freshnessVefication", resultSet.getInt("freshness_vefication"));
				json.put("name", resultSet.getString("name"));
				json.put("geometry", GeoTranslator.jts2Geojson(geometry));
				json.put("uRecord", resultSet.getInt("u_record"));
				json.put("status", resultSet.getInt("status"));
				json.put("collectTime", resultSet.getString("collect_time"));
				array.add(json);
			}
			result.put("total", total);

			result.put("rows", array);

			return result;
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
	
	
	/**
	 * 安卓端检查是否有可下载的POI
	 * @param gridDate
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONObject downloadCheck(JSONObject gridDate) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT count(1) num");
		sb.append(" FROM ix_poi");
		sb.append(" WHERE sdo_relate(geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		sb.append(" AND u_date>'"+gridDate.getString("date")+"'");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			GridUtils gu = new GridUtils();
			String wkt = gu.grid2Wkt(gridDate.getString("grid"));

			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			
			int num = 0;
			if (resultSet.next()) {
				num = resultSet.getInt("num");
			}
			
			JSONObject ret = new JSONObject();
			
			ret.put("gridId", gridDate.getString("grid"));
			if (num>0) {
				ret.put("flag", 1);
			} else {
				ret.put("flag", 0);
			}
			
			return ret;
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
	
	/**
	 * 根据rowId获取POI（返回名称和分类）
	 * @param rowId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getByRowIdForAndroid(String rowId) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT name,kind_code");
		sb.append(" FROM ix_poi");
		sb.append(" WHERE row_id=:1");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, rowId);
			resultSet = pstmt.executeQuery();
			
			JSONObject ret = new JSONObject();
			if (resultSet.next()) {
				ret.put("name", resultSet.getString("name"));
				ret.put("kindCode", resultSet.getString("kind_code"));
			}
			
			return ret;
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
	
	/**
	 * 根据pid获取POI的rowId
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public JSONObject getRowIdById(int pid) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT row_id");
		sb.append(" FROM ix_poi");
		sb.append(" WHERE pid=:1");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, pid);
			resultSet = pstmt.executeQuery();
			
			JSONObject ret = new JSONObject();
			if (resultSet.next()) {
				ret.put("rowId", resultSet.getString("row_id"));
			}
			
			return ret;
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

	/**
	 * 设置属性
	 * 
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void setAttr(IxPoi ixPoi, ResultSet resultSet) throws Exception {
		ixPoi.setPid(resultSet.getInt("pid"));

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

		ixPoi.setEditionFlag(resultSet.getString("edition_flag"));

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
		
		ixPoi.setLevel(resultSet.getString("LEVEL"));
		
		ixPoi.setSportsVenue(resultSet.getString("sports_venue"));
		
		ixPoi.setIndoor(resultSet.getInt("indoor"));
		
		ixPoi.setVipFlag(resultSet.getString("vip_flag"));

		ixPoi.setRowId(resultSet.getString("row_id"));
		
		ixPoi.setuDate(resultSet.getString("u_date"));

	}
	public String loadRowIdByPid(int pid,boolean isLock ) throws Exception{


		String sql = "select row_id from  ix_poi where pid=:1";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return resultSet.getString("row_id");
				
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
		
	}
}
