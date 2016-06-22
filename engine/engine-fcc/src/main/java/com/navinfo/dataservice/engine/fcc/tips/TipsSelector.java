package com.navinfo.dataservice.engine.fcc.tips;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.hbase.async.KeyValue;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.HBaseController;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.GridUtils;

/**
 * Tips查询
 */
public class TipsSelector {

	private SolrController conn = new SolrController();

	public TipsSelector() {
	}

	/**
	 * 范围查询Tips
	 * 
	 * @param wkt
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataBySpatial(String wkt) throws Exception {
		JSONArray array = new JSONArray();

		List<JSONObject> snapshots = conn.queryTipsWeb(wkt);

		for (JSONObject snapshot : snapshots) {

			snapshot.put("t", 1);

			array.add(snapshot);
		}

		return array;
	}

	/**
	 * 范围瓦片查询Tips
	 * 
	 * @throws Exception
	 */
	public JSONArray searchDataByTileWithGap(int x, int y, int z, int gap,
			JSONArray types) throws Exception {
		JSONArray array = new JSONArray();

		try {

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			List<JSONObject> snapshots = conn.queryTipsWebType(wkt, types);

			for (JSONObject json : snapshots) {

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(json.getString("id"));

				int type = Integer.valueOf(json.getString("s_sourceType"));

				snapshot.setT(type);

				JSONObject geojson = JSONObject.fromObject(json
						.getString("g_location"));

				Geojson.coord2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				JSONObject m = new JSONObject();

				m.put("a", json.getString("stage"));

				m.put("b", json.getString("t_lifecycle"));
				
				JSONObject g_guide = JSONObject.fromObject(json
						.getString("g_guide"));
				
				m.put("h", g_guide.getJSONArray("coordinates"));

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				if (type == 1201) {
					m.put("c", String.valueOf(deep.getInt("kind")));
				} else if (type == 2001 || type == 1901) {

					JSONObject geo = deep.getJSONObject("geo");

					Geojson.coord2Pixel(geo, z, px, py);

					m.put("c", geo.getJSONArray("coordinates"));
				} else if (type == 1203 || type == 1101 || type == 1407
						|| type == 1403 || type == 1401 || type == 1402
						|| type == 1405 || type == 1406 || type == 1409
						|| type == 1105 || type == 1109 || type == 1107
						|| type ==1110 ) {

					m.put("c", String.valueOf(deep.getDouble("agl")));

					if (type == 1203) {
						m.put("d", String.valueOf(deep.get("dr")));
					}
					else if (type == 1105) {
						m.put("d", String.valueOf(deep.get("tp")));
					}
					else if(type == 1107){
						m.put("d", deep.getString("name"));
					}
					else if(type == 1109){
						
						String tp = TipsSelectorUtils.convertElecEyeKind(deep.getInt("tp"));
						
						String loc = TipsSelectorUtils.convertElecEyeLocation(deep.getInt("loc"));
						
						double value = deep.getDouble("value");
						
						String d = tp + "|" + loc;
						
						if((int)value != 0){
							d += "|" + value;
						}
						
						m.put("d", d);
					}
					

				} else if (type == 1510 || type == 1514 || type == 1501 || type == 1515) {

					JSONObject gSLoc = deep.getJSONObject("gSLoc");

					Geojson.coord2Pixel(gSLoc, z, px, py);

					m.put("c", gSLoc.getJSONArray("coordinates"));

					JSONObject gELoc = deep.getJSONObject("gELoc");

					Geojson.coord2Pixel(gELoc, z, px, py);

					m.put("d", gELoc.getJSONArray("coordinates"));
				}else if (type == 1801 || type == 1803 || type ==  1806){
					JSONArray feedbacks = JSONArray.fromObject(json.getString("feedback"));
					
					JSONArray a = new JSONArray();
					
					for(int j=0; j<feedbacks.size();j++){
						JSONObject feedback = feedbacks.getJSONObject(j);
						
						if(feedback.getInt("type") == 6){
							JSONArray content = feedback.getJSONArray("content");
							
							for(int i=0;i<content.size();i++){
								JSONObject obj = content.getJSONObject(i);
								
								JSONObject geo = obj.getJSONObject("geo");
								
								String style = obj.getString("style");
								
								JSONObject o = new JSONObject();
								
								Geojson.coord2Pixel(geo, z, px, py);
								
								o.put("g", geo.getJSONArray("coordinates"));
								
								o.put("s", style);
								
								a.add(o);
							}
							
							break;
						}
					}
					m.put("c", a);
				}

				snapshot.setM(m);

				array.add(snapshot.Serialize(null));

			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
		}
		return array;
	}

	/**
	 * 通过rowkey获取Tips
	 * 
	 * @param rowkey
	 * @return Tips JSON对象
	 * @throws Exception
	 */
	public JSONObject searchDataByRowkey(String rowkey) throws Exception {
		JSONObject json = new JSONObject();

		try {

			HBaseController controller = new HBaseController();

			ArrayList<KeyValue> list = controller.getTipsByRowkey(rowkey);
			

			json.put("rowkey", rowkey);

			for (KeyValue kv : list) {
				JSONObject injson = JSONObject
						.fromObject(new String(kv.value()));

				String key = new String(kv.qualifier());

				if (key.equals("feedback")) {
					json.put("feedback", injson);
				} else {
					json.putAll(injson);
				}
			}

		} catch (Exception e) {

			throw e;
		}

		return json;
	}

	/**
	 * 通过条件查询Tips
	 * 
	 * @param condition
	 *            查询条件
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public JSONArray searchDataByCondition(String condition) throws Exception {
		JSONArray array = new JSONArray();

		return array;
	}

	/**
	 * 统计tips
	 * 
	 * @param grids
	 * @param stages
	 * @return
	 * @throws Exception
	 */
	public JSONObject getStats(JSONArray grids, JSONArray stages)
			throws Exception {
		JSONObject jsonData = new JSONObject();

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, stages);

		for (JSONObject json : tips) {
			int type = Integer.valueOf(json.getInt("s_sourceType"));

			if (map.containsKey(type)) {
				map.put(type, map.get(type) + 1);
			} else {
				map.put(type, 1);
			}
		}

		JSONArray data = new JSONArray();

		Set<Entry<Integer, Integer>> set = map.entrySet();

		int num = 0;

		Iterator<Entry<Integer, Integer>> it = set.iterator();

		while (it.hasNext()) {
			Entry<Integer, Integer> en = it.next();

			num += en.getValue();

			JSONObject jo = new JSONObject();

			jo.put(en.getKey(), en.getValue());

			data.add(jo);
		}

		jsonData.put("total", num);

		jsonData.put("rows", data);

		return jsonData;
	}

	/**
	 * 获取单种类型快照
	 * 
	 * @param grids
	 * @param stages
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public JSONArray getSnapshot(JSONArray grids, JSONArray stages, int type,
			int dbId) throws Exception {
		JSONArray jsonData = new JSONArray();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, type, stages);

		Map<Integer, String> map = null;

		Set<Integer> linkPids = new HashSet<Integer>();

		//根据tip类型不同，查询关联对象的pid(这里是关联link)，用于e字段结果
		for (JSONObject json : tips) {
			
			JSONObject deep = JSONObject.fromObject(json.getString("deep"));

			if (type == 1201 || type == 1203 || type == 1101 || type == 1109) {
				JSONObject f = deep.getJSONObject("f");

				if (f.getInt("type") == 1) {
					linkPids.add(Integer.valueOf(f.getString("id")));
				}
			}

			else if (type == 1301 || type == 1407 || type == 1302
					|| type == 1403|| type == 1401 || type == 1402
					|| type == 1405|| type == 1406 || type == 1409 
					|| type == 1105 || type == 1107 || type == 1703) {
				JSONObject f = deep.getJSONObject("in");

				if (f.getInt("type") == 1) {
					linkPids.add(Integer.valueOf(f.getString("id")));
				}
			}else if (type == 1110) {
				JSONObject f = deep.getJSONObject("out");

				if (f.getInt("type") == 1) {
					linkPids.add(Integer.valueOf(f.getString("id")));
				}
			} else if (type == 1604 || type == 1514||type==1515) {
				JSONArray a = deep.getJSONArray("f_array");

				for (int i = 0; i < a.size(); i++) {
					JSONObject f = a.getJSONObject(i);
					if (f.getInt("type") == 1) {
						linkPids.add(Integer.valueOf(f.getString("id")));
					}
				}
			}
		}

		Connection oraConn = null;

		try {

			oraConn = DBConnector.getInstance().getConnectionById(dbId);;

			RdLinkSelector selector = new RdLinkSelector(oraConn);
			
			//关联link的道路名 map<linkPid, name>
			map = selector.loadNameByLinkPids(linkPids);

		} catch (Exception e) {

			throw e;
		} finally {
			try {
				oraConn.close();
			} catch (Exception e) {

			}
		}

		for (JSONObject json : tips) {

			SearchSnapshot snapshot = new SearchSnapshot();

			snapshot.setI(json.getString("id"));

			snapshot.setT(Integer.valueOf(json.getString("s_sourceType")));

			JSONObject glocation = JSONObject.fromObject(json
					.getString("g_location"));

			snapshot.setG(glocation.getJSONArray("coordinates"));

			JSONObject m = new JSONObject();

			m.put("a", json.getString("stage"));

			m.put("b", json.getString("t_lifecycle"));

			String operateDate = json.getString("t_operateDate");

			m.put("f", DateUtils.stringToLong(operateDate, "yyyyMMddHHmmss"));

			JSONObject deep = JSONObject.fromObject(json.getString("deep"));
			
			//e字段的返回结果，不同类型不同s
			if (type == 1201 || type == 1203 || type == 1101) {
				JSONObject f = deep.getJSONObject("f");

				if (f.getInt("type") == 1) {

					String name = "无名路";

					int linkPid = Integer.valueOf(f.getString("id"));

					if (map.containsKey(linkPid)) {

						name = map.get(linkPid);

					}

					if (type == 1201) {

						int kind = deep.getInt("kind");

						name += "(K" + kind + ")";
					} else if (type == 1203) {

						int dr = deep.getInt("dr");

						if (dr == 1) {
							name += "(双方向)";
						} else {
							name += "(单方向)";
						}
					} else if (type == 1101) {

						double value = deep.getDouble("value");

						name += "(" + Math.round(value) + "km/h)";
					}

					m.put("e", name);

				} else {
					String name = "无道路";

					if (type == 1101) {

						double value = deep.getDouble("value");

						name += "(" + Math.round(value) + "km/h)";
					}

					m.put("e", name);
				}
			}

			else if (type == 1301 || type == 1407 || type == 1302
					|| type == 1403) {
				JSONObject f = deep.getJSONObject("in");

				if (f.getInt("type") == 1) {
					int linkPid = Integer.valueOf(f.getString("id"));

					if (map.containsKey(linkPid)) {

						String name = map.get(linkPid);

						m.put("e", name);
					} else {
						m.put("e", "无名路");
					}
				} else {
					m.put("e", "无道路");
				}
			} else if (type == 1604 || type == 1514 || type == 1515) {
				JSONArray a = deep.getJSONArray("f_array");

				boolean hasLink = false;

				for (int i = 0; i < a.size(); i++) {
					JSONObject f = a.getJSONObject(i);
					if (f.getInt("type") == 1) {

						hasLink = true;

						int linkPid = Integer.valueOf(f.getString("id"));

						if (map.containsKey(linkPid)) {

							String name = map.get(linkPid);

							m.put("e", name);

							break;
						}
					}
				}

				if (!hasLink) {
					m.put("e", "无道路");
				} else {
					if (!m.containsKey("e")) {
						m.put("e", "无名路");
					}
				}
				//1515和1514确认了不需要加时间段
		/*		if(type == 1515){
					String name = m.getString("e");
					
					String time = deep.getString("time");
					
					if(time!=null && !time.isEmpty()){
						name+="("+time+")";
						
						m.put("e", name);
					}
				}*/
			} else if (type == 1704 || type == 1510 || type == 1107) {

				String name = deep.getString("name");

				if (name.equals("null")) {
					if (type == 1510) {
						m.put("e", "无名桥");
					} else {
						m.put("e", name);
					}
				} else {
					m.put("e", name);
				}
			} else if (type == 2001) {

				double length = deep.getDouble("len");

				double lengthInKM = Math.round(length / 10) / 100.0;

				m.put("e", "测线(" + lengthInKM + "公里)");
			} else if (type == 1901) {

				JSONArray a = deep.getJSONArray("n_array");

				if (a.size() > 0) {
					m.put("e", a.get(0));
				}
			} else if (type == 1501){
				m.put("e", "上下线分离");
			} else if (type == 1801){
				m.put("e", "立交");
			}else if(type==1806){
				m.put("e", "草图");
			}else if(type==1205){
				m.put("e", "SA");
			}else if(type==1206){
				m.put("e", "PA");
			}

			

			if (!m.containsKey("e")) {
				m.put("e", JSONNull.getInstance());
			}

			snapshot.setM(m);

			jsonData.add(snapshot.Serialize(null));

		}

		return jsonData;
	}

	/**
	 * 根据grid和时间戳查询是否有可下载的数据
	 * 
	 * @param grid
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public boolean checkUpdate(String grid, String date) throws Exception {

		String wkt = GridUtils.grid2Wkt(grid);

		boolean flag = conn.checkTipsMobile(wkt, date);

		return flag;
	}

	public static void main(String[] args) throws Exception {
		// JSONArray ja =
		// searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

		// System.out.println(ja.size());

		// System.out.println(checkUpdate("59567201","20151227163723"));
//		ConfigLoader
//				.initDBConn("C:/Users/wangshishuai3966/Desktop/config.properties");
		TipsSelector selector = new TipsSelector();
		
//		System.out.println(selector.searchDataByRowkey("0212014bb47de20366413db30504af53243a00"));
		JSONArray grid = JSONArray
				.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
		System.out.println(grid);
		JSONArray stage = new JSONArray();
		stage.add(1);
		int type = 1101;
		int projectId=11;
		System.out.println(selector.getSnapshot(grid, stage, type, projectId));
		// System.out.println(selector.getStats(a, b));

		// JSONArray types = new JSONArray();
		// types.add(1301);
		// types.add(1901);
		// System.out.println(selector.searchDataByTileWithGap(107944, 49615,
		// 17,
		// 20, types));
	}
}
