package com.navinfo.dataservice.engine.fcc.tips;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.audio.AudioGetter;
import com.navinfo.dataservice.engine.fcc.photo.PhotoGetter;
import com.navinfo.navicommons.geo.computation.GridUtils;

public class TipsExporter {

	private SolrController solr = new SolrController();

	private String folderName;
	public TipsExporter() {
	}

	private List<Get> generateGets(String gridId, String date)
			throws Exception {
		List<Get> gets = new ArrayList<Get>();

		Set<String> set = new HashSet<String>();

		String wkt = GridUtils.grid2Wkt(gridId);
		
		List<String> rowkeys = solr.queryTipsMobile(wkt, date,TipsUtils.notExpSourceType);

		for (String rowkey : rowkeys) {
			if (set.contains(rowkey)) {
				continue;
			}

			set.add(rowkey);

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "geometry".getBytes());

			get.addColumn("data".getBytes(), "deep".getBytes());

			get.addColumn("data".getBytes(), "source".getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());

			get.addColumn("data".getBytes(), "feedback".getBytes());

			gets.add(get);
		}


		return gets;
	}

	private JSONArray exportByGets(List<Get> gets, Set<String> patternImages)
			throws Exception {

		JSONArray ja = new JSONArray();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Result[] results = htab.get(gets);

	/*	List<Get> photoGets = new ArrayList<Get>();
		List<Get> audioGets = new ArrayList<Get>(); 
		
		List<JSONObject> hasPhotoDatas = new ArrayList<JSONObject>(); //含照片的数据
		
		List<JSONObject> hasAudioDatas = new ArrayList<JSONObject>(); //含语音的数据
*/		
		Map<String, Photo> photoMap =new HashMap<String, Photo>();//照片信息map,key=照片id，value=照片信息 
		
		Map<String, Audio> audioMap =new HashMap<String, Audio>();//照片信息map,key=语音id，value=语音新
		
		//1.先导出照片语音，并获取附件信息
		exportPhotoAudioAndGetAttachmentInfo(results,photoMap,audioMap);

		//1.循环便利每条数据，并组导出txt的返回值。如果包含照片或者语音，单独挑出来
		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			JSONObject json = new JSONObject();

			String rowkey = new String(result.getRow());

			json.put("rowkey", rowkey);

			String source = new String(result.getValue("data".getBytes(),
					"source".getBytes()));
			

			json.putAll(JSONObject.fromObject(source));
			
			//s_project 需要赋值为空
			
			json.put("s_project","");

			String sourceType = json.getString("s_sourceType");

			String deep = new String(result.getValue("data".getBytes(),
					"deep".getBytes()));

			JSONObject deepjson = JSONObject.fromObject(deep);

			if (deepjson.containsKey("agl")) {
				json.put("angle", deepjson.getDouble("agl"));

			} else {
				json.put("angle", 0);
			}

			json.put("deep", deepjson);

			if (sourceType.equals("1406") || sourceType.equals("1401")) {
				// 需要导出关联的模式图

				if (deepjson.containsKey("ptn")) {
					String ptn = deepjson.getString("ptn");

					if (ptn != null && ptn.length() > 0) {
						patternImages.add(ptn);
					}
				}
			}

			String geometry = new String(result.getValue("data".getBytes(),
					"geometry".getBytes()));

			json.putAll(JSONObject.fromObject(geometry));

			String track = new String(result.getValue("data".getBytes(),
					"track".getBytes()));

			JSONObject trackjson = JSONObject.fromObject(track);

			json.put("t_lifecycle", trackjson.getInt("t_lifecycle"));

			json.put("t_command", trackjson.getInt("t_command"));

			JSONArray tTrackInfo = trackjson.getJSONArray("t_trackInfo");

			JSONObject lastTrackInfo = tTrackInfo.getJSONObject(tTrackInfo
					.size() - 1);

			String lastDate = lastTrackInfo.getString("date");
            //track.t_trackInfo中最后一条date赋值	
			json.put("t_operateDate", lastDate);

			json.put("t_handler", 0);
/*			boolean hasPhotoFlag = false;
			boolean hasAudioFlag = false;*/
			
			//附件的转出
			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));
				
				JSONArray farray = feedback.getJSONArray("f_array");

				json.put("attachments", farray);
				
				//返回的附件信息
				JSONArray farrayExport = new JSONArray();

				for (int i = 0; i < farray.size(); i++) {
					JSONObject jo = farray.getJSONObject(i);
					int type = jo.getInt("type");
					JSONObject attachment=new JSONObject();
					//照片的转出
					if (type == 1) {

					//hasPhotoFlag = true;

					//String id = jo.getString("content");
				/*	if (photoIdSet.contains(id)) {
						continue;
					}
					photoIdSet.add(id);*/

				/*	Get get = new Get(id.getBytes());

					get.addColumn("data".getBytes(), "attribute".getBytes());

					get.addColumn("data".getBytes(), "origin".getBytes());*/

					//photoGets.add(get);
					
					String id=jo.getString("content");
					Photo photo=photoMap.get(id);
					
					if(photo==null){
						continue;
					}
					
					attachment.put("id", id);
					attachment.put("content", photo.getA_fileName());
					attachment.put("type", 1);
					
					JSONObject  ext=new JSONObject();
					ext.put("latitude", photo.getA_latitude());
					ext.put("longitude", photo.getA_longitude());
					ext.put("direction", photo.getA_direction());
					ext.put("shootDate", photo.getA_shootDate());
					ext.put("deviceNum", photo.getA_deviceNum());
					
					attachment.put("extContent", ext);
					farrayExport.add(attachment);
					
					}
					//语音的转出
					else if (type == 2) {

					/*	hasAudioFlag = true;
	
						String id = jo.getString("content");
						
						if (audioIdSet.contains(id)) {
							continue;
						}
						audioIdSet.add(id);
	
						Get get = new Get(id.getBytes());
	
						get.addColumn("data".getBytes(), "attribute".getBytes());
	
						get.addColumn("data".getBytes(), "origin".getBytes());
	
						audioGets.add(get);*/
						
						String id=jo.getString("content");
						Audio audio =audioMap.get(id);
						if(audio==null){
							continue;
						}
						attachment.put("id", id);
						attachment.put("content", audio.getA_fileName());
						attachment.put("type", 2);
						attachment.put("extContent", JSONNull.getInstance());
						farrayExport.add(attachment);
					
					}
					
					//文字转出
					else if (type == 3) {
						attachment.put("id", "");
						attachment.put("content", jo.getString("content"));
						attachment.put("type", 3);
						attachment.put("extContent", JSONNull.getInstance());
						farrayExport.add(attachment);
					}
					
					//草图转出
					else if (type == 6) {
						attachment.put("id", "");
						attachment.put("content", jo.getString("content"));
						attachment.put("type", 6);
						attachment.put("extContent", JSONNull.getInstance());
						farrayExport.add(attachment);
					}
					
				}
				json.put("attachments", farrayExport);
			} else {
				json.put("attachments", new JSONArray());
			}
		/*	
			if (hasPhotoFlag) {
				hasPhotoDatas.add(json);
			}
			
			if (hasAudioFlag) {
				hasAudioDatas.add(json);
			}*/
			//不包含照片或者语音的，数据就直接就可以返回了
		/*	if(!hasAudioFlag&&!hasPhotoFlag){
				ja.add(json);
			}*/
			
			ja.add(json);
			
		}
			
		/*//含照片，则照片附件重新赋值，并导出照片文件
		if (hasPhotoDatas.size() > 0) {
			Map<String, JSONObject> photoMap = exportPhotos(photoGets);
			
			for (int i = 0; i < hasPhotoDatas.size(); i++) {
				JSONObject json = hasPhotoDatas.get(i);

				JSONArray feedbacks = json.getJSONArray("attachments");//已有的

				JSONArray newFeedbacks = new JSONArray();

				for (int j = 0; j < feedbacks.size(); j++) {

					JSONObject feedback = feedbacks.getJSONObject(j);

					int type = feedback.getInt("type");

					String content = feedback.getString("content"); //id
					
					if(type==1){
						
						JSONObject newFeedback = new JSONObject();

						newFeedback.put("type", type);

						newFeedback.put("content", content);

						newFeedback.put("id", content);

						JSONObject extContent = photoMap.get(content);

						newFeedback.put("extContent", extContent);
						
						newFeedbacks.add(newFeedback);
					}

					
				}

				json.put("attachments", newFeedbacks);

				ja.add(json);
			}
		}*/

		return ja;
	}

	/**
	 * @Description:导出照片语音，并获取附件信息
	 * @param photoMap
	 * @param audioMap
	 * @author: y
	 * @param results 
	 * @throws Exception 
	 * @time:2016-12-13 下午8:22:10
	 */
	private void exportPhotoAudioAndGetAttachmentInfo(
			Result[] results, Map<String, Photo> photoMap, Map<String, Audio> audioMap) throws Exception {
		
	/*	Set<String> audioIdSet = new HashSet<String>(); //照片id集合
		Set<String> photoIdSet = new HashSet<String>();//语音id集合
*/		
		List<Get> photoGets = new ArrayList<Get>();
		List<Get> audioGets = new ArrayList<Get>(); 
		
		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}
			//从feedback读取照片或语音
			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));
				
				JSONArray farray = feedback.getJSONArray("f_array");

				for (int i = 0; i < farray.size(); i++) {
					JSONObject jo = farray.getJSONObject(i);
					int type = jo.getInt("type");
					//获取照片id
					if (type == 1) {
					String id = jo.getString("content");
					
					Get get = new Get(id.getBytes());

					get.addColumn("data".getBytes(), "attribute".getBytes());

					get.addColumn("data".getBytes(), "origin".getBytes());

					photoGets.add(get);
					//photoIdSet.add(id);
					}
					//获取语音id
					if (type == 2) {
						String id = jo.getString("content");
						
						Get get = new Get(id.getBytes());
						
						get.addColumn("data".getBytes(), "attribute".getBytes());
	
						get.addColumn("data".getBytes(), "origin.o_audio".getBytes());
	
						audioGets.add(get);
						//audioIdSet.add(id);
					}
				}
			}
			
		}
		
		exportPhotos(photoGets,photoMap);
		
		exportAudio(audioGets,audioMap);
			
	}

	
	/**
	 * @Description:导出语音信息
	 * @param audioGets
	 * @return
	 * @author: y
	 * @param audioMap 
	 * @throws Exception 
	 * @time:2016-12-14 上午9:55:54
	 */
	private Map<String, Audio> exportAudio(List<Get> audioGets, Map<String, Audio> audioMap) throws Exception {
	/*Map<String, JSONObject> photoMap = new HashMap<String, JSONObject>();*/
		
		//Map<String, Audio> audioMap = new HashMap<String, Audio>();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName
				.valueOf(HBaseConstant.audioTab));

		Result[] results = htab.get(audioGets);

		for (Result result : results) {
			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			byte[] data = result.getValue("data".getBytes(),
					"origin.o_audio".getBytes());

			if (data == null || data.length == 0) {
				continue;
			}

			JSONObject attribute = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "attribute".getBytes())));
			Audio audio=new Audio();
			
			audio.setRowkey(rowkey);
			
			audio.setA_fileName(attribute.getString("a_fileName"));

			String fileName = this.folderName
					+ attribute.getString("a_fileName");

			audioMap.put(rowkey, audio);
			
			//FileUtils.makeSmallImage(data, fileName);
			
			exportAudioFile(data,fileName);

		}

		return audioMap;
	}

	/**
	 * @Description:导出音频文件
	 * @param data
	 * @param fileName
	 * @author: y
	 * @throws Exception 
	 * @time:2016-12-14 下午2:07:22
	 */
	private void exportAudioFile(byte[] data, String fileName) throws Exception {
	        OutputStream fstream =null;
	        try
	        {
	        	fstream=new FileOutputStream(fileName);
	            fstream.write(data);
	        }
	        catch (Exception ex)
	        {
	           throw ex;
	        }
	        finally
	        {
	        	if(fstream!=null){
	        		try {
						fstream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	
	        	}
	        }
	}

	/**
	 * @Description:导出照片信息
	 * @param gets
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-13 下午8:33:26
	 */
	private Map<String, Photo> exportPhotos(List<Get> gets,Map<String, Photo> photoMap)
			throws Exception {

		/*Map<String, JSONObject> photoMap = new HashMap<String, JSONObject>();*/

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName
				.valueOf(HBaseConstant.photoTab));

		Result[] results = htab.get(gets);

		for (Result result : results) {
			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			byte[] data = result.getValue("data".getBytes(),
					"origin".getBytes());

			if (data == null || data.length == 0) {
				continue;
			}

			JSONObject attribute = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "attribute".getBytes())));

/*			JSONObject extContent = new JSONObject();
			*/
			Photo photo=new Photo();
			
			photo.setRowkey(rowkey);
			
			photo.setA_latitude(attribute.getDouble("a_latitude"));
			
			photo.setA_longitude(attribute.getDouble("a_longitude"));
			
			photo.setA_direction(attribute.getInt("a_direction"));
			
			photo.setA_shootDate(attribute.getString("a_shootDate"));
			
			photo.setA_deviceNum(attribute.getString("a_deviceNum"));
			
			photo.setA_fileName(attribute.getString("a_fileName"));
			
/*
			extContent.put("latitude", attribute.getDouble("a_latitude"));

			extContent.put("longitude", attribute.getDouble("a_longitude"));

			extContent.put("direction", attribute.getInt("a_direction"));

			extContent.put("shootDate", attribute.getString("a_shootDate"));

			extContent.put("deviceNum", attribute.getString("a_deviceNum"));*/

			String fileName = this.folderName
					+ attribute.getString("a_fileName");

			/*photoMap.put(rowkey, extContent);*/

			photoMap.put(rowkey, photo);
			
			FileUtils.makeSmallImage(data, fileName);

		}

		return photoMap;

	}

	/**
	 * 根据网格和时间导出tips
	 * 
	 * @param condition
	 *            网格 、时间戳对象数组 整型
	 * @param date
	 *            时间
	 * @param fileName
	 *            导出文件名
	 * @return 导出个数
	 * @throws Exception
	 */
	public int export(JSONArray condition, String folderName,
			String fileName, Set<String> patternImages) throws Exception {

		int count = 0;

		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		this.folderName = folderName;

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		
		List<Get> getsAll = new ArrayList<>();
		
		for (Object obj : condition) {
			
			JSONObject conJson=JSONObject.fromObject(obj);
			
			String grid=conJson.getString("grid");
			
			String date=conJson.getString("date");
			
			if("null".equalsIgnoreCase(date)){
			    date=null;
			}
			
			List<Get> gets = generateGets(grid, date);
			
			getsAll.addAll(gets);
		}

		JSONArray ja = exportByGets(getsAll, patternImages);

		for (int j = 0; j < ja.size(); j++) {
			pw.println(ja.getJSONObject(j).toString());

			count += 1;
		}

		pw.close();

		return count;
	}

	public static void main(String[] args) throws Exception {
		JSONArray condition = new JSONArray();

		String s ="60560301,60560302,60560303,60560311,60560312,60560313,60560322,60560323,60560331,60560332,60560333,60560320,60560330,60560300,60560321,60560310";
		
		String[] st = s.split(",");
		
		for(String ss : st){
			JSONObject obj=new JSONObject();
			obj.put("grid", ss);
			obj.put("date", "20160912205811");
			condition.add(obj);
		}

		Set<String> images = new HashSet<String>();

		TipsExporter exporter = new TipsExporter();
		System.out.println(exporter.export(condition,
				"F:/07 DataService/test", "1.txt", images));
		System.out.println(images);
		System.out.println("done");
	}
}
