package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDeepStatusSelector;
import com.navinfo.dataservice.dao.log.LogReader;
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
	 * @zhaokk 查询poi列表值 20161010修改 by jch,1）修复无名称的poi查不出bug；2）删除poi可见
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
	public JSONObject loadPids(boolean isLock, String pidName,
			int type, int subtaskId, int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM (SELECT /*+ index(ip IX_POI_GEOMETRY) leading(ps,ip,ipn) use_hash(ps,ip,ipn)*/  COUNT (1) OVER (PARTITION BY 1) total,");
		buffer.append(" ip.pid,ip.kind_code,ip.poi_num,ip.poi_memo,ps.fresh_verified as freshness_vefication,ps.raw_fields as flag,ipn.name,ip.geometry,ip.collect_time ");
		buffer.append(" FROM ix_poi ip, (SELECT * FROM ix_poi_name WHERE lang_code = 'CHI' AND name_type = 2 AND name_class = 1) ipn, poi_edit_status ps ");
		buffer.append(" WHERE  ip.pid = ipn.poi_pid(+) and ip.pid = ps.pid ");

		// buffer.append(" AND ipn.lang_code = 'CHI'");
		// buffer.append(" AND ipn.name_type = 2 ");
		// buffer.append(" AND ipn.name_class = 1");

		buffer.append(" AND ps.work_type=1 AND ps.status = " + type + "");
		buffer.append(" AND (ps.QUICK_SUBTASK_ID="+subtaskId+" or ps.MEDIUM_SUBTASK_ID="+subtaskId+") ");
		
		if (!pidName.isEmpty()) {
			Pattern pattern = Pattern.compile("[0-9]*");
			Matcher isNum = pattern.matcher(pidName);
			if (isNum.matches()) {
				buffer.append(" AND (ip.pid = " + pidName
						+ " OR ipn.name like '%" + pidName + "%') ");
			} else {
				if (StringUtils.isNotBlank(pidName)) {
					buffer.append(" AND ipn.name like '%" + pidName + "%'");
				}
			}
		}

		// if (pid != 0) {
		// buffer.append(" AND ip.pid = " + pid + "");
		// } else {
		// if (StringUtils.isNotBlank(pidName)) {
		// buffer.append(" AND ipn.name like '%" + pidName + "%'");
		// }
		// }

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
			LogReader logRead = new LogReader(conn);
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				// STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				// Geometry geometry = GeoTranslator.struct2Jts(struct, 1, 0);
				JSONObject json = new JSONObject();

				json.put("poiNum", resultSet.getString("poi_num"));
				json.put("pid", resultSet.getInt("pid"));
				json.put("status", logRead.getObjectState(
						resultSet.getInt("pid"), "IX_POI"));
				json.put("name", resultSet.getString("name"));
				json.put("kindCode", resultSet.getString("kind_code"));
				String flag = "";
				if (StringUtils.isNotEmpty(resultSet.getString("flag"))) {
					flag = resultSet.getString("flag");
				}
				json.put("flag", flag);
				IxPoiDeepStatusSelector selector = new IxPoiDeepStatusSelector(
						conn);
				json.put("photo",
						selector.getPoiPhotoTotal(resultSet.getInt("pid")));
				String poiMemo = "";
				if (StringUtils.isNotEmpty(resultSet.getString("poi_memo"))) {
					poiMemo = resultSet.getString("poi_memo");
				}
				json.put("memo", poiMemo);
				String collectTime = "";
				if (StringUtils.isNotEmpty(resultSet.getString("collect_time"))) {
					collectTime = resultSet.getString("collect_time");
				}
				json.put("collectTime", collectTime);
				json.put("freshnessVefication",
						resultSet.getInt("freshness_vefication"));
				json.put("errorCount",
						getcheckErrorTotal(resultSet.getInt("pid"), "IX_POI"));
				json.put("errorType", "");
				json.put("auditProblem", "");
				json.put("auditStatus", "");
				// json.put("geometry", GeoTranslator.jts2Geojson(geometry));
				// json.put("uRecord", resultSet.getInt("u_record"));

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
	 * 获取poi的检查项错误个数
	 * 
	 * @param pid
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public int getcheckErrorTotal(int pid, String tableName) throws Exception {
		int total = 0;
		String sql = "select count(n.RULEID) as count from ni_val_exception n,ck_result_object c where n.MD5_CODE=c.MD5_CODE and c.PID = :1 and c.TABLE_NAME = :2 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);
			pstmt.setString(2, tableName);

			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				total = resultSet.getInt("count");
			}

			return total;
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

		IxPoi poi = null;
		LogReader logRead = new LogReader(conn);
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(id);
		List<IRow> iRows = loadByIds(ids, isLock, false);
		// 查询删除poi的信息
		if (iRows.size() == 0 || iRows == null) {
			poi = (IxPoi) loadAllById(id, isLock);
			// 根据履历过滤掉子表单独删除的记录，只留最后一次和主表同时删除的子表记录
			logRead.filterValidRowId(poi);
		} else {
			poi = (IxPoi) loadById(id, isLock);
		}
		IxSamepoiPartSelector samepoiPartsSelector = new IxSamepoiPartSelector(
				conn);

		List<IRow> parts = samepoiPartsSelector.loadByPoiPid(poi.pid(), isLock);

		JSONObject poiEditStatus = getPoiStatusByPid(poi.getPid());

		poi.setSamepoiParts(parts);
		String rawFields = null;
		
		//这个接口日编和月编都调用了，而月编没有这些字段，所以要加判断（RAW_FIELDS，STATUS，FRESH_VERIFIED）
		if (poiEditStatus.containsKey("RAW_FIELDS")) {
			if (poiEditStatus.get("RAW_FIELDS") == null) {
				poi.setRawFields(rawFields);
			} else {
				rawFields = (String) poiEditStatus.get("RAW_FIELDS");
				poi.setRawFields(rawFields);
			}
		}
		poi.setState(logRead.getObjectState(poi.pid(), "IX_POI"));
		if(poiEditStatus.containsKey("STATUS")){
		poi.setStatus(poiEditStatus.getInt("STATUS"));}
		
		if(poiEditStatus.containsKey("FRESH_VERIFIED")){
			poi.setFreshVerified(poiEditStatus.getInt("FRESH_VERIFIED"));
		}
		
		
		return poi;
	}

	/**
	 * 查询最新RAW_FIELDS
	 * 
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public String loadRawByRowId(int pid) throws Exception {

		String sql = "SELECT RAW_FIELDS FROM POI_EDIT_STATUS WHERE pid=" + pid
				+ " ORDER BY UPLOAD_DATE DESC";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			String rawFields = null;
			while (resultSet.next()) {
				rawFields = resultSet.getString("RAW_FIELDS");
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

	/**
	 * 根据子任务获取该任务圈下所有的PID
	 * 
	 * @param subtask
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getPidsBySubTask(Subtask subtask) throws Exception {
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select p.pid from IX_POI p");
			sb.append(" WHERE sdo_within_distance(p.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE'");

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, subtask.getGeometry());

			resultSet = pstmt.executeQuery();

			List<Integer> pids = new ArrayList<Integer>();

			while (resultSet.next()) {
				pids.add(resultSet.getInt("pid"));
			}

			return pids;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * 查询POI的状态信息
	 * 
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public JSONObject getPoiStatusByPid(int pid) throws Exception {

		String sql = "SELECT PS.STATUS,PS.IS_UPLOAD,PS.UPLOAD_DATE,PS.FRESH_VERIFIED,PS.RAW_FIELDS,PS.WORK_TYPE,PS.COMMIT_HIS_STATUS,PS.SUBMIT_DATE FROM POI_EDIT_STATUS PS WHERE PS.pid="
				+ pid + " ORDER BY PS.UPLOAD_DATE DESC";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			JSONObject result = new JSONObject();
			while (resultSet.next()) {
				result.put("STATUS", resultSet.getInt("STATUS"));
				result.put("IS_UPLOAD", resultSet.getInt("IS_UPLOAD"));
				result.put("FRESH_VERIFIED", resultSet.getInt("FRESH_VERIFIED"));
				result.put("RAW_FIELDS", resultSet.getString("RAW_FIELDS"));
				result.put("WORK_TYPE", resultSet.getInt("WORK_TYPE"));
				result.put("COMMIT_HIS_STATUS",
						resultSet.getInt("COMMIT_HIS_STATUS"));
				break;
			}
			return result;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

}
