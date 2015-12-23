package com.navinfo.dataservice.FosEngine.tips;

import java.lang.reflect.Field;
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
import org.hbase.async.Scanner;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.edit.search.SearchSnapshot;
import com.navinfo.dataservice.commons.constant.BusinessConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.GeohashUtils;
import com.navinfo.dataservice.commons.util.GridUtils;

/**
 * Tips查询
 */
public class TipsSelector {

	/**
	 * 范围查询Tips
	 * 
	 * @param wkt
	 * @return Tips JSON数组
	 * @throws Exception
	 */
	public static JSONArray searchDataBySpatial(String wkt) throws Exception {
		JSONArray array = new JSONArray();

		try {

			double mbr[] = GeoTranslator.getMBR(wkt);

			String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[1], mbr[0], 12);

			String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[3], mbr[2], 12) + "{";

			Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

			scanner.setStartKey(startRowkey);

			scanner.setStopKey(stopRowkey);

			scanner.setFamily("data");

			scanner.setQualifier("geometry");

			byte[][] qs = new byte[2][];

			qs[0] = "geometry".getBytes();

			qs[1] = "deep".getBytes();

			scanner.setQualifiers(qs);

			ArrayList<ArrayList<KeyValue>> rows;

			while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

				for (List<KeyValue> list : rows) {

					JSONObject json = new JSONObject();
					for (KeyValue kv : list) {

						if ("geometry".equals(new String(kv.qualifier()))) {
							JSONObject jsonGeom = JSONObject
									.fromObject(new String(kv.value()));

							json.put("id", new String(kv.key()));

							json.put("type", "tips");

							json.put("g", jsonGeom.getJSONObject("g_location"));

						} else {
							JSONObject jsonDeep = JSONObject
									.fromObject(new String(kv.value()));

							JSONArray info = jsonDeep.getJSONArray("info");

							json.put("info", info);
						}

					}
					array.add(json);
				}
			}
		} catch (Exception e) {

			throw e;
		}

		return array;
	}

	/**
	 * 范围查询Tips
	 * 
	 * @throws Exception
	 */
	public static JSONArray searchDataByTileWithGap(int x, int y, int z, int gap)
			throws Exception {
		JSONArray array = new JSONArray();

		try {

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			double mbr[] = GeoTranslator.getMBR(wkt);

			String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[1], mbr[0], 12);

			String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[3], mbr[2], 12) + "{";

			Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

			scanner.setStartKey(startRowkey);

			scanner.setStopKey(stopRowkey);

			scanner.setFamily("data");

			byte[][] qs = new byte[2][];

			qs[0] = "geometry".getBytes();

			qs[1] = "track".getBytes();

			scanner.setQualifiers(qs);

			ArrayList<ArrayList<KeyValue>> rows = null;

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

				for (List<KeyValue> list : rows) {

					boolean flag = false;

					SearchSnapshot snapshot = new SearchSnapshot();

					for (KeyValue kv : list) {

						String rowkey = new String(kv.key());

						if ("geometry".equals(new String(kv.qualifier()))) {
							JSONObject jsonGeom = JSONObject
									.fromObject(new String(kv.value()));

							snapshot.setI(rowkey);

							snapshot.setT(1);

							JSONObject geojson = jsonGeom
									.getJSONObject("g_location");

							Geojson.point2Pixel(geojson, z, px, py);

							snapshot.setG(geojson.getJSONArray("coordinates"));

						} else if ("track".equals(new String(kv.qualifier()))) {
							JSONObject jsonTrack = JSONObject
									.fromObject(new String(kv.value()));

							JSONArray tTrackInfo = jsonTrack
									.getJSONArray("t_trackInfo");

							int stage = tTrackInfo.getJSONObject(
									tTrackInfo.size() - 1).getInt("stage");

							if (stage != 1 && stage != 3) {
								break;
							}

							JSONObject jo = new JSONObject();

							jo.put("a", String.valueOf(stage));

							snapshot.setM(jo);

							flag = true;
						}
					}
					if (flag) {
						array.add(snapshot.Serialize(null));
					}
				}
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
	public static JSONObject searchDataByRowkey(String rowkey) throws Exception {
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
	public static JSONArray searchDataByCondition(String condition)
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
	public static JSONObject getStats(JSONArray grids, JSONArray stages)
			throws Exception {
		JSONObject jsonData = new JSONObject();

		String enclosingGeohash[] = GridUtils.getEnclosingRectangle(grids);

		String startRowkey = enclosingGeohash[0];

		String stopRowkey = enclosingGeohash[1] + "{";

		Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

		scanner.setStartKey(startRowkey);

		scanner.setStopKey(stopRowkey);

		scanner.setFamily("data");

		scanner.setQualifier("track");

		ArrayList<ArrayList<KeyValue>> rows = null;

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		Class<BusinessConstant> bc = BusinessConstant.class;

		Field[] fields = bc.getFields();

		for (Field f : fields) {
			if (f.getName().startsWith("tips")) {
				map.put(Integer.valueOf((String) f.get(bc)), 0);
			}
		}

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (List<KeyValue> list : rows) {

				for (KeyValue kv : list) {

					String rowkey = new String(kv.key());

					double[] lonlat = GridUtils.geohash2Lonlat(rowkey
							.substring(0, 12));

					if (GridUtils.isInGrids(lonlat[0], lonlat[1], grids)) {

						JSONObject jsonTrack = JSONObject
								.fromObject(new String(kv.value()));

						JSONArray tTrackInfo = jsonTrack
								.getJSONArray("t_trackInfo");

						int stage = tTrackInfo.getJSONObject(
								tTrackInfo.size() - 1).getInt("stage");

						if (stages.contains(stage)) {

							int type = Integer
									.valueOf(rowkey.substring(12, 14));

							map.put(type, map.get(type) + 1);
						}
					}

				}
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
	public static JSONArray getSnapshot(JSONArray grids, JSONArray stages,
			int type) throws Exception {
		JSONArray jsonData = new JSONArray();

		String enclosingGeohash[] = GridUtils.getEnclosingRectangle(grids);

		String strType = String.format("%02d", type);

		String startRowkey = enclosingGeohash[0] + strType;

		String stopRowkey = enclosingGeohash[1] + strType + "{";

		Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

		scanner.setStartKey(startRowkey);

		scanner.setStopKey(stopRowkey);

		scanner.setFamily("data");

		byte[][] qs = new byte[2][];

		qs[0] = "geometry".getBytes();

		qs[1] = "track".getBytes();

		scanner.setQualifiers(qs);

		ArrayList<ArrayList<KeyValue>> rows = null;

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (List<KeyValue> list : rows) {

				boolean flag = false;

				JSONArray coords = null;

				SearchSnapshot snapshot = new SearchSnapshot();

				for (KeyValue kv : list) {

					String rowkey = new String(kv.key());

					double[] lonlat = GridUtils.geohash2Lonlat(rowkey
							.substring(0, 12));

					if ("track".equals(new String(kv.qualifier()))) {
						if (GridUtils.isInGrids(lonlat[0], lonlat[1], grids)) {

							JSONObject jsonTrack = JSONObject
									.fromObject(new String(kv.value()));

							JSONArray tTrackInfo = jsonTrack
									.getJSONArray("t_trackInfo");

							int stage = tTrackInfo.getJSONObject(
									tTrackInfo.size() - 1).getInt("stage");

							if (stages.contains(stage)) {

								snapshot.setI(rowkey);

								snapshot.setT(type);

								JSONObject jm = new JSONObject();

								jm.put("a", String.valueOf(stage));

								snapshot.setM(jm);

								flag = true;
							}
						}
					} else if ("geometry".equals(new String(kv.qualifier()))) {

						coords = JSONObject.fromObject(new String(kv.value()))
								.getJSONObject("g_location")
								.getJSONArray("coordinates");
					}

					snapshot.setG(coords);

				}

				if (flag) {
					jsonData.add(snapshot);
				}
			}

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
	public static boolean checkUpdate(String grid, String date)
			throws Exception {
		boolean flag = false;

		double mbr[] = GridUtils.grid2Location(grid);

		String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
				mbr[1], mbr[0], 12);

		String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(mbr[3],
				mbr[2], 12) + "{";

		Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

		scanner.setStartKey(startRowkey);

		scanner.setStopKey(stopRowkey);

		scanner.setFamily("data");

		scanner.setQualifier("track");

		ArrayList<ArrayList<KeyValue>> rows = null;

		JSONArray grids = new JSONArray();

		grids.add(grid);

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (List<KeyValue> list : rows) {

				for (KeyValue kv : list) {

					String rowkey = new String(kv.key());

					if (!GeohashUtils.isWithin(rowkey, mbr[0], mbr[2], mbr[1],
							mbr[3])) {
						break;
					}

					if ("track".equals(new String(kv.qualifier()))) {

						JSONObject jo = JSONObject.fromObject(new String(kv
								.value()));

						JSONArray tTrackInfo = jo.getJSONArray("t_trackInfo");

						String lastDate = tTrackInfo.getJSONObject(
								tTrackInfo.size() - 1).getString("date");

						if (date.compareTo(lastDate) < 0) {
							flag = true;
						}

					}
				}

				if (flag) {
					break;
				}
			}

		}

		return flag;
	}

}
