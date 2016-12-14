package com.navinfo.dataservice.dao.photo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.ByteUtils;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;

public class HBaseController {

	private ArrayList<KeyValue> getByRowkey(String tableName, String rowkey,
			String family, String... qualifiers) throws Exception {

		final GetRequest get = new GetRequest(tableName, rowkey);

		if (family != null) {
			get.family(family);
		}

		if (qualifiers.length > 0) {
			get.qualifiers(ByteUtils.toBytes(qualifiers));
		}

		ArrayList<KeyValue> list = HBaseConnector.getInstance().getClient()
				.get(get).joinUninterruptibly();

		return list;
	}

	public byte[] getPhotoByRowkey(String rowkey) throws Exception {

		List<KeyValue> list = getByRowkey("photo", rowkey, "data", "origin");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}

	public byte[] getPhotoDetailByRowkey(String rowkey) throws Exception {

		List<KeyValue> list = getByRowkey("photo", rowkey, "data", "attribute");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}

	private ArrayList<ArrayList<KeyValue>> scan(String tableName,
			String startKey, String stopKey, String family,
			String... qualifiers) throws Exception {

		Scanner scanner = HBaseConnector.getInstance().getClient()
				.newScanner(tableName);

		scanner.setStartKey(startKey);

		scanner.setStopKey(stopKey);

		if (family != null) {
			scanner.setFamily(family);
		}

		if (qualifiers.length > 0) {
			scanner.setQualifiers(ByteUtils.toBytes(qualifiers));
		}

		ArrayList<ArrayList<KeyValue>> rows;

		ArrayList<ArrayList<KeyValue>> result = new ArrayList<ArrayList<KeyValue>>();

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (ArrayList<KeyValue> list : rows) {
				result.add(list);
			}
		}

		return result;
	}

	public ArrayList<ArrayList<KeyValue>> getPhotoBySpatial(String wkt)
			throws Exception {

		double[] mbr = GeoTranslator.getMBR(wkt);

		String startKey = GeoHash.geoHashStringWithCharacterPrecision(mbr[1],
				mbr[0], 12);

		String stopKey = GeoHash.geoHashStringWithCharacterPrecision(mbr[3],
				mbr[2], 12);

		ArrayList<ArrayList<KeyValue>> result = scan("photo", startKey,
				stopKey, "data", "brief");

		return result;
	}

	public ArrayList<ArrayList<KeyValue>> getPhotoByTileWithGap(int x, int y,
			int z, int gap) throws Exception {

		String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

		return getPhotoBySpatial(wkt);
	}

	public ArrayList<ArrayList<KeyValue>> getPhotoTile(double minLon,
			double minLat, double maxLon, double maxLat, int zoom)
			throws Exception {

		long xmin = MercatorProjection.longitudeToTileX(minLon, (byte) zoom);

		long xmax = MercatorProjection.longitudeToTileX(maxLon, (byte) zoom);

		long ymax = MercatorProjection.latitudeToTileY(minLat, (byte) zoom);

		long ymin = MercatorProjection.latitudeToTileY(maxLat, (byte) zoom);

		String startKey = String.format("%02d%08d%07d", zoom, xmin, ymin);

		String stopKey = String.format("%02d%08d%07d", zoom, xmax, ymax);

		ArrayList<ArrayList<KeyValue>> result = scan("photoTile", startKey,
				stopKey, "data", "photo");

		return result;
	}
	
	public void putPhoto(String rowkey, InputStream in) throws Exception{
		
		Photo photo = new Photo();
		
		photo.setRowkey(rowkey);
		photo.setA_version(SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.seasonVersion));
		
		int count = in.available();
		
		byte[] bytes = new byte[(int) count];

		in.read(bytes);
		
		byte[] sbytes = FileUtils.makeSmallImage(bytes);
		
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
		try{
			Put put = new Put(rowkey.getBytes());
			
			put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
					.fromObject(photo).toString().getBytes());
			
			put.addColumn("data".getBytes(), "origin".getBytes(), bytes);
			
			put.addColumn("data".getBytes(), "thumbnail".getBytes(), sbytes);
			
			htab.put(put);
		}finally{
			if (htab!=null){
				htab.close();
			}
		}
		
		
	}

	public String putPhoto(InputStream in) throws Exception{
		String rowkey = UuidUtils.genUuid();
		putPhoto(rowkey, in);
		return rowkey;
	}
	
	public List<Map<String, Object>> getPhotosByRowkey(JSONArray rowkeys) throws Exception{
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
		List<Get> getList=new ArrayList<Get>();
		for(Object rowkey:rowkeys){
			Get get = new Get(((String)rowkey).getBytes());
			getList.add(get);
		}
		Result[] rs = htab.get(getList);
		List<Map<String, Object>> photos=new ArrayList<Map<String,Object>>();
		String seasonVersion=SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
		for (Result result : rs) {
			if (result.isEmpty()) {continue;}
			Map<String, Object> photoMap=new HashMap<String, Object>();
			String rowkey = new String(result.getRow());
			photoMap.put("rowkey", rowkey);
//			byte[] thumbnail = FileUtils.makeSmallImage(result.getValue("data".getBytes(),
//					"origin".getBytes()));
//			photoMap.put("thumbnail", thumbnail);
			String attribute = new String(result.getValue("data".getBytes(),
					"attribute".getBytes()));
			
			JSONObject attrJson = JSONObject.fromObject(attribute);
			if(seasonVersion!=null&&seasonVersion.equals(attrJson.getString("a_version"))){
				photoMap.put("version", 1);}
			else{photoMap.put("version", 0);}
			photos.add(photoMap);
		}
		return photos;
	}
	
	
	/**
	 * @Description:根据rowkey查询tips是否存，存在则返回<rowkey,1>
	 * @param rowkeys
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-14 下午6:35:23
	 */
	public Map<String, Integer> getExistPhotosByRowkey(JSONArray rowkeys) throws Exception{
		 Map<String, Integer> resultMap=new HashMap<String, Integer>();
		
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
		List<Get> getList=new ArrayList<Get>();
		for(Object rowkey:rowkeys){
			Get get = new Get(((String)rowkey).getBytes());
			getList.add(get);
		}
		Result[] rs = htab.get(getList);
		for (Result result : rs) {
			if (result.isEmpty()) {continue;}
			String rowkey = new String(result.getRow());
			resultMap.put(rowkey, 1);
		}
		return resultMap;
	}
}
