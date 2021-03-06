package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.io.FileInputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.convert.DefaultObjConvertor;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UploadOperationByGather {
	
//	private EditApi apiService ;
	//private QueryRunner runn;
	private Long userId;
	private int subtaskId=0;
	
	protected Logger log = Logger.getLogger(UploadOperationByGather.class);
//	protected Map<String,String> errLog = new HashMap<String,String>();
	protected JSONArray errLog = new JSONArray();
	public UploadOperationByGather(Long userId) {
		//log.info("ApplicationContextUtil.containsBean('editApi'): "+ApplicationContextUtil.containsBean("editApi"));
		//this.apiService=(EditApi) ApplicationContextUtil.getBean("editApi");
		//log.info("apiService: "+apiService);
	//	runn = new QueryRunner();
		this.userId = userId;
	}
	public UploadOperationByGather(Long userId,int subtaskId) {
		//log.info("ApplicationContextUtil.containsBean('editApi'): "+ApplicationContextUtil.containsBean("editApi"));
		//this.apiService=(EditApi) ApplicationContextUtil.getBean("editApi");
		//log.info("apiService: "+apiService);
	//	runn = new QueryRunner();
		this.userId = userId;
		this.subtaskId=subtaskId;
	}
	
	/**
	 * 读取txt，解析，入库
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public JSONObject importPoi(String fileName) throws Exception {
		JSONObject retObj = new JSONObject();
		Scanner importPois = new Scanner(new FileInputStream(fileName));
		JSONArray ja = new JSONArray();
		while (importPois.hasNextLine()) {
			try {
				String line = importPois.nextLine();
				if(line != null && StringUtils.isNotEmpty(line)){
					JSONObject json = JSONObject.fromObject(line);
					ja.add(json);
				}
				
			} catch (Exception e) {
				throw e;
			}
		}
		retObj = changeData(ja);
		return retObj;
	}

	/**
	 * 数据解析分类
	 * @param line
	 * @return
	 */
	@SuppressWarnings("static-access")
	private JSONObject changeData(JSONArray ja) throws Exception {
		Date startTime = new Date();
		JSONObject retObj = new JSONObject();
		//List<String> errList = new ArrayList<String>();
		Connection manConn = null;
		//Connection conn = null;
		// 获取当前做业季
		//String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			Map<Integer,MultiSrcUploadPois> poiMap =  distribute(manConn,ja);
			
			
			// 执行转数据
			for(Map.Entry<Integer, MultiSrcUploadPois> entry:poiMap.entrySet()){
				Integer dbId = entry.getKey();
				log.info("start txt import to oracle dbId="+dbId);
				MultiSrcUploadPois pois = entry.getValue();
				Connection conn=null;
//				List<BasicObj> ixPoiObjs = new ArrayList<BasicObj>();
				try{
					conn=DBConnector.getInstance().getConnectionById(dbId);

					ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
					Map<String,Integer> taskMap = manApi.getTaskBySubtaskId(subtaskId);
					int taskId=0;
					int taskType=0;
					if(taskMap!=null&&taskMap.size()>0){
						taskId=taskMap.get("taskId");
						taskType=taskMap.get("programType");
					}
					//导入数据
					MultiSrcPoiDayImportorCommand cmd = new MultiSrcPoiDayImportorCommand(dbId,pois);
					CollectorUploadOperation imp = new CollectorUploadOperation(conn,null);
					imp.setSubtaskId(subtaskId);
					imp.operate(cmd);
					//持久化会重置对象的操作状态，所以在持久化之前做更新edit_status
//					PoiEditStatus.forCollector(conn,imp.getResult(),subtaskId,taskId,taskType);
					//持久化
					imp.persistChangeLog(OperationSegment.SG_ROW, userId);//userid 未写
					
					//导入父子关系
					PoiRelationImportorCommand relCmd = new PoiRelationImportorCommand();
					relCmd.setPoiRels(imp.getParentPid());
					PoiRelationImportor relImp = new PoiRelationImportor(conn,imp.getResult());
					relImp.setSubtaskId(subtaskId);
					relImp.operate(relCmd);
					//持久化会重置对象的操作状态，所以在持久化之前做更新edit_status
//					PoiEditStatus.forCollector(conn,relImp.getResult(),subtaskId,taskId,taskType);
					//持久化
					relImp.persistChangeLog(OperationSegment.SG_ROW, userId);
					
				
					errLog.addAll(imp.getErrLog());
					log.debug("dbId("+dbId+")转入成功。");
					
					/*//*************zl 2017.03.14 采集成果批处理**************
//					runBatchPoi(result,dbId);
					Map<Long,BasicObj> mapObj =result.getObjsMapByType(ObjectName.IX_POI);
					if(mapObj.values() != null && mapObj.values().size() > 0){
						ixPoiObjs.addAll(mapObj.values());
					}*/
					
				}catch(Exception e){
					DbUtils.rollbackAndCloseQuietly(conn);
					log.error(e.getMessage(),e);
					throw new ThreadExecuteException("");
				}finally{
					DbUtils.commitAndCloseQuietly(conn);
					log.info("3.....");
				}
				
				/*//poi上传批处理
				try{
					log.info("runBatchPoi begin1.....");
					runBatchPoi(ixPoiObjs,dbId);
					log.info("runBatchPoi end2.....");
				}catch(Exception e){
					log.error(e.getMessage(),e);
				}*/
			}
			
			retObj.put("success", ja.size()-errLog.size());//成功的poi 总数
			
			retObj.put("fail", errLog);
			return retObj;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(manConn);
			Date endTime = new Date();
			log.info("total time:"+ (endTime.getTime() - startTime.getTime())+"ms");
		}
	}

	private String calDbDataMapping( Connection manConn,String grid) throws SQLException {
		String dbId = "";
		String manQuery = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
		QueryRunner qRunner = new QueryRunner();
		dbId = qRunner.queryForString(manConn, manQuery, grid);
		return dbId;
	}
	
	//分库
	private Map<Integer,MultiSrcUploadPois> distribute(Connection manConn,JSONArray pois)throws Exception{
		Map<Integer,MultiSrcUploadPois> poiMap = new HashMap<Integer,MultiSrcUploadPois>();//key:大区dbid
		//ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		MultiMap gridDataMapping = new MultiValueMap();
		for (int i = 0; i < pois.size(); i++) {
			JSONObject jo = pois.getJSONObject(i);
			if(jo.getString("fid").equals("0010130518WMH00081")){
				log.info("");
			}
			// 坐标确定grid，grid确定区库ID
			String wkt = jo.getString("geometry");
			Geometry point = new WKTReader().read(wkt);
			Coordinate[] coordinate = point.getCoordinates();
			CompGridUtil gridUtil = new CompGridUtil();
			String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
			gridDataMapping.put(grid, jo);
			
		}
		log.info("计算dbid和上传数据的对应关系");
		MultiMap dbDataMapping = new MultiValueMap();//dbid--jsonlist 对应map
		for (Object grid :gridDataMapping.keySet()){
			String  dbId = calDbDataMapping( manConn, grid.toString());
			log.info("gridId:"+grid+",dbId:"+dbId);
			if (dbId == null || dbId.isEmpty()) {
				 List mapcoll = (List) gridDataMapping.get(grid);
	             Iterator ii = mapcoll.iterator();  
	             while(ii.hasNext()){  
	                JSONObject mailValue = (JSONObject) ii.next();  
					JSONObject errObj = new JSONObject();
					errObj.put("fid", mailValue.get("fid"));
					errObj.put("reason",  "通过poi坐标计算出来的grid："+grid+",无法查询得到对应的大区库");
					errLog.add(errObj);
				}
//				errLog.put(gridDataMapping.get(grid).toString(), "通过poi坐标计算出来的grid："+grid+",无法查询得到对应的大区库");
				continue;
			}
			if((List)gridDataMapping.get(grid) != null && ((List)gridDataMapping.get(grid)).size() > 0){
				for (Object data: (List)gridDataMapping.get(grid)) {
					dbDataMapping.put(dbId,data);
				}
			}
			
		}
		//每个db中计算出上传poi对应的fid，lifecycle，urecord，pid
		for(Iterator<String> iter = dbDataMapping.keySet().iterator(); iter.hasNext();){
			String dbId = iter.next();
			List<JSONObject> poiList = (List<JSONObject>) dbDataMapping.get(dbId);
			Map<String,PoiWrap> fidPoiMap = new HashMap<String,PoiWrap>();
			for(JSONObject poiJson:poiList){
				String fid = poiJson.getString("fid");
				log.info("dbId:  "+dbId+" fid:"+fid);
				int lifecycle = poiJson.getInt("t_lifecycle");
				PoiWrap poiWrap = new PoiWrap(fid,lifecycle,poiJson);
				fidPoiMap.put(fid, poiWrap);
			}
			log.info("开始计算dbid:"+dbId+",中的数据record和pid");
			calPoiFromDbByFids(null, dbId,fidPoiMap);
			log.info("把db中的数据，分为增删改");
			
			for(String fid:fidPoiMap.keySet()){
				// 判断每一条数据是新增、修改还是删除
				PoiWrap poiWrap = fidPoiMap.get(fid);
				
				JSONObject poi = poiWrap.getPoiJson();
				int uRecord = poiWrap.getuRecord();
				int lifecycle = poiWrap.getLifecycle();
				if (uRecord != -1) {
					// 能找到，判断lifecycle和u_record
					poi.put("pid", poiWrap.getPid());
					if (lifecycle == 1) {
						poi.put("delFlag", 1);
						poi.put("addFlag", 0);
						poi.put("updateFlag", 0);
						log.info("需要删除的poi: fid:"+fid+" pid:"+poiWrap.getPid());
					} else {
						if (uRecord == 2) {
							JSONObject errObj = new JSONObject();
							errObj.put("fid", fid);
							errObj.put("reason", "数据为修改数据，但库中u_record为2");
							errLog.add(errObj);
//							errLog.put(fid,"数据为修改数据，但库中u_record为2");
							poi.put("delFlag", 0);
							poi.put("addFlag", 0);
							poi.put("updateFlag", 0);
							continue;
						} else {
							poi.put("delFlag", 0);
							poi.put("addFlag", 0);
							poi.put("updateFlag", 1);
							log.info("需要修改的poi: fid:"+fid+" pid:"+poiWrap.getPid());
						}
					}
				} else {
					// 找不到，判断lifecycle是否为1
					if (lifecycle == 1) {
						JSONObject errObj = new JSONObject();
						errObj.put("fid", fid);
						errObj.put("reason", "数据在库中找不到对应数据，但lifecycle为1");
						errLog.add(errObj);
//						errLog.put(fid,"数据在库中找不到对应数据，但lifecycle为1");
						poi.put("delFlag", 0);
						poi.put("addFlag", 0);
						poi.put("updateFlag", 0);
						continue;
					} else {
						poi.put("delFlag", 0);
						poi.put("addFlag", 1);
						poi.put("updateFlag", 0);
						log.info("需要新增的poi: fid:"+fid+" pid:"+poiWrap.getPid());
					}
				}
				MultiSrcUploadPois upoi = poiMap.get(Integer.parseInt(dbId));
				if(upoi==null){
					upoi=new MultiSrcUploadPois();
					poiMap.put(Integer.parseInt(dbId), upoi);
				}
				upoi.addJsonPoi(poi);
			}
			
		}
		
		log.debug("分发数据完成");
		return poiMap;
	}
	private void calPoiFromDbByFids(Connection conn, String dbId,Map<String,PoiWrap>fidPoiMap) throws Exception {
		if (conn != null) {
			DBUtils.closeConnection(conn);
		}
		conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Clob fidClod = null;
		try {
			fidClod = ConnectionUtil.createClob(conn);
			String fidSeq = org.apache.commons.lang.StringUtils.join(fidPoiMap.keySet(), ",");
			log.info("fidSeq"+fidSeq);
			fidClod.setString(1, fidSeq);
			stmt = conn.prepareStatement("SELECT poi_num,u_record,pid FROM ix_poi WHERE poi_num in( select column_value from table(clob_to_table(?)))");
			stmt.setClob(1, fidClod);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int uRecord = rs.getInt("u_record");
				int pid = rs.getInt("pid");
				String fid = rs.getString("poi_num");
				PoiWrap poiWrap = fidPoiMap.get(fid);
				poiWrap.setPid(pid);
				poiWrap.setuRecord(uRecord);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}

	private class PoiWrap{

		private String fid;
		private int lifecycle;
		private JSONObject poiJson;
		private int pid=0;
		private int uRecord=-1;

		public PoiWrap(String fid, int lifecycle, JSONObject poiJson) {
			this.fid = fid;
			this.lifecycle =lifecycle;
			this.poiJson = poiJson;
		}

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public int getuRecord() {
			return uRecord;
		}

		public void setuRecord(int uRecord) {
			this.uRecord = uRecord;
		}

		public String getFid() {
			return fid;
		}

		public int getLifecycle() {
			return lifecycle;
		}

		public JSONObject getPoiJson() {
			return poiJson;
		}
		
		
	}
}
