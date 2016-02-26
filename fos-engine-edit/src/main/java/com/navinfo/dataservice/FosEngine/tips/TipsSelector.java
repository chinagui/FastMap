package com.navinfo.dataservice.FosEngine.tips;

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

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.search.SearchSnapshot;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.GridUtils;
import com.navinfo.dataservice.solr.core.SConnection;

/**
 * Tips查询
 */
public class TipsSelector {

	private SConnection conn;

	public TipsSelector(String solrUrl) {
		conn = new SConnection(solrUrl);
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
	 * 范围查询Tips
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

				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				if (type == 1201) {
					m.put("c", String.valueOf(deep.getInt("kind")));
				} else if (type == 2001 || type == 1901) {

					JSONObject geo = deep.getJSONObject("geo");

					Geojson.coord2Pixel(geo, z, px, py);

					m.put("c", geo.getJSONArray("coordinates"));
				} else if (type == 1203 || type == 1101 || type == 1407) {

					m.put("c", String.valueOf(deep.getDouble("agl")));

				} else if (type == 1510) {

					JSONObject gSLoc = deep.getJSONObject("gSLoc");

					Geojson.coord2Pixel(gSLoc, z, px, py);

					m.put("c", gSLoc.getJSONArray("coordinates"));

					JSONObject gELoc = deep.getJSONObject("gELoc");

					Geojson.coord2Pixel(gELoc, z, px, py);

					m.put("d", gELoc.getJSONArray("coordinates"));
				}

				snapshot.setM(m);

				array.add(snapshot.Serialize(null));

			}
		} catch (Exception e) {
			throw e;
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

			final GetRequest get = new GetRequest("tips", rowkey);

			ArrayList<KeyValue> list = HBaseAddress.getHBaseClient().get(get)
					.joinUninterruptibly();

			json.put("rowkey", rowkey);

			for (KeyValue kv : list) {
				JSONObject injson = JSONObject
						.fromObject(new String(kv.value()));

				json.putAll(injson);
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
			int projectId) throws Exception {
		JSONArray jsonData = new JSONArray();

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, type, stages);

		Map<Integer, String> map = null;

		if (type == 1201 || type == 1302 || type == 1203 || type == 1101
				|| type == 1301 || type == 1407 || type == 1604) {

			Set<Integer> linkPids = new HashSet<Integer>();

			for (JSONObject json : tips) {
				JSONObject deep = JSONObject.fromObject(json.getString("deep"));

				if (type == 1201 || type == 1203 || type == 1101) {
					JSONObject f = deep.getJSONObject("f");

					if (f.getInt("type") == 1) {
						linkPids.add(Integer.valueOf(f.getString("id")));
					}
				}

				else if (type == 1301 || type == 1407 || type == 1302) {
					JSONObject f = deep.getJSONObject("in");

					if (f.getInt("type") == 1) {
						linkPids.add(Integer.valueOf(f.getString("id")));
					}
				} else if (type == 1604) {
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

				oraConn = DBOraclePoolManager.getConnection(projectId);

				RdLinkSelector selector = new RdLinkSelector(oraConn);

				map = selector.loadNameByLinkPids(linkPids);

			} catch (Exception e) {

				throw e;
			} finally {
				try {
					oraConn.close();
				} catch (Exception e) {

				}
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

			JSONObject deep = JSONObject.fromObject(json.getString("deep"));

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

			else if (type == 1301 || type == 1407 || type == 1302) {
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
			} else if (type == 1604) {
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
			} else if (type == 1704 || type == 1510) {

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
		ConfigLoader
				.initDBConn("C:/Users/wangshishuai3966/Desktop/config.properties");
		TipsSelector selector = new TipsSelector(
				"http://192.168.4.130:8081/solr/tips/");
		JSONArray a = JSONArray
				.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
		JSONArray b = new JSONArray();
		b.add(1);
		int type = 1101;
		System.out.println(selector.getSnapshot(a, b, type, 11));
		// System.out.println(selector.getStats(a, b));

		// JSONArray types = new JSONArray();
		// types.add(1301);
		// types.add(1901);
		// System.out.println(selector.searchDataByTileWithGap(107944, 49615,
		// 17,
		// 20, types));
	}
}
