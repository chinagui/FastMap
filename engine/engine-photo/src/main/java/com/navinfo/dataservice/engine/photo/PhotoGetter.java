package com.navinfo.dataservice.engine.photo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hbase.async.KeyValue;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.photo.HBaseController;

/**
 * 获取照片类
 */
public class PhotoGetter {

	/**
	 * 通过uuid获取照片，返回原图或缩略图
	 * 
	 * @param rowkey
	 * @param type
	 *            origin 原图; thumbnail 缩略图
	 * @return JSONObject
	 * @throws Exception
	 */
	public byte[] getPhotoByRowkey(String rowkey, String type)
			throws Exception {

		HBaseController control = new HBaseController();

		byte[] photo = control.getPhotoByRowkey(rowkey);

		if ("origin".equals(type)) {
			return photo;
		} else {
			return FileUtils.makeSmallImage(photo);
		}
	}
	
	public List<Map<String, Object>> getPhotosByRowkey(JSONArray rowkeys) throws Exception {
		HBaseController control = new HBaseController();
		List<Map<String, Object>> photos = control.getPhotosByRowkey(rowkeys);
		return photos;
	}
	
	
	
	/**
	 * @Description:根据rowkey查询tips是否存，存在则返回<rowkey,1>
	 * @param rowkeys
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-14 下午6:38:26
	 */
	public Map<String, Integer> getPhotosExistByRowkey(JSONArray rowkeys) throws Exception {
		HBaseController control = new HBaseController();
		Map<String, Integer> exitstPhotoKeyMap = control.getExistPhotosByRowkey(rowkeys);
		return exitstPhotoKeyMap;
	}

	public JSONObject getPhotoDetailByRowkey(String rowkey) throws Exception {

		HBaseController control = new HBaseController();

		byte[] photo = control.getPhotoDetailByRowkey(rowkey);

		JSONObject json = JSONObject.fromObject(new String(photo));

		return json;
	}

	/**
	 * 空间查询照片，返回概要信息
	 * 
	 * @param wkt
	 * @return JSONArray
	 * @throws Exception
	 */
	public JSONArray getPhotoBySpatial(String wkt) throws Exception {

		JSONArray array = new JSONArray();
		HBaseController controller = new HBaseController();

		ArrayList<ArrayList<KeyValue>> rows = controller.getPhotoBySpatial(wkt);

		for (List<KeyValue> list : rows) {

			for (KeyValue kv : list) {

				JSONObject jsonGeom = JSONObject.fromObject(new String(kv
						.value()));

				array.add(jsonGeom);
			}
		}

		return array;
	}

	/**
	 * 空间查询照片，返回概要信息
	 * 
	 * @param wkt
	 * @return JSONArray
	 * @throws Exception
	 */
	public JSONArray getPhotoByTileWithGap(int x, int y, int z, int gap)
			throws Exception {

		JSONArray array = new JSONArray();

		double px = MercatorProjection.tileXToPixelX(x);

		double py = MercatorProjection.tileYToPixelY(y);

		HBaseController controller = new HBaseController();

		ArrayList<ArrayList<KeyValue>> rows = controller.getPhotoByTileWithGap(
				x, y, z, gap);

		for (List<KeyValue> list : rows) {

			for (KeyValue kv : list) {

				JSONObject jsonGeom = JSONObject.fromObject(new String(kv
						.value()));

				JSONObject json = new JSONObject();

				json.put("i", jsonGeom.getString("rowkey"));

				json.put("t", 2);

				json.put("g", Geojson.lonlat2Pixel(
						jsonGeom.getDouble("a_longitude"),
						jsonGeom.getDouble("a_latitude"), z, px, py));

				array.add(json);
			}
		}

		return array;
	}

	public JSONArray getPhotoTile(double minLon, double minLat,
			double maxLon, double maxLat, int zoom) throws Exception {

		JSONArray array = new JSONArray();

		HBaseController controller = new HBaseController();

		ArrayList<ArrayList<KeyValue>> rows = controller.getPhotoTile(minLon,
				minLat, maxLon, maxLat, zoom);

		for (List<KeyValue> list : rows) {

			for (KeyValue kv : list) {

				JSONArray a = JSONArray.fromObject(new String(kv.value()));

				array.addAll(a);
			}
		}

		return array;

	}

	public static void main(String[] args) throws Exception {
//		HBaseAddress.initHBaseClient("192.168.3.156");

//		System.out.println(PhotoGetter.getPhotoTile(117.44933, 31.042581,
//				117.44944, 31.0426, 7));
		
		PhotoGetter g = new PhotoGetter();
		
		System.out.println(g.getPhotoByRowkey("38602314949e4bcca7bc965969cd9fa9", "origin"));
		
		
	}
}
