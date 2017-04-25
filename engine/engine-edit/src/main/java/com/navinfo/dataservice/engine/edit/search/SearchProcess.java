package com.navinfo.dataservice.engine.edit.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminTreeSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ADLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.LcLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.LuLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ObjectSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.search.rd.utils.ZoneLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

/**
 * 查询进程
 */
public class SearchProcess {

	private Connection conn;

	public SearchProcess(Connection conn) throws Exception {

		this.conn = conn;

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

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);

				List<SearchSnapshot> list = search.searchDataByTileWithGap(x,
						y, z, gap);

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

						int speedDependnt = -1;

						if (condition.containsKey("speedDependnt")) {
							speedDependnt = condition.getInt("speedDependnt");
						}

						List<Integer> nextLinkPids = searchUtils
								.getConnectLinks(linkPid, direct, speedDependnt);

						JSONArray linkPidsArray = new JSONArray();

						for (int pid : nextLinkPids) {
							linkPidsArray.add(pid);
						}

						array.add(linkPidsArray);

						JSONArray speedlimitArray = searchUtils
								.getRdLinkSpeedlimit(nextLinkPids,
										speedDependnt);

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
				//追踪闭合的面 1 顺时针 2 逆时针
				if(condition.containsKey("cisFlag")){
					int cisFlag  = condition.getInt("cisFlag");
					int linkPid =  condition.getInt("linkPid");
					RdLinkSearchUtils linkSearchUtils = new RdLinkSearchUtils(conn);
					List<RdLink> links = linkSearchUtils.getCloseTrackLinks(linkPid, cisFlag);
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
				//追踪闭合的面 1 顺时针 2 逆时针
				if(condition.containsKey("cisFlag")){
					int cisFlag  = condition.getInt("cisFlag");
					int linkPid =  condition.getInt("linkPid");
					ADLinkSearchUtils linkSearchUtils = new ADLinkSearchUtils(conn);
					List<AdLink> links = linkSearchUtils.getCloseTrackLinks(linkPid, cisFlag);
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
				//追踪闭合的面 1 顺时针 2 逆时针
				if(condition.containsKey("cisFlag")){
					int cisFlag  = condition.getInt("cisFlag");
					int linkPid =  condition.getInt("linkPid");
					ZoneLinkSearchUtils linkSearchUtils = new ZoneLinkSearchUtils(conn);
					List<ZoneLink> links = linkSearchUtils.getCloseTrackLinks(linkPid, cisFlag);
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
				//追踪闭合的面 1 顺时针 2 逆时针
				if(condition.containsKey("cisFlag")){
					int cisFlag  = condition.getInt("cisFlag");
					int linkPid =  condition.getInt("linkPid");
					LuLinkSearchUtils linkSearchUtils = new LuLinkSearchUtils(conn);
					List<LuLink> links = linkSearchUtils.getCloseTrackLinks(linkPid, cisFlag);
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
				//追踪闭合的面 1 顺时针 2 逆时针
				if(condition.containsKey("cisFlag")){
					int cisFlag  = condition.getInt("cisFlag");
					int linkPid =  condition.getInt("linkPid");
					LcLinkSearchUtils linkSearchUtils = new LcLinkSearchUtils(conn);
					List<LcLink> links = linkSearchUtils.getCloseTrackLinks(linkPid, cisFlag);
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

					// 计算经过线
					List<Integer> viaList = calLinkOperateUtils.calViaLinks(
							this.conn, inLinkPid, nodePid, outLinkPid);

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
						obj.put("errInfo", "所选进入线、进入点、退出线不连通");
					}

					JSONArray viaArray = new JSONArray();

					if (CollectionUtils.isNotEmpty(viaList)) {
						for (Integer via : viaList) {
							viaArray.add(via);
						}

					}
					obj.put("links", viaArray);
					array.add(obj);

					return array;
				}
				if (!condition.containsKey("nodePid")) {
					int inLinkPid = condition.getInt("inLinkPid");
					int outLinkPid = condition.getInt("outLinkPid");
					int nodePid = 0;
					RdLinkSelector linkSelector = new RdLinkSelector(conn);
					IRow row = linkSelector.loadById(inLinkPid, true, true);
					RdLink link = (RdLink) row;
					List<Integer> viaList = new ArrayList<Integer>();
					if (link.getDirect() == 2) {
						nodePid = link.geteNodePid();
						viaList = calLinkOperateUtils.calViaLinks(this.conn,
								inLinkPid, nodePid, outLinkPid);
					}
					if (link.getDirect() == 3) {
						nodePid = link.getsNodePid();
						viaList = calLinkOperateUtils.calViaLinks(this.conn,
								inLinkPid, nodePid, outLinkPid);
					}
					if (link.getDirect() == 1) {
						List<Integer> sviaList = calLinkOperateUtils
								.calViaLinks(this.conn, inLinkPid,
										link.getsNodePid(), outLinkPid);
						List<Integer> eviaList = calLinkOperateUtils
								.calViaLinks(this.conn, inLinkPid,
										link.geteNodePid(), outLinkPid);
						if (sviaList.size() == 0 && eviaList.size() == 0) {
							viaList = sviaList;
						}
						if (sviaList.size() == 0 && eviaList.size() > 0) {
							viaList = eviaList;
						}
						if (eviaList.size() == 0 && sviaList.size() > 0) {
							viaList = sviaList;
						}
						if (eviaList.size() > 0 && sviaList.size() > 0) {
							double eLength = linkSelector.loadByPidsLength(
									eviaList, true);
							double sLength = linkSelector.loadByPidsLength(
									eviaList, true);
							viaList = (eLength >= sLength) ? sviaList
									: eviaList;

						}

					}
					// 计算经过线

					for (Integer pid : viaList) {
						array.add(pid);
					}
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
                    CmgBuildlinkSelector selector = new CmgBuildlinkSelector(this.conn);
                    List<CmgBuildlink> cmglinks = selector.listTheAssociatedLinkOfTheNode(nodePid, false);
                    for (CmgBuildlink link : cmglinks) {
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
}
