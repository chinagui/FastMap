package com.navinfo.dataservice.engine.edit.search.rd.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.search.AdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.AdNodeSearch;
import com.navinfo.dataservice.dao.glm.search.CmgBuildlinkSearch;
import com.navinfo.dataservice.dao.glm.search.CmgBuildnodeSearch;
import com.navinfo.dataservice.dao.glm.search.LcLinkSearch;
import com.navinfo.dataservice.dao.glm.search.LcNodeSearch;
import com.navinfo.dataservice.dao.glm.search.LuLinkSearch;
import com.navinfo.dataservice.dao.glm.search.LuNodeSearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.dao.glm.search.ZoneLinkSearch;
import com.navinfo.dataservice.dao.glm.search.ZoneNodeSearch;

public class ObjectSearchUtils {
	
	private Connection conn;
	
	private JsonConfig jsonConfig;

	/**
	 * 查询要素，返回渲染格式数据
	 * @param conn
	 * @param jsonConfig
	 * @throws Exception
	 */
	public ObjectSearchUtils(Connection conn,JsonConfig jsonConfig) throws Exception {
		this.conn = conn;
		
		this.jsonConfig = jsonConfig;
	}
	/**
	 * 根据node查link
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public JSONObject searchLinkByNode(JSONObject condition) throws Exception {

		JSONObject json = new JSONObject();

		ObjType type = ObjType.valueOf(condition.getString("type"));

		int pid = condition.getInt("pid");

		json = getLink(pid, type);

		return json;
	}
	
	private JSONObject getLink(int pid, ObjType type) throws Exception {
		
		JSONObject json = new JSONObject();
		
		try {

			List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

			switch (type) {

			case RDNODE:

				RdLinkSearch linkSearch = new RdLinkSearch(conn);

				list = linkSearch.searchDataByNodePid(pid);

				break;

			default:
				break;
			}

			JSONArray array = new JSONArray();

			for (SearchSnapshot snap : list) {

				array.add(snap.Serialize(ObjLevel.BRIEF), jsonConfig);
			}

			json.accumulate(ObjType.RDLINK.toString(), array, jsonConfig);

		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}
	
	
	/**
	 * 按渲染格式查数据
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public JSONObject searchObject(JSONObject condition) throws Exception {
		
		JSONObject json = new JSONObject();

		ObjType type = ObjType.valueOf(condition.getString("type"));

		JSONArray pids = condition.getJSONArray("pids");
		
		@SuppressWarnings("unchecked")
		List<Integer> pidList = JSONArray.toList(pids, Integer.class,
				JsonUtils.getJsonConfig());
		
		List<SearchSnapshot> list =new ArrayList<SearchSnapshot>();

		try {
			switch (type) {

			case RDLINK:

				RdLinkSearch rdLinkSearch = new RdLinkSearch(conn);

				list = rdLinkSearch.searchDataByLinkPids(pidList);

				break;
			case ADLINK:

				AdLinkSearch adLinkSearch = new AdLinkSearch(conn);

				list = adLinkSearch.searchDataByLinkPids(pidList);

				break;
			case LULINK:

				LuLinkSearch luLinkSearch = new LuLinkSearch(conn);

				list = luLinkSearch.searchDataByLinkPids(pidList);

				break;
			case LCLINK:

				LcLinkSearch lcLinkSearch = new LcLinkSearch(conn);

				list = lcLinkSearch.searchDataByLinkPids(pidList);

				break;
			case ZONELINK:

				ZoneLinkSearch zoneLinkSearch = new ZoneLinkSearch(conn);

				list = zoneLinkSearch.searchDataByLinkPids(pidList);

				break;
			case CMGBUILDLINK:

				CmgBuildlinkSearch cmgBuildlinkSearch = new CmgBuildlinkSearch(
						conn);

				list = cmgBuildlinkSearch.searchDataByLinkPids(pidList);

				break;

			case RDNODE:

				RdNodeSearch nodeSearch = new RdNodeSearch(conn);

				list = nodeSearch.searchDataByNodePids(pidList);

				break;

			case ADNODE:

				AdNodeSearch adNodeSearch = new AdNodeSearch(conn);

				list = adNodeSearch.searchDataByNodePids(pidList);

				break;

			case LUNODE:

				LuNodeSearch luNodeSearch = new LuNodeSearch(conn);

				list = luNodeSearch.searchDataByNodePids(pidList);

				break;

			case LCNODE:

				LcNodeSearch lcNodeSearch = new LcNodeSearch(conn);

				list = lcNodeSearch.searchDataByNodePids(pidList);

				break;

			case ZONENODE:

				ZoneNodeSearch zoneNodeSearch = new ZoneNodeSearch(conn);

				list = zoneNodeSearch.searchDataByNodePids(pidList);

				break;

			case CMGBUILDNODE:

				CmgBuildnodeSearch cmgBuildnodeSearch = new CmgBuildnodeSearch(
						conn);

				list = cmgBuildnodeSearch.searchDataByNodePids(pidList);

				break;

			default:
				break;
			}

			JSONArray array = new JSONArray();

			for (SearchSnapshot snap : list) {

				array.add(snap.Serialize(ObjLevel.BRIEF), jsonConfig);
			}

			json.accumulate(type.toString(), array, jsonConfig);

		} catch (Exception e) {

			throw e;

		} finally {
		}

		return json;
	}
}
