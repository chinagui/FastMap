package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

/** 
 * @ClassName: MultiSrcPoiDayImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月17日
 * @Description: MultiSrcPoiDayImportor.java
 */
public class MultiSrcPoiDayImportor extends AbstractOperation {

	protected Map<String,String> errLog = new HashMap<String,String>();
	protected List<PoiRelation> parentPid = new ArrayList<PoiRelation>();
	protected Map<Long,String> sourceTypes = new HashMap<Long,String>();
	private int dbId;
	
	protected Map<Long,Integer> quickSubtaskIdMap = new HashMap<Long,Integer>();
	protected Map<Long,Integer> mediumSubtaskIdMap = new HashMap<Long,Integer>();
	protected List<PoiRelation> samePoiPid = new ArrayList<PoiRelation>();
	protected Set<Long> insertPids = new HashSet<Long>();
	protected Set<Long> pidWithoutSubtask = new HashSet<Long>();
	protected Set<Long> pids = new HashSet<Long>();
	
	private List<Map<String,Object>> scPointTruckList = new ArrayList<Map<String,Object>>();

	
	public Map<Long, String> getSourceTypes() {
		return sourceTypes;
	}
	
	public Map<Long, Integer> getQuickSubtaskIdMap() {
		return quickSubtaskIdMap;
	}
	public Map<Long, Integer> getMediumSubtaskIdMap() {
		return mediumSubtaskIdMap;
	}

	public MultiSrcPoiDayImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}

	public Map<String, String> getErrLog() {
		return errLog;
	}

	public List<PoiRelation> getParentPid() {
		return parentPid;
	}
	public List<PoiRelation> getSamePoiPid() {
		return samePoiPid;
	}
	public Set<Long> getInsertPids() {
		return insertPids;
	}
	public Set<Long> getPidWithoutSubtask() {
		return pidWithoutSubtask;
	}
	public Set<Long> getPids() {
		return pids;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		MultiSrcUploadPois pois = ((MultiSrcPoiDayImportorCommand)cmd).getPois();
		this.dbId= ((MultiSrcPoiDayImportorCommand)cmd).getDbId();
		
		//入库时如果常规数据或众包数据未作业完成，即处于"待作业"或"待提交"状态且存在常规子任务或众包子任务号，则多源数据不入大区域库，返回失败信息，失败信息报log：常规(众包)子任务XX正在作业！
		filterPoiUnderSubtask(pois);

		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		//获取多源子任务-grid
		Map<Integer,List<Integer>> quickSubtaskGridMapping = manApi.getOpendMultiSubtaskGridMappingByDbId(this.dbId,4);
		Map<Integer,List<Integer>> mediumSubtaskGridMapping = manApi.getOpendMultiSubtaskGridMappingByDbId(this.dbId,1);
		
		if(pois!=null){
			//加载元数据库sc_point_truck
			MetadataApi metaApi = (MetadataApi)ApplicationContextUtil.getBean("metadataApi");
			this.scPointTruckList = metaApi.getScPointTruckList();
			//新增
			Map<String, JSONObject> addPois = pois.getAddPois();
			if(addPois!=null&&addPois.size()>0){
				List<IxPoiObj> ixPoiObjAdd = this.improtAdd(conn,addPois);
				//根据多源子任务范围删选数据，记录POI多源子任务信息
				List<IxPoiObj> ixPoiObjAddResult = filterPoiUnderSubtask(ixPoiObjAdd,quickSubtaskGridMapping,mediumSubtaskGridMapping);
				result.putAll(ixPoiObjAddResult);
			}
			//修改
			Map<String, JSONObject> updatePois = pois.getUpdatePois();
			if(updatePois!=null&&updatePois.size()>0){
				List<IxPoiObj> ixPoiObjUpdate = this.improtUpdate(conn,updatePois);
				//根据多源子任务范围删选数据，记录POI多源子任务信息
				List<IxPoiObj> ixPoiObjUpdateResult = filterPoiUnderSubtask(ixPoiObjUpdate,quickSubtaskGridMapping,mediumSubtaskGridMapping);
				result.putAll(ixPoiObjUpdateResult);
			}
			//删除
			Map<String, JSONObject> deletePois = pois.getDeletePois();
			if(deletePois!=null&&deletePois.size()>0){
				List<IxPoiObj> ixPoiObjDelete = this.improtDelete(conn, deletePois);
				//根据多源子任务范围删选数据，记录POI多源子任务信息
				List<IxPoiObj> ixPoiObjDeleteResult = filterPoiUnderSubtask(ixPoiObjDelete,quickSubtaskGridMapping,mediumSubtaskGridMapping);
				result.putAll(ixPoiObjDeleteResult);
			}

		}
	}
	
	/**
	 * @param ixPoiObjList
	 * @param mediumSubtaskGridMapping 
	 * @param quickSubtaskGridMapping 
	 * @throws Exception 
	 */
	private List<IxPoiObj> filterPoiUnderSubtask(List<IxPoiObj> ixPoiObjList, Map<Integer, List<Integer>> quickSubtaskGridMapping, Map<Integer, List<Integer>> mediumSubtaskGridMapping) throws Exception {
		List<IxPoiObj> result = new ArrayList<IxPoiObj>();
		for(IxPoiObj obj:ixPoiObjList){
			boolean flg = false;
			IxPoi poi = (IxPoi)obj.getMainrow();
			Set<String> gridSet = CompGeometryUtil.geo2GridsWithoutBreak(poi.getGeometry());
			
			for(String gridId:gridSet){
				for(Entry<Integer, List<Integer>> entry:quickSubtaskGridMapping.entrySet()){
					if(entry.getValue().contains(Integer.parseInt(gridId))){
						if(quickSubtaskIdMap.containsKey(poi.getPid())){
							int subtaskId = quickSubtaskIdMap.get(poi.getPid());
							if(subtaskId < entry.getKey()){
								flg = true;
								if(pidWithoutSubtask.contains(poi.getPid())){
									break;
								}
								quickSubtaskIdMap.put(poi.getPid(), entry.getKey());
							}
						}else{
							flg = true;
							if(pidWithoutSubtask.contains(poi.getPid())){
								break;
							}
							quickSubtaskIdMap.put(poi.getPid(), entry.getKey());

						}
					}
				}
				
				if(!flg){
					for(Entry<Integer, List<Integer>> entry:mediumSubtaskGridMapping.entrySet()){
						if(entry.getValue().contains(Integer.parseInt(gridId))){
							if(mediumSubtaskIdMap.containsKey(poi.getPid())){
								int subtaskId = mediumSubtaskIdMap.get(poi.getPid());
								if(subtaskId<entry.getKey()){
									flg = true;
									if(pidWithoutSubtask.contains(poi.getPid())){
										break;
									}
									mediumSubtaskIdMap.put(poi.getPid(), entry.getKey());
								}
							}else{
								flg = true;
								if(pidWithoutSubtask.contains(poi.getPid())){
									break;
								}
								mediumSubtaskIdMap.put(poi.getPid(), entry.getKey());
							}
						}
					}
				}
			}

			if(flg){
				result.add(obj);
			}else{
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				errLog.put(ixPoi.getPoiNum(), "FID为" + ixPoi.getPoiNum() + "的数据找不到对应的多源子任务！");
			}
		}
		return result;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void multiSubtaskInfo() throws Exception {
		
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		//获取多源子任务-grid
		Map<Integer,List<Integer>> quickSubtaskGridMapping = manApi.getOpendMultiSubtaskGridMappingByDbId(this.dbId,4);
		Map<Integer,List<Integer>> mediumSubtaskGridMapping = manApi.getOpendMultiSubtaskGridMappingByDbId(this.dbId,1);
		
		List<BasicObj> objs = result.getAllObjs();
		for(BasicObj obj:objs){
			boolean flg = false;
			IxPoi poi = (IxPoi)obj.getMainrow();
			Set<String> gridSet = CompGeometryUtil.geo2GridsWithoutBreak(poi.getGeometry());

			if(pidWithoutSubtask.contains(poi.getPid())){
				continue;
			}
			
			for(String gridId:gridSet){
				for(Entry<Integer, List<Integer>> entry:quickSubtaskGridMapping.entrySet()){
					if(entry.getValue().contains(Integer.parseInt(gridId))){
						if(quickSubtaskIdMap.containsKey(poi.getPid())){
							int subtaskId = quickSubtaskIdMap.get(poi.getPid());
							if(subtaskId<entry.getKey()){
								quickSubtaskIdMap.put(poi.getPid(), entry.getKey());
								flg = true;
							}
						}else{
							quickSubtaskIdMap.put(poi.getPid(), entry.getKey());
							flg = true;
						}
					}
				}
				
				if(quickSubtaskIdMap.containsKey(poi.getPid())){
					break;
				}
				
				for(Entry<Integer, List<Integer>> entry:mediumSubtaskGridMapping.entrySet()){
					if(entry.getValue().contains(Integer.parseInt(gridId))){
						if(mediumSubtaskIdMap.containsKey(poi.getPid())){
							int subtaskId = mediumSubtaskIdMap.get(poi.getPid());
							if(subtaskId<entry.getKey()){
								mediumSubtaskIdMap.put(poi.getPid(), entry.getKey());
								flg = true;
							}
						}else{
							mediumSubtaskIdMap.put(poi.getPid(), entry.getKey());
							flg = true;
						}
					}
				}
				break;
			}
			if(!flg){
				
			}

		}
	}

	/**
	 * @param pois 
	 * @throws Exception 
	 * 
	 */
	private void filterPoiUnderSubtask(MultiSrcUploadPois pois) throws Exception {
		//全部fids
		List<String> fids = new ArrayList<String>();
		//修改或者删除的fids
		List<String> uOrDfids = new ArrayList<String>();

		fids.addAll(pois.getAddPois().keySet());
		fids.addAll(pois.getUpdatePois().keySet());
		fids.addAll(pois.getDeletePois().keySet());

		
		Map<String,Integer> poiUnderNormalSubtask = PoiEditStatus.poiUnderSubtask(conn,this.dbId,fids,1);
		Map<String,Integer> poiUnderCrowdsSubtask = PoiEditStatus.poiUnderSubtask(conn,this.dbId,fids,2);

		//无子任务数据：status in (1,2),无子任务号
		pidWithoutSubtask = PoiEditStatus.poiWithOutSubtask(conn,this.dbId,fids);

		for(Map.Entry<String, Integer> entry:poiUnderNormalSubtask.entrySet()){
			errLog.put(entry.getKey(), "常规(众包)子任务"+ entry.getValue() +"正在作业！");
			if(pois.getAddPois().containsKey(entry.getKey())){
				pois.getAddPois().remove(entry.getKey());
			}else if(pois.getUpdatePois().containsKey(entry.getKey())){
				pois.getUpdatePois().remove(entry.getKey());
			}else if(pois.getDeletePois().containsKey(entry.getKey())){
				pois.getDeletePois().remove(entry.getKey());
			}
		}
		for(Map.Entry<String, Integer> entry:poiUnderCrowdsSubtask.entrySet()){
			errLog.put(entry.getKey(), "常规(众包)子任务"+ entry.getValue() +"正在作业！");
			if(pois.getAddPois().containsKey(entry.getKey())){
				pois.getAddPois().remove(entry.getKey());
			}else if(pois.getUpdatePois().containsKey(entry.getKey())){
				pois.getUpdatePois().remove(entry.getKey());
			}else if(pois.getDeletePois().containsKey(entry.getKey())){
				pois.getDeletePois().remove(entry.getKey());
			}
		}
	}

	/**
	 * 新增数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtAdd(Connection conn,Map<String, JSONObject> addPois)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//排除fid已存在的
		filterAddedPoi(addPois);
		
		for (Map.Entry<String, JSONObject> entry : addPois.entrySet()) {
			JSONObject jo = entry.getValue();
			//日志
			log.info("多源新增json数据"+jo.toString());
			try {
				IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
				importAddByJson(poiObj, jo);
				ixPoiObjList.add(poiObj);
				insertPids.add(poiObj.getMainrow().getObjPid());
				pids.add(poiObj.getMainrow().getObjPid());
				
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"新增执行失败!":e.getMessage());
			}
		}
		return ixPoiObjList;
	}
	
	/**
	 * 修改数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jso
	 * @param updatePois 
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtUpdate(Connection conn,Map<String, JSONObject> updatePois)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,updatePois.keySet(),true,true);

		//排除有变更的数据
		filterUpdatePoi(updatePois);
		//开始导入
		for (Map.Entry<String, JSONObject> jo : updatePois.entrySet()) {
			//日志
			log.info("多源修改json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(jo.getKey(), "日库中没有查到相应的数据");
			}else{
				try{
					IxPoiObj ixPoiObj = (IxPoiObj)obj;
					if(ixPoiObj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						this.importUpdateByJson(ixPoiObj, jo.getValue());
						ixPoiObjList.add(ixPoiObj);
						pids.add(ixPoiObj.getMainrow().getObjPid());
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(jo.getKey(), StringUtils.isEmpty(e.getMessage())?"修改执行出现空指针错误":e.getMessage());
				}
			}
		}
		return ixPoiObjList;
	}
	
	/**
	 * 删除数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtDelete(Connection conn,Map<String,JSONObject> deletePois)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,deletePois.keySet(),true,true);
		//排除有变更的数据
		filterUpdatePoi(deletePois);
		//开始导入
		for (Map.Entry<String, JSONObject> jo : deletePois.entrySet()) {
			//日志
			log.info("多源删除json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(jo.getKey(), "日库中没有查到相应的数据");
			}else{
				try{
					IxPoiObj ixPoiObj = (IxPoiObj)obj;
					if(ixPoiObj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						this.importDeleteByJson(ixPoiObj, jo.getValue());
						ixPoiObjList.add(ixPoiObj);
						pids.add(ixPoiObj.getMainrow().getObjPid());
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(jo.getKey(), StringUtils.isEmpty(e.getMessage())?"删除执行出现空指针错误":e.getMessage());
				}
			}
		}
		return ixPoiObjList;
	}
	
	public boolean importAddByJson(IxPoiObj poi,JSONObject jo)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//POI主表
				IxPoi ixPoi = (IxPoi) poi.getMainrow();
				//显示坐标经度
				Double lng = (Double) jo.getDouble("lng");
				//显示坐标纬度
				Double lat = (Double) jo.getDouble("lat");
				//显示坐标经纬度--图幅号码meshId
				String[] meshes = MeshUtils.point2Meshes(lng, lat);
				if(meshes.length>1){
					throw new ImportException("POI坐标不能在图框线上");
				}
				ixPoi.setMeshId(Integer.parseInt(meshes[0]));
				//FID--POI编号poiNum
				String fid = jo.getString("fid");
				ixPoi.setPoiNum(fid);
				//显示坐标经纬度--显示坐标
				Geometry geometry = GeoTranslator.point2Jts(lng, lat);
				ixPoi.setGeometry(geometry);
				//引导坐标经度--引导 X坐标xGuide
				Double guidelon = jo.getDouble("guidelon");
				ixPoi.setXGuide(guidelon);
				//引导坐标纬度--引导Y坐标yGuide
				Double guidelat = jo.getDouble("guidelat");
				ixPoi.setYGuide(guidelat);
				//判断是大陆/港澳
				String langCode = null;
				if(!JSONUtils.isNull(jo.get("regionInfo"))){
					String regionInfo = jo.getString("regionInfo");
					if("D".equals(regionInfo)){
						//大陆
						langCode = "CHI";
					}else if("HM".equals(regionInfo)){
						//港澳
						langCode = "CHT";
					} 
				}else{
					throw new Exception("区域信息regionInfo字段名不存在");
				}
				//名称
				if(!JSONUtils.isNull(jo.get("name"))){
					String name = jo.getString("name");
					//IX_POI_NAME表
					IxPoiName ixPoiName = poi.createIxPoiName();
					ixPoiName.setName(name);
					ixPoiName.setNameClass(1);
					ixPoiName.setNameType(2);
					ixPoiName.setLangCode(langCode);
				}else{
					throw new Exception("名称name字段名不存在");
				}
				//二代分类-种别代码 KIND_CODE
				if(!JSONUtils.isNull(jo.get("kind"))){
					String kind = jo.getString("kind");
					ixPoi.setKindCode(kind);
				}else{
					throw new Exception("二代分类kind字段名不存在");
				}
				//[集合]风味类型
				if(!JSONUtils.isNull(jo.get("foodType"))){
					String foodType = jo.getString("foodType");
					if(StringUtils.isNotEmpty(foodType)){
						//IX_POI_RESTAURANT表
						IxPoiRestaurant ixPoiRestaurant = poi.createIxPoiRestaurant();
						ixPoiRestaurant.setFoodType(foodType);
					}
				}else{
					throw new Exception("风味类型foodType字段名不存在");
				}
				//[集合]联系方式
				if(!JSONUtils.isNull(jo.get("contacts"))){
					String contacts = jo.getString("contacts");
					if(!"[]".equals(contacts)){
						JSONArray ja = JSONArray.fromObject(contacts);
						for (int i=0;i<ja.size();i++) {
							JSONObject jso = ja.getJSONObject(i);
							//号码number
							String number = null;
							if(!JSONUtils.isNull(jso.get("number"))){
								number = jso.getString("number");
							}else{
								throw new Exception("号码number字段名不存在");
							}
							//联系方式类型type
							int type = jso.getInt("type");
							//IX_POI_CONTACT表
							IxPoiContact ixPoiContact = poi.createIxPoiContact();
							ixPoiContact.setContact(number);
							ixPoiContact.setContactType(type);
							//zl 2017.11.08 新增电话优先级赋值
							System.out.println("新增电话优先级: "+i+1);
							ixPoiContact.setPriority(i+1);
						}
					}
				}else{
					throw new Exception("联系方式contacts字段名不存在");
				}
				//邮编--邮政编码 POST_CODE
				if(!JSONUtils.isNull(jo.get("postCode"))){
					String postCode = jo.getString("postCode");
					ixPoi.setPostCode(postCode);
				}else{
					throw new Exception("邮编postCode字段名不存在");
				}
				//地址
				if(!JSONUtils.isNull(jo.get("address"))){
					if(StringUtils.isNotEmpty(jo.getString("address"))){
						String address = jo.getString("address");
						//IX_POI_ADDRESS表
						IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
						ixPoiAddress.setFullname(address);
						ixPoiAddress.setLangCode(langCode);
					}
				}else{
					throw new Exception("地址address字段名不存在");
				}
				
				//父子关系
				
//				//POI等级--POI 等级 LEVEL
//				if(!JSONUtils.isNull(jo.get("level"))){
//					String level = jo.getString("level");
//					ixPoi.setLevel(level);
//				}else{
//					throw new Exception("POI等级level字段名不存在");
//				}
				//内部--内部标识 INDOOR
				int indoorType =jo.getInt("indoorType");
				ixPoi.setIndoor(indoorType);
				//24小时开放--全天营业 OPEN_24H
				int open24H =jo.getInt("open24H");
				if(open24H == 0){
					open24H =2;
				}
				ixPoi.setOpen24h(open24H);
				//品牌chain--连锁品牌 CHAIN
				if(!JSONUtils.isNull(jo.get("chain"))){
					String chain = jo.getString("chain");
					ixPoi.setChain(chain);
				}else{
					throw new Exception("品牌chain字段名不存在");
				}
				//星级
				if(jo.getInt("rating") >-1){
					int rating =jo.getInt("rating");
					//IX_POI_HOTEL表
					IxPoiHotel ixPoiHotel = poi.createIxPoiHotel();
					ixPoiHotel.setRating(rating);
				}
				//备注
				if(!JSONUtils.isNull(jo.get("remark"))){
					String remark = jo.getString("remark");
					ixPoi.setPoiMemo(remark);
				}else{
					throw new Exception("备注remark字段名不存在");
				}
				//网址
				if(!JSONUtils.isNull(jo.get("website"))){
					if(StringUtils.isNotEmpty(jo.getString("website"))){
						String website = jo.getString("website");
						//IX_POI_DETAIL表
						IxPoiDetail ixPoiDetail = poi.createIxPoiDetail();
						ixPoiDetail.setWebSite(website);
					}
				}else{
					throw new Exception("网址website字段名不存在");
				}
				//处理父子关系
				String fatherson = null;
				if(!JSONUtils.isNull(jo.get("fatherson"))){
					fatherson = jo.getString("fatherson");
				}else{
					throw new Exception("父子关系fatherson字段名不存在");
				}
				PoiRelation pr = new PoiRelation();
				pr.setFatherFid(fatherson);
				pr.setPid(poi.objPid());
				pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
				parentPid.add(pr);
				
				//新增poi批level
				usdateLevel(poi);
				
				//多源类型
				String sourceProvider  = null;
				if(!JSONUtils.isNull(jo.get("sourceProvider"))){
					sourceProvider  = jo.getString("sourceProvider");
				}else{
					throw new Exception("多源类型sourceProvider字段名不存在");
				}
				
				/*
				 * 2017.06.30 zl 取消新增多源入ix_poi_flag 需求 1450
				 * //IX_POI_FLAG表
				IxPoiFlag ixPoiFlag = poi.createIxPoiFlag();
				if(sourceProvider.equals("001000020000")){
					ixPoiFlag.setFlagCode("110000240000");
				}else if(sourceProvider.equals("001000030000")||sourceProvider.equals("001000030001")||sourceProvider.equals("001000030002")||sourceProvider.equals("001000030003")){
					ixPoiFlag.setFlagCode("110000280000");
				}else if(sourceProvider.equals("001000030004")){
					ixPoiFlag.setFlagCode("110000270000");
				}*/
				
//				sourceTypes.put(poi.objPid(), sourceProvider );
				//truck
				boolean flg = false;
				for(Map<String,Object> entry:scPointTruckList){
					if(entry.get("kind")!=null&&entry.get("kind").equals(jo.getString("kind"))){
						flg = true;
						if(entry.get("type")!=null&&entry.get("type").equals("1")){
							ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
						}else if(entry.get("type")!=null&&entry.get("chain")!=null&&entry.get("type").equals("3")&&entry.get("chain").equals(entry.get("chain"))){
							ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
						}
						break;
					}
				}
				
				if(!flg){
					for(Map<String,Object> entry:scPointTruckList){
						if(entry.get("chain")!=null&&entry.get("chain").equals(jo.getString("chain"))){
							if(entry.get("kind")!=null&&!entry.get("kind").equals(jo.getString("kind"))){
								if(entry.get("type")!=null&&entry.get("type").equals("2")&&entry.get("chain").equals(entry.get("chain"))){
									ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
								}
							}else if(entry.get("kind")==null){
								if(entry.get("type")!=null&&entry.get("type").equals("2")&&entry.get("chain").equals(entry.get("chain"))){
									ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
								}
							}
						}
						
					}
				}
				
				
				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}

	public boolean importUpdateByJson(IxPoiObj poi,JSONObject jo) throws ImportException, Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//查询的POI主表
				IxPoi ixPoi = (IxPoi) poi.getMainrow();
				//修改履历
				String log = null;
				if(!JSONUtils.isNull(jo.get("log")) && StringUtils.isNotEmpty(jo.getString("log")) 
						&& (jo.getString("log").contains("改名称") || jo.getString("log").contains("改分类") 
								|| jo.getString("log").contains("改电话") || jo.getString("log").contains("改地址") 
								|| jo.getString("log").contains("改邮编") || jo.getString("log").contains("改风味类型") 
								|| jo.getString("log").contains("改父子关系")  || jo.getString("log").contains("改品牌") 
								|| jo.getString("log").contains("改24小时") || jo.getString("log").contains("改星级") 
								|| jo.getString("log").contains("改内部POI") || jo.getString("log").contains("改显示坐标")  
								|| jo.getString("log").contains("改引导坐标") )){
					log = jo.getString("log");
				}else{
					throw new Exception("修改履历log字段名不存在");
				}
				//判断是大陆/港澳
				String langCode = null;
				if(!JSONUtils.isNull(jo.get("regionInfo"))){
					String regionInfo = jo.getString("regionInfo");
					if("D".equals(regionInfo)){
						//大陆
						langCode = "CHI";
					}else if("HM".equals(regionInfo)){
						//港澳
						langCode = "CHT";
					} 
				}else{
					throw new Exception("区域信息regionInfo字段名不存在");
				}
				//改名称
				if(StringUtils.contains(log,"改名称")){
					this.usdateName(poi, jo, langCode);
				}
				//改分类
				if(StringUtils.contains(log,"改分类")){
					if(!JSONUtils.isNull(jo.get("kind"))){
						String kind = jo.getString("kind");
						ixPoi.setKindCode(kind);
					}else{
						throw new Exception("二代分类kind字段名不存在");
					}
				}
				//改电话
				if(StringUtils.contains(log,"改电话")){
					this.usdateContact(poi, jo);
				}
				//改地址
				if(StringUtils.contains(log,"改地址")){
					this.usdateAddress(poi, jo, langCode);
				}
				//改邮编
				if(StringUtils.contains(log,"改邮编")){
					if(!JSONUtils.isNull(jo.get("postCode"))){
						String postCode = jo.getString("postCode");
						ixPoi.setPostCode(postCode);
					}else{
						throw new Exception("邮编postCode字段名不存在");
					}
				}
				//改风味类型
				if(StringUtils.contains(log,"改风味类型")){
					this.usdateFoodType(poi, jo);
				}
				//改品牌
				if(StringUtils.contains(log,"改品牌")){
					if(!JSONUtils.isNull(jo.get("chain"))){
						String chain = jo.getString("chain");
						ixPoi.setChain(chain);
					}else{
						throw new Exception("品牌chain字段名不存在");
					}
				}
				//改24小时
				if(StringUtils.contains(log,"改24小时")){
					int open24H =jo.getInt("open24H");
					if(open24H == 0){
						open24H =2;
					}
					ixPoi.setOpen24h(open24H);
				}
				//改星级
				if(StringUtils.contains(log,"改星级")){
					//查询的IX_POI_HOTEL表
					List<IxPoiHotel> ixPoiHotels = poi.getIxPoiHotels();
					//星级
					if(jo.getInt("rating") >-1){
						int rating =jo.getInt("rating");
						if(ixPoiHotels != null && ixPoiHotels.size()>0){
							ixPoiHotels.get(0).setRating(rating);
						}else{
							//IX_POI_HOTEL表
							IxPoiHotel ixPoiHotel = poi.createIxPoiHotel();
							ixPoiHotel.setRating(rating);
						}
					}
				}
				//改内部POI
				if(StringUtils.contains(log,"改内部POI")){
					int indoorType =jo.getInt("indoorType");
					ixPoi.setIndoor(indoorType);
				}
				//******2017.07.31**********
				//改显示坐标
				if(StringUtils.contains(log,"改显示坐标")){
					double lng =jo.getDouble("lng");//经度
					double lat =jo.getDouble("lat");//纬度
					//显示坐标经纬度--显示坐标
					Geometry point = GeoTranslator.point2Jts(lng, lat);
					
					ixPoi.setGeometry(point);
				}
				//改引导坐标
				if(StringUtils.contains(log,"改引导坐标")){
					double guidelon =jo.getDouble("guidelon");//经度
					double guidelat =jo.getDouble("guidelat");//纬度
					ixPoi.setXGuide(guidelon);
					ixPoi.setYGuide(guidelat);
					ixPoi.setLinkPid(0);
				}
				
				
				//改父子关系
				if(StringUtils.contains(log,"改父子关系")){
					//处理父子关系
					String fatherson = null;
					if(!JSONUtils.isNull(jo.get("fatherson"))){
						fatherson = jo.getString("fatherson");
					}else{
						throw new Exception("父子关系fatherson字段名不存在");
					}
					PoiRelation pr = new PoiRelation();
					pr.setFatherFid(fatherson);
					pr.setPid(poi.objPid());
					pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
					parentPid.add(pr);
				}
				//log包含“改分类”或“改品牌”或“改星级”，如果分类为200200，log包含“改名称”
				if(StringUtils.contains(log,"改分类")||StringUtils.contains(log,"改品牌")||StringUtils.contains(log,"改星级")
						||(StringUtils.contains(log,"改名称")&&ixPoi.getKindCode().equals("200200"))){
					usdateLevel(poi);
				}
//				//多源类型
//				String sourceProvider = null;
//				if(!JSONUtils.isNull(jo.get("sourceProvider"))){
//					sourceProvider = jo.getString("sourceProvider");
//				}else{
//					throw new Exception("多源类型sourceProvider字段名不存在");
//				}
//				sourceTypes.put(poi.objPid(), sourceProvider );
				
				//truck
				if(StringUtils.contains(log,"改分类")||StringUtils.contains(log,"改品牌")){
					String kind = ixPoi.getKindCode();
					String chain =ixPoi.getChain();
					if(StringUtils.contains(log,"改分类")){
						if(!JSONUtils.isNull(jo.get("kind"))){
							kind = jo.getString("kind");
						}
					}
					if(StringUtils.contains(log,"改品牌")){
						if(!JSONUtils.isNull(jo.get("chain"))){
							chain = jo.getString("chain");
						}
					}
					
					boolean flg = false;
					for(Map<String,Object> entry:scPointTruckList){
						if(entry.get("kind")!=null&&entry.get("kind").equals(jo.getString("kind"))){
							flg = true;
							if(entry.get("type")!=null&&entry.get("type").equals("1")){
								ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
							}else if(entry.get("type")!=null&&entry.get("type").equals("3")&&entry.get("chain")!=null&&entry.get("chain").equals(entry.get("chain"))){
								ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
							}
							break;
						}
					}
					
					if(!flg){
						for(Map<String,Object> entry:scPointTruckList){
							if(entry.get("chain")!=null&&entry.get("chain").equals(jo.getString("chain"))){
								if(entry.get("kind")!=null&&entry.get("chain")!=null&&!entry.get("kind").equals(jo.getString("kind"))){
									if(entry.get("type")!=null&&entry.get("type").equals("2")&&entry.get("chain")!=null&&entry.get("chain").equals(entry.get("chain"))){
										ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
									}
								}else if(entry.get("kind")==null){
									if(entry.get("type")!=null&&entry.get("type").equals("2")&&entry.get("chain").equals(entry.get("chain"))){
										ixPoi.setTruckFlag(Integer.parseInt(entry.get("truck").toString()));
									}
								}
							}
							
						}
					}

				}
				
				
				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}
	
	/**
	 * 改地址
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @param langCode
	 * @throws Exception 
	 */
	public void usdateAddress(IxPoiObj poi,JSONObject jo,String langCode) throws Exception{
		//查询的IX_POI_ADDRESS表
		List<IxPoiAddress> ixPoiAddresses = poi.getIxPoiAddresses();
		if(!JSONUtils.isNull(jo.get("address"))){
			if(StringUtils.isNotEmpty(jo.getString("address"))){
				String address = jo.getString("address");
				boolean flag = true;
				for (IxPoiAddress ixPoiAddress : ixPoiAddresses) {
					//多源address不为空，赋值给IX_POI_ADDRESS.FULLNAME(中文地址)
					if("CHI".equals(ixPoiAddress.getLangCode())){
						ixPoiAddress.setFullname(address);
						flag = false;
					}else if("CHT".equals(ixPoiAddress.getLangCode())){
						ixPoiAddress.setFullname(address);
						flag = false;
					}
				}
				if(flag){
					//不存在IX_POI_ADDRESS记录，则增加一条记录
					//IX_POI_ADDRESS表
					IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
					ixPoiAddress.setFullname(address);
					ixPoiAddress.setLangCode(langCode);
				}
			}else{
				//逻辑删除日库中所有地址记录
				for (IxPoiAddress ixPoiAddress : ixPoiAddresses) {
					poi.deleteSubrow(ixPoiAddress);
				}
			}
		}else{
			throw new Exception("地址address字段名不存在");
		}
	}
	
	/**
	 * 改电话
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @throws Exception
	 */
	public void usdateContact(IxPoiObj poi,JSONObject jo) throws Exception{
		//[集合]联系方式
		String contacts = null;
		if(!JSONUtils.isNull(jo.get("contacts"))){
			contacts = jo.getString("contacts");
		}else{
			throw new Exception("联系方式contacts字段名不存在");
		}
		//查询IX_POI_CONTACT表
		List<IxPoiContact> ixPoiContacts = poi.getIxPoiContacts();
		if(!"[]".equals(contacts)){
			JSONArray ja = JSONArray.fromObject(contacts);
			if(ixPoiContacts != null && ixPoiContacts.size() > 0){//原数据库中存在电话,多源上传文件中也存在
				//获取多源上传所有 电话
				List<String> jaList = new ArrayList<String>();
				for (int i=0;i<ja.size();i++) {
					JSONObject jso = ja.getJSONObject(i);
					//号码number
					if(!JSONUtils.isNull(jso.get("number"))){
						String number = jso.getString("number");
						jaList.add(number);
					}else{
						throw new Exception("号码number字段名不存在");
					}
				}
				//保存查询IX_POI_CONTACT表
				List<IxPoiContact> contactList = new ArrayList<IxPoiContact>();
				//库中电话的集合
				List<String> oldConList = new ArrayList<String>();
				//初始化电话优先级
				int priority=1 ;
				
				//多源中不存在，但是日库中存在的电话逻辑删除
				for (IxPoiContact contact : ixPoiContacts) {
					System.out.println("contact : "+contact.getContact());
					if(jaList.contains(contact.getContact())){//如果多源上传包含数据库中数据
						contactList.add(contact);
						oldConList.add(contact.getContact());
						//比较库中数据的电话优先级和初始化定义
						if(contact.getPriority() > priority){
							priority = contact.getPriority();
						}
					}else{//如果不包含,则删除数据库中数据
						poi.deleteSubrow(contact);
					}
//					contactList.add(contact);
				}
				for (int i=0;i<ja.size();i++) {
					JSONObject jso = ja.getJSONObject(i);
					//号码number
					String number = null;
					if(!JSONUtils.isNull(jso.get("number"))){
						number = jso.getString("number");
					}else{
						throw new Exception("号码number字段名不存在");
					}
					//联系方式类型type
					int type = jso.getInt("type");
					if(contactList != null  && contactList.size()>0){
						if(oldConList.contains(number)){
							for (IxPoiContact contact : contactList) {
								if(number.equals(contact.getContact())
										&& type!=contact.getContactType()){
									//不一致，则更新电话和电话类型追加到日库中
									//IX_POI_CONTACT表
									IxPoiContact ixPoiContact = poi.createIxPoiContact();
									ixPoiContact.setContact(number);
									ixPoiContact.setContactType(type);
									//zl 2017.11.08 新增电话优先级赋值
									System.out.println("2新增电话优先级: "+(priority+1));
									ixPoiContact.setPriority(priority+1);
									priority= priority+1;
								}
							}
						}else{//多源上传的 电话库中没有,至接新增
							//IX_POI_CONTACT表
							IxPoiContact ixPoiContact = poi.createIxPoiContact();
							ixPoiContact.setContact(number);
							ixPoiContact.setContactType(type);
							//zl 2017.11.08 新增电话优先级赋值
							System.out.println("3新增电话优先级: "+(priority+1));
							ixPoiContact.setPriority(priority+1);
							priority= priority+1;
						}
						
					}else{//数据库中无可更新数据,至接将多源上传的电话新增进数据库
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
						//zl 2017.11.08 新增电话优先级赋值
						System.out.println("3新增电话优先级: "+(i+1));
						ixPoiContact.setPriority(i+1);
						priority= i+1;
					}
					
				}
			}else{//原数据库中不存在电话,多源上传文件中存在电话
				//直接新增
				for (int i=0;i<ja.size();i++) {
					JSONObject jso = ja.getJSONObject(i);
					//号码number
					String number = null;
					if(!JSONUtils.isNull(jso.get("number"))){
						number = jso.getString("number");
					}else{
						throw new Exception("号码number字段名不存在");
					}
					//联系方式类型type
					int type = jso.getInt("type");
					//不一致，则更新电话和电话类型追加到日库中
					//IX_POI_CONTACT表
					IxPoiContact ixPoiContact = poi.createIxPoiContact();
					ixPoiContact.setContact(number);
					ixPoiContact.setContactType(type);
					//zl 2017.11.08 新增电话优先级赋值
					System.out.println("新增电话优先级: "+(i+1));
					ixPoiContact.setPriority(i+1);
					
				}
			}

		}else if("[]".equals(contacts)){
			//逻辑删除日库中所有电话记录
			for (IxPoiContact ixPoiContact : ixPoiContacts) {
				poi.deleteSubrow(ixPoiContact);
			}
		}
	}
	
	/**
	 * 改名称
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @param langCode
	 * @throws Exception
	 */
	public void usdateName(IxPoiObj poi,JSONObject jo,String langCode) throws Exception{
		//名称
		String name = null;
		if(!JSONUtils.isNull(jo.get("name"))){
			name = jo.getString("name");
		}else{
			throw new Exception("名称name字段名不存在");
		}
		//IX_POI_NAME表
		List<IxPoiName> ixPoiNames = poi.getIxPoiNames();
		boolean flag = true;
		if(ixPoiNames != null && ixPoiNames.size()>0){
			for (IxPoiName ixPoiName : ixPoiNames) {
				if(ixPoiName.getNameClass()==1 &&
						ixPoiName.getNameType()==2 &&
						"CHI".equals(ixPoiName.getLangCode())){
					ixPoiName.setName(name);
					flag = false;
				}else if(ixPoiName.getNameClass()==1 &&
						ixPoiName.getNameType()==2 &&
						"CHT".equals(ixPoiName.getLangCode())){
					ixPoiName.setName(name);
					flag = false;
				}
			}
			if(flag){
				
				//IX_POI_NAME表
				IxPoiName ixPoiName = poi.createIxPoiName();
				ixPoiName.setName(name);
				ixPoiName.setNameClass(1);
				ixPoiName.setNameType(2);
				ixPoiName.setLangCode(langCode);
			}
			
		}
	}
	
	/**
	 * 该风味类型
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @throws Exception
	 */
	public void usdateFoodType(IxPoiObj poi,JSONObject jo) throws Exception{
		//查询的IX_POI_RESTAURANT表
		List<IxPoiRestaurant> ixPoiRestaurants = poi.getIxPoiRestaurants();
		//[集合]风味类型
		String foodType = null;
		if(!JSONUtils.isNull(jo.get("foodType"))){
			foodType = jo.getString("foodType");
		}else{
			throw new Exception("风味类型foodType字段名不存在");
		}
		if(StringUtils.isNotEmpty(foodType)){
			if(ixPoiRestaurants != null && ixPoiRestaurants.size()>0){
				ixPoiRestaurants.get(0).setFoodType(foodType);
			}else{
				//IX_POI_RESTAURANT表
				IxPoiRestaurant ixPoiRestaurant = poi.createIxPoiRestaurant();
				ixPoiRestaurant.setFoodType(foodType);
			}
		}else{
			//逻辑删除日库中所有风味类型记录
			IxPoiRestaurant ixPoiRestaurant = ixPoiRestaurants.get(0);
			poi.deleteSubrow(ixPoiRestaurant);
		}
	}
	
	/**
	 * 改level
	 * @author zhangxiaoyi
	 * @param poi
	 * @param jo
	 * @param langCode
	 * @throws Exception
	 */
	public void usdateLevel(IxPoiObj poi) throws Exception{		
		JSONObject jsonObj=new JSONObject();
		jsonObj.put("dbId", this.dbId);
		jsonObj.put("pid",Integer.valueOf(String.valueOf(poi.objPid())));
		IxPoi poiMain=(IxPoi) poi.getMainrow();
		jsonObj.put("poi_num",poiMain.getPoiNum());
		jsonObj.put("kindCode",poiMain.getKindCode());
		String chain = poiMain.getChain();
		jsonObj.put("chainCode","");
		if(StringUtils.isNotEmpty(chain)){jsonObj.put("chainCode",chain);}
		IxPoiName nameObj = poi.getOfficeOriginCHName();
		String name="";
		if(nameObj!=null){name=nameObj.getName();}
		jsonObj.put("name",name);
		String level = poiMain.getLevel();
		jsonObj.put("level","");
		if(StringUtils.isNotEmpty(level)){jsonObj.put("level",level);}
		List<IxPoiHotel> hotels = poi.getIxPoiHotels();
		jsonObj.put("rating",0);
		if(hotels!=null&&hotels.size()>0){
			jsonObj.put("rating",hotels.get(0).getRating());
		}
		MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
		String levelStr=metadataApi.getLevelForMulti(jsonObj);
		if(StringUtils.isNotEmpty(levelStr)){
			poiMain.setLevel(levelStr);
		}
	}

	
	public boolean importDeleteByJson(IxPoiObj poi,JSONObject jo)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//判断是否已逻辑删除
				if(poi.isDeleted()){
					//已逻辑删除
					throw new Exception("该数据已经逻辑删除");
				}else{
					//该对象逻辑删除
					poi.deleteObj();
					
					//处理父子关系
					PoiRelation pr = new PoiRelation();
					pr.setPid(poi.objPid());
					pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
					parentPid.add(pr);
					
					//处理同一关系
					PoiRelation pr2 = new PoiRelation();
					pr2.setPid(poi.objPid());
					pr2.setPoiRelationType(PoiRelationType.SAME_POI);
					samePoiPid.add(pr);
					//多源类型
//					String sourceProvider = null;
//					if(!JSONUtils.isNull(jo.get("sourceProvider"))){
//						sourceProvider = jo.getString("sourceProvider");
//					}else{
//						throw new Exception("多源类型sourceProvider字段名不存在");
//					}
//					sourceTypes.put(poi.objPid(), sourceProvider);
					
				}
				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
		
	}
	
	/**
	 * 获取查询所需子表
	 * @author Han Shaoming
	 * @return
	 */
	public Set<String> getTabNames(){
		//添加所需的子表
		Set<String> tabNames = new HashSet<>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_CONTACT");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_RESTAURANT");
		tabNames.add("IX_POI_CHILDREN");
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_DETAIL");
		return tabNames;
	}
	
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MultiSrcPoiDayImportor";
	}
	
	private void filterAddedPoi(Map<String, JSONObject> addPois)throws Exception{
		Map<String,Long> exists = IxPoiSelector.getPidByFids(conn,addPois.keySet());
		if(exists==null)return;
		for(String fid:exists.keySet()){
			errLog.put(fid, "fid库中已存在");
			addPois.remove(fid);
		}
	}
	
	private void filterUpdatePoi(Map<String,JSONObject> pois)throws Exception{
		//判断数据是否有变更,一批数据的时间可能不同
		//先写入临时表
		PreparedStatement pstm = null;
		try{
			new QueryRunner().execute(conn, "DELETE FROM SVR_MULTISRC_DAY_IMP");
			Iterator<Map.Entry<String, JSONObject>> it = pois.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, JSONObject> entry = it.next();
				try{
					String uTime = entry.getValue().getString("updateTime");
					if(StringUtils.isEmpty(uTime)){
						errLog.put(entry.getKey(), "updateTime为空");
						it.remove();;
						continue;
					}
					if(pstm==null){
						pstm=conn.prepareStatement("INSERT INTO SVR_MULTISRC_DAY_IMP VALUES (?,TO_DATE(?,'yyyymmddhh24miss'),SYSDATE)");
					}
					pstm.setString(1,entry.getKey());
					pstm.setString(2, uTime);
					pstm.addBatch();
				}catch(Exception e){
					log.error(e.getMessage(),e);
					errLog.put(entry.getKey(), "updateTime字段错误");
					it.remove();
				}
			}
			pstm.executeBatch();
		}finally{
			DbUtils.closeQuietly(pstm);
		}
		//获取有变更的fid
		Collection<String> updateFids = new LogReader(conn).getUpdatedPoiByTemp("SVR_MULTISRC_DAY_IMP");
		if(updateFids!=null){
			for(String f:updateFids){
				errLog.put(f, "updateTime字段错误");
				pois.remove(f);
			}
		}
	}
}
