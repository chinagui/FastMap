package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
	 * @zhaokk 查询poi列表值
	 * 20161010修改 by jch,1）去掉官方原始查询条件；2）删除poi可见
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
	public JSONObject loadPids(boolean isLock, int pid, String pidName,
			int type, String g, int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM (SELECT /*+ leading(ip,ipn,ps) use_hash(ip,ipn,ps)*/  COUNT (1) OVER (PARTITION BY 1) total,");
		buffer.append(" ip.pid,ip.kind_code,ps.status, 0 as freshness_vefication,ipn.name,ip.geometry,ip.collect_time,ip.u_record ");
		buffer.append(" FROM ix_poi ip, ix_poi_name ipn, poi_edit_status ps ");
		buffer.append(" WHERE  ip.pid = ipn.poi_pid(+) and ip.row_id = ps.row_id ");
		
		buffer.append(" AND ipn.lang_code = 'CHI'");
		buffer.append(" AND ipn.name_type = 2 ");
		buffer.append(" AND ipn.name_class = 1");
		
		buffer.append(" AND ps.status = " + type + "");
		buffer.append(" AND sdo_within_distance(ip.geometry, sdo_geometry(    '"
				+ g + "'  , 8307), 'mask=anyinteract') = 'TRUE' ");
		if (pid != 0) {
			buffer.append(" AND ip.pid = " + pid + "");
		} else {
			if (StringUtils.isNotBlank(pidName)) {
				buffer.append(" AND ipn.name like '%" + pidName + "%'");
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
				//STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				//Geometry geometry = GeoTranslator.struct2Jts(struct, 1, 0);
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("kindCode", resultSet.getString("kind_code"));
				json.put("freshnessVefication",
						resultSet.getInt("freshness_vefication"));
				json.put("name", resultSet.getString("name"));
				//json.put("geometry", GeoTranslator.jts2Geojson(geometry));
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

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 安卓端检查是否有可下载的POI
	 * 
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
		sb.append(" AND u_date>'" + gridDate.getString("date") + "'");

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
			if (num > 0) {
				ret.put("flag", 1);
			} else {
				ret.put("flag", 0);
			}

			return ret;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * 根据rowId获取POI（返回名称和分类）
	 * 
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

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * 根据pid获取POI的rowId
	 * 
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

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	public String loadRowIdByPid(int pid, boolean isLock) throws Exception {

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

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

	}

	/**
	 * 根据引导LINK查询poi对象
	 * 
	 * @param linkPid
	 *            引导link
	 * @param isLock
	 *            是否加锁
	 * @return poi对象
	 * @throws Exception
	 */
	public List<IxPoi> loadIxPoiByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<IxPoi> poiList = new ArrayList<>();

		IxPoi ixPoi = null;

		String sql = "select * from  ix_poi where link_pid=:1 and u_record !=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ixPoi = new IxPoi();
				ReflectionAttrUtils.executeResultSet(ixPoi, resultSet);
				poiList.add(ixPoi);
			}

		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return poiList;
	}

	public IRow loadByIdAndChildren(int id, boolean isLock) throws Exception {
		IxPoi poi = (IxPoi) super.loadAllById(id, isLock);

		IxSamepoiPartSelector samepoiPartsSelector = new IxSamepoiPartSelector(
				conn);
		
		List<IRow> parts = samepoiPartsSelector.loadPoiByPid(poi.pid(), isLock);

		poi.setSamepoiParts(parts);
		
		poi.setRawFields(loadRawByRowId(poi.getRowId()));
		
		StaticsApi statics = (StaticsApi) ApplicationContextUtil
				.getBean("staticsApi");
		
		poi.setState(statics.getObjectState(poi.pid(), "ix_poi", conn));

		return poi;
	}
	
	/**
	 * 查询最新RAW_FIELDS
	 * @param rowId
	 * @return
	 * @throws Exception
	 */
	public String loadRawByRowId(String rowId) throws Exception {

		String sql="SELECT RAW_FIELDS FROM POI_EDIT_STATUS WHERE ROW_ID=HEXTORAW('"+rowId+"') ORDER BY UPLOAD_DATE DESC";
	
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			String rawFields=null;
			while (resultSet.next()) {
				rawFields=resultSet.getString("RAW_FIELDS");
				break;
			}
			return rawFields;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

}
