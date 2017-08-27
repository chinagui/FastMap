package com.navinfo.dataservice.engine.edit.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.StringUtils;

import com.navinfo.dataservice.dao.glm.iface.*;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminTreeSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ADLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.CmgLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.LcLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.LuLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ObjectSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ZoneLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.DbMeshInfoUtil;

/**
 * 查询进程
 */
public class SearchProcess {

	private Connection conn;
	private static final Logger logger = Logger.getLogger(SearchProcess.class);

	public SearchProcess(Connection conn) throws Exception {

		this.conn = conn;

	}

	public SearchProcess() throws Exception {

	}

	private int dbId;
	private int z;
	private int taskId;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	private JSONArray array;

	public JSONArray getArray() {
		return array;
	}

	public void setArray(JSONArray array) {
		this.array = array;
	}

	/**
	 * 控制输出JSON的格式
	 * 
	 * @return JsonConfig
	 */
	private JsonConfig getJsonConfig() {
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(String.class,
				new JsonValueProcessor() {

					@Override
					public Object processObjectValue(String key, Object value,
							JsonConfig arg2) {
						if (value == null) {
							return null;
						}

						if (JSONUtils.mayBeJSON(value.toString())) {
							return "\"" + value + "\"";
						}

						return value;

					}

					@Override
					public Object processArrayValue(Object value,
							JsonConfig arg1) {
						return value;
					}
				});

		return jsonConfig;
	}

	/**
	 * 根据矩形框空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public JSONObject searchDataBySpatial(List<ObjType> types, String box)
			throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);

				String wkt = Geojson.geojson2Wkt(box);

				List<SearchSnapshot> list = search.searchDataBySpatial(wkt);

				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : list) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(type.toString(), array, getJsonConfig());
			}
		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}

	/**
	 * 根据瓦片空间查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public JSONObject searchDataByTileWithGap(List<ObjType> types, int x,
			int y, int z, int gap) throws Exception {

		JSONObject json = new JSONObject();

		try {

			// 1.计算瓦片的几何
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
			// 2 根据瓦片计算
			Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator
					.wkt2Geometry(wkt));
			Map<String, List<SearchSnapshot>> map = new HashMap<String, List<SearchSnapshot>>();
			for (int dbId : dbIds) {
				try {
					logger.info("dbId========" + dbId);
					conn = DBConnector.getInstance().getConnectionById(dbId);
					SearchFactory factory = new SearchFactory(conn);
					for (ObjType type : types) {
						if (dbId != this.getDbId()) {
							if (!this.getBasicObjForRender(type)) {
								continue;
							}
						}
						List<SearchSnapshot> list = null;
						if (type == ObjType.IXPOI) {
							IxPoiSearch ixPoiSearch = new IxPoiSearch(conn);
							list = ixPoiSearch.searchDataByTileWithGap(x, y, z,
									gap, this.getArray());
						} else {
							ISearch search = factory.createSearch(type);
							list = search.searchDataByTileWithGap(x, y, z, gap);
						}
						for (SearchSnapshot snapshot : list) {
							snapshot.setDbId(dbId);
						}
						if (map.containsKey(type.toString())) {
							List<SearchSnapshot> snapshots = map.get(type
									.toString());

							for (SearchSnapshot snapshot : list) {
								if (!snapshots.contains(snapshot)) {
									snapshots.add(snapshot);
								}

							}
						} else {
							map.put(type.toString(), list);
						}

					}
				} catch (Exception e) {

					throw e;

				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			for (Map.Entry<String, List<SearchSnapshot>> entry : map.entrySet()) {
				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : entry.getValue()) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(entry.getKey(), array, getJsonConfig());

			}

		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}

	private boolean getBasicObjForRender(ObjType type) {
		if (type == ObjType.RDLINK) {
			return true;
		} else if (type == ObjType.RDNODE) {
			return true;
		} else if (type == ObjType.ADNODE) {
			return true;
		} else if (type == ObjType.ADLINK) {
			return true;
		} else if (type == ObjType.ADFACE) {
			return true;
		} else if (type == ObjType.LUNODE) {
			return true;
		} else if (type == ObjType.LULINK) {
			return true;
		} else if (type == ObjType.LUFACE) {
			return true;
		} else if (type == ObjType.LCNODE) {
			return true;
		} else if (type == ObjType.LCLINK) {
			return true;
		} else if (type == ObjType.LCFACE) {
			return true;
		} else if (type == ObjType.RWLINK) {
			return true;
		} else if (type == ObjType.RWNODE) {
			return true;
		} else if (type == ObjType.RDOBJECT) {
			return true;
		} else if (type == ObjType.RDROAD) {
			return true;
		} else if (type == ObjType.RDINTER) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @Title: searchDataByTileWithGap
	 * @Description: 平台渲染接口
	 * @param types
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @param taskId
	 * @return
	 * @throws Exception
	 *             JSONObject
	 * @throws
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年7月4日 上午10:30:49
	 */
	public JSONObject searchDataByTileWithGapForMan(List<ObjType> types, int x,
			int y, int z, int gap) throws Exception {
		JSONObject json = new JSONObject();

		try {

			// 1.计算瓦片的几何
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
			// 2 根据瓦片计算
			Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator
					.wkt2Geometry(wkt));
			Map<String, List<SearchSnapshot>> map = new HashMap<String, List<SearchSnapshot>>();
			for (int dbId : dbIds) {
				try {
					logger.info("dbId========" + dbId);
					conn = DBConnector.getInstance().getConnectionById(dbId);
					for (ObjType type : types) {
						if (dbId != this.getDbId()) {
							if (!this.getBasicObjForRender(type)) {
								continue;
							}
						}
						List<SearchSnapshot> list = null;
						
						if(z <= 14){
							if (type == ObjType.IXPOI) {
								IxPoiSearch ixPoiSearch = new IxPoiSearch(conn);
								list = ixPoiSearch.searchDataByTileWithGapSnapshot(x, y, z,
										gap, taskId);
							} else if (type == ObjType.RDLINK) {
								RdLinkSearch rdLinkSearch = new RdLinkSearch(conn);
								list = rdLinkSearch.searchDataByTileWithGapSnapshot(x, y,
										z, gap, taskId);
							}
						}else{
							if (type == ObjType.IXPOI) {
								IxPoiSearch ixPoiSearch = new IxPoiSearch(conn);
								list = ixPoiSearch.searchDataByTileWithGap(x, y, z,
										gap, taskId);
							} else if (type == ObjType.RDLINK) {
								RdLinkSearch rdLinkSearch = new RdLinkSearch(conn);
								list = rdLinkSearch.searchDataByTileWithGap(x, y,
										z, gap, taskId);
							}
						}
						
						for (SearchSnapshot snapshot : list) {
							snapshot.setDbId(dbId);
						}
						if (map.containsKey(type.toString())) {
							List<SearchSnapshot> snapshots = map.get(type
									.toString());
							if (list != null && list.size() > 0) {
								for (SearchSnapshot snapshot : list) {
									if (!snapshots.contains(snapshot)) {
										snapshots.add(snapshot);
									}

								}
							}

						} else {
							map.put(type.toString(), list);
						}

					}
				} catch (Exception e) {

					throw e;

				} finally {
					DbUtils.closeQuietly(conn);
				}
			}
			for (Map.Entry<String, List<SearchSnapshot>> entry : map.entrySet()) {
				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : entry.getValue()) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(entry.getKey(), array, getJsonConfig());

			}

		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}

	/**
	 * 根据pid查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public IObj searchDataByPid(ObjType type, int pid) throws Exception {

		try {
			SearchFactory factory = new SearchFactory(conn);

			ISearch search = factory.createSearch(type);

			IObj obj = search.searchDataByPid(pid);

			return obj;
		} catch (Exception e) {

			throw e;

		} finally {

		}

	}
	/**
	 * 根据pid查询删除数据
	 *
	 * @return 查询结果
	 * @throws Exception
	 */
	public IObj searchDelDataByPid(ObjType type, int pid) throws Exception {

		try {
			SearchFactory factory = new SearchFactory(conn);

			ISearch search = factory.createSearch(type);

			if (search instanceof ISearchDelObj) {

				ISearchDelObj searchDelObj = (ISearchDelObj) search;

				return searchDelObj.searchDelDataByPid(pid);
			}
			return null;


		} catch (Exception e) {

			throw e;

		} finally {

		}

	}

	/**
	 * 根据pids查询
	 * 
	 * @return 查询结果
	 * @throws Exception
	 */
	public List<? extends IRow> searchDataByPids(ObjType type, JSONArray pids)
			throws Exception {

		try {
			SearchFactory factory = new SearchFactory(conn);

			ISearch search = factory.createSearch(type);

			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(pids, Integer.class,
					JsonUtils.getJsonConfig());

			List<? extends IRow> objList = search.searchDataByPids(pidList);

			return objList;
		} catch (Exception e) {

			throw e;

		} finally {

		}

	}

	public JSONArray searchDataByCondition(ObjType type, JSONObject condition)
			throws Exception {

		try {
			JSONArray array = new JSONArray();

			switch (type) {

			case IXPOI:

				if (condition.containsKey("pids")) {
					@SuppressWarnings({ "unchecked" })
					List<Integer> pids = (List<Integer>) JSONArray
							.toCollection(condition.getJSONArray("pids"),
									Integer.class);

					IxPoiSelector selector = new IxPoiSelector(this.conn);

					array = selector.loadNamesByPids(pids, false);

				}
				break;

			case RDCROSS:

				if (condition.containsKey("nodePid")) {

					int nodePid = condition.getInt("nodePid");

					RdCrossSelector selector = new RdCrossSelector(this.conn);

					RdCross cross = selector.loadCrossByNodePid(nodePid, false);

					if (cross != null) {
						array.add(cross.Serialize(ObjLevel.FULL));
					}
				}
				break;

			case RDLINK:

				if (condition.containsKey("queryType")) {

					String queryType = condition.getString("queryType");

					// 批量编辑限速link追踪
					if (queryType.equals("RDSPEEDLIMIT")) {

						int linkPid = condition.getInt("linkPid");

						int direct = condition.getInt("direct");

						RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(
								conn);

						int speedDependent = -1;

						if (condition.containsKey("speedDependent")) {
							speedDependent = condition.getInt("speedDependent");
						}

						List<Integer> nextLinkPids = searchUtils
								.getConnectLinks(linkPid, direct,
										speedDependent);

						JSONArray linkPidsArray = new JSONArray();

						for (int pid : nextLinkPids) {
							linkPidsArray.add(pid);
						}

						array.add(linkPidsArray);

						JSONArray speedlimitArray = searchUtils
								.getRdLinkSpeedlimit(nextLinkPids,
										speedDependent);

						array.add(speedlimitArray);
					}
					// 可变限速link追踪
					if (queryType.equals("RDVARIABLESPEED")) {
						int linkPid = condition.getInt("linkPid");

						int nodePid = condition.getInt("nodePid");

						RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(
								conn);

						List<RdLink> links = searchUtils
								.variableSpeedNextLinks(linkPid, nodePid);

						for (RdLink link : links) {
							array.add(link.Serialize(ObjLevel.BRIEF));
						}
					}
					// 坡度追踪原则开发
					if (queryType.equals("RDSLOPE")) {
						int cruuentNodePidDir = condition.getInt("nodePidDir");
						int cuurentLinkPid = condition.getInt("linkPid");
						int length = condition.getInt("length");
						RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(
								conn);
						List<RdLink> links = searchUtils.getNextLinksForSlope(
								length, cuurentLinkPid, cruuentNodePidDir);
						for (RdLink link : links) {
							array.add(link.Serialize(ObjLevel.BRIEF));
						}
					}

					return array;
				}
				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					RdLinkSearchUtils linkSearchUtils = new RdLinkSearchUtils(
							conn);
					List<RdLink> links = linkSearchUtils.getCloseTrackLinks(
							linkPid, cisFlag);
					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				// node追踪原则
				if (condition.containsKey("nodePid")) {

					int nodePid = condition.getInt("nodePid");

					RdLinkSelector selector = new RdLinkSelector(this.conn);

					List<RdLink> links = selector.loadByNodePid(nodePid, false);

					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				} // 上线线分离追踪原则
				else if (condition.containsKey("nodePidDir")) {
					int cruuentNodePidDir = condition.getInt("nodePidDir");
					int cuurentLinkPid = condition.getInt("linkPid");
					int maxNum = 30;
					boolean loadChild = false;
					// 默认是11条 以传入为准
					if (condition.containsKey("maxNum")) {
						maxNum = condition.getInt("maxNum");
					}
					if (condition.containsKey("loadChild")) {
						int flag = condition.getInt("loadChild");

						if (flag == 1) {
							loadChild = true;
						}
					}
					RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(conn);
					List<RdLink> links = searchUtils.getNextTrackLinks(
							cuurentLinkPid, cruuentNodePidDir, maxNum,
							loadChild);
					for (RdLink link : links) {
						if (loadChild) {
							array.add(link.Serialize(ObjLevel.FULL));
						} else {
							array.add(link.Serialize(ObjLevel.BRIEF));
						}
					}
				} else if (condition.containsKey("linkPids")) {
					JSONArray linkPids = condition.getJSONArray("linkPids");
					List<Integer> pids = new ArrayList<Integer>();
					for (int i = 0; i < linkPids.size(); i++) {
						int pid = linkPids.getInt(i);

						if (!pids.contains(pid)) {
							pids.add(pid);
						}
					}

					RdLinkSelector selector = new RdLinkSelector(this.conn);

					array = selector.loadGeomtryByLinkPids(pids);
				} else if (condition.containsKey("arrows")) {
					@SuppressWarnings("unchecked")
					List<String> arrows = JSONArray.toList(
							condition.getJSONArray("arrows"), String.class,
							JsonUtils.getJsonConfig());
					int inNodePid = condition.getInt("inNodePid");
					int inLinkPid = condition.getInt("inLinkPid");
					CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils(
							conn);
					Map<String, List<Integer>> map = calLinkOperateUtils
							.getOutLinkForArrow(inNodePid, inLinkPid, arrows);
					array = JSONArray.fromObject(map);

				}

				break;

			case RDBRANCH:
				if (condition.containsKey("detailId")) {

					int detailId = condition.getInt("detailId");
					int branchType = condition.getInt("branchType");
					String rowId = condition.getString("rowId");

					RdBranchSelector selector = new RdBranchSelector(conn);

					IRow row = selector.loadByDetailId(detailId, branchType,
							rowId, false);

					array.add(row.Serialize(ObjLevel.FULL));
				}
				break;
			case ADADMINGROUP:
				if (condition.containsKey("subTaskId")) {
					AdAdminTreeSelector adAdminTreeSelector = new AdAdminTreeSelector(
							conn);

					int subTaskId = condition.getInt("subTaskId");

					IRow row = adAdminTreeSelector.loadRowsBySubTaskId(
							subTaskId, false);

					array.add(row.Serialize(ObjLevel.BRIEF));
				} else {
					throw new Exception("缺少子任务ID（subTaskId）参数");
				}
				break;
			case ADLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");
					AdLinkSelector selector = new AdLinkSelector(this.conn);
					List<AdLink> adLinks = selector.loadByNodePid(nodePid,
							false);
					for (AdLink link : adLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					ADLinkSearchUtils linkSearchUtils = new ADLinkSearchUtils(
							conn);
					List<AdLink> links = linkSearchUtils.getCloseTrackLinks(
							linkPid, cisFlag);
					for (AdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case RWLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");
					RwLinkSelector selector = new RwLinkSelector(this.conn);
					List<RwLink> rwLinks = selector.loadByNodePid(nodePid,
							false);
					for (RwLink link : rwLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case ZONELINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					ZoneLinkSelector selector = new ZoneLinkSelector(this.conn);

					List<ZoneLink> zoneLinks = selector.loadByNodePid(nodePid,
							false);

					for (ZoneLink link : zoneLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					ZoneLinkSearchUtils linkSearchUtils = new ZoneLinkSearchUtils(
							conn);
					List<ZoneLink> links = linkSearchUtils.getCloseTrackLinks(
							linkPid, cisFlag);
					for (ZoneLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case LULINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					LuLinkSelector selector = new LuLinkSelector(this.conn);

					List<LuLink> luLinks = selector.loadByNodePid(nodePid,
							false);

					for (LuLink link : luLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					LuLinkSearchUtils linkSearchUtils = new LuLinkSearchUtils(
							conn);
					List<LuLink> links = linkSearchUtils.getCloseTrackLinks(
							linkPid, cisFlag);
					for (LuLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case LCLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					LcLinkSelector selector = new LcLinkSelector(this.conn);

					List<LcLink> lcLinks = selector.loadByNodePid(nodePid,
							false);

					for (LcLink link : lcLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					LcLinkSearchUtils linkSearchUtils = new LcLinkSearchUtils(
							conn);
					List<LcLink> links = linkSearchUtils.getCloseTrackLinks(
							linkPid, cisFlag);
					for (LcLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case RDOBJECTNAME:
				if (condition.containsKey("pid")) {
					int pid = condition.getInt("pid");

					RdObjectSelector selector = new RdObjectSelector(this.conn);

					List<String> names = selector.getRdObjectName(pid, false);

					for (String name : names) {
						array.add(name);
					}
				}
				break;
			case RDLANEVIA:
				CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils(
						conn);
				if (condition.containsKey("inLinkPid")
						&& condition.containsKey("nodePid")
						&& condition.containsKey("outLinkPid")) {

					int inLinkPid = condition.getInt("inLinkPid");

					int nodePid = condition.getInt("nodePid");

					int outLinkPid = condition.getInt("outLinkPid");

					List<Integer> viaList = new ArrayList<>();

					String errInfo = "";
					try {
						// 计算经过线
						viaList = calLinkOperateUtils.calViaLinks(this.conn,
								inLinkPid, nodePid, outLinkPid);
					} catch (Exception e) {

						if (e.getMessage().equals("未计算出经过线，请手动选择经过线")) {

							errInfo = "未计算出经过线，请手动选择经过线\r\n";

						} else {
							throw e;
						}
					}

					// 计算关系类型
					int relationShipType = calLinkOperateUtils
							.getRelationShipType(nodePid, outLinkPid);

					JSONObject obj = new JSONObject();

					obj.put("relationshipType", relationShipType);

					List<Integer> linkpids = new ArrayList<Integer>();

					linkpids.add(inLinkPid);

					linkpids.addAll(viaList);

					linkpids.add(outLinkPid);

					if (!calLinkOperateUtils.isConnect(linkpids, nodePid)) {

						errInfo += "所选进入线、进入点、退出线不连通";
					}

					JSONArray viaArray = new JSONArray();

					if (CollectionUtils.isNotEmpty(viaList)) {
						for (Integer via : viaList) {
							viaArray.add(via);
						}

					}
					obj.put("links", viaArray);

					if (StringUtils.isNotEmpty(errInfo)) {

						obj.put("errInfo", errInfo);
					}

					array.add(obj);

					return array;
				}
				if (!condition.containsKey("nodePid")) {
					int inLinkPid = condition.getInt("inLinkPid");
					int outLinkPid = condition.getInt("outLinkPid");
					RdLinkSelector linkSelector = new RdLinkSelector(conn);

					IRow row = linkSelector.loadById(inLinkPid, true, true);

					RdLink link = (RdLink) row;

					List<Integer> viaList = calLinkOperateUtils.calViaLinks(this.conn, link, outLinkPid);

					for (Integer pid : viaList) {
						array.add(pid);
					}
					return array;

				}
				break;
			case RDLANE:
				// 按照方向 查询link车道信息
				if (condition.containsKey("linkPid")
						&& condition.containsKey("laneDir")) {
					int linkPid = condition.getInt("linkPid");
					int laneDir = condition.getInt("laneDir");
					RdLaneSelector selector = new RdLaneSelector(this.conn);
					List<RdLane> lanes = selector.loadByLink(linkPid, laneDir,
							false);
					for (RdLane lane : lanes) {
						array.add(lane);
					}

				}
				// 按照进入点 进入link 查找退出link
				if (condition.containsKey("nodePid")
						&& condition.containsKey("linkPid")) {
					int linkPid = condition.getInt("linkPid");
					int nodePid = condition.getInt("nodePid");
					RdLaneTopoDetailSelector detailSelector = new RdLaneTopoDetailSelector(
							conn);
					List<Integer> list = detailSelector.loadOutLinkByinLink(
							linkPid, nodePid, false);
					for (Integer pid : list) {
						array.add(pid);
					}

				}
				// 按照一组link查询车道联通信息
				if (condition.containsKey("linkPids")) {
					JSONArray arrayTopo = new JSONArray();
					JSONObject object = new JSONObject();
					JsonUtils.getStringValueFromJSONArray(condition
							.getJSONArray("linkPids"));

					@SuppressWarnings("unchecked")
					List<Integer> pids = (List<Integer>) JSONArray
							.toCollection(condition.getJSONArray("linkPids"),
									Integer.class);
					RdLaneSelector selector = new RdLaneSelector(this.conn);
					RdLaneTopoDetailSelector detailSelector = new RdLaneTopoDetailSelector(
							conn);
					List<IRow> rows = detailSelector.loadByLinkPids(pids,
							condition.getInt("nodePid"), false);
					object.put("laneInfos", selector.loadByLinks(pids, false));
					for (IRow row : rows) {
						arrayTopo.add(row);
					}
					object.put("laneTopoInfos", arrayTopo);
					array.add(object);

				}
			case CMGBUILDLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");
					CmgBuildlinkSelector selector = new CmgBuildlinkSelector(
							this.conn);
					List<CmgBuildlink> cmglinks = selector
							.listTheAssociatedLinkOfTheNode(nodePid, false);
					for (CmgBuildlink link : cmglinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}

				// 追踪闭合的面 1 顺时针 2 逆时针
				if (condition.containsKey("cisFlag")) {
					int cisFlag = condition.getInt("cisFlag");
					int linkPid = condition.getInt("linkPid");
					CmgLinkSearchUtils linkSearchUtils = new CmgLinkSearchUtils(
							conn);
					List<CmgBuildlink> links = linkSearchUtils
							.getCloseTrackLinks(linkPid, cisFlag);
					for (CmgBuildlink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
			}
			return array;
		} catch (Exception e) {

			throw e;

		} finally {

		}
	}

	public JSONObject searchDataByObject(JSONObject condition) throws Exception {

		ObjectSearchUtils objectSearchUtils = new ObjectSearchUtils(conn,
				getJsonConfig());

		JSONObject json = objectSearchUtils.searchObject(condition);

		return json;

	}

	public JSONObject searchLinkByNode(JSONObject condition) throws Exception {

		ObjectSearchUtils objectSearchUtils = new ObjectSearchUtils(conn,
				getJsonConfig());

		JSONObject json = objectSearchUtils.searchLinkByNode(condition);

		return json;

	}

	public JSONObject searchInfoByTileWithGap(List<ObjType> types, int x,
			int y, int z, int gap) throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {
				List<SearchSnapshot> list = null;

				ISearch search = factory.createSearch(type);
				list = search.searchDataByTileWithGap(x, y, z, gap);
				JSONArray array = new JSONArray();

				for (SearchSnapshot snap : list) {

					array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig());
				}

				json.accumulate(type.toString(), array, getJsonConfig());
			}
		} catch (Exception e) {

			throw e;

		} finally {
		}
		return json;
	}

	public static void main(String[] args) throws Exception {
		SearchProcess p = new SearchProcess();
		p.setArray(null);
		p.setDbId(13);
		int x = 442895;
		int y = 212474;
		int z = 19;
		List<ObjType> types = new ArrayList<ObjType>();
		types.add(ObjType.RDLINK);
		types.add(ObjType.RDNODE);
		types.add(ObjType.IXPOI);
		types.add(ObjType.ZONELINK);
		types.add(ObjType.LULINK);
		types.add(ObjType.ZONENODE);
		types.add(ObjType.ADADMIN);
		int gap = 10;

		JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);
		System.out.println(data);

		// parameter={"dbId":13,"gap":10,"types":["RDLINK","RDNODE","IXPOI","ADLINK","ZONELINK","LULINK","ZONENODE","ADADMIN"],"x":442895,"y":212474,"z":19}

		/*
		 * JSONObject json = new JSONObject();
		 * 
		 * String str1 =
		 * "{\"ZONELINK\":[{\"i\":401000024,\"m\":{\"a\":406000027,\"b\":408000021}},{\"i\":400000017,\"m\":{\"a\":401000020,\"b\":409000013}}],\"ZONENODE\":[{\"i\":401000020,\"m\":{\"a\":\"400000017\"}},{\"i\":406000027,\"m\":{\"a\":\"401000024\"}}],\"ZONEFACE\":[]}"
		 * ; JSONObject obj1 = JSONObject.fromObject(str1); String str2 =
		 * "{\"ZONELINK\":[{\"i\":401000024,\"m\":{\"a\":406000027,\"b\":408000021}},{\"i\":400000017,\"m\":{\"a\":401000020,\"b\":409000013}}],\"ZONENODE\":[{\"i\":401000020,\"m\":{\"a\":\"400000017\"}},{\"i\":406000027,\"m\":{\"a\":\"401000024\"}}],\"ZONEFACE\":[]}"
		 * ; JSONObject obj2 = JSONObject.fromObject(str2);
		 * System.out.println(obj2); System.out.println(obj1);
		 * obj1.accumulateAll(obj2); System.out.println(obj1);
		 * 
		 * Map<String, List<SearchSnapshot>> map = new HashMap<String,
		 * List<SearchSnapshot>>();
		 * 
		 * List<SearchSnapshot> list1 = new ArrayList<SearchSnapshot>();
		 * List<SearchSnapshot> list2 = new ArrayList<SearchSnapshot>();
		 * SearchSnapshot snapshot11 = new SearchSnapshot();
		 * snapshot11.setI(1101); SearchSnapshot snapshot12 = new
		 * SearchSnapshot(); snapshot12.setI(1102); // list1.add(snapshot11); //
		 * list1.add(snapshot12);
		 * 
		 * SearchSnapshot snapshot21 = new SearchSnapshot();
		 * snapshot21.setI(2101); SearchSnapshot snapshot22 = new
		 * SearchSnapshot(); snapshot22.setI(1102); // list2.add(snapshot21); //
		 * list2.add(snapshot22);
		 * 
		 * List<List<SearchSnapshot>> lists = new
		 * ArrayList<List<SearchSnapshot>>();
		 * 
		 * lists.add(list1); lists.add(list2);
		 * 
		 * List<ObjType> types = new ArrayList<ObjType>();
		 * types.add(ObjType.ADLINK); types.add(ObjType.ADLINK); for (int i = 0;
		 * i < lists.size(); i++) { for (ObjType type : types) { if
		 * (map.containsKey(type.toString())) { List<SearchSnapshot> snapshots =
		 * map.get(type.toString());
		 * 
		 * for (SearchSnapshot snapshot : lists.get(i)) { if
		 * (!snapshots.contains(snapshot)) { snapshots.add(snapshot); }
		 * 
		 * } } else { map.put(type.toString(), lists.get(i)); } } } for
		 * (Map.Entry<String, List<SearchSnapshot>> entry : map.entrySet()) {
		 * JSONArray array = new JSONArray();
		 * 
		 * for (SearchSnapshot snap : entry.getValue()) {
		 * 
		 * try { array.add(snap.Serialize(ObjLevel.BRIEF), getJsonConfig()); }
		 * catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 * 
		 * json.accumulate(entry.getKey(), array, getJsonConfig());
		 * 
		 * } System.out.println(json);
		 */}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
}
