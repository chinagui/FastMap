package com.navinfo.dataservice.engine.edit.edit.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminTreeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.search.rd.utils.RdLinkSearchUtils;

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

					array.add(cross.Serialize(ObjLevel.FULL));

				}
				break;
				
			case RDLINK:
				if (condition.containsKey("nodePid")) {

					int nodePid = condition.getInt("nodePid");

					RdLinkSelector selector = new RdLinkSelector(this.conn);

					List<RdLink> links = selector.loadByNodePid(nodePid, false);

					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				else if (condition.containsKey("nodePidDir")){
					int cruuentNodePidDir = condition.getInt("nodePidDir");
					int cuurentLinkPid =  condition.getInt("linkPid");
					RdLinkSearchUtils searchUtils = new RdLinkSearchUtils(conn);
					List<RdLink>links = searchUtils.getNextTrackLinks(cuurentLinkPid, cruuentNodePidDir);
					for (RdLink link : links) {
						array.add(link.Serialize(ObjLevel.BRIEF));
					}
				}
				else if (condition.containsKey("linkPids")){
					JSONArray linkPids = condition.getJSONArray("linkPids");
					
					List<Integer> pids = new ArrayList<Integer>();
					
					for(int i=0;i<linkPids.size();i++){
						int pid = linkPids.getInt(i);
						
						if(!pids.contains(pid)){
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

					RdBranchSelector selector = new RdBranchSelector(conn);
					
					IRow row = selector.loadByDetailId(detailId, false);
					
					array.add(row.Serialize(ObjLevel.FULL));
				}
			case ADADMINGROUP:
				if(condition.containsKey("projectId"))
				{
					AdAdminTreeSelector adAdminTreeSelector = new AdAdminTreeSelector(conn);
					
					int projectId = condition.getInt("projectId");
					
					IRow row = adAdminTreeSelector.loadRowsByProjectId(projectId,false);

					array.add(row.Serialize(ObjLevel.BRIEF));
				}
			case ADLINK:
				if (condition.containsKey("nodePid"))
			{
				int nodePid = condition.getInt("nodePid");
				AdLinkSelector selector  = new AdLinkSelector(this.conn);
				List<AdLink> adLinks   =selector.loadByNodePid(nodePid, true);
				for (AdLink link : adLinks) {
					array.add(link.Serialize(ObjLevel.BRIEF));
				}
			}
			
				break;
			}

			return array;
		}
		 catch (Exception e) {

			throw e;

		} finally {

		}
	}
//	public static void main(String[] args) throws Exception {
//		Connection conn = DBConnector.getInstance().getConnectionById(11);
//		SearchProcess p = new SearchProcess(conn);
//		
//		JSONObject condition = new JSONObject();
//		JSONArray pid = new JSONArray();
//		pid.add(13474060);
//		pid.add(13474059);
//		condition.put("linkPids", pid);
//		System.out.println(p.searchDataByCondition(ObjType.RDLINK, condition));
//	}
}
