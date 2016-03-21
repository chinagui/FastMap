package com.navinfo.dataservice.engine.photo;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class PoiPhotoImporter {

	public static int importPhoto(String zipFile) throws Exception {

		String unzipFolder = zipFile.replace(".zip", "");

		ZipUtils.unzipFile(zipFile, unzipFolder);

		Map<String, byte[]> mapPhoto = FileUtils.readPhotos(unzipFolder);

		if (mapPhoto.size() == 0) {
			return 0;
		}

		Map<String, Photo> mapInfo = loadPhotoInfo(unzipFolder
				+ "/Datum_Point.json");

		Table photoTab = HBaseAddress.getHBaseConnection().getTable(
				TableName.valueOf(HBaseConstant.photoTab));

		List<Put> puts = new ArrayList<Put>();

		Set<Entry<String, byte[]>> set = mapPhoto.entrySet();

		Iterator<Entry<String, byte[]>> it = set.iterator();

		int num = 0;

		while (it.hasNext()) {
			Entry<String, byte[]> entry = it.next();

			String fileName = entry.getKey();

			if (!mapInfo.containsKey(fileName)) {
				continue;
			}

			byte[] data = mapPhoto.get(fileName);

			Photo photo = mapInfo.get(fileName);

			Put put = enclosedPut(data, photo);

			puts.add(put);

			num++;

			if (num % 1000 == 0) {
				photoTab.put(puts);

				puts.clear();
			}
		}

		photoTab.put(puts);

		photoTab.close();

		return num;
	}

	private static Put enclosedPut(byte[] data, Photo photo) throws Exception {
		Put put = new Put(photo.getRowkey().getBytes());

		put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
				.fromObject(photo).toString().getBytes());

		put.addColumn("data".getBytes(), "origin".getBytes(), data);

		return put;
	}

	private static Map<String, Photo> loadPhotoInfo(String fileName)
			throws Exception {

		Map<String, Photo> map = new HashMap<String, Photo>();

		Scanner scanner = new Scanner(new FileInputStream(fileName));

		WKTReader reader = new WKTReader();

		DecimalFormat df = new DecimalFormat("#.00000");

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			JSONObject json = JSONObject.fromObject(line);

			String geometry = json.getString("geometry");

			Geometry g = reader.read(geometry);

			double lon = Double.valueOf(df.format(g.getCoordinate().x));

			double lat = Double.valueOf(df.format(g.getCoordinate().y));

			String rowkey = GeoHash.geoHashStringWithCharacterPrecision(lat,
					lon, 12) + "b";

			JSONArray attachments = JSONArray.fromObject(json.getString("attachments"));

			int meshId = json.getInt("meshid");

			for (int i = 0; i < attachments.size(); i++) {
				JSONObject attachment = attachments.getJSONObject(i);

				if (attachment.getInt("type") == 1) {

					String uuid = UuidUtils.genUuid();

					String url = attachment.getString("url");

					int ind = url.lastIndexOf("/");

					if (ind != -1) {
						url = url.substring(ind + 1);
					}

					Photo photo = new Photo();

					photo.setA_latitude(lat);

					photo.setA_longitude(lon);

					photo.setA_fileName(url);

					photo.setA_mesh(meshId);

					photo.setA_uuid(uuid);

					photo.setRowkey(rowkey + uuid);

					map.put(url, photo);
				}
			}
		}

		return map;
	}
	
	public static void main(String[] args) throws Exception{
		HBaseAddress.initHBaseAddress("192.168.3.156");
		
		String file = "C:/1/IncrementalData_3438_4667_20160110112024.zip";
		
		System.out.println(PoiPhotoImporter.importPhoto(file));
	}
}
