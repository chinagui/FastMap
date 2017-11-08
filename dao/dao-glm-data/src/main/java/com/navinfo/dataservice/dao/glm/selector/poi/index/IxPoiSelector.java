package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.photo.HBaseController;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
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
	public static Logger log = Logger.getLogger(IxPoiSelector.class);

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
	public JSONObject loadPids(boolean isLock, String pidName, int type,
			int subtaskId, int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		Map<Long, JSONObject> objs = new LinkedHashMap<Long, JSONObject>();

		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;

		int endRow = pageNum * pageSize;
		NiValExceptionSelector selector = new NiValExceptionSelector(conn);
		List<String> checkRuleList=selector.loadByOperationName("POI_ROW_COMMIT");
		String ckRules = "('";
		ckRules += StringUtils.join(checkRuleList.toArray(), "','") + "')";
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM (SELECT   COUNT (1) OVER (PARTITION BY 1) total,");
		buffer.append(" ip.pid,ip.kind_code,ip.poi_num,ip.poi_memo,ps.fresh_verified as freshness_vefication,ps.raw_fields as flag,ipn.name,ip.collect_time, ");
		buffer.append(" (SELECT COUNT (1)  FROM ix_poi_photo iph WHERE ip.pid = iph.poi_pid(+) AND U_RECORD != 2) as photocount  ,");
		buffer.append("  (SELECT COUNT (n.RULEID) FROM ni_val_exception n, ck_result_object c  WHERE     n.MD5_CODE = c.MD5_CODE AND ip.pid = c.pid(+) AND c.TABLE_NAME = 'IX_POI' AND n.ruleid in "+ckRules+") as checkcount ");
		buffer.append(" FROM ix_poi ip, (SELECT * FROM ix_poi_name WHERE lang_code = 'CHI' AND name_type = 2 AND name_class = 1) ipn, poi_edit_status ps ");
		buffer.append(" WHERE  ip.pid = ipn.poi_pid(+) and ip.pid = ps.pid ");

		buffer.append(" AND ps.work_type=1 AND ps.status = " + type + "");
		buffer.append(" AND (ps.QUICK_SUBTASK_ID=" + subtaskId
				+ " or ps.MEDIUM_SUBTASK_ID=" + subtaskId + ") ");

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
		buffer.append(" ) c");
		// 增加按照采集时间升序排列
		buffer.append(" WHERE ROWNUM <= :1 ORDER BY c.collect_time ) ");
		buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}
		log.info(buffer.toString());
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
				JSONObject json = new JSONObject();
				long pid = resultSet.getLong("pid");
				json.put("poiNum", resultSet.getString("poi_num") == null ? "" : resultSet.getString("poi_num"));
				json.put("pid", pid);
				json.put("name", resultSet.getString("name") == null ? "" : resultSet.getString("name"));
				json.put("kindCode", resultSet.getString("kind_code"));
				String flag = "";
				if (StringUtils.isNotEmpty(resultSet.getString("flag"))) {
					flag = resultSet.getString("flag");
				}
				json.put("flag", flag);

				json.put("photo", resultSet.getInt("photocount"));
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
				json.put("errorCount", resultSet.getInt("checkcount"));
				json.put("errorType", "");
				json.put("auditProblem", "");
				json.put("auditStatus", "");

				objs.put(pid, json);
			}
			result.put("total", total);
			// get status
			if (objs.size() > 0) {
				LogReader logRead = new LogReader(conn);
				Map<Long, Integer> objStatus = logRead.getObjectState(
						objs.keySet(), ObjectName.IX_POI);
				for (Entry<Long, JSONObject> entry : objs.entrySet()) {
					Integer status = objStatus.get(entry.getKey());
					JSONObject jo = entry.getValue();
					jo.put("status", status == null ? 0 : status);
					array.add(jo);
				}
			}
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
		String gridId = gridDate.getString("grid");
		String  increDownLoadDate = gridDate.getString("date");
		LogReader logReader = new LogReader(conn);
		Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POI","IX_POI", gridId, increDownLoadDate);
		JSONObject ret = new JSONObject();
		ret.put("gridId", gridId);
		ret.put("flag", getChangedPoiCount(poiStatus)?1:0);
		return ret;
	}

	private boolean getChangedPoiCount(Map<Integer, Collection<Long>> poiStatus) {
		for(Collection<Long> c:poiStatus.values()){
			if(c != null && c.size() > 0){
				return true;
			}
		}
		return false;
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

		// 这个接口日编和月编都调用了，而月编没有这些字段，所以要加判断（RAW_FIELDS，STATUS，FRESH_VERIFIED）
		if (poiEditStatus.containsKey("RAW_FIELDS")) {
			if (poiEditStatus.get("RAW_FIELDS") == null) {
				poi.setRawFields(rawFields);
			} else {
				rawFields = (String) poiEditStatus.get("RAW_FIELDS");
				poi.setRawFields(rawFields);
			}
		}
		poi.setState(logRead.getObjectState(poi.pid(), "IX_POI"));
		if (poiEditStatus.containsKey("STATUS")) {
			poi.setStatus(poiEditStatus.getInt("STATUS"));
		}

		if (poiEditStatus.containsKey("FRESH_VERIFIED")) {
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

	/**
	 * 根据引导LINK查询poi对象
	 * 
	 * @param linkPids
	 *            引导link
	 * @param isLock
	 *            是否加锁
	 * @return poi主对象
	 * @throws Exception
	 */
	public List<IxPoi> loadIxPoiByLinkPids(List<Integer> linkPids,
			boolean isLock) throws Exception {

		List<IxPoi> poiList = new ArrayList<>();

		if (linkPids.size() < 1) {

			return poiList;
		}

		String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "select * from  ix_poi where link_pid IN (" + ids
				+ ") and u_record !=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoi ixPoi = new IxPoi();

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

	/**
	 * 根据引导POI pids 查询官方原始名称 zhakk
	 * 
	 * @param POI
	 * @param isLock
	 *            是否加锁
	 * @return Map
	 * @throws Exception
	 */
	public JSONArray loadNamesByPids(List<Integer> poiPids, boolean isLock)
			throws Exception {

		JSONArray array = new JSONArray();

		if (poiPids.size() < 1) {

			return array;
		}

		String ids = org.apache.commons.lang.StringUtils.join(poiPids, ",");
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT ip.pid, im.name ");
		buffer.append(" FROM ix_poi_name im, ix_poi ip ");
		buffer.append(" WHERE     im.u_record(+) <> 2 AND ip.u_record <> 2 ");
		buffer.append(" AND im.lang_code(+) = 'CHI'  AND name_class(+) = 1  AND name_type(+) = 2  ");
		buffer.append(" AND ip.pid = im.poi_pid(+) ");
		Clob pidClod = null;
		if (poiPids.size() > 1000) {
			pidClod = ConnectionUtil.createClob(conn);
			pidClod.setString(1, ids);
			buffer.append(" AND ip.pid IN (select to_number(column_value) from table(clob_to_table(?))) ");
		} else {
			buffer.append("　AND ip.pid IN (" + ids + ") ");
		}

		if (isLock) {
			buffer.append(" for update nowait ");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(buffer.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				JSONObject object = new JSONObject();
				int pid = resultSet.getInt("pid");
				String name = resultSet.getString("name");
				object.put("pid", pid);
				object.put("name", name);

				array.add(object);
			}

		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return array;
	}
	
	
	/**
	 * 查询POI的省市县名称
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public String getPoiAdminName(long pid) throws Exception{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		sb.append("WITH T1 AS");
		sb.append(" (SELECT AAN1.REGION_ID, AAN1.NAME");
		sb.append("    FROM IX_POI P, AD_ADMIN_NAME AAN1");
		sb.append("   WHERE P.REGION_ID = AAN1.REGION_ID");
		sb.append("     AND AAN1.LANG_CODE = 'CHI'");
		sb.append("     AND AAN1.NAME_CLASS = 1");
		sb.append("     AND P.PID = ?),");
		sb.append("T2 AS");
		sb.append(" (SELECT AAN2.NAME      AS NAME2,");
		sb.append("         AAN2.REGION_ID AS REGION2,");
		sb.append("         T1.NAME        AS NAME1,");
		sb.append("         T1.REGION_ID   AS REGION1");
		sb.append("    FROM AD_ADMIN_NAME AAN2, T1");
		sb.append("   WHERE AAN2.REGION_ID =");
		sb.append("         (SELECT AAG1.REGION_ID_UP");
		sb.append("            FROM AD_ADMIN_GROUP AAG1");
		sb.append("           WHERE AAG1.GROUP_ID = (SELECT GROUP_ID");
		sb.append("                                    FROM AD_ADMIN_PART");
		sb.append("                                   WHERE REGION_ID_DOWN = T1.REGION_ID");
		sb.append("                                     AND ROWNUM = 1))");
		sb.append("     AND AAN2.LANG_CODE = 'CHI'");
		sb.append("     AND AAN2.NAME_CLASS = 1)");
		sb.append("SELECT AAN3.NAME AS PROVINCE, T2.NAME2 AS CITY, T1.NAME AS COUNTY");
		sb.append("  FROM AD_ADMIN_NAME AAN3, T2, T1");
		sb.append(" WHERE AAN3.REGION_ID =");
		sb.append("       (SELECT AAG1.REGION_ID_UP");
		sb.append("          FROM AD_ADMIN_GROUP AAG1");
		sb.append("         WHERE AAG1.GROUP_ID = (SELECT GROUP_ID");
		sb.append("                                  FROM AD_ADMIN_PART");
		sb.append("                                 WHERE REGION_ID_DOWN = T2.REGION2");
		sb.append("                                   AND ROWNUM = 1))");
		sb.append("   AND AAN3.LANG_CODE = 'CHI'");
		sb.append("   AND AAN3.NAME_CLASS = 1");
		try{
			log.info("查询POI行政区划名称sql:" + sb.toString());
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, pid);
			rs = pstmt.executeQuery();
			String county = "";
			String city = "";
			String province = "";
			if (rs.next()){
				province = rs.getString("PROVINCE");
				city = rs.getString("CITY");
				county = rs.getString("COUNTY");
			}
			
			return  province + city + county;
		}catch(Exception e){
			log.error("查询POI行政区划名称错误: " + e.getMessage());
			throw e;
		}finally{
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 根据regionId查询上一级的行政区划名称和regionId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getUpAdminNameRegion(int regionId) throws Exception{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		JSONObject data = new JSONObject();
		StringBuilder sb = new StringBuilder();
		sb.append("select aan2.name, aan2.region_id");
		sb.append("  from ad_admin_name aan2");
		sb.append(" where aan2.region_id = ");
		sb.append("       (select aag1.region_id_up");
		sb.append("          from ad_admin_group aag1");
		sb.append("         where aag1.group_id = (select group_id");
		sb.append("                                  from ad_admin_part");
		sb.append("                                 where region_id_down = ?");
		sb.append("                                   and rownum = 1))");
		sb.append("    and aan2.lang_code='CHI'");
		sb.append("    and aan2.name_class=1");
		try{
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, regionId);
			rs = pstmt.executeQuery();
			String name = "";
			int upRegionId = 0;
			if (rs.next()){
				name = rs.getString("name");
				upRegionId = rs.getInt("region_id");
			}
			data.put("name", name);
			data.put("regionId", upRegionId);
			
			return data;
		}catch(Exception e){
			throw e;
		}finally{
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
	}
}
