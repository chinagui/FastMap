package com.navinfo.dataservice.engine.fcc.tips;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.MD5Utils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

/**
 * 保存上传的tips数据
 * 
 */


class ErrorType {
	static int InvalidLifecycle = 1;

	static int Deleted = 2;

	static int InvalidDate = 3;

	static int Notexist = 4;

	static int InvalidData = 5;
	
	static int FreshnessVerificationData = 6; //鲜度验证tips(不入库)
	
}

	

public class TipsUpload {
	
	static int IMPORT_STATE=1;

	private Map<String, JSONObject> insertTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> updateTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> oldTips = new HashMap<String, JSONObject>();
	
	private Map<String, JSONObject> roadNameTips = new HashMap<String, JSONObject>(); //道路名tips，用户道路名入元数据库
	
	private static final Logger logger = Logger.getLogger(TipsUpload.class);

	
	private String currentDate;

	private int total;

	private int failed;  //记录导入错误的条数

	private JSONArray reasons;

	private SolrController solr;

	public JSONArray getReasons() {
		return reasons;
	}

	public void setReasons(JSONArray reasons) {
		this.reasons = reasons;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @param audioMap 
	 * @param photoMap 
	 * @throws Exception
	 */
	public void run(String fileName, Map<String, Photo> photoMap, Map<String, Audio> audioMap) throws Exception {

		total = 0;

		failed = 0;

		reasons = new JSONArray();

		currentDate = StringUtils.getCurrentTime();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		List<Get> gets = loadFileContent(fileName, photoMap,audioMap);

		solr = new SolrController();

		loadOldTips(htab, gets);

		List<Put> puts = new ArrayList<Put>();
		
		//新增(已存在)或者修改的时候判断是否是鲜度验证的tips

		doInsert(puts);

		doUpdate(puts);

		htab.put(puts);

		htab.close();
		
		//道路名入元数据库
		importRoadNameToMeta();

	}
	
	
	/**
	 * 道路名入元数据库
	 */

	private void importRoadNameToMeta() {
		
		Set<Entry<String, JSONObject>> set = roadNameTips.entrySet();
		Iterator<Entry<String, JSONObject>> it = set.iterator();
		while (it.hasNext()) {
			Entry<String, JSONObject> en = it.next();

			String rowkey = en.getKey();
			// 坐标
			JSONObject nameTipJson = en.getValue();
			JSONObject deep = JSONObject.fromObject(nameTipJson.get("deep"));
			JSONObject geo = deep.getJSONObject("geo");
			JSONArray jaCoords = geo.getJSONArray("coordinates");
			double longitude=jaCoords.getDouble(0);
			double latitude=jaCoords.getDouble(1);
			
			// 名称,调用元数据库接口入库
			MetadataApi metaApi=(MetadataApi) ApplicationContextUtil.getBean("metaApi");
			JSONArray names = deep.getJSONArray("n_array");
			for (Object name : names) {
				if (name != null && StringUtils.isNotEmpty(name.toString())) {
					try {
						metaApi.nameImport(name.toString(), longitude, latitude, rowkey);
					} catch (Exception e) {
						//reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));
						logger.error(e.getMessage(), e.getCause());
					}
				}
			}
			
		}
	}


	/**
	 * 读取Tips文件，组装Get列表
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private List<Get> loadFileContent(String fileName,
			Map<String, Photo> photoInfo,
			Map<String, Audio> AudioInfo) throws Exception {

		Scanner scanner = new Scanner(new FileInputStream(fileName));

		List<Get> gets = new ArrayList<Get>();

		while (scanner.hasNextLine()) {

			total += 1;

			String rowkey = "";

			try {

				String line = scanner.nextLine();

				JSONObject json = JSONObject.fromObject(line);

				rowkey = json.getString("rowkey");

				int lifecycle = json.getInt("t_lifecycle");

				if (lifecycle == 0) {
					failed += 1;

					reasons.add(newReasonObject(rowkey,
							ErrorType.InvalidLifecycle));

					continue;
				}

				//String operateDate = json.getString("t_operateDate");

				JSONArray attachments = json.getJSONArray("attachments");

				JSONArray newFeedbacks = new JSONArray();

				for (int i = 0; i < attachments.size(); i++) {
					
					//attachment结构：{"id":"","type":1,"content":""}
					JSONObject attachment = attachments.getJSONObject(i);

					int type = attachment.getInt("type");

					String content = "";
					//照片
					if (1 == type) {
						
						content=attachment.getString("content"); //是文件名

						Photo photo = getPhoto(attachment, json);

						photoInfo.put(content, photo);  //文件名为key

						content = photo.getRowkey();
					}
					//语音
					if (2 == type) {

						Audio audio = getAudio(attachment, json);
						
						content = attachment.getString("id"); //id为key

						AudioInfo.put(content, audio); //id为key

					}
					//文字
					if (3 == type) {
						content = attachment.getString("content"); 
					}
					//草图
					if (6 == type) {
						content = attachment.getString("content"); 
					}
					/*
					║║ user	整数	edit_Tips	t_handler	原值导入
					║║ userRole				空
					║║ type		edit_Tips	attachments	attachments.type
					║║ content		edit_Tips	attachments	attachments.content
					║║ auditRemark				空
					║║ date				数据入库时服务器时间
					*/

					JSONObject newFeedback = new JSONObject();

					newFeedback.put("user", json.getInt("t_handler"));

					newFeedback.put("userRole", "");

					newFeedback.put("type", type);

					newFeedback.put("content", content);

					newFeedback.put("auditRemark", "");

					newFeedback.put("date", json.getString("t_operateDate")); //原值导入

					newFeedbacks.add(newFeedback);
				}

				JSONObject  feedbackObj=new JSONObject();
				feedbackObj.put("f_array", newFeedbacks);
				json.put("feedback", feedbackObj);
				

				String sourceType = json.getString("s_sourceType");

				if (sourceType.equals("2001")) {
					JSONObject glocation = json.getJSONObject("g_location");

					double length = GeometryUtils.getLinkLength(GeoTranslator
							.geojson2Jts(glocation));

					JSONObject deep = JSONObject.fromObject(json
							.getString("deep"));

					deep.put("len", length);

					json.put("deep", deep);
				}
				
				json.put("t_cStatus", 1);
				
				json.put("t_dStatus", 0);
				
				json.put("t_mStatus", 0);
				
				json.put("t_inMeth", 0);
				
                json.put("t_pStatus", 0);
				
				json.put("t_dInProc", 0);
				
				json.put("t_mInProc", 0);
					
				//道路名测线
				if(sourceType.equals("1901")){
					roadNameTips.put(rowkey, json);
				}

				if (3 == lifecycle) {
					insertTips.put(rowkey, json);
				} else {
					updateTips.put(rowkey, json);
				}

				Get get = new Get(rowkey.getBytes());

				get.addColumn("data".getBytes(), "track".getBytes());

				get.addColumn("data".getBytes(), "feedback".getBytes());
				
				get.addColumn("data".getBytes(), "deep".getBytes());
				
				get.addColumn("data".getBytes(), "geometry".getBytes());

				gets.add(get);

			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}

		}

		return gets;
	}

	/**
	 * @Description:TOOD
	 * @param attachment
	 * @param json
	 * @return
	 * @author: y
	 * @time:2016-12-3 下午3:22:17
	 */
	private Audio getAudio(JSONObject attachment, JSONObject json) {
		
		Audio audio = new Audio();

		String id = attachment.getString("id");

		audio.setRowkey(id);
		
		audio.setA_uuid(id); 
		
		audio.setA_fileName(attachment.getString("content"));

		audio.setA_uploadUser(json.getInt("t_handler"));

		audio.setA_uploadDate(json.getString("t_operateDate"));

		return audio;
	}

	/**
	 * 从Hbase读取要修改和删除的Tips
	 * 
	 * @param htab
	 * @param gets
	 * @throws Exception
	 */
	private void loadOldTips(Table htab, List<Get> gets) throws Exception {

		if (0 == gets.size()) {
			return;
		}

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			try {
				JSONObject jo = new JSONObject();  

				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));
				jo.put("track",track);

				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}
				
				String geometry = new String(result.getValue("data".getBytes(),
						"geometry".getBytes()));
				jo.put("geometry",geometry);
				
				
				String deep = new String(result.getValue("data".getBytes(),
						"deep".getBytes()));
				jo.put("deep",deep);
				
				

				oldTips.put(rowkey, jo);
			} catch (Exception e) {
				logger.error(e.getMessage(),e.getCause());
				throw e;
			}
		}
	}

	/**
	 * 处理新增的Tips
	 * 
	 * @param puts
	 */
	private void doInsert(List<Put> puts) throws Exception {
		Set<Entry<String, JSONObject>> set = insertTips.entrySet();

		Iterator<Entry<String, JSONObject>> it = set.iterator();

		while (it.hasNext()) {

			String rowkey = "";

			try {
				Entry<String, JSONObject> en = it.next();

				rowkey = en.getKey();

				JSONObject json = en.getValue();  

				Put put = null;
				
				
				//old有则更新,判断是否已存在
				if (oldTips.containsKey(rowkey)) {

					JSONObject oldTip = oldTips.get(rowkey);
					
					//差分判断是不是tips无变更(鲜度验证的tips)，是则不更新
					if(isFreshnessVerification(oldTip,json)){
						
						reasons.add(newReasonObject(rowkey,
								ErrorType.FreshnessVerificationData));
						
						continue;
					}
					
					//对比采集时间，采集时间和数据库中 hbase old.trackinfo.date(最后一条)
					int res = canUpdate(oldTip, json.getString("t_operateDate"));
					if (res < 0) {
						failed += 1;
						//-1表示old已删除
						if (res == -1) {
							reasons.add(newReasonObject(rowkey,
									ErrorType.Deleted));
						} 
						//else =-2表示当前采集时间较旧
						else {
							reasons.add(newReasonObject(rowkey,
									ErrorType.InvalidDate));
						}
						continue;
					}
					
				    put = updatePut(rowkey, json, oldTip);

				} else {
					put = insertPut(rowkey, json);
				}

				
				JSONObject solrIndex = TipsUtils.generateSolrIndex(json,currentDate);

				solr.addTips(solrIndex);

				puts.add(put);

			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}
		}
	}

	/**
	 * @Description:判断是否是鲜度验证的tips
	 * @param oldTip
	 * @param json
	 * @return
	 * @author: y
	 * @time:2017-1-18 下午1:39:55
	 */
	private boolean isFreshnessVerification(JSONObject oldTip, JSONObject json) {
		
		//（鲜度验证的数据不入库：geometry、deep、feedback、track.t_command等字段内容不变）。 已确认不需要每个属性字段差分。几个属性合起来比较就可以了
		//old
		//solr里面就是字符串
		String geometryOld=oldTip.getString("geometry");
		JSONObject geoObj=JSONObject.fromObject(geometryOld);
		String g_locationOld=geoObj.getString("g_location");
		String g_guideOld=geoObj.getString("g_guide");
		String deepOld=oldTip.getString("deep");
		String feedbackOld=oldTip.getString("feedback");
		
		JSONObject trackOld=oldTip.getJSONObject("track");
		int tCommandOld=trackOld.getInt("t_command");
		
		String mdb5Old=MD5Utils.md5(g_locationOld+g_guideOld+deepOld+feedbackOld+tCommandOld);
		
		
		//new
		String g_locationNew=json.getString("g_location");
		String g_guideNew=json.getString("g_guide");
		String deepNew=json.getString("deep");
		String feedbackNew=json.getString("feedback");
		
		//JSONObject trackNew=json.getJSONObject("track");
		int tCommandNew=json.getInt("t_command");
		
		String mdb5New=MD5Utils.md5(g_locationNew+g_guideNew+deepNew+feedbackNew+tCommandNew);
		
		if(mdb5Old.equals(mdb5New)){
			return true;
		}
		
		return false;
	}

	private Put insertPut(String rowkey, JSONObject json) {

		Put put = new Put(rowkey.getBytes());
		
		JSONObject jsonTrack =TipsUtils.generateTrackJson(3, TipsUpload.IMPORT_STATE,json.getInt("t_handler"),
				json.getInt("t_command"), null, json.getString("t_operateDate"),currentDate,
				json.getInt("t_cStatus"),json.getInt("t_dStatus"),json.getInt("t_mStatus"),json.getInt("t_inMeth")
				,json.getInt("t_pStatus"),json.getInt("t_dInProc"),json.getInt("t_mInProc"));

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();
		JSONObject jsonSource = new JSONObject();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

		itKey = jsonGeomTemplate.keys();

		JSONObject jsonGeom = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonGeom.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
				.toString().getBytes());

		put.addColumn("data".getBytes(), "deep".getBytes(),
				json.getString("deep").getBytes());

		JSONObject feedback = json.getJSONObject("feedback");

		//feedback.put("f_array", json.getJSONArray("feedback"));

		put.addColumn("data".getBytes(), "feedback".getBytes(), feedback
				.toString().getBytes());

		return put;
	}

	/**
	 * 处理修改和删除的Tips
	 * 
	 * @param puts
	 */
	private void doUpdate(List<Put> puts) throws Exception {
		Set<Entry<String, JSONObject>> set = updateTips.entrySet();
		Iterator<Entry<String, JSONObject>> it = set.iterator();

		while (it.hasNext()) {
			String rowkey = "";

			try {
				Entry<String, JSONObject> en = it.next();

				rowkey = en.getKey();

				if (!oldTips.containsKey(rowkey)) {
					failed += 1;

					reasons.add(newReasonObject(rowkey, ErrorType.Notexist));

					continue;
				}

				JSONObject oldTip = oldTips.get(rowkey);

				JSONObject json = en.getValue();
				
				//是否是鲜度验证的tips
				if(isFreshnessVerification(oldTip,json)){
					
					reasons.add(newReasonObject(rowkey,
							ErrorType.FreshnessVerificationData));
					
					continue;
				}

				//时间判断
				int res = canUpdate(oldTip, json.getString("t_operateDate"));
				if (res < 0) {
					failed += 1;

					if (res == -1) {
						reasons.add(newReasonObject(rowkey, ErrorType.Deleted));
					} else {
						reasons.add(newReasonObject(rowkey,
								ErrorType.InvalidDate));
					}

					continue;
				}
				

				Put put = updatePut(rowkey, json, oldTip);

				JSONObject solrIndex = TipsUtils.generateSolrIndex(json,currentDate);

				solr.addTips(solrIndex);

				puts.add(put);
			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}
		}
	}

	private Put updatePut(String rowkey, JSONObject json, JSONObject oldTip) {

		Put put = new Put(rowkey.getBytes());

		int lifecycle = json.getInt("t_lifecycle");
		
		JSONObject oldTrack=oldTip.getJSONObject("track");

		JSONObject jsonTrack = TipsUtils.generateTrackJson(lifecycle,TipsUpload.IMPORT_STATE,
				json.getInt("t_handler"), json.getInt("t_command"),
				oldTrack.getJSONArray("t_trackInfo"),json.getString("t_operateDate"),currentDate,
				json.getInt("t_cStatus"),json.getInt("t_dStatus"),json.getInt("t_mStatus"),json.getInt("t_inMeth")
				,json.getInt("t_pStatus"),json.getInt("t_dInProc"),json.getInt("t_mInProc"));

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		JSONObject jsonSource = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

		itKey = jsonGeomTemplate.keys();

		JSONObject jsonGeom = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonGeom.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
				.toString().getBytes());

		put.addColumn("data".getBytes(), "deep".getBytes(),
				json.getString("deep").getBytes());
		
		/*JSONObject oldFeedBack = oldTip.getJSONObject("feedback");

		if (oldFeedBack.isNullObject()) {

			oldFeedBack = new JSONObject();

			oldFeedBack.put("f_array", new JSONArray());

		}

		JSONArray fArray = oldFeedBack.getJSONArray("f_array");

		JSONArray newFArray = new JSONArray();

		JSONObject graph = null;

		Set<String> picNames = new HashSet<String>();

		for (int i = 0; i < fArray.size(); i++) {
			JSONObject jo = fArray.getJSONObject(i);

			int type = jo.getInt("type");

			if (type == 1) {
				picNames.add(jo.getString("content"));
			}

			if (type == 6) {
				graph = jo;
			} else {
				newFArray.add(jo);
			}
		}*/

		/*JSONArray newFeedbacks = json.getJSONArray("feedback");

		for (int i = 0; i < newFeedbacks.size(); i++) {
			JSONObject newFeedback = newFeedbacks.getJSONObject(i);

			int type = newFeedback.getInt("type");
			if (type == 1) {
				if (!picNames.contains(newFeedback.getString("content"))) {
					newFArray.add(newFeedback);
				}
			} else if (type == 6) {
				graph = newFeedback;
			} else {
				newFArray.add(newFeedback);
			}
		}

		if (graph != null) {
			newFArray.add(graph);
		}*/

	/*	oldFeedBack.put("f_array", newFArray);
	 * 
	 * 
*/
		JSONObject feedback = json.getJSONObject("feedback");

		put.addColumn("data".getBytes(), "feedback".getBytes(), feedback
				.toString().getBytes());

		return put;
	}
	

	
	

	private Photo getPhoto(JSONObject attachment, JSONObject tip) {

		Photo photo = new Photo();

		JSONObject extContent = attachment.getJSONObject("extContent");

		double lng = extContent.getDouble("longitude");

		double lat = extContent.getDouble("latitude");

		// String uuid = fileName.replace(".jpg", "");
		//
		// String type = String.format("%02d",
		// Integer.valueOf(tip.getString("s_sourceType")));
		//
		// String key = GeoHash.geoHashStringWithCharacterPrecision(lat, lng,
		// 12)
		// + type + uuid;

		String id = attachment.getString("id");

		photo.setRowkey(id);

		//photo.setA_uuid(id.substring(14));
		
		photo.setA_uuid(id); 

		photo.setA_uploadUser(tip.getInt("t_handler"));

		photo.setA_uploadDate(tip.getString("t_operateDate"));

		photo.setA_longitude(lng);

		photo.setA_latitude(lat);

		// photo.setA_sourceId(tip.getString("s_sourceId"));

		photo.setA_direction(extContent.getDouble("direction"));

		photo.setA_shootDate(extContent.getString("shootDate"));

		photo.setA_deviceNum(extContent.getString("deviceNum"));

		photo.setA_fileName(attachment.getString("content"));

		photo.setA_content(1);

		return photo;
	}

	private int canUpdate(JSONObject oldTips, String operateDate) {
		JSONObject oldTrack= oldTips.getJSONObject("track");
		
		int lifecycle = oldTrack.getInt("t_lifecycle");

		JSONArray tracks = oldTrack.getJSONArray("t_trackInfo");
		
		String lastDate = null;
		
		//入库仅与上次stage=1的数组data进行比较. 最后一条stage=1的数据
		for (int i = tracks.size(); i >0; i--) {
			
			JSONObject info=tracks.getJSONObject(i-1);
			
			if(info.getInt("stage")==1){
				
				lastDate=info.getString("date");
				
				break;
			}
		}
			
		JSONObject lastTrack = tracks.getJSONObject(tracks.size() - 1);

		int lastStage = lastTrack.getInt("stage");
		

		//lifecycle:0（无） 1（删除）2（修改）3（新增） ;
		//stage:0 初始化；1 外业采集；2 内业日编；3 内业月编 ；4 GDB增量
		//库里最后最状态是不是增量更新删除：lifecycle=1（删除），t_stage=4（增量更新）,是，则不更新
		if (lifecycle == 1 && lastStage == 4) {

			return -1;
		}
        //最后一条数据stage!=0需要用stage=1的最后一条数据和采集端对比（stage=0是初始化数据，不进行时间对比）
		if (lastStage!=0&&operateDate.compareTo(lastDate) <= 0) {
			return -2;
		}

		return 0;
	}

	private JSONObject newReasonObject(String rowkey, int type) {

		JSONObject json = new JSONObject();

		json.put("rowkey", rowkey);

		json.put("type", type);

		return json;
	}

	public static void main(String[] args) throws Exception {
		
		Map<String, Photo> photoMap=new HashMap<String, Photo>();
		
		Map<String, Audio> audioMap=new HashMap<String, Audio>();

		TipsUpload a = new TipsUpload();

		a.run("D:/4.txt",photoMap,audioMap);
		
		System.out.println("成功");
	}
}
