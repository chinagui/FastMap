package com.navinfo.dataservice.control.service;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorUploadPois;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: UploadManager
 * @author xiaoxiaowen4127
 * @date 2017年4月25日
 * @Description: UploadManager.java
 */
public class UploadManager {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Long userId;
	private String fileName;
	private int subtaskId=0;
	private boolean multiThread=false;
	
	private UploadResult result;
	public UploadManager(long userId,String fileName){
		this.userId=userId;
		this.fileName=fileName;
	}
	public int getSubtaskId(){
		return subtaskId;
	}
	public void setSubtaskId(int subtaskId){
		this.subtaskId=subtaskId;
	}
	
	public UploadResult upload()throws Exception{
		result = new UploadResult();
		//1.读取文件
		if(StringUtils.isEmpty(fileName)) throw new Exception("上传文件名为空");
		JSONArray rawPois = readPois();
		if(rawPois==null||rawPois.size()==0){
			log.warn("从文件中未读取到有效poi,导入0条数据。");
			return result;
		}
		log.info("从文件中读取poi："+rawPois.size()+"条。");
		//2.将pois分发到各个大区
		Map<Integer,CollectorUploadPois> pois = distribute(rawPois);//key:大区库id
		//3.开始导入数据
		//先获取任务信息
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		Map<String,Integer> taskMap = manApi.getTaskBySubtaskId(subtaskId);
		int taskId=0;
		int taskType=0;
		if(taskMap!=null&&taskMap.size()>0){
			taskId=taskMap.get("taskId");
			taskType=taskMap.get("programType");
		}
		//开始入库
		for(Entry<Integer, CollectorUploadPois> entry:pois.entrySet()){
			int dbId = entry.getKey();
			CollectorUploadPois uPois = entry.getValue();
			log.info("start importing pois in dbId:"+dbId);
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				CollectorPoiImportorCommand cmd = new CollectorPoiImportorCommand(dbId,uPois);
				CollectorPoiImportor imp = new CollectorPoiImportor(conn,null);
				imp.setSubtaskId(subtaskId);
				imp.operate(cmd);
				//持久化会重置对象的操作状态，所以在持久化之前做更新edit_status
				PoiEditStatus.forCollector(conn,imp.getResult(),subtaskId,taskId,taskType);
				//写入数据库
				imp.persistChangeLog(OperationSegment.SG_ROW, userId);
			}catch(Exception e){
				log.error(e.getMessage(),e);
				DbUtils.rollbackAndCloseQuietly(conn);
				//如果发生异常，整个db的poi都未入库
				result.addResults(0, uPois.allFail("Db("+dbId+")连接异常"));
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
			}
			log.info("end importing pois in dbId:"+dbId);
		}
		log.info("Imported all pois.");
		return result;
		
	}
	private JSONArray readPois() throws Exception {
		FileInputStream fis=null;
		Scanner scan = null;
		try{
			fis = new FileInputStream(fileName);
			scan = new Scanner(fis);
			JSONArray pois = new JSONArray();
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(StringUtils.isNotEmpty(line)){
					pois.add(JSONObject.fromObject(line));
				}
			}
			return pois;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(fis!=null)fis.close();
			if(scan!=null)scan.close();
		}
	}
	private Map<Integer,CollectorUploadPois> distribute(JSONArray rawPois)throws Exception{
		Map<Integer,CollectorUploadPois> poiMap = new HashMap<Integer,CollectorUploadPois>();//key:大区dbid
		//计算poi所属图幅
		Map<String,Map<String,JSONObject>> meshPoiMap=new HashMap<String,Map<String,JSONObject>>();//key:mesh_id,value(key:fid,value:poi json)
		for (int i = 0; i < rawPois.size(); i++) {
			JSONObject jo = rawPois.getJSONObject(i);
			String fid = jo.getString("fid");
			try{
				// 坐标确定mesh，mesh确定区库ID
				String wkt = jo.getString("geometry");
				
				Geometry point = new WKTReader().read(wkt);
				Coordinate[] coordinate = point.getCoordinates();
				String mesh = MeshUtils.point2Meshes(coordinate[0].x, coordinate[0].y)[0];
				if(!meshPoiMap.containsKey(mesh)){
					meshPoiMap.put(mesh, new HashMap<String,JSONObject>());
				}
				meshPoiMap.get(mesh).put(fid, jo);
			}catch(Exception e){
				result.addFail(new ErrorLog(fid,"几何错误"));
				log.error(e.getMessage(),e);
			}
		}
		log.info("所有poi所属的图幅号："+StringUtils.join(meshPoiMap.keySet(),","));
		//映射到对应的大区库上
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		List<RegionMesh> regions = manApi.queryRegionWithMeshes(meshPoiMap.keySet());
		if(regions==null||regions.size()==0){
			log.error("根据图幅未查询到所属大区库信息");
			throw new Exception("根据图幅未查询到所属大区库信息");
		}
		for(Entry<String, Map<String,JSONObject>> entry:meshPoiMap.entrySet()){
			String meshId = entry.getKey();
			int dbId=0;
			for(RegionMesh r:regions){
				if(r.meshContains(meshId)){
					dbId = r.getDailyDbId();
					break;
				}
			}
			if(dbId>0){
				if(!poiMap.containsKey(dbId)){
					poiMap.put(dbId, new CollectorUploadPois());
				}
				poiMap.get(dbId).addJsonPois(entry.getValue());
			}else{
				for(String f:entry.getValue().keySet()){
					result.addFail(new ErrorLog(f,"所属图幅未找到大区库ID"));
				}
				log.warn("图幅（"+meshId+"）未找到大区库ID，涉及的poi有："+StringUtils.join(entry.getValue().keySet(),","));
			}
		}
		return poiMap;
	}
}
