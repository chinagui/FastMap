package com.navinfo.dataservice.control.service;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
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
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.model.UploadIxPoi;
import com.navinfo.dataservice.control.model.UploadIxPoiAttachments;
import com.navinfo.dataservice.control.model.UploadIxPoiChargingPole;
import com.navinfo.dataservice.control.model.UploadIxPoiChargingStation;
import com.navinfo.dataservice.control.model.UploadIxPoiContacts;
import com.navinfo.dataservice.control.model.UploadIxPoiFoodtypes;
import com.navinfo.dataservice.control.model.UploadIxPoiGuide;
import com.navinfo.dataservice.control.model.UploadIxPoiHotel;
import com.navinfo.dataservice.control.model.UploadIxPoiIndoor;
import com.navinfo.dataservice.control.model.UploadIxPoiParkings;
import com.navinfo.dataservice.control.model.UploadIxPoiRelateChildren;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiPcRelationImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiPcRelationImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiSpRelationImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorPoiSpRelationImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorUploadPois;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
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
	
	public UploadResult upload(Map<String, Photo> photoMap)throws Exception{
		result = new UploadResult();
		//1.读取文件
		if(StringUtils.isEmpty(fileName)) throw new Exception("上传文件名为空");
		JSONArray rawPois = readPois();
		if(rawPois==null||rawPois.size()==0){
			log.warn("从文件中未读取到有效poi,导入0条数据。");
			return result;
		}
		result.setTotal(rawPois.size());
		log.info("从文件中读取poi："+result.getTotal()+"条。");
		//检查上传的文件的字段的格式
		checkAttribute(rawPois);
		
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
				imp.operate(cmd,photoMap,userId);
				
				
				Set<Long> freshVerPois = imp.getFreshVerPois();
				//获取所有pois
				Map<Long,String> allPois = imp.getAllPois();
				//写入数据库
				imp.persistChangeLog(OperationSegment.SG_ROW, userId);
				result.addResults(imp.getSuccessNum(), imp.getErrLogs());
				//父子关系
				CollectorPoiPcRelationImportorCommand pcCmd = new CollectorPoiPcRelationImportorCommand(dbId,imp.getPcs());
				CollectorPoiPcRelationImportor pcImp = new CollectorPoiPcRelationImportor(conn,imp.getResult());
				pcImp.setSubtaskId(subtaskId);
				pcImp.operate(pcCmd);
				for(Long l:pcImp.getChangedPids()){
					freshVerPois.remove(l);
				}
				pcImp.persistChangeLog(OperationSegment.SG_ROW, userId);
				result.addWarnPcs(pcImp.getErrLogs());
				//同一关系
				CollectorPoiSpRelationImportorCommand spCmd = new CollectorPoiSpRelationImportorCommand(dbId,imp.getSps());
				CollectorPoiSpRelationImportor spImp = new CollectorPoiSpRelationImportor(conn,null);
				spImp.setSubtaskId(subtaskId);
				spImp.operate(spCmd);
				for(Long l:spImp.getChangedPids()){
					freshVerPois.remove(l);
				}
				spImp.persistChangeLog(OperationSegment.SG_ROW, userId);
				result.addWarnSps(spImp.getErrLogs());
				//维护编辑状态
//				PoiEditStatus.forCollector(conn,allPois,freshVerPois,subtaskId,taskId,taskType);
				//从所有的poi map中排除鲜度验证的poi
				for(Long fpi : freshVerPois){
					allPois.remove(fpi);
				}
				Set<Long> freshVerPoisForPhoto = imp.getFreshVerPoisForPhoto();
				PoiEditStatus.forCollector(conn,allPois,freshVerPois,subtaskId,taskId,taskType,freshVerPoisForPhoto);
			}catch(Exception e){
				log.error(e.getMessage(),e);
				DbUtils.rollbackAndCloseQuietly(conn);
				//如果发生异常，整个db的poi都未入库
				result.addResults(0, uPois.allFail("Db("+dbId+")入库异常："+e.getMessage()));
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
				result.addFail(new ErrorLog(fid,0,"几何错误"));
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
					result.addFail(new ErrorLog(f,0,"所属图幅未找到大区库ID"));
				}
				log.warn("图幅（"+meshId+"）未找到大区库ID，涉及的poi有："+StringUtils.join(entry.getValue().keySet(),","));
			}
		}
		return poiMap;
	}
	
	/**
	 * @Title: checkAttribute
	 * @Description: 检查上传poi 的各个属性值是否格式错误
	 * @param rawPois  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月22日 上午9:33:54 
	 */
	public  void checkAttribute(JSONArray rawPois){
		System.out.println("rawPois: "+rawPois.size());
		List<UploadIxPoi> uploadIxPois = new ArrayList<UploadIxPoi>();
		for (int i = 0; i < rawPois.size(); i++) {
			JSONObject jo = rawPois.getJSONObject(i);
			UploadIxPoi uploadIxPoi= jsonToPoiBean(jo);
			if(uploadIxPoi != null){
				uploadIxPois.add(uploadIxPoi);
			}
		}
		System.out.println("uploadIxPois: "+uploadIxPois.size());
	}
	
	/**
	 * @Title: jsonToPoiBean
	 * @Description: 上传poi.txt 文件转java bean
	 * @param jo
	 * @return  UploadIxPoi
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月22日 上午9:32:30 
	 */
	public UploadIxPoi jsonToPoiBean(JSONObject jo){
		UploadIxPoi uploadIxPoi = new UploadIxPoi();
		String fid = null;
		String errorFiled = null;
		try{
			errorFiled = "fid";
			if(jo.containsKey("fid") && !(jo.get("fid") instanceof JSONNull)){
				fid = jo.getString("fid");
				uploadIxPoi.setFid(fid);
			}
			errorFiled = "name";
			if(jo.containsKey("name") && !(jo.get("name") instanceof JSONNull)){
				uploadIxPoi.setName(jo.getString("name"));
			}
			errorFiled = "pid";
			if(jo.containsKey("pid")){
				uploadIxPoi.setPid(jo.getInt("pid"));
			}
			errorFiled = "meshid";
			if(jo.containsKey("meshid")){
				uploadIxPoi.setMeshid(jo.getInt("meshid"));
			}
			errorFiled = "kindCode";
			if(jo.containsKey("kindCode") &&  !(jo.get("kindCode") instanceof JSONNull)){
				uploadIxPoi.setKindCode(jo.getString("kindCode"));
			}
			errorFiled = "guide";
			if(jo.containsKey("guide") && !(jo.get("guide") instanceof JSONNull)){
				JSONObject guideJobj = jo.getJSONObject("guide");
				UploadIxPoiGuide poiGuide = new UploadIxPoiGuide();
				errorFiled="guide:latitude";
				if(guideJobj.containsKey("latitude")){
					poiGuide.setLatitude(guideJobj.getDouble("latitude"));
				}
				errorFiled="guide:linkPid";
				if(guideJobj.containsKey("linkPid")){
					poiGuide.setLinkPid(guideJobj.getInt("linkPid"));
				}
				errorFiled="guide:longitude";
				if(guideJobj.containsKey("longitude")){
					poiGuide.setLongitude(guideJobj.getDouble("longitude"));
				}
				
				if(poiGuide != null){
					uploadIxPoi.setGuide(poiGuide);
				}
			}
			errorFiled = "address";
			if(jo.containsKey("address") &&  !(jo.get("address") instanceof JSONNull)){
				uploadIxPoi.setAddress(jo.getString("address"));
			}
			
			errorFiled = "postCode";
			if(jo.containsKey("postCode") &&  !(jo.get("postCode") instanceof JSONNull)){
				uploadIxPoi.setPostCode(jo.getString("postCode"));
			}
			errorFiled = "level";
			if(jo.containsKey("level") &&  !(jo.get("level") instanceof JSONNull)){
				uploadIxPoi.setLevel(jo.getString("level"));
			}
			errorFiled = "open24H";
			if(jo.containsKey("open24H")){
				uploadIxPoi.setOpen24h(jo.getInt("open24H"));
			}
			errorFiled = "parentFid";
			if(jo.containsKey("parentFid") &&  !(jo.get("parentFid") instanceof JSONNull)){
				uploadIxPoi.setParentFid(jo.getString("parentFid"));
			}
			errorFiled = "relateChildren";
			if(jo.containsKey("relateChildren") && !(jo.get("relateChildren") instanceof JSONNull) && jo.getJSONArray("relateChildren").size() > 0){
				JSONArray relateChildrenJarr = jo.getJSONArray("relateChildren");
				Set<UploadIxPoiRelateChildren> relateChildrenSet = new HashSet<UploadIxPoiRelateChildren>();
				for(int i=0; i<relateChildrenJarr.size() ; i++){
					UploadIxPoiRelateChildren ixPoiRelateChildren = new UploadIxPoiRelateChildren();
					JSONObject relateChildrenJobj = (JSONObject) relateChildrenJarr.get(i);
					errorFiled = "relateChildren:type";
					if(relateChildrenJobj.containsKey("type")){
						ixPoiRelateChildren.setType(relateChildrenJobj.getInt("type"));
					}
					errorFiled = "relateChildren:childPid";
					if(relateChildrenJobj.containsKey("childPid")){
						ixPoiRelateChildren.setChildPid(relateChildrenJobj.getInt("childPid"));					
					}
					errorFiled = "relateChildren:rowId";
					if(relateChildrenJobj.containsKey("rowId") &&  !(relateChildrenJobj.get("rowId") instanceof JSONNull)){
						ixPoiRelateChildren.setRowId(relateChildrenJobj.getString("rowId"));
					}
					
					if(ixPoiRelateChildren != null){
						relateChildrenSet.add(ixPoiRelateChildren);
					}
				}
				if(relateChildrenSet != null && relateChildrenSet.size() >0){
					uploadIxPoi.setRelateChildren(relateChildrenSet);
				}
			}
			errorFiled = "contacts";
			if(jo.containsKey("contacts") && !(jo.get("contacts") instanceof JSONNull) && jo.getJSONArray("contacts").size() > 0){
				JSONArray contactsJarr = jo.getJSONArray("contacts");
				Set<UploadIxPoiContacts> contactsSet = new HashSet<>();
				for(int i=0; i<contactsJarr.size() ; i++){
					UploadIxPoiContacts ixPoiContacts= new UploadIxPoiContacts();
					JSONObject relateChildrenJobj = (JSONObject) contactsJarr.get(i);
					errorFiled = "contacts:number";
					if(relateChildrenJobj.containsKey("number") &&  !(relateChildrenJobj.get("number") instanceof JSONNull)){
						ixPoiContacts.setNumber(relateChildrenJobj.getString("number"));
					}	
					errorFiled = "relateChildren:type";
					if(relateChildrenJobj.containsKey("type")){
						ixPoiContacts.setType(relateChildrenJobj.getInt("type"));				
					}
					errorFiled = "relateChildren:linkman";
					if(relateChildrenJobj.containsKey("linkman") &&  !(relateChildrenJobj.get("linkman") instanceof JSONNull)){
						ixPoiContacts.setLinkman(relateChildrenJobj.getString("linkman"));
					}
					errorFiled = "relateChildren:priority";
					if(relateChildrenJobj.containsKey("priority")){
						ixPoiContacts.setPriority(relateChildrenJobj.getInt("priority"));
					}
					errorFiled = "relateChildren:rowId";
					if(relateChildrenJobj.containsKey("rowId") &&  !(relateChildrenJobj.get("rowId") instanceof JSONNull)){
						ixPoiContacts.setRowId(relateChildrenJobj.getString("rowId"));
					}
					contactsSet.add(ixPoiContacts);
				}
				if(contactsSet != null && contactsSet.size() >0){
					uploadIxPoi.setContacts(contactsSet);
				}
				
			}
			errorFiled = "foodtypes";
			if(jo.containsKey("foodtypes") && !(jo.get("foodtypes") instanceof JSONNull)){
				UploadIxPoiFoodtypes ixPoiFoodtypes=new UploadIxPoiFoodtypes();
				JSONObject foodtypesJobj = jo.getJSONObject("foodtypes");
				errorFiled = "foodtypes:foodtype";
				if(foodtypesJobj.containsKey("foodtype") &&  !(foodtypesJobj.get("foodtype") instanceof JSONNull)){
					ixPoiFoodtypes.setFoodtype(foodtypesJobj.getString("foodtype"));
				}
				errorFiled = "foodtypes:creditCards";
				if(foodtypesJobj.containsKey("creditCards") &&  !(foodtypesJobj.get("creditCards") instanceof JSONNull)){
					ixPoiFoodtypes.setCreditCards(foodtypesJobj.getString("creditCards"));
				}
				errorFiled = "foodtypes:parking";
				if(foodtypesJobj.containsKey("parking")){
					ixPoiFoodtypes.setParking(foodtypesJobj.getInt("parking"));
				}
				errorFiled = "foodtypes:openHour";
				if(foodtypesJobj.containsKey("openHour") &&  !(foodtypesJobj.get("openHour") instanceof JSONNull)){
					ixPoiFoodtypes.setOpenHour(foodtypesJobj.getString("openHour"));
				}
				errorFiled = "foodtypes:avgCost";
				if(foodtypesJobj.containsKey("avgCost")){
					ixPoiFoodtypes.setAvgCost(foodtypesJobj.getInt("avgCost"));
				}
				errorFiled = "foodtypes:rowId";
				if(foodtypesJobj.containsKey("rowId") &&  !(foodtypesJobj.get("rowId") instanceof JSONNull)){
					ixPoiFoodtypes.setRowId(foodtypesJobj.getString("rowId"));
				}
				if(ixPoiFoodtypes != null){
					uploadIxPoi.setFoodtypes(ixPoiFoodtypes);
				}
			}
			if(jo.containsKey("parkings") && !(jo.get("parkings") instanceof JSONNull)){
				UploadIxPoiParkings ixPoiParkings = new UploadIxPoiParkings();
				JSONObject parkingsJobj = jo.getJSONObject("parkings");
				errorFiled = "parkings:tollStd";
				if(parkingsJobj.containsKey("tollStd") &&  !(parkingsJobj.get("tollStd") instanceof JSONNull)){
					ixPoiParkings.setTollStd(parkingsJobj.getString("tollStd"));
				}
				errorFiled = "parkings:tollDes";
				if(parkingsJobj.containsKey("tollDes") &&  !(parkingsJobj.get("tollDes") instanceof JSONNull)){
					ixPoiParkings.setTollDes(parkingsJobj.getString("tollDes"));
				}
				errorFiled = "parkings:tollWay";
				if(parkingsJobj.containsKey("tollWay") &&  !(parkingsJobj.get("tollWay") instanceof JSONNull)){
					ixPoiParkings.setTollWay(parkingsJobj.getString("tollWay"));
				}
				errorFiled = "parkings:openTime";
				if(parkingsJobj.containsKey("openTime") &&  !(parkingsJobj.get("openTime") instanceof JSONNull)){
					ixPoiParkings.setOpenTime(parkingsJobj.getString("openTime"));
				}
				errorFiled = "parkings:totalNum";
				if(parkingsJobj.containsKey("totalNum")){
					ixPoiParkings.setTotalNum(parkingsJobj.getInt("totalNum"));
				}
				errorFiled = "parkings:payment";
				if(parkingsJobj.containsKey("payment") &&  !(parkingsJobj.get("payment") instanceof JSONNull)){
					ixPoiParkings.setPayment(parkingsJobj.getString("payment"));
				}
				errorFiled = "parkings:remark";
				if(parkingsJobj.containsKey("remark") &&  !(parkingsJobj.get("remark") instanceof JSONNull)){
					ixPoiParkings.setRemark(parkingsJobj.getString("remark"));
				}
				errorFiled = "parkings:buildingType";
				if(parkingsJobj.containsKey("buildingType") &&  !(parkingsJobj.get("buildingType") instanceof JSONNull)){
					ixPoiParkings.setBuildingType(parkingsJobj.getString("buildingType"));
				}
				errorFiled = "parkings:resHigh";
				if(parkingsJobj.containsKey("resHigh")){
					ixPoiParkings.setResHigh(parkingsJobj.getInt("resHigh"));
				}
				errorFiled = "parkings:resWidth";
				if(parkingsJobj.containsKey("resWidth")){
					ixPoiParkings.setResWidth(parkingsJobj.getInt("resWidth"));
				}
				errorFiled = "parkings:resWeigh";
				if(parkingsJobj.containsKey("resWeigh")){
					ixPoiParkings.setResWeigh(parkingsJobj.getInt("resWeigh"));
				}
				errorFiled = "parkings:certificate";
				if(parkingsJobj.containsKey("certificate")){
					ixPoiParkings.setCertificate(parkingsJobj.getInt("certificate"));
				}
				errorFiled = "parkings:vehicle";
				if(parkingsJobj.containsKey("vehicle")){
					ixPoiParkings.setVehicle(parkingsJobj.getInt("vehicle"));
				}
				errorFiled = "parkings:haveSpecialPlace";
				if(parkingsJobj.containsKey("haveSpecialPlace") &&  !(parkingsJobj.get("haveSpecialPlace") instanceof JSONNull)){
					ixPoiParkings.setHaveSpecialPlace(parkingsJobj.getString("haveSpecialPlace"));
				}
				errorFiled = "parkings:womenNum";
				if(parkingsJobj.containsKey("womenNum")){
					ixPoiParkings.setWomenNum(parkingsJobj.getInt("womenNum"));
				}
				errorFiled = "parkings:handicapNum";
				if(parkingsJobj.containsKey("handicapNum")){
					ixPoiParkings.setHandicapNum(parkingsJobj.getInt("handicapNum"));
				}
				errorFiled = "parkings:miniNum";
				if(parkingsJobj.containsKey("miniNum")){
					ixPoiParkings.setMiniNum(parkingsJobj.getInt("miniNum"));
				}
				errorFiled = "parkings:vipNum";
				if(parkingsJobj.containsKey("vipNum")){
					ixPoiParkings.setVipNum(parkingsJobj.getInt("vipNum"));
				}
				errorFiled = "parkings:rowId";
				if(parkingsJobj.containsKey("rowId") &&  !(parkingsJobj.get("rowId") instanceof JSONNull)){
					ixPoiParkings.setRowId(parkingsJobj.getString("rowId"));
				}
				
				if(ixPoiParkings != null){
					uploadIxPoi.setParkings(ixPoiParkings);
				}
			}
			
			errorFiled = "hotel";
			if(jo.containsKey("hotel") && !(jo.get("hotel") instanceof JSONNull)){
				UploadIxPoiHotel ixPoiHotel = new UploadIxPoiHotel();
				JSONObject hotelJobj = jo.getJSONObject("hotel");
				errorFiled = "hotel:rating";
				if(hotelJobj.containsKey("rating")){
					ixPoiHotel.setRating(hotelJobj.getInt("rating"));
				}
				errorFiled = "hotel:creditCards";
				if(hotelJobj.containsKey("creditCards") &&  !(hotelJobj.get("creditCards") instanceof JSONNull)){
					ixPoiHotel.setCreditCards(hotelJobj.getString("creditCards"));
				}
				errorFiled = "hotel:description";
				if(hotelJobj.containsKey("description") &&  !(hotelJobj.get("description") instanceof JSONNull)){
					ixPoiHotel.setDescription(hotelJobj.getString("description"));
				}
				errorFiled = "hotel:checkInTime";
				if(hotelJobj.containsKey("checkInTime") &&  !(hotelJobj.get("checkInTime") instanceof JSONNull)){
					ixPoiHotel.setCheckInTime(hotelJobj.getString("checkInTime"));
				}
				errorFiled = "hotel:checkOutTime";
				if(hotelJobj.containsKey("checkOutTime") &&  !(hotelJobj.get("checkOutTime") instanceof JSONNull)){
					ixPoiHotel.setCheckOutTime(hotelJobj.getString("checkOutTime"));
				}
				errorFiled = "hotel:roomCount";
				if(hotelJobj.containsKey("roomCount")){
					ixPoiHotel.setRoomCount(hotelJobj.getInt("roomCount"));
				}
				errorFiled = "hotel:roomType";
				if(hotelJobj.containsKey("roomType") &&  !(hotelJobj.get("roomType") instanceof JSONNull)){
					ixPoiHotel.setRoomType(hotelJobj.getString("roomType"));
				}
				errorFiled = "hotel:roomPrice";
				if(hotelJobj.containsKey("roomPrice") &&  !(hotelJobj.get("roomPrice") instanceof JSONNull)){
					ixPoiHotel.setRoomPrice(hotelJobj.getString("roomPrice"));
				}
				errorFiled = "hotel:breakfast";
				if(hotelJobj.containsKey("breakfast")){
					ixPoiHotel.setBreakfast(hotelJobj.getInt("breakfast"));
				}
				errorFiled = "hotel:service";
				if(hotelJobj.containsKey("service") &&  !(hotelJobj.get("service") instanceof JSONNull)){
					ixPoiHotel.setService(hotelJobj.getString("service"));
				}
				errorFiled = "hotel:parking";
				if(hotelJobj.containsKey("parking")){
					ixPoiHotel.setParking(hotelJobj.getInt("parking"));
				}
				errorFiled = "hotel:openHour";
				if(hotelJobj.containsKey("openHour") &&  !(hotelJobj.get("openHour") instanceof JSONNull)){
					ixPoiHotel.setOpenHour(hotelJobj.getString("openHour"));
				}
				errorFiled = "hotel:rowId";
				if(hotelJobj.containsKey("rowId") &&  !(hotelJobj.get("rowId") instanceof JSONNull)){
					ixPoiHotel.setRowId(hotelJobj.getString("rowId"));
				}
				if(ixPoiHotel != null){
					uploadIxPoi.setHotel(ixPoiHotel);
				}
			}
			errorFiled = "chargingStation";
			if(jo.containsKey("chargingStation") && !(jo.get("chargingStation") instanceof JSONNull)){
				UploadIxPoiChargingStation ixPoiChargingStation = new UploadIxPoiChargingStation();
				JSONObject chargingStationJobj = jo.getJSONObject("chargingStation");
				errorFiled = "chargingStation:type";
				if(chargingStationJobj.containsKey("type")){
					ixPoiChargingStation.setType(chargingStationJobj.getInt("type"));
				}
				errorFiled = "chargingStation:changeBrands";
				if(chargingStationJobj.containsKey("changeBrands") &&  !(chargingStationJobj.get("changeBrands") instanceof JSONNull)){
					ixPoiChargingStation.setChangeBrands(chargingStationJobj.getString("changeBrands"));
				}
				errorFiled = "chargingStation:changeOpenType";
				if(chargingStationJobj.containsKey("changeOpenType") &&  !(chargingStationJobj.get("changeOpenType") instanceof JSONNull)){
					ixPoiChargingStation.setChangeOpenType(chargingStationJobj.getString("changeOpenType"));
				}
				errorFiled = "chargingStation:servicePro";
				if(chargingStationJobj.containsKey("servicePro") &&  !(chargingStationJobj.get("servicePro") instanceof JSONNull)){
					ixPoiChargingStation.setServicePro(chargingStationJobj.getString("servicePro"));
				}
				errorFiled = "chargingStation:chargingNum";
				if(chargingStationJobj.containsKey("chargingNum")){
					ixPoiChargingStation.setChargingNum(chargingStationJobj.getInt("chargingNum"));
				}
				errorFiled = "chargingStation:openHour";
				if(chargingStationJobj.containsKey("openHour") &&  !(chargingStationJobj.get("openHour") instanceof JSONNull)){
					ixPoiChargingStation.setOpenHour(chargingStationJobj.getString("openHour"));
				}
				errorFiled = "chargingStation:parkingFees";
				if(chargingStationJobj.containsKey("parkingFees")){
					ixPoiChargingStation.setParkingFees(chargingStationJobj.getInt("parkingFees"));
				}
				errorFiled = "chargingStation:parkingInfo";
				if(chargingStationJobj.containsKey("parkingInfo") &&  !(chargingStationJobj.get("parkingInfo") instanceof JSONNull)){
					ixPoiChargingStation.setParkingInfo(chargingStationJobj.getString("parkingInfo"));
				}
				errorFiled = "chargingStation:availableState";
				if(chargingStationJobj.containsKey("availableState")){
					ixPoiChargingStation.setAvailableState(chargingStationJobj.getInt("availableState"));
				}
				errorFiled = "chargingStation:rowId";
				if(chargingStationJobj.containsKey("rowId") &&  !(chargingStationJobj.get("rowId") instanceof JSONNull)){
					ixPoiChargingStation.setRowId(chargingStationJobj.getString("rowId"));
				}
				if(ixPoiChargingStation != null){
					uploadIxPoi.setChargingStation(ixPoiChargingStation);
				}
			}
			errorFiled = "chargingPole";
			if(jo.containsKey("chargingPole") && !(jo.get("chargingPole") instanceof JSONNull) && jo.getJSONArray("chargingPole").size() > 0){
				JSONArray chargingPoleJarr = jo.getJSONArray("chargingPole");
				Set<UploadIxPoiChargingPole> chargingPoleSet = new HashSet<UploadIxPoiChargingPole>();
				for(int i=0; i<chargingPoleJarr.size() ; i++){
					UploadIxPoiChargingPole ixPoiChargingPole = new UploadIxPoiChargingPole();
					JSONObject chargingPoleJarrJobj = (JSONObject) chargingPoleJarr.get(i);
					errorFiled = "chargingPole:groupId";
					if(chargingPoleJarrJobj.containsKey("groupId")){
						ixPoiChargingPole.setGroupId(chargingPoleJarrJobj.getInt("groupId"));
					}
					errorFiled = "chargingPole:acdc";
					if(chargingPoleJarrJobj.containsKey("acdc")){
						ixPoiChargingPole.setAcdc(chargingPoleJarrJobj.getInt("acdc"));
					}
					errorFiled = "chargingPole:plugType";
					if(chargingPoleJarrJobj.containsKey("plugType") &&  !(chargingPoleJarrJobj.get("plugType") instanceof JSONNull)){
						ixPoiChargingPole.setPlugType(chargingPoleJarrJobj.getString("plugType"));
					}
					errorFiled = "chargingPole:power";
					if(chargingPoleJarrJobj.containsKey("power") &&  !(chargingPoleJarrJobj.get("power") instanceof JSONNull)){
						ixPoiChargingPole.setPower(chargingPoleJarrJobj.getString("power"));
					}
					errorFiled = "chargingPole:voltage";
					if(chargingPoleJarrJobj.containsKey("voltage") &&  !(chargingPoleJarrJobj.get("voltage") instanceof JSONNull)){
						ixPoiChargingPole.setVoltage(chargingPoleJarrJobj.getString("voltage"));
					}
					errorFiled = "chargingPole:current";
					if(chargingPoleJarrJobj.containsKey("current") &&  !(chargingPoleJarrJobj.get("current") instanceof JSONNull)){
						ixPoiChargingPole.setCurrent(chargingPoleJarrJobj.getString("current"));
					}
					errorFiled = "chargingPole:mode";
					if(chargingPoleJarrJobj.containsKey("mode")){
						ixPoiChargingPole.setMode(chargingPoleJarrJobj.getInt("mode"));
					}
					errorFiled = "chargingPole:count";
					if(chargingPoleJarrJobj.containsKey("count")){
						ixPoiChargingPole.setCount(chargingPoleJarrJobj.getInt("count"));
					}
					errorFiled = "chargingPole:plugNum";
					if(chargingPoleJarrJobj.containsKey("plugNum")){
						ixPoiChargingPole.setPlugNum(chargingPoleJarrJobj.getInt("plugNum"));
					}
					errorFiled = "chargingPole:prices";
					if(chargingPoleJarrJobj.containsKey("prices") &&  !(chargingPoleJarrJobj.get("prices") instanceof JSONNull)){
						ixPoiChargingPole.setPrices(chargingPoleJarrJobj.getString("prices"));
					}
					errorFiled = "chargingPole:openType";
					if(chargingPoleJarrJobj.containsKey("openType") &&  !(chargingPoleJarrJobj.get("openType") instanceof JSONNull)){
						ixPoiChargingPole.setOpenType(chargingPoleJarrJobj.getString("openType"));
					}
					errorFiled = "chargingPole:availableState";
					if(chargingPoleJarrJobj.containsKey("groupIdavailableState")){
						ixPoiChargingPole.setAvailableState(chargingPoleJarrJobj.getInt("groupIdavailableState"));
					}
					errorFiled = "chargingPole:manufacturer";
					if(chargingPoleJarrJobj.containsKey("manufacturer") &&  !(chargingPoleJarrJobj.get("manufacturer") instanceof JSONNull)){
						ixPoiChargingPole.setManufacturer(chargingPoleJarrJobj.getString("manufacturer"));
					}
					errorFiled = "chargingPole:factoryNum";
					if(chargingPoleJarrJobj.containsKey("factoryNum") &&  !(chargingPoleJarrJobj.get("factoryNum") instanceof JSONNull)){
						ixPoiChargingPole.setFactoryNum(chargingPoleJarrJobj.getString("factoryNum"));
					}
					errorFiled = "chargingPole:plotNum";
					if(chargingPoleJarrJobj.containsKey("plotNum") &&  !(chargingPoleJarrJobj.get("plotNum") instanceof JSONNull)){
						ixPoiChargingPole.setPlotNum(chargingPoleJarrJobj.getString("plotNum"));
					}
					errorFiled = "chargingPole:productNum";
					if(chargingPoleJarrJobj.containsKey("productNum") &&  !(chargingPoleJarrJobj.get("productNum") instanceof JSONNull)){
						ixPoiChargingPole.setProductNum(chargingPoleJarrJobj.getString("productNum"));
					}
					errorFiled = "chargingPole:parkingNum";
					if(chargingPoleJarrJobj.containsKey("parkingNum") &&  !(chargingPoleJarrJobj.get("parkingNum") instanceof JSONNull)){
						ixPoiChargingPole.setParkingNum(chargingPoleJarrJobj.getString("parkingNum"));
					}
					errorFiled = "chargingPole:floor";
					if(chargingPoleJarrJobj.containsKey("floor")){
						ixPoiChargingPole.setFloor(chargingPoleJarrJobj.getInt("floor"));
					}
					errorFiled = "chargingPole:locationType";
					if(chargingPoleJarrJobj.containsKey("locationType")){
						ixPoiChargingPole.setLocationType(chargingPoleJarrJobj.getInt("locationType"));
					}
					errorFiled = "chargingPole:payment";
					if(chargingPoleJarrJobj.containsKey("payment") &&  !(chargingPoleJarrJobj.get("payment") instanceof JSONNull)){
						ixPoiChargingPole.setPayment(chargingPoleJarrJobj.getString("payment"));
					}
					errorFiled = "chargingPole:rowId";
					if(chargingPoleJarrJobj.containsKey("rowId") &&  !(chargingPoleJarrJobj.get("rowId") instanceof JSONNull)){
						ixPoiChargingPole.setRowId(chargingPoleJarrJobj.getString("rowId"));
					}
					chargingPoleSet.add(ixPoiChargingPole);
				}
				if(chargingPoleSet != null && chargingPoleSet.size() > 0){
					uploadIxPoi.setChargingPole(chargingPoleSet);
				}
			}
			errorFiled = "indoor";
			if(jo.containsKey("indoor") && !(jo.get("indoor") instanceof JSONNull)){
				UploadIxPoiIndoor ixPoiIndoor= new UploadIxPoiIndoor();
				JSONObject indoorJobj = jo.getJSONObject("indoor");
				errorFiled = "indoor:floor";
				if(indoorJobj.containsKey("floor") &&  !(indoorJobj.get("floor") instanceof JSONNull)){
					ixPoiIndoor.setFloor(indoorJobj.getString("floor"));
				}
				errorFiled = "indoor:type";
				if(indoorJobj.containsKey("type")){
					ixPoiIndoor.setType(indoorJobj.getInt("type"));
				}
				if(ixPoiIndoor != null){
					uploadIxPoi.setIndoor(ixPoiIndoor);
				}
			}
			errorFiled = "attachments";
			if(jo.containsKey("attachments") && !(jo.get("attachments") instanceof JSONNull) && jo.getJSONArray("attachments").size() > 0){
				JSONArray attachmentsJarr = jo.getJSONArray("attachments");
				Set<UploadIxPoiAttachments> attachmentsSet = new HashSet<UploadIxPoiAttachments>();
				for(int i=0; i<attachmentsJarr.size() ; i++){
					UploadIxPoiAttachments ixPoiAttachments = new UploadIxPoiAttachments();
					JSONObject attachmentsJobj = (JSONObject) attachmentsJarr.get(i);
					errorFiled = "attachments:content";
					if(attachmentsJobj.containsKey("content") &&  !(attachmentsJobj.get("content") instanceof JSONNull)){
						ixPoiAttachments.setContent(attachmentsJobj.getString("content"));
					}
					errorFiled = "attachments:extContent";
					if(attachmentsJobj.containsKey("extContent") &&  !(attachmentsJobj.get("extContent") instanceof JSONNull)){
						ixPoiAttachments.setExtContent(attachmentsJobj.getString("extContent"));
					}
					errorFiled = "attachments:id";
					if(attachmentsJobj.containsKey("id") &&  !(attachmentsJobj.get("id") instanceof JSONNull)){
						ixPoiAttachments.setId(attachmentsJobj.getString("id"));
					}
					errorFiled = "attachments:tag";
					if(attachmentsJobj.containsKey("tag")){
						ixPoiAttachments.setTag(attachmentsJobj.getInt("tag"));
					}
					errorFiled = "attachments:type";
					if(attachmentsJobj.containsKey("type")){
						ixPoiAttachments.setType(attachmentsJobj.getInt("type"));
					}
					attachmentsSet.add(ixPoiAttachments);
				}
				if(attachmentsSet !=  null && attachmentsSet.size() > 0){
					uploadIxPoi.setAttachments(attachmentsSet);
				}
			}
			errorFiled = "chain";
			if(jo.containsKey("chain") &&  !(jo.get("chain") instanceof JSONNull)){
				uploadIxPoi.setChain(jo.getString("chain"));
			}
			
			errorFiled = "rawFields";
			if(jo.containsKey("rawFields") &&  !(jo.get("rawFields") instanceof JSONNull)){
				uploadIxPoi.setRawFields(jo.getString("rawFields"));
			}
			errorFiled = "t_lifecycle";
			if(jo.containsKey("t_lifecycle")){
				int t_lifecycle = jo.getInt("t_lifecycle");
				uploadIxPoi.setT_lifecycle(jo.getInt("t_lifecycle"));
				if(t_lifecycle != 1 && t_lifecycle != 2 && t_lifecycle != 3){
					result.addFail(new ErrorLog(fid,2,"lifecycle不为1,2,3 !"));
				}
			}
			errorFiled = "geometry";
			if(jo.containsKey("geometry") &&  !(jo.get("geometry") instanceof JSONNull)){
				uploadIxPoi.setGeometry(jo.getString("geometry"));
			}
			errorFiled = "vipFlag";
			if(jo.containsKey("vipFlag") &&  !(jo.get("vipFlag") instanceof JSONNull)){
				uploadIxPoi.setVipFlag(jo.getString("vipFlag"));
			}
			errorFiled = "t_operateDate";
			if(jo.containsKey("t_operateDate") &&  !(jo.get("t_operateDate") instanceof JSONNull)){
				uploadIxPoi.setT_operateDate(jo.getString("t_operateDate"));
			}
			errorFiled = "truck";
			if(jo.containsKey("truck")){
				uploadIxPoi.setTruck(jo.getInt("truck"));
			}
			
			errorFiled = "sameFid";
			if(jo.containsKey("sameFid") &&  !(jo.get("sameFid") instanceof JSONNull)){
				uploadIxPoi.setSameFid(jo.getString("sameFid"));
			}
			errorFiled = "sourceName";
			if(jo.containsKey("sourceName") &&  !(jo.get("sourceName") instanceof JSONNull)){
				uploadIxPoi.setSourceName(jo.getString("sourceName"));
			}
		}catch(Exception e){
			result.addFail(new ErrorLog(fid,1,"数据属性（"+errorFiled+"）格式错误!"));
			log.error(e.getMessage(),e);
		}
		return uploadIxPoi;
	}
	
	public static void main(String[] args) {
		String poiStr = "{'fid':'00166420170608112433','name':'小区1','pid':420000114,'meshid':595672,"
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
	
		 JSONObject poiJson = JSONObject.fromObject(poiStr);
		 
//		UploadIxPoi poi =jsonToPoiBean(poiJson);
	
	}
}
