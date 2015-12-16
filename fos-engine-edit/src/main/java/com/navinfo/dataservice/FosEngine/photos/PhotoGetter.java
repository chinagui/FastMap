package com.navinfo.dataservice.FosEngine.photos;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.geom.GeoTranslator;
import com.navinfo.dataservice.FosEngine.comm.geom.Geojson;
import com.navinfo.dataservice.FosEngine.comm.mercator.MercatorProjection;
import com.navinfo.dataservice.FosEngine.comm.util.FileUtils;
import com.navinfo.dataservice.FosEngine.edit.search.SearchSnapshot;

/**
 * 获取照片类
 */
public class PhotoGetter {


	/**
	 * 通过uuid获取照片，返回原图或缩略图
	 * 
	 * @param uuid
	 * @param type
	 *            origin 原图; thumbnail 缩略图
	 * @return JSONObject
	 * @throws Exception
	 */
	public static byte[] getPhotoByUuid(String uuid, String type)
			throws Exception {

		try {
			final GetRequest get = new GetRequest("photo", uuid);

			get.family("data");

			get.qualifier("origin");

			ArrayList<KeyValue> list = HBaseAddress.getHBaseClient().get(get)
					.joinUninterruptibly();

			for (KeyValue kv : list) {
				if ("origin".equals(type)) {
					return kv.value();
				} else {
					return FileUtils.makeSmallImage(kv.value());
				}

			}

			return null;
		} catch (Exception e) {

			throw e;
		}

	}

	/**
	 * 通过uuid获取照片详细信息
	 * 
	 * @param uuid
	 * @return JSONObject
	 * @throws Exception
	 */
	public static JSONObject getPhotoDetailByUuid(String uuid) throws Exception {

		JSONObject json = new JSONObject();

		try {

			final GetRequest get = new GetRequest("photo", uuid);

			get.family("data");

			get.qualifier("attribute");

			ArrayList<KeyValue> list = HBaseAddress.getHBaseClient().get(get)
					.joinUninterruptibly();

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
	 * 空间查询照片，返回概要信息
	 * 
	 * @param wkt
	 * @return JSONArray
	 * @throws Exception
	 */
	public static JSONArray getPhotoBySpatial(String wkt) throws Exception {

		JSONArray array = new JSONArray();

		double[] mbr = GeoTranslator.getMBR(wkt);

		try {
			String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[1], mbr[0], 12);

			String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[3], mbr[2], 12);

			Scanner scanner = HBaseAddress.getHBaseClient().newScanner("photo");

			scanner.setStartKey(startRowkey);

			scanner.setStopKey(stopRowkey);

			scanner.setFamily("data");

			scanner.setQualifier("brief");

			ArrayList<ArrayList<KeyValue>> rows;

			while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

				for (List<KeyValue> list : rows) {

					for (KeyValue kv : list) {

						JSONObject jsonGeom = JSONObject.fromObject(new String(
								kv.value()));

						array.add(jsonGeom);
					}
				}
			}
		} catch (Exception e) {

			throw e;
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
	public static JSONArray getPhotoByTileWithGap(int x, int y, int z, int gap)
			throws Exception {

		JSONArray array = new JSONArray();

		String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

		double[] mbr = GeoTranslator.getMBR(wkt);

		try {
			String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[1], mbr[0], 12);

			String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(
					mbr[3], mbr[2], 12);

			Scanner scanner = HBaseAddress.getHBaseClient().newScanner("photo");

			scanner.setStartKey(startRowkey);

			scanner.setStopKey(stopRowkey);

			scanner.setFamily("data");

			scanner.setQualifier("brief");

			ArrayList<ArrayList<KeyValue>> rows;

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

				for (List<KeyValue> list : rows) {

					for (KeyValue kv : list) {

						JSONObject jsonGeom = JSONObject.fromObject(new String(
								kv.value()));

						SearchSnapshot snapshot = new SearchSnapshot();

						snapshot.setI(jsonGeom.getString("rowkey"));

						snapshot.setT(2);

						snapshot.setG(Geojson.lonlat2Pixel(
								jsonGeom.getDouble("a_longitude"),
								jsonGeom.getDouble("a_latitude"), z, px, py));

						array.add(snapshot);
					}
				}
			}
		} catch (Exception e) {

			throw e;
		}

		return array;
	}
}
