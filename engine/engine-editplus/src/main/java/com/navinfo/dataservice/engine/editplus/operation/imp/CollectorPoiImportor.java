package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

import com.ctc.wstx.util.DataUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

/**
 * 
 * @ClassName: CollectorPoiImportor
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: CollectorPoiImportor.java
 */
public class CollectorPoiImportor extends AbstractOperation {

	// 获取当前做业季
	String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
	
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();
	protected List<PoiRelation> addPcRelations = new ArrayList<PoiRelation>();
	protected List<PoiRelation> deletePcRelations = new ArrayList<PoiRelation>();

	public CollectorPoiImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}

	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}
	
	public List<PoiRelation> getAddPcRelations() {
		return addPcRelations;
	}

	public List<PoiRelation> getDeletePcRelations() {
		return deletePcRelations;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		CollectorUploadPois uploadPois = ((CollectorPoiImportorCommand)cmd).getPois();
		//处理修改的数据
		Map<String,JSONObject> updatePois = uploadPois.getUpdatePois();
		if(updatePois!=null&&updatePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,getTabNames(),updatePois.keySet(),true,true);
			
			for(Entry<String, JSONObject> entry:updatePois.entrySet()){
				IxPoiObj poiObj = null;
				if(objs!=null&&objs.keySet().contains(entry.getKey())){
					//处理未修改
					log.info("fid:"+entry.getKey()+"在库中存在，作为修改处理");
					poiObj = (IxPoiObj)objs.get(entry.getKey());
				}else{
					//库中未找到数据，处理为新增
					log.info("fid:"+entry.getKey()+"在库中未找到，作为新增处理");
					poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
				}
				setPoiAttr(poiObj,entry.getValue());
				result.putObj(poiObj);
				//关系处理
				//...
			}
			
			if(objs!=null&&objs.size()>0){
				
			}else{
				log.info("");
			}
		}else{
			log.info("无修改的poi数据需要导入");
		}
		//处理删除的数据
		Map<String,JSONObject> deletePois = uploadPois.getDeletePois();
		if(deletePois!=null&&deletePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,getTabNames(),deletePois.keySet(),true,true);
			if(objs!=null&&objs.size()>0){
				Set<String> keys = objs.keySet();
				Collection<BasicObj> values = objs.values();
				for(BasicObj obj:values){
					//删除
					obj.deleteObj();
					result.putObj(obj);
					//关系数据处理
					//...
				}
				for(String fid:deletePois.keySet()){
					if(!keys.contains(fid)){
						log.info("删除的poi在库中未找到。fid:"+fid);
						//
						errLogs.add(new ErrorLog(fid,"删除的poi在库中未找到"));
					}
				}
			}else{
				log.info("删除的poi在库中均没找到。pids:"+StringUtils.join(deletePois.keySet(),","));
				//err log
				for(String fid:deletePois.keySet()){
					errLogs.add(new ErrorLog(fid,"删除的poi在库中未找到"));
				}
			}
		}else{
			log.info("无删除的poi数据需要导入");
		}
	}
	
	public void setPoiAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		//to-do
		//table IX_POI
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		//kindCode,默认空字符串
		ixPoi.setKindCode(jo.getString("kindCode"));
		//geometry
		Geometry geometry = JtsGeometryFactory.read(jo.getString("geometry"));
		ixPoi.setGeometry(geometry);
		//guide
		if(JSONUtils.isNull(jo.get("guide"))){
			ixPoi.setXGuide(0);
			ixPoi.setYGuide(0);
			ixPoi.setLinkPid(0);
		}else {
			JSONObject guide = jo.getJSONObject("guide");
			double newXGuide = guide.getDouble("longitude");
			ixPoi.setXGuide(DoubleUtil.keepSpecDecimal(newXGuide));
			double newYGuide = guide.getDouble("latitude");
			ixPoi.setYGuide(DoubleUtil.keepSpecDecimal(newYGuide));
			long newLinkPid = guide.getLong("linkPid");
			ixPoi.setLinkPid(newLinkPid);
		}
		//chain,默认空字符串
		ixPoi.setChain(jo.getString("chain"));
		//open24h,int型
		ixPoi.setOpen24h(jo.getInt("open24H"));
		//rawFields,默认空字符串
		ixPoi.setRawFields(jo.getString("rawFields"));
		// meshid非0时原值转出；为0时根据几何计算；
		int meshId = jo.getInt("meshid");
		if (meshId == 0) {
			String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
			meshId = Integer.parseInt(meshIds[0]);
		}
		ixPoi.setMeshId(meshId);
		//postCode,默认空字符串
		ixPoi.setPostCode(jo.getString("postCode"));
		//name
		ixPoi.setOldName(jo.getString("name"));
		//address
		ixPoi.setOldAddress(jo.getString("address"));
		//fid
		ixPoi.setPoiNum(jo.getString("fid"));
		//season version
		ixPoi.setDataVersion(version);
		//t_operateDate
		ixPoi.setCollectTime(jo.getString("t_operateDate"));
		//level
		ixPoi.setLevel(jo.getString("level"));
		//sportsVenues
		ixPoi.setSportsVenue(jo.getString("sportsVenues"));
		//indoor
		if(JSONUtils.isNull(jo.get("indoor"))){
			ixPoi.setIndoor(0);
		}else{
			JSONObject indoor = jo.getJSONObject("indoor");
			if(indoor.getInt("type")==3){
				ixPoi.setIndoor(1);
			}else{
				ixPoi.setIndoor(0);
			}
		}
		//vipFlag
		ixPoi.setVipFlag(jo.getString("vipFlag"));
		//truck
		ixPoi.setTruckFlag(jo.getInt("truck"));
		//parentFid
		/**
		 * 父子关系采集端双向维护，两个POI同时上传（两个POI同时在库中存在），那么只需维护relateChildren即可
		 * 但有特殊情况，当父子不同时存在（没有同时上传，或者其中有一个入库失败）
		 * 那么父存在，子不存在，然后当子后续上传时，不处理parentFid会丢失父子关系，
		 * 当子存在，父不存在，然后当父上传时，不处理parentFid，父子关系同时是维护正确
		 * 所以只需处理poi的parentFid有值的情况即可
		 */
		PoiRelation pr = new PoiRelation(PoiRelationType.FATHER_AND_SON);
		String parentFid = jo.getString("parentFid");
		if(StringUtils.isEmpty(parentFid)){
			pr.setFatherFid(parentFid);
			pr.setPid(poiObj.objPid());
			addPcRelations.add(pr);
		}
		
		/*** 子表  ***/
		//hotel
		setHotelAttr(poiObj,jo);
		//name
		if(ixPoi.isChanged(IxPoi.OLD_NAME)){
			setNameAttr(poiObj);
		}
		//address
		if(ixPoi.isChanged(IxPoi.OLD_NAME)){
			setAddressAttr(poiObj);
		}
		//photo
		setPhotoAttr(poiObj,jo);
		//contacts
		setContactAttr(poiObj,jo);
		//relateChildren
		setChildrenAttr(poiObj,jo);
		//gasStation
		setGasStationAttr(poiObj,jo);
		//parkings
		//foodtypes
		//chargingStation
		//chargingPole
		
		//处理日志类字段
		//fieldState
		if(ixPoi.isChanged(IxPoi.KIND_CODE)){
			if(StringUtils.isEmpty(ixPoi.getFieldState())){
				ixPoi.setFieldState("改种别代码");
			}else{
				if(ixPoi.getFieldState().indexOf("改种别代码")==-1){
					ixPoi.setFieldState(ixPoi.getFieldState()+"|改种别代码");
				}
			}
		}
		if(ixPoi.isChanged(IxPoi.CHAIN)){
			if(StringUtils.isEmpty(ixPoi.getFieldState())){
				ixPoi.setFieldState("改连锁品牌");
			}else{
				if(ixPoi.getFieldState().indexOf("改连锁品牌")==-1){
					ixPoi.setFieldState(ixPoi.getFieldState()+"|改连锁品牌");
				}
			}
		}
		//hotel.rating
		//...
		//outDoorLog
		
	}
	private void setHotelAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		//获取原始
		if(JSONUtils.isNull(jo.get("hotel"))){
			JSONObject hotel = jo.getJSONObject("hotel");
			//
			boolean exists = false;
			
			List<IxPoiHotel> ixPoihotels = poiObj.getIxPoiHotels();
			if(ixPoihotels!=null){
				
			}
			
		}else{//上传中没有hotel信息，删除原有的
			poiObj.deleteSubrow("IX_POI_HOTEL");
		}
	}
	private void setNameAttr(IxPoiObj poiObj)throws Exception{
		
	}
	private void setAddressAttr(IxPoiObj poiObj)throws Exception{
		
	}
	/**
	 * 判断不存在就增加，只增不删
	 * @param poiObj
	 * @param jo
	 * @throws Exception
	 */
	private void setPhotoAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		JSONArray photos = jo.getJSONArray("attachments");
		List<IxPoiPhoto> objPhotos = poiObj.getIxPoiPhotos();
		Collection<String> objPhotoPIds = null;
		if(objPhotos!=null){
			objPhotoPIds = new HashSet<String>();
			for(IxPoiPhoto ipp:objPhotos){
				objPhotoPIds.add(ipp.getPid());
			}
		}
		String memo = null;
		for(Object photo:photos){
			JSONObject pJo = (JSONObject)photo;
			int type = pJo.getInt("type");
			String fccpid = pJo.getString("id");
			if(type==1&&objPhotoPIds!=null&&(!objPhotoPIds.contains(fccpid))){
				IxPoiPhoto ixPoiPhoto = poiObj.createIxPoiPhoto();//poi_pid,row_id已经赋值
				ixPoiPhoto.setPid(fccpid);
				ixPoiPhoto.setTag(pJo.getInt("tag"));
			}else if(type==3){
				memo = pJo.getString("content");
			}
		}
		if(StringUtils.isNotEmpty(memo)){
			IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
			ixPoi.setPoiMemo(memo);
		}
	}

	private void setContactAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		
	}
	private void setChildrenAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		
	}

	private void setGasStationAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		
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
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_GASSTATION");
		tabNames.add("IX_POI_PARKING");
		tabNames.add("IX_POI_HOTEL");
		tabNames.add("IX_POI_CHARGINGSTATION");
		tabNames.add("IX_POI_CHARGINGPLOT");
		return tabNames;
	}
	
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CollectorUpload";
	}
	
	public static void main(String[] args) {

//		JSONObject obj = JSONObject.fromObject("{\"key1\":\"\",\"key2\":null,\"key3\":[]}");
//		System.out.println(JSONUtils.isNull(obj.get("key1")));
//		System.out.println(JSONUtils.isNull(obj.get("key2")));
//		System.out.println(obj.has("key2"));
//		System.out.println(obj.getJSONObject("key2").isEmpty());
//		System.out.println(obj.get("key3"));
//		System.out.println(obj.get("key3").getClass());
//		System.out.println(JSONUtils.isNull(obj.get("key4")));
		
		List<String> list1 = new ArrayList<String>();
		for(int i=0;i<10;i++){
			list1.add(String.valueOf(i));
		}
		List<String> list2 = list1;
		for(String s:list2){
			if(s.equals("7")){
				list1.remove(s);
			}
		}
		for(Iterator<String> it = list2.iterator();it.hasNext();){
			String s = it.next();
			if(s.equals("7")){
				list1.remove(s);
			}
		}
	}
	
}
