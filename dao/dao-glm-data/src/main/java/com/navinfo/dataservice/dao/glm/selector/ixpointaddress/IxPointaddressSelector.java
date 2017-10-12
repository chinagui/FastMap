package com.navinfo.dataservice.dao.glm.selector.ixpointaddress;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.glm.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Pa基础信息表 selector
 * 
 * @author zl
 * 
 */
public class IxPointaddressSelector extends AbstractSelector {

	private Connection conn;
	public static Logger log = Logger.getLogger(IxPointaddressSelector.class);

	public IxPointaddressSelector(Connection conn) {

		super(conn);
		this.conn = conn;
		this.setCls(IxPointaddress.class);
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
		Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POINTADDRESS","IX_POINTADDRESS", gridId, increDownLoadDate);
		JSONObject ret = new JSONObject();
		ret.put("gridId", gridId);
		ret.put("flag", getChangedPoiCount(poiStatus)?1:0);
		return ret;
	}

	private boolean getChangedPoiCount(Map<Integer, Collection<Long>> paStatus) {
		for(Collection<Long> c:paStatus.values()){
			if(c != null && c.size() > 0){
				return true;
			}
		}
		return false;
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
			sb.append("select p.pid from IX_POINTADDRESS p");
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
}
