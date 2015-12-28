package com.navinfo.dataservice.FosEngine.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.FosEngine.edit.search.SearchSnapshot;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.GridUtils;
import com.navinfo.dataservice.solr.core.SConnection;

/**
 * Tips查询
 */
public class TipsSelector {
	
	private SConnection solrConn;
	
	public TipsSelector(String solrUrl){
		solrConn = new SConnection(solrUrl);
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

		SConnection conn = new SConnection(
				"http://192.168.4.130:8081/solr/tips/");

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
	public JSONArray searchDataByTileWithGap(int x, int y, int z, int gap)
			throws Exception {
		JSONArray array = new JSONArray();

		try {

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			SConnection conn = new SConnection(
					"http://192.168.4.130:8081/solr/tips/");

			List<JSONObject> snapshots = conn.queryTipsWeb(wkt);

			for (JSONObject json : snapshots) {
				
				SearchSnapshot snapshot = new SearchSnapshot();
				
				snapshot.setI(json.getString("id"));

				snapshot.setT(1);
				
				JSONObject geojson = JSONObject.fromObject(json.getString("g_location"));

				Geojson.point2Pixel(geojson, z, px, py);
				
				snapshot.setG(geojson.getJSONArray("coordinates"));

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
	public JSONArray searchDataByCondition(String condition)
			throws Exception {
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

		SConnection conn = new SConnection(
				"http://192.168.4.130:8081/solr/tips/");

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt, stages);

		for (JSONObject json : tips) {
			int type = Integer.valueOf(json.getInt("s_sourceType"));

			if (map.containsKey(type)){
				map.put(type, map.get(type) + 1);
			}
			else{
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
	public JSONArray getSnapshot(JSONArray grids, JSONArray stages,
			int type) throws Exception {
		JSONArray jsonData = new JSONArray();

		SConnection conn = new SConnection(
				"http://192.168.4.130:8081/solr/tips/");

		String wkt = GridUtils.grids2Wkt(grids);

		List<JSONObject> tips = conn.queryTipsWeb(wkt,
				type, stages);

		for (JSONObject json : tips) {
			
			SearchSnapshot snapshot = new SearchSnapshot();
			
			snapshot.setI(json.getString("id"));

			snapshot.setT(1);
			
			snapshot.setG(JSONObject.fromObject(json.getString("g_location")).getJSONArray("coordinates"));
			
			JSONObject m = new JSONObject();
			
			m.put("a", json.getString("stage"));
			
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
	public boolean checkUpdate(String grid, String date)
			throws Exception {

		String wkt = GridUtils.grid2Wkt(grid);

		SConnection conn = new SConnection(
				"http://192.168.4.130:8081/solr/tips/");

		boolean flag = conn.checkTipsMobile(wkt, date);

		return flag;
	}

	public static void main(String[] args) throws Exception {
		// JSONArray ja =
		// searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

		// System.out.println(ja.size());

		// System.out.println(checkUpdate("59567201","20151227163723"));

		TipsSelector selector = new TipsSelector("http://192.168.4.130:8081/solr/tips/");
		JSONArray a = new JSONArray();
		a.add(59567201);
		JSONArray b = new JSONArray();
		b.add(1);
		int type = 1407;
		//System.out.println(selector.getSnapshot(a, b, type));
		System.out.println(selector.getStats(a, b));
	}
}
