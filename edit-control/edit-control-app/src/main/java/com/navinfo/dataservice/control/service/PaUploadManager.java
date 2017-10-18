package com.navinfo.dataservice.control.service;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.app.upload.stat.UploadCrossRegionInfoDao;
import com.navinfo.dataservice.control.app.upload.stat.UploadRegionInfoOperator;
import com.navinfo.dataservice.dao.plus.editman.PointaddressEditStatus;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPaImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPaImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorUploadPas;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: UploadManager
 * @author xiaoxiaowen4127
 * @date 2017年4月25日
 * @Description: UploadManager.java
 */
public class PaUploadManager {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Long userId;
	private String dir;
	private int subtaskId=0;
	private UploadResult result;
	
	public PaUploadManager(long userId,String dir){
		this.userId=userId;
		this.dir=dir;
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
		if(StringUtils.isEmpty(dir)) throw new Exception("上传目录为空");
		JSONArray rawPas = readPas();
		if(rawPas==null||rawPas.size()==0){
			log.warn("从文件中未读取到有效pa,导入0条数据。");
			return result;
		}
		result.setTotal(rawPas.size());
		log.info("从文件中读取pa："+result.getTotal()+"条。");
		//检查上传的文件的字段的格式
//		checkAttribute(rawPas);

		//先获取任务信息
		int taskId=0;
		int taskType=0;
		int taskDbId=0;
		if(subtaskId>0){
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			Map<String,Integer> taskMap = manApi.getTaskBySubtaskId(subtaskId);
			if(taskMap!=null&&taskMap.size()>0){
				taskId=taskMap.get("taskId");
				taskType=taskMap.get("programType");
			}else{
				throw new Exception("未查询到该子任务所属的任务。subtaskId:"+subtaskId);
			}
			Subtask subtask = manApi.queryBySubtaskId(subtaskId);
			taskDbId = subtask.getDbId();
		}
		//2.将pas分发到各个大区
		Collection<CollectorUploadPas> pas = distribute(rawPas);//
		//3.开始导入数据
		//开始入库
		for(CollectorUploadPas p:pas){
			//跨大区强制无任务号
			if(p.getRegionDayDbId()==taskDbId){
				uploadSingleRegion(p,subtaskId,taskId,taskType);
			}else{
				uploadSingleRegion(p,0,0,0);
				//无任务号写统计
				writeCrossRegionStat(subtaskId,p);
			}
		}
		log.info("Imported all pas .");
		return result;
		
	}
	private void uploadSingleRegion(CollectorUploadPas uPas,int stkId,int tkId,int tkType)throws Exception{
		long t1 = System.currentTimeMillis();
		log.info("start importing pas in regionId:"+uPas.getRegionId()+", dbId:"+uPas.getRegionDayDbId()+", stkId:"+stkId);
		
		int dbId = uPas.getRegionDayDbId();
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(dbId);
			CollectorPaImportorCommand cmd = new CollectorPaImportorCommand(dbId,uPas);
			cmd.setUserId(userId);
			CollectorPaImportor imp = new CollectorPaImportor(conn,null);
			imp.setSubtaskId(stkId);
			imp.operate(cmd);
			
			
			Set<Long> freshVerPas = imp.getFreshVerPas();
			//只修改了memo 的记录
			Set<Long> memoPas = imp.getMemoPas();
			//获取所有pois
			Map<Long,String> allPas = imp.getAllPas();
			//写入数据库
			imp.persistChangeLog(OperationSegment.SG_ROW, userId);
			result.addResults(imp.getSuccessNum(), imp.getErrLogs());
			RegionUploadResult regionResult = new RegionUploadResult(uPas.getRegionId());
			regionResult.setSubtaskId(stkId);
			regionResult.addResult(imp.getSuccessNum(), imp.getErrLogs().size());
			result.addRegionResult(regionResult);
			uPas.filterErrorFid(imp.getErrLogs());
			
			//维护编辑状态
			log.info("start writing pa edit status in dbId:"+dbId);
			//从所有的pa map中排除鲜度验证的pa
			for(Long fpi : freshVerPas){
				allPas.remove(fpi);
			}
			PointaddressEditStatus.forCollector(conn,allPas,freshVerPas,memoPas,stkId,tkId,tkType);
			long t2 = System.currentTimeMillis();
			log.info("pas imported,total time:"+(t2-t1)+"ms.");
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			//如果发生异常，整个db的pa都未入库
			List<ErrorLog> errs = uPas.allFail("Db("+dbId+")入库异常："+e.getMessage());
			result.addResults(0, errs);
			RegionUploadResult regionResult = new RegionUploadResult(uPas.getRegionId());
			regionResult.setSubtaskId(stkId);
			regionResult.addResult(0, errs.size());
			result.addRegionResult(regionResult);
			uPas.filterErrorFid(errs);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		log.info("end importing pas in dbId:"+dbId);
		
	}
	private void writeCrossRegionStat(int fromSubtaskId,CollectorUploadPas uPas){
		//
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			List<UploadCrossRegionInfoDao> infos = new ArrayList<UploadCrossRegionInfoDao>();
			for(Entry<String,Set<String>> entry:uPas.gridFids.entrySet()){
				if(entry.getValue()!=null&&entry.getValue().size()>0){
					UploadCrossRegionInfoDao info = new UploadCrossRegionInfoDao();
					info.setUserId(userId);
					info.setFromSubtaskId(fromSubtaskId);
					info.setUploadType(1);
					info.setOutRegionId(uPas.getRegionId());
					info.setOutGridId(Integer.valueOf(entry.getKey()));
					info.setOutGridNumber(entry.getValue().size());
					infos.add(info);
				}
			}
			UploadRegionInfoOperator op = new UploadRegionInfoOperator(conn);
			op.save(infos);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			//不抛异常
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private JSONArray readPas() throws Exception {
		FileInputStream fis=null;
		Scanner scan = null;
		try{
			fis = new FileInputStream(dir + "/pa.txt");
			scan = new Scanner(fis);
			JSONArray pas = new JSONArray();
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(StringUtils.isNotEmpty(line)){
					pas.add(JSONObject.fromObject(line));
				}
			}
			return pas;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(fis!=null)fis.close();
			if(scan!=null)scan.close();
		}
	}
	
	private Collection<CollectorUploadPas> distribute(JSONArray rawPas)throws Exception{
		Map<Integer,CollectorUploadPas> regionPaMap = new HashMap<Integer,CollectorUploadPas>();//key:regionId,values:upload pois
		//计算poi所属grid
		Map<String,Map<String,JSONObject>> gridPaMap=new HashMap<String,Map<String,JSONObject>>();//key:grid_id,value(key:fid,value:poi json)
		for (int i = 0; i < rawPas.size(); i++) {
			JSONObject jo = rawPas.getJSONObject(i);
			String fid = jo.getString("fid");
			try{
				// 坐标确定mesh，mesh确定区库ID
				String wkt = jo.getString("geometry");
				
				Geometry point = new WKTReader().read(wkt);
				Coordinate[] coordinate = point.getCoordinates();
				String grid = CompGridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
				if(!gridPaMap.containsKey(grid)){
					gridPaMap.put(grid, new HashMap<String,JSONObject>());
				}
				gridPaMap.get(grid).put(fid, jo);
			}catch(Exception e){
				result.addFail(new ErrorLog(fid,0,"几何错误"));
				log.error(e.getMessage(),e);
			}
		}
		log.info("所有pa所属的grid号："+StringUtils.join(gridPaMap.keySet(),","));
		//换算成图幅
		Set<String> meshes = new HashSet<String>();
		for(String g:gridPaMap.keySet()){
			meshes.add(g.substring(0, g.length()-2));
		}
		log.info("换算成图幅号："+StringUtils.join(meshes,","));
		//映射到对应的大区库上
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		List<RegionMesh> regions = manApi.queryRegionWithMeshes(meshes);
		if(regions==null||regions.size()==0){
			log.error("根据图幅未查询到所属大区库信息");
			throw new Exception("根据图幅未查询到所属大区库信息");
		}
		for(Entry<String, Map<String,JSONObject>> entry:gridPaMap.entrySet()){
			String gridId = entry.getKey();
			String meshId = gridId.substring(0, gridId.length()-2);
			int regionId=0;
			int regionDayDbId=0;
			for(RegionMesh r:regions){
				if(r.meshContains(meshId)){
					regionId = r.getRegionId();
					regionDayDbId = r.getDailyDbId();
					break;
				}
			}
			if(regionId>0&&regionDayDbId>0){
				if(!regionPaMap.containsKey(regionId)){
					CollectorUploadPas c = new CollectorUploadPas();
					c.setRegionId(regionId);
					c.setRegionDayDbId(regionDayDbId);
					regionPaMap.put(regionId, c);
				}
				regionPaMap.get(regionId).addJsonPas(entry.getValue());
				regionPaMap.get(regionId).gridFids.put(gridId, entry.getValue().keySet());
			}else{
				for(String f:entry.getValue().keySet()){
					result.addFail(new ErrorLog(f,0,"所属grid未找到大区库ID"));
				}
				log.warn("grid（"+gridId+"）未找到大区库ID，涉及的poi有："+StringUtils.join(entry.getValue().keySet(),","));
			}
		}
		return regionPaMap.values();
	}
	
	
	public static void main(String[] args) throws ParseException {
		/*String poiStr = "{'fid':'00166420170608112433','name':'小区1','pid':420000114,'meshid':595672,"
				+ "'kindCode':'120201','guide':{'latitude':39.93744,'linkPid':401714,'longitude':116.3441},"
				+ "'address':'','postCode':'','level':'B1','open24H':2,'parentFid':'',"
				+ "'relateChildren':[{'childFid':'00166420170608112348','childPid':0,'type':2,"
				+ "'rowId':'1717B716D39148D99620B5DCC59D42E9'}],"
				+ "'contacts':[{'number':'010-61784288','type':1,'linkman':'','priority':1,'rowId':'0'}],"
				+ "'foodtypes':{'foodtype':'2016','creditCards':'','parking':0,'openHour':'','avgCost':0,'rowId':'0'},"
				+ "'parkings':{'tollStd':'','tollDes':'','tollWay':'','openTime':'','totalNum':0,'payment':'',"
				+ "'remark':'','buildingType':'1','resHigh':0,'resWidth':0,'resWeigh':0,'certificate':0,"
				+ "'vehicle':0,'haveSpecialPlace':'','womenNum':0,'handicapNum':0,'miniNum':0,'vipNum':0,'rowId':'0'},"
				+ "'hotel':null,'sportsVenues':'','chargingStation':null,'chargingPole':[],'gasStation':null,"
				+ "'indoor':{'floor':'','type':0},"
				+ "'attachments':[{'content':'16778C3AE9B34A75ACDDF8883C3F37DE.jpg','extContent':'',"
				+ "'id':'16778C3AE9B34A75ACDDF8883C3F37DE','tag':3,'type':1}],"
				+ "'chain':'','rawFields':'','t_lifecycle':3,'geometry':'POINT (116.34411 39.93753)',"
				+ "'vipFlag':'',"
				+ "'t_operateDate':'20170608113754','truck':0,'sameFid':'','sourceName':'Android'}";
	
		 JSONObject poiJson = JSONObject.fromObject(poiStr);*/
		 
//		UploadIxPoi poi =jsonToPoiBean(poiJson);
	
		
		// 坐标确定mesh，mesh确定区库ID
		String wkt = "POINT (116.34411 39.93753)";
		
		Geometry point = new WKTReader().read(wkt);
		Coordinate[] coordinate = point.getCoordinates();
		String grid = CompGridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
		System.out.println(grid);
	}
}
