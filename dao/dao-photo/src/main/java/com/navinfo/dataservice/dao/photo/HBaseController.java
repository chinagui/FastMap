package com.navinfo.dataservice.dao.photo;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
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

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.ByteUtils;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

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

		List<KeyValue> list = getByRowkey(HBaseConstant.photoTab, rowkey, "data", "origin");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}

	public byte[] getPhotoDetailByRowkey(String rowkey) throws Exception {

		List<KeyValue> list = getByRowkey(HBaseConstant.photoTab, rowkey, "data", "attribute");

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

		ArrayList<ArrayList<KeyValue>> result = scan(HBaseConstant.photoTab, startKey,
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

		Table htab =null;
		try{
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
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
	public void putPhoto(String rowkey, InputStream in,Photo photo) throws Exception{
		
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
	//设置aContent
	public String putPhoto(InputStream in,int aContent) throws Exception{
		String rowkey = UuidUtils.genUuid();
		Photo photo = new Photo();
		
		photo.setRowkey(rowkey);
		photo.setA_uuid(rowkey);
		photo.setA_version(SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.seasonVersion));
		photo.setA_content(aContent);
		// 设置上传时间
		String a_uploadDate = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);
		photo.setA_uploadDate(a_uploadDate);
		putPhoto(rowkey, in,photo);
		return rowkey;
	}
	public static void main(String[] args) throws Exception {
		HBaseController hbaseController = new HBaseController();
		hbaseController.putPhoto(null,13,"1664",862);
	}
	//设置photo属性
	public String putPhoto(InputStream in,int dbId,String userId, int pid) throws Exception{
		String rowkey = UuidUtils.genUuid();
		Photo photo = new Photo();
		photo.setRowkey(rowkey);
		photo.setA_version(SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.seasonVersion));
		
		//2017.08.10添加到新属性
		int a_uploadUser = 0;
		if(StringUtils.isNotEmpty(userId)){
			a_uploadUser = Integer.parseInt(userId);
		}
		String a_uploadDate = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);
		//坐标
		double a_latitude = 0;
		double a_longitude = 0;
		java.sql.Connection conn=null;
		try {
			conn=DBConnector.getInstance().getConnectionById(dbId);
			BasicObj obj=ObjSelector.selectByPid(conn, ObjectName.IX_POI, null,true, pid, false);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
			Geometry geometry = ixPoi.getGeometry();
			a_latitude = geometry.getCoordinate().y;
			a_longitude = geometry.getCoordinate().x;;
		} catch (Exception e) {
			System.out.println("pid("+pid+")的照片上传显示坐标查询失败:"+e.getMessage());
		}finally {
			DBUtils.closeConnection(conn);
		}
		photo.setA_uuid(rowkey);
		photo.setA_uploadUser(a_uploadUser);
		photo.setA_uploadDate(a_uploadDate);
		photo.setA_latitude(a_latitude);
		photo.setA_longitude(a_longitude);
		photo.setA_title("");
		photo.setA_subtitle("");
		photo.setA_sourceId(7);
		photo.setA_direction(0);
		photo.setA_shootDate("");
		photo.setA_deviceNum("");
		photo.setA_content(2);
		photo.setA_address("");
		photo.setA_fileName("");
		photo.setA_collectUser(0);
		photo.setA_mesh(0);
		photo.setA_admin("");
		photo.setA_deviceOrient(0);
		List<String> a_tag = new ArrayList<String>();
		photo.setA_tag(a_tag);
		photo.setA_refUuid("");
		
		putPhoto(rowkey, in,photo);
		return rowkey;
	}
	
	public void updatePhoto(String rowkey, InputStream in) throws Exception{
		
		int count = in.available();
		
		byte[] bytes = new byte[(int) count];

		in.read(bytes);
		
		byte[] sbytes = FileUtils.makeSmallImage(bytes);
		
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = null;
		try{
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
			Put put = new Put(rowkey.getBytes());
			
			put.addColumn("data".getBytes(), "origin".getBytes(), bytes);
			
			put.addColumn("data".getBytes(), "thumbnail".getBytes(), sbytes);
			
			htab.put(put);
		}finally{
			if (htab!=null){
				htab.close();
			}
		}
		
	}
	
	public List<Map<String, Object>> getPhotosByRowkey(JSONArray rowkeys) throws Exception{
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = null;
		List<Map<String, Object>> photos=new ArrayList<Map<String,Object>>();
		try{
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
			List<Get> getList=new ArrayList<Get>();
			for(Object rowkey:rowkeys){
				Get get = new Get(((String)rowkey).getBytes());
				getList.add(get);
			}
			Result[] rs = htab.get(getList);
			
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
		}catch (Exception e) {
			throw e;
		}finally {
			if(htab!=null){
				htab.close();
			}
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
		Table htab =null;
		try{
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
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
		}catch (Exception e) {
			throw e;
		}finally {
			if(htab!=null){
				htab.close();
			}
		}
		return resultMap;
	}
}
