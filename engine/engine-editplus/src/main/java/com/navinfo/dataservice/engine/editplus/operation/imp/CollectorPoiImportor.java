package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
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
	String langCode = "CHI";//FIXME:为什么这里是CHI。CHT呢？默认返回简体中文; 港澳的后续在增加逻辑吧。
	Map<String,String> attrTableMap;
	Set<String> tabNames;
	
	protected int successNum = 0;
	protected List<ErrorLog> errLogs = new ArrayList<ErrorLog>();
	protected CollectorUploadPoiSpRelation sps = new CollectorUploadPoiSpRelation();
	protected CollectorUploadPoiPcRelation pcs = new CollectorUploadPoiPcRelation();
	protected Set<Long> freshVerPois = new HashSet<Long>();
	
	//****zl 2017.06.06 ***
	protected Map<Long,String> allPois = new HashMap<Long,String>();
	public Map<Long, String> getAllPois() {
		return allPois;
	}


	protected Set<Long> noChangedPois = new HashSet<Long>();
	//父子关系暂时不处理

	public CollectorPoiImportor(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}
	
	public void setLangCode(String langCode){
		this.langCode=langCode;
	}

	public int getSuccessNum(){
		return this.successNum;
	}
	
	public List<ErrorLog> getErrLogs() {
		return errLogs;
	}

	public CollectorUploadPoiSpRelation getSps() {
		return sps;
	}
	

	public CollectorUploadPoiPcRelation getPcs() {
		return pcs;
	}
	
	public Set<Long> getFreshVerPois(){
		return freshVerPois;
	}
	
	public Set<Long> getNoChangedPois(){
		return noChangedPois;
	}
	
	/**
	 * 初始化所需子表、属性和子表名的映射关系
	 */
	public void init(){
		//添加所需的子表
		tabNames = new HashSet<String>();
		tabNames.add(IxPoiObj.IX_POI_NAME);
		tabNames.add(IxPoiObj.IX_POI_CONTACT);
		tabNames.add(IxPoiObj.IX_POI_ADDRESS);
		tabNames.add(IxPoiObj.IX_POI_RESTAURANT);
		tabNames.add(IxPoiObj.IX_POI_CHILDREN);
		tabNames.add(IxPoiObj.IX_POI_PARENT);
		tabNames.add(IxPoiObj.IX_POI_DETAIL);
		tabNames.add(IxPoiObj.IX_POI_PHOTO);
		tabNames.add(IxPoiObj.IX_POI_GASSTATION);
		tabNames.add(IxPoiObj.IX_POI_PARKING);
		tabNames.add(IxPoiObj.IX_POI_HOTEL);
		tabNames.add(IxPoiObj.IX_POI_CHARGINGSTATION);
		tabNames.add(IxPoiObj.IX_POI_CHARGINGPLOT);
		//属性和表名映射
		//hotel|gasStation|parkings|foodtypes|chargingStation
		attrTableMap = new HashMap<String,String>();
		attrTableMap.put("hotel", IxPoiObj.IX_POI_HOTEL);
		attrTableMap.put("gasStation", IxPoiObj.IX_POI_GASSTATION);
		attrTableMap.put("parkings", IxPoiObj.IX_POI_PARKING);
		attrTableMap.put("foodtypes", IxPoiObj.IX_POI_RESTAURANT);
		attrTableMap.put("chargingStation", IxPoiObj.IX_POI_CHARGINGSTATION);
		attrTableMap.put("contacts", IxPoiObj.IX_POI_CONTACT);
		attrTableMap.put("chargingPole", IxPoiObj.IX_POI_CHARGINGPLOT);
		attrTableMap.put("relateChildren", IxPoiObj.IX_POI_CHILDREN);
		//...
	}

	@Override
	public String getName() {
		return "CollectorPoiImportor";
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		CollectorUploadPois uploadPois = ((CollectorPoiImportorCommand)cmd).getPois();
		init();
		//处理修改的数据
		Map<String,JSONObject> updatePois = uploadPois.getUpdatePois();
		if(updatePois!=null&&updatePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,updatePois.keySet(),true,true);
			
			for(Entry<String, JSONObject> entry:updatePois.entrySet()){
				String fid = entry.getKey();
				try{
					IxPoiObj poiObj = null;
					if(objs!=null&&objs.keySet().contains(fid)){
						//处理未修改
						poiObj = (IxPoiObj)objs.get(fid);
						if(poiObj.opType().equals(OperationType.PRE_DELETED)){
							log.info("fid:"+fid+"在库中已删除");
							errLogs.add(new ErrorLog(fid,3,"poi在库中已删除"));
							continue;
						}else{
							log.info("fid:"+fid+"在库中存在，作为修改处理");
						}
					}else{
						//库中未找到数据，处理为新增
						log.info("fid:"+fid+"在库中未找到，作为新增处理");
						poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
					}
					setPoiAttr(poiObj,entry.getValue());
					//计算鲜度验证
					if(StringUtils.isEmpty(entry.getValue().getString("rawFields")) 
							&& poiObj.isFreshFlag()){//鲜度验证
						freshVerPois.add(poiObj.objPid());
//							if((!poiObj.isSubrowChanged(IxPoiObj.IX_POI_PHOTO))&&(!poiObj.getMainrow().isChanged(IxPoi.POI_MEMO))){
//								noChangedPois.add(poiObj.objPid());
//							}
					}
					//所有的poi
					allPois.put(poiObj.objPid(), entry.getValue().getString("rawFields"));
						
					
					
					result.putObj(poiObj);
					successNum++;
					//同一关系处理
					//sameFid
					String sFid = entry.getValue().getString("sameFid");
					if(StringUtils.isEmpty(sFid)){
						sps.deletePoiSp(entry.getValue().getString("fid"));
					}else{
						sps.addUpdatePoiSp(entry.getValue().getString("fid"),sFid);
					}
				}catch(Exception e){
					errLogs.add(new ErrorLog(fid,0,"未分类错误："+e.getMessage()));
					log.warn("fid（"+fid+"）入库发生错误："+e.getMessage());
					log.warn(e.getMessage(),e);
				}
			}
		}else{
			log.info("无修改的poi数据需要导入");
		}
		//处理删除的数据
		Map<String,JSONObject> deletePois = uploadPois.getDeletePois();
		if(deletePois!=null&&deletePois.size()>0){
			//根据fid查询poi
			//key:fid
			Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,deletePois.keySet(),true,true);
			if(objs!=null&&objs.size()>0){
				Set<String> keys = objs.keySet();
				Collection<BasicObj> values = objs.values();
				for(BasicObj obj:values){
					//删除
					obj.deleteObj();
					result.putObj(obj);
					allPois.put(obj.objPid(), deletePois.get(((IxPoi)obj.getMainrow()).getPoiNum()).getString("rawFields"));
					successNum++;
					//关系数据处理
					//同一关系处理
					//sameFid
					sps.deletePoiSp(((IxPoi)obj.getMainrow()).getPoiNum());
				}
				for(String fid:deletePois.keySet()){
					if(!keys.contains(fid)){
						log.info("删除的poi在库中未找到。fid:"+fid);
						//
						errLogs.add(new ErrorLog(fid,0,"删除的poi在库中未找到"));
					}
				}
			}else{
				log.info("删除的poi在库中均没找到。pids:"+StringUtils.join(deletePois.keySet(),","));
				//err log
				for(String fid:deletePois.keySet()){
					errLogs.add(new ErrorLog(fid,0,"删除的poi在库中未找到"));
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
		String name = jo.getString("name");
		ixPoi.setOldName(name);
		if(ixPoi.isChanged(IxPoi.OLD_NAME)){
			setNameAndAttr(poiObj,name);
		}
		//address
		//address
		String addr = jo.getString("address");
		ixPoi.setOldAddress(addr);
		if(ixPoi.isChanged(IxPoi.OLD_ADDRESS)){
			setAddressAndAttr(poiObj,addr);
		}
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
		 * ???只有当父子关系的poi分开不同时上传，才可能需要考虑parentFid入库
		 * 本次暂时不考虑
		 * 父子关系采集端双向维护，两个POI同时上传（两个POI同时在库中存在），那么只需维护relateChildren即可
		 * 但有特殊情况，当父子不同时存在（没有同时上传，或者其中有一个入库失败）
		 * 那么父存在，子不存在，然后当子后续上传时，不处理parentFid会丢失父子关系，
		 * 当子存在，父不存在，然后当父上传时，不处理parentFid，父子关系同时是维护正确
		 * 所以只需处理poi的parentFid有值的情况即可
		 */
		
		//relateChildren
		setChildrenAndAttr(poiObj,jo);
		
		/*** 子表  ***/
		//hotel
		setSubrow(poiObj,jo,"hotel");
		//photo
		setPhotoAndAttr(poiObj,jo);
		//contacts
		setSubrows(poiObj,jo,"contacts");
		//gasStation
		setSubrow(poiObj,jo,"gasStation");
		//parkings
		setSubrow(poiObj,jo,"parkings");
		//foodtypes
		setSubrow(poiObj,jo,"foodtypes");
		//chargingStation
		setSubrow(poiObj,jo,"chargingStation");
		//chargingPole
		setSubrows(poiObj,jo,"chargingPole");
		
		//处理日志类字段
		//fieldState
//		if(ixPoi.isChanged(IxPoi.KIND_CODE)){
//			if(StringUtils.isEmpty(ixPoi.getFieldState())){
//				ixPoi.setFieldState("改种别代码");
//			}else{
//				if(ixPoi.getFieldState().indexOf("改种别代码")==-1){
//					ixPoi.setFieldState(ixPoi.getFieldState()+"|改种别代码");
//				}
//			}
//		}
//		if(ixPoi.isChanged(IxPoi.CHAIN)){
//			if(StringUtils.isEmpty(ixPoi.getFieldState())){
//				ixPoi.setFieldState("改连锁品牌");
//			}else{
//				if(ixPoi.getFieldState().indexOf("改连锁品牌")==-1){
//					ixPoi.setFieldState(ixPoi.getFieldState()+"|改连锁品牌");
//				}
//			}
//		}
		//hotel.rating
		//...
		//outDoorLog
	}
	
	/**
	 * 属性是唯一的对象，并且根据rowId差分
	 * hotel|gasStation|parkings|foodtypes|chargingStation
	 * @param poiObj
	 * @param jo
	 * @param keyName：
	 * @throws Exception
	 */
	private void setSubrow(IxPoiObj poiObj,JSONObject jo,String keyName)throws Exception{
		String tableName = attrTableMap.get(keyName);
		//获取原始
		if(JSONUtils.isNull(jo.get(keyName))){//上传中没有子表信息，删除所有原有的记录
			poiObj.deleteSubrows(tableName);
		}else{
			JSONObject subJo = jo.getJSONObject(keyName);
			String rowid = subJo.getString("rowId");
			rowid = rowid.toUpperCase();
			//
			boolean exists = false;
			
			List<BasicRow> rows = poiObj.getRowsByName(tableName);
			if(rows!=null){
				for(BasicRow r:rows){//遍历删除非上传的记录
					if(r.getRowId().equals(rowid)){
						exists = true;
						setSubrowAttr(r,subJo,keyName);
					}else{
						poiObj.deleteSubrow(r);
					}
				}
			}
			//在库中不存在，则新增
			if(!exists){
				BasicRow nr = poiObj.createSubRowByTableName(tableName);
				nr.setRowId(rowid);
				setSubrowAttr(nr,subJo,keyName);
			}
		}
		
	}

	/**
	 * 属性是对象的数组，并且数组内的对象根据rowId差分
	 * contacts|chargingPole
	 * @param poiObj
	 * @param jo：raw poi json
	 * @param keyName：contacts|chargingPole
	 * @throws Exception
	 */
	private void setSubrows(IxPoiObj poiObj,JSONObject jo,String keyName)throws Exception{
		String tableName = attrTableMap.get(keyName);
		//获取原始
		if(JSONUtils.isNull(jo.get(keyName))){//上传中没有子表信息，删除所有原有的记录
			poiObj.deleteSubrows(tableName);
		}else{
			JSONArray subJos = jo.getJSONArray(keyName);
			if(subJos.size()==0){//上传中没有子表信息，删除所有原有的记录
				poiObj.deleteSubrows(tableName);
			}else{
				//转map
				Map<String,JSONObject> subJoMap = new HashMap<String,JSONObject>();
				for(Object so:subJos){
					JSONObject j = (JSONObject)so;
					subJoMap.put(j.getString("rowId").toUpperCase(), j);
				}
				List<BasicRow> rows = poiObj.getRowsByName(tableName);
				if(rows!=null&&rows.size()>0){
					//开始差分
					//获取已存在的rowId
					Map<String,BasicRow> exists = null;
					Set<BasicRow> notExists = null; 
					for(BasicRow r:rows){
						if(subJoMap.keySet().contains(r.getRowId())){
							if(exists==null){
								exists = new HashMap<String,BasicRow>();
							}
							exists.put(r.getRowId(),r);
						}else{
							if(notExists==null){
								notExists = new HashSet<BasicRow>();
							}
							notExists.add(r);
						}
					}
					//删除rows中存在，exists中不存在的
					if(notExists!=null){
						for(BasicRow c:notExists){
							poiObj.deleteSubrow(c);
						}
					}
					//修改exists中存在
					if(exists!=null){
						for(Entry<String,BasicRow> e:exists.entrySet()){
							setSubrowAttr(e.getValue(),subJoMap.get(e.getKey()),keyName);
							subJoMap.remove(e.getKey());
						}
					}
					//
					//在库中不存在，则新增
					if(subJoMap!=null&&subJoMap.size()>0){
						for(Entry<String,JSONObject> e : subJoMap.entrySet()){//经过修改中删除，剩下的都是要新增入库的
							BasicRow br = poiObj.createSubRowByTableName(tableName);
							br.setRowId(e.getKey());
							setSubrowAttr(br,e.getValue(),keyName);
						}
					}
				}else{
					for(Entry<String,JSONObject> e : subJoMap.entrySet()){//全部新增
						BasicRow br = poiObj.createSubRowByTableName(tableName);
						br.setRowId(e.getKey());
						setSubrowAttr(br,e.getValue(),keyName);
					}
				}
			}
		}
	}
	
	private void setSubrowAttr(BasicRow row,JSONObject jo,String keyName)throws Exception{
		if("hotel".equals(keyName)){
			setHotelAttr((IxPoiHotel)row,jo);
		}else if("gasStation".equals(keyName)){
			setGasStationAttr((IxPoiGasstation)row,jo);
		}else if("parkings".equals(keyName)){
			setParkingAttr((IxPoiParking)row,jo);
		}else if("foodtypes".equals(keyName)){
			setRestaurantAttr((IxPoiRestaurant)row,jo);
		}else if("chargingStation".equals(keyName)){
			setChargingstationAttr((IxPoiChargingstation)row,jo);
		}else if("contacts".equals(keyName)){
			setContactAttr((IxPoiContact)row,jo);
		}else if("chargingPole".equals(keyName)){
			setChargingPlotAttr((IxPoiChargingplot)row,jo);
		}
	}

	/**
	 * 名称特殊处理
	 * @param poiObj
	 * @param name
	 * @throws Exception
	 */
	private void setNameAndAttr(IxPoiObj poiObj,String name)throws Exception{
		//获取原始
		if(StringUtils.isEmpty(name)){//上传中没有子表信息，删除所有官方原始中文
			IxPoiName r = poiObj.getNameByLct(langCode, 1, 2);
			if(r!=null){
				r.setName(name);
			}
		}else{
			IxPoiName r = poiObj.getNameByLct(langCode, 1, 2);
			if(r!=null){
				r.setName(name);
			}else{
				//在库中不存在，则新增
				//IX_POI_NAME表
				IxPoiName ixPoiName = poiObj.createIxPoiName();
				ixPoiName.setName(name);
				ixPoiName.setNameClass(1);
				ixPoiName.setNameType(2);
				ixPoiName.setLangCode(langCode);
			}
		}
	}
	
	/**
	 * 地址特殊处理
	 * @param poiObj
	 * @param addr
	 * @throws Exception
	 */
	private void setAddressAndAttr(IxPoiObj poiObj,String addr)throws Exception{
		//获取原始
		if(StringUtils.isEmpty(addr)){//上传中没有子表信息，删除官方原始中文
			IxPoiAddress r = poiObj.getCHAddress();
			if(r!=null){
				poiObj.deleteSubrow(r);
			}
		}else{
			IxPoiAddress r = poiObj.getCHAddress();
			if(r!=null){
				r.setFullname(addr);
			}else{
				//在库中不存在，则新增
				//IX_POI_ADDRESS表
				IxPoiAddress ixPoiAddress = poiObj.createIxPoiAddress();
				ixPoiAddress.setFullname(addr);
				ixPoiAddress.setLangCode(langCode);
			}
		}
	}
	/**
	 * 照片特殊处理，只增不删
	 * @param poiObj
	 * @param jo
	 * @throws Exception
	 */
	private void setPhotoAndAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		JSONArray photos = jo.getJSONArray("attachments");
		List<IxPoiPhoto> objPhotos = poiObj.getIxPoiPhotos();
		Collection<String> objPhotoPIds = new HashSet<String>();
		if(objPhotos!=null){
			for(IxPoiPhoto ipp:objPhotos){
				objPhotoPIds.add(ipp.getPid());
			}
		}
		String memo = null;
		for(Object photo:photos){
			JSONObject pJo = (JSONObject)photo;
			int type = pJo.getInt("type");
			String fccpid = pJo.getString("id");
			if(type==1&&(!objPhotoPIds.contains(fccpid))){
				IxPoiPhoto ixPoiPhoto = poiObj.createIxPoiPhoto();//poi_pid,row_id已经赋值
				ixPoiPhoto.setPid(fccpid);
				ixPoiPhoto.setTag(pJo.getInt("tag"));
				ixPoiPhoto.setRowId(fccpid);
			}else if(type==3){
				memo = pJo.getString("content");
			}
		}
		if(StringUtils.isNotEmpty(memo)){
			IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
			ixPoi.setPoiMemo(memo);
		}
	}
	
	/**
	 * 父子关系特殊处理
	 * @param poiObj
	 * @param jo
	 * @throws Exception
	 */
	private void setChildrenAndAttr(IxPoiObj poiObj,JSONObject jo)throws Exception{
		if(JSONUtils.isNull(jo.get("relateChildren"))){
			poiObj.deleteSubrows(IxPoiObj.IX_POI_CHILDREN);
			poiObj.deleteSubrows(IxPoiObj.IX_POI_PARENT);
		}else{
			JSONArray subJos = jo.getJSONArray("relateChildren");
			if(subJos.size()==0){//上传中没有子表信息，删除所有原有的记录
				poiObj.deleteSubrows(IxPoiObj.IX_POI_CHILDREN);
				poiObj.deleteSubrows(IxPoiObj.IX_POI_PARENT);
			}else{
				//交给差分
				pcs.addUpdateChildren(poiObj.objPid(), subJos);
			}
		}
	}
	
	/** 表属性转换 **/
	private void setHotelAttr(IxPoiHotel row,JSONObject jo)throws Exception{
		row.setCreditCard(jo.getString("creditCards"));
		row.setRating(jo.getInt("rating"));
		row.setCheckinTime(jo.getString("checkInTime"));
		row.setCheckoutTime(jo.getString("checkOutTime"));
		row.setRoomCount(jo.getInt("roomCount"));
		row.setRoomType(jo.getString("roomType"));
		row.setRoomPrice(jo.getString("roomPrice"));
		row.setBreakfast(jo.getInt("breakfast"));
		row.setService(jo.getString("service"));
		row.setParking(jo.getInt("parking"));
		row.setLongDescription(jo.getString("description"));
		row.setOpenHour(jo.getString("openHour"));
	}
	private void setContactAttr(IxPoiContact row,JSONObject jo)throws Exception{
		String linkman = jo.getString("linkman");
		String linkmanNum = "";
		if (linkman.indexOf("总机") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		if (linkman.indexOf("客服") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		if (linkman.indexOf("预订") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		if (linkman.indexOf("销售") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		if (linkman.indexOf("维修") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		if (linkman.indexOf("其他") >= 0) {
			linkmanNum = "1" + linkmanNum;
		} else {
			linkmanNum = "0" + linkmanNum;
		}
		int contactInt = Integer.parseInt(linkmanNum, 2);
		row.setContactDepart(contactInt);
		row.setContact(jo.getString("number"));
		row.setPriority(jo.getInt("priority"));
		row.setContactType(jo.getInt("type"));
	}
	
	private void setChargingPlotAttr(IxPoiChargingplot row,JSONObject jo)throws Exception{
		row.setGroupId(jo.getInt("groupId"));
		row.setCount(jo.getInt("count"));
		row.setAcdc(jo.getInt("acdc"));
		row.setPlugType(jo.getString("plugType"));
		row.setPower(jo.getString("power"));
		row.setVoltage(jo.getString("voltage"));
		row.setCurrent(jo.getString("current"));
		row.setMode(jo.getInt("mode"));
		row.setPlugNum(jo.getInt("plugNum"));
		row.setPrices(jo.getString("prices"));
		row.setOpenType(jo.getString("openType"));
		row.setAvailableState(jo.getInt("availableState"));
		row.setManufacturer(jo.getString("manufacturer"));
		row.setFactoryNum(jo.getString("factoryNum"));
		row.setPlotNum(jo.getString("plotNum"));
		row.setProductNum(jo.getString("productNum"));
		row.setParkingNum(jo.getString("parkingNum"));
		row.setFloor(jo.getInt("floor"));
		row.setLocationType(jo.getInt("locationType"));
		row.setPayment(jo.getString("payment"));
	}
	
	private void setGasStationAttr(IxPoiGasstation row,JSONObject jo)throws Exception{
		row.setServiceProv(jo.getString("servicePro"));
		row.setFuelType(jo.getString("fuelType"));
		row.setOilType(jo.getString("oilType"));
		row.setEgType(jo.getString("egType"));
		row.setMgType(jo.getString("mgType"));
		row.setPayment(jo.getString("payment"));
		row.setService(jo.getString("service"));
		row.setOpenHour(jo.getString("openHour"));
	}
	private void setParkingAttr(IxPoiParking row,JSONObject jo)throws Exception{
		row.setParkingType(jo.getString("buildingType"));
		row.setTollStd(jo.getString("tollStd"));
		row.setTollDes(jo.getString("tollDes"));
		row.setTollWay(jo.getString("tollWay"));
		row.setPayment(jo.getString("payment"));
		row.setRemark(jo.getString("remark"));
		row.setOpenTiime(jo.getString("openTime"));
		row.setTotalNum(jo.getLong("totalNum"));
		row.setResHigh(jo.getDouble("resHigh"));
		row.setResWidth(jo.getDouble("resWidth"));
		row.setResWeigh(jo.getDouble("resWeigh"));
		row.setCertificate(jo.getInt("certificate"));
		row.setVehicle(jo.getInt("vehicle"));
		row.setHaveSpecialplace(jo.getString("haveSpecialPlace"));
		row.setWomenNum(jo.getInt("womenNum"));
		row.setHandicapNum(jo.getInt("handicapNum"));
		row.setMiniNum(jo.getInt("miniNum"));
		row.setVipNum(jo.getInt("vipNum"));
	}
	
	private void setRestaurantAttr(IxPoiRestaurant row,JSONObject jo)throws Exception{
		row.setFoodType(jo.getString("foodtype"));
		row.setCreditCard(jo.getString("creditCards"));
		row.setAvgCost(jo.getInt("avgCost"));
		row.setParking(jo.getInt("parking"));
		row.setOpenHour(jo.getString("openHour"));
	}
	
	private void setChargingstationAttr(IxPoiChargingstation row,JSONObject jo)throws Exception{
		row.setChargingType(jo.getInt("type"));
		row.setChangeBrands(jo.getString("changeBrands"));
		row.setChangeOpenType(jo.getString("changeOpenType"));
		row.setChargingNum(jo.getInt("chargingNum"));
		row.setServiceProv(jo.getString("servicePro"));
		row.setOpenHour(jo.getString("openHour"));
		row.setParkingFees(jo.getInt("parkingFees"));
		row.setParkingInfo(jo.getString("parkingInfo"));
		row.setAvailableState(jo.getInt("availableState"));
	}
	
	public static void main(String[] args) {

//		JSONObject obj = JSONObject.fromObject("{\"key1\":\"\",\"key2\":null,\"key3\":[]}");
//		JSONArray ja = new JSONArray();
//		ja.add(obj);
//		System.out.println(ja.toString());

//		System.out.println(JSONUtils.isNull(obj.get("key1")));
//		System.out.println(JSONUtils.isNull(obj.get("key2")));
//		System.out.println(obj.has("key2"));
//		System.out.println(obj.getJSONObject("key2").isEmpty());
//		System.out.println(obj.get("key3"));
//		System.out.println(obj.get("key3").getClass());
//		System.out.println(JSONUtils.isNull(obj.get("key4")));
		
/*		List<String> list1 = new ArrayList<String>();
		for(int i=0;i<10;i++){
			list1.add(String.valueOf(i));
		}
		List<String> list2 = new ArrayList<String>();
		for(int i=0;i<5;i++){
			list2.add(list1.get(i));
		}
		System.out.println(StringUtils.join(list1,","));
		for(String s:list2){
			list1.remove(s);
		}
		System.out.println(StringUtils.join(list1,","));*/
//		List<String> list2 = list1;
//		for(String s:list2){
//			if(s.equals("7")){
//				list1.remove(s);
//			}
//		}
//		for(Iterator<String> it = list2.iterator();it.hasNext();){
//			String s = it.next();
//			if(s.equals("7")){
//				list1.remove(s);
//			}
//		}
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("AA", "AA123");
		Set<String> maoKey = map.keySet();
		maoKey.add("BB");
		for(Entry<String,String> entry:map.entrySet()){
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
	}
	
}
