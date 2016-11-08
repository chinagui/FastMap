package com.navinfo.dataservice.engine.edit.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminTreeSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

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

		jsonConfig.registerJsonValueProcessor(String.class, new JsonValueProcessor() {

			@Override
			public Object processObjectValue(String key, Object value, JsonConfig arg2) {
				if (value == null) {
					return null;
				}

				if (JSONUtils.mayBeJSON(value.toString())) {
					return "\"" + value + "\"";
				}

				return value;

			}

			@Override
			public Object processArrayValue(Object value, JsonConfig arg1) {
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
	public JSONObject searchDataBySpatial(List<ObjType> types, String box) throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);

				List<SearchSnapshot> list = search.searchDataBySpatial(box);

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
	public JSONObject searchDataByTileWithGap(List<ObjType> types, int x, int y, int z, int gap) throws Exception {

		JSONObject json = new JSONObject();

		SearchFactory factory = new SearchFactory(conn);

		try {

			for (ObjType type : types) {

				ISearch search = factory.createSearch(type);

				List<SearchSnapshot> list = search.searchDataByTileWithGap(x, y, z, gap);

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

	public JSONArray searchDataByCondition(ObjType type, JSONObject condition) throws Exception {

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

				if (condition.containsKey("queryType") && condition.containsKey("linkPid")
						&& condition.containsKey("direct")) {

					String queryType = condition.getString("queryType");

					if (queryType.equals("RDLINKSPEEDLIMIT") || queryType.equals("RDSPEEDLIMIT")) {

						int linkPid = condition.getInt("linkPid");

						int direct = condition.getInt("direct");

						RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(conn);

						List<Integer> nextLinkPids = searchUtils.getConnectLinks(linkPid, direct, queryType);

						JSONArray linkPidsArray = new JSONArray();

						for (int pid : nextLinkPids) {
							linkPidsArray.add(pid);
						}

						array.add(linkPidsArray);

						JSONArray speedlimitArray = searchUtils.getRdLinkSpeedlimit(nextLinkPids);

						array.add(speedlimitArray);
					}

					return array;
				}

				if (condition.containsKey("nodePid")) {

					int nodePid = condition.getInt("nodePid");

					RdLinkSelector selector = new RdLinkSelector(this.conn);

					List<RdLink> links = selector.loadByNodePid(nodePid, false);

					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				} else if (condition.containsKey("nodePidDir")) {
					int cruuentNodePidDir = condition.getInt("nodePidDir");
					int cuurentLinkPid = condition.getInt("linkPid");
					RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(conn);
					List<RdLink> links = searchUtils.getNextTrackLinks(cuurentLinkPid, cruuentNodePidDir);
					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
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

					IRow row = selector.loadByDetailId(detailId, branchType, rowId, false);

					array.add(row.Serialize(ObjLevel.FULL));
				}
				break;
			case ADADMINGROUP:
				if (condition.containsKey("subTaskId")) {
					AdAdminTreeSelector adAdminTreeSelector = new AdAdminTreeSelector(conn);

					int subTaskId = condition.getInt("subTaskId");

					IRow row = adAdminTreeSelector.loadRowsBySubTaskId(subTaskId, false);

					array.add(row.Serialize(ObjLevel.BRIEF));
				} else {
					throw new Exception("缺少子任务ID（subTaskId）参数");
				}
				break;
			case ADLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");
					AdLinkSelector selector = new AdLinkSelector(this.conn);
					List<AdLink> adLinks = selector.loadByNodePid(nodePid, true);
					for (AdLink link : adLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case RWLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");
					RwLinkSelector selector = new RwLinkSelector(this.conn);
					List<RwLink> rwLinks = selector.loadByNodePid(nodePid, true);
					for (RwLink link : rwLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case ZONELINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					ZoneLinkSelector selector = new ZoneLinkSelector(this.conn);

					List<ZoneLink> zoneLinks = selector.loadByNodePid(nodePid, true);

					for (ZoneLink link : zoneLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case LULINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					LuLinkSelector selector = new LuLinkSelector(this.conn);

					List<LuLink> luLinks = selector.loadByNodePid(nodePid, true);

					for (LuLink link : luLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case LCLINK:
				if (condition.containsKey("nodePid")) {
					int nodePid = condition.getInt("nodePid");

					LcLinkSelector selector = new LcLinkSelector(this.conn);

					List<LcLink> lcLinks = selector.loadByNodePid(nodePid, true);

					for (LcLink link : lcLinks) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				break;
			case RDOBJECTNAME:
				if (condition.containsKey("pid")) {
					int pid = condition.getInt("pid");

					RdObjectSelector selector = new RdObjectSelector(this.conn);

					List<String> names = selector.getRdObjectName(pid, true);

					for (String name : names) {
						array.add(name);
					}
				}
				break;
			case RDLANEVIA:
				if (condition.containsKey("inLinkPid") && condition.containsKey("nodePid")
						&& condition.containsKey("outLinkPid")) {

					int inLinkPid = condition.getInt("inLinkPid");

					//要素类型
					String objType = null;
					if(condition.containsKey("type"))
					{
						objType = condition.getString("type");
					}
					
					int nodePid = condition.getInt("nodePid");

					int outLinkPid = condition.getInt("outLinkPid");

					CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

					//计算经过线
					List<Integer> viaList = calLinkOperateUtils.calViaLinks(this.conn, inLinkPid, nodePid, outLinkPid);

					//计算关系类型
					int relationShipType = calLinkOperateUtils.getRelationShipType(conn, nodePid, outLinkPid);

					JSONObject obj = new JSONObject();

					if (CollectionUtils.isNotEmpty(viaList)) {
						JSONArray viaArray = new JSONArray();
						for(Integer via : viaList)
						{
							viaArray.add(via);
						}
						//路口关系交限不记经过link
						if (StringUtils.isNotEmpty(objType) && ObjType.valueOf(objType) == ObjType.RDRESTRICTION) {
							if (relationShipType == 2) {
								return array;
							}
							else
							{
								obj.put("relationshipType", relationShipType);

								obj.put("links", viaArray);

								array.add(obj);
							}
						} else {
							obj.put("relationshipType", relationShipType);

							obj.put("links", viaArray);

							array.add(obj);
						}
					}
					return array;
				}
				break;
			case RDLANE:
				if (condition.containsKey("linkPid")) {
					int linkPid = condition.getInt("linkPid");
					int laneDir = condition.getInt("laneDir");
					RdLaneSelector selector = new RdLaneSelector(this.conn);
					List<RdLane> lanes = selector.loadByLink(linkPid, laneDir, false);
					for (RdLane lane : lanes) {
						array.add(lane);
					}

				}
				if (condition.containsKey("linkPids")) {
					JSONArray arrayTopo = new JSONArray();
					JSONObject object = new JSONObject();
					JsonUtils.getStringValueFromJSONArray(condition.getJSONArray("linkPids"));

					@SuppressWarnings("unchecked")
					List<Integer> pids = (List<Integer>) JSONArray.toCollection(condition.getJSONArray("linkPids"),
							Integer.class);
					RdLaneSelector selector = new RdLaneSelector(this.conn);
					RdLaneTopoDetailSelector detailSelector = new RdLaneTopoDetailSelector(conn);
					List<IRow> rows = detailSelector.loadByLinkPids(pids, condition.getInt("nodePid"), false);
					object.put("laneInfos", selector.loadByLinks(pids, false));
					for (IRow row : rows) {
						arrayTopo.add(row);
					}
					object.put("laneTopoInfos", arrayTopo);
					array.add(object);

				}

			}
			return array;
		} catch (Exception e) {

			throw e;

		} finally {

		}
	}
}
