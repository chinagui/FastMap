package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
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
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
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

	protected Map<String,String> errLog=new HashMap<String,String>();
	protected List<PoiRelation> parentPid = new ArrayList<PoiRelation>();
	
	protected Map<Long,String> sourceTypes = new HashMap<Long,String>();
	
	public Map<Long, String> getSourceTypes() {
		return sourceTypes;
	}

	public void setSourceTypes(Map<Long, String> sourceTypes) {
		this.sourceTypes = sourceTypes;
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

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		UploadPois pois = ((MultiSrcPoiDayImportorCommand)cmd).getPois();
		if(pois!=null){
			//新增
			List<IxPoiObj> ixPoiObjAdd = this.improtAdd(conn, pois.getAddPois());
			result.putAll(ixPoiObjAdd);
			//修改
			Map<String, JSONObject> updatePois = pois.getUpdatePois();
			List<IxPoiObj> ixPoiObjUpdate = this.improtUpdate(conn,updatePois);
			result.putAll(ixPoiObjUpdate);
			//删除
			Map<String, JSONObject> deletePois = pois.getDeletePois();
			List<IxPoiObj> ixPoiObjDelete = this.improtDelete(conn, deletePois);
			result.putAll(ixPoiObjDelete);
			
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
		for (Map.Entry<String, JSONObject> entry : addPois.entrySet()) {
			JSONObject jo = entry.getValue();
			//日志
			log.info("多源新增json数据"+jo.toString());
			try {
				IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectType.IX_POI);
				importAddByJson(poiObj, jo);
				ixPoiObjList.add(poiObj);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"新增执行成功":e.getMessage());
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
		Collection<Object> fids = new ArrayList<Object>();
		List<JSONObject> joList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : updatePois.entrySet()) {
			JSONObject jso = entry.getValue();
			joList.add(jso);
			fids.add(entry.getKey());
		}
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		List<BasicObj> basicObjList = ObjBatchSelector.selectBySpecColumn(conn,ObjectType.IX_POI,tabNames,"POI_NUM",fids,true,true);
		for (JSONObject jo : joList) {
			//日志
			log.info("多源修改json数据"+jo.toString());
			boolean flag = true;
			//判断查询记录
			for (BasicObj basicObj : basicObjList) {
				IxPoiObj queryObj = (IxPoiObj) basicObj;
				IxPoi ixPoi = (IxPoi) queryObj.getMainrow();
				if(ixPoi.getPoiNum().equals(jo.getString("fid"))){
					flag = false;
					try{
						if(queryObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
							throw new Exception("该数据已经逻辑删除");
						}else{
							this.importUpdateByJson(queryObj, jo);
							ixPoiObjList.add(queryObj);
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
						errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"修改执行成功":e.getMessage());
					}
					break;
				}
			}
			//没有查到数据
			if(flag){
				errLog.put(jo.getString("fid"), "日库中没有查到相应的数据");
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
		Collection<Object> fids = new ArrayList<Object>();
		List<JSONObject> joList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : deletePois.entrySet()) {
			JSONObject jso = entry.getValue();
			joList.add(jso);
			fids.add(entry.getKey());
		}
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		List<BasicObj> basicObjList = ObjBatchSelector.selectBySpecColumn(conn,ObjectType.IX_POI,tabNames,"POI_NUM",fids,true,true);
		for (JSONObject jo : joList) {
			//日志
			log.info("多源删除json数据"+jo.toString());
			boolean flag = true;
			//判断查询记录
			for (BasicObj basicObj : basicObjList) {
				IxPoiObj deleteObj = (IxPoiObj) basicObj;
				IxPoi ixPoi = (IxPoi) deleteObj.getMainrow();
				if(ixPoi.getPoiNum().equals(jo.getString("fid"))){
					flag = false;
					try{
						//判断是否已逻辑删除
						if(deleteObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
							//已逻辑删除
							throw new Exception("该数据已经逻辑删除");
						}else{
							//该对象逻辑删除
							this.importDeleteByJson(deleteObj, jo);
							ixPoiObjList.add(deleteObj);
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
						errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"删除执行成功":e.getMessage());
					}
					break;
				}
			}
			//没有查到数据
			if(flag){
				errLog.put(jo.getString("fid"), "日库中没有查到相应的数据");
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
					if(foodType!= null){
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
							if(!JSONUtils.isNull(jo.get("number"))){
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
				if(!JSONUtils.isNull(jo.get("地址"))){
					if(jo.getString("address") != null){
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
				
				//POI等级--POI 等级 LEVEL
				if(!JSONUtils.isNull(jo.get("level"))){
					String level = jo.getString("level");
					ixPoi.setLevel(level);
				}else{
					throw new Exception("POI等级level字段名不存在");
				}
				//内部--内部标识 INDOOR
				int indoorType =jo.getInt("indoorType");
				ixPoi.setIndoor(indoorType);
				//24小时开放--全天营业 OPEN_24H
				int open24H =jo.getInt("open24H");
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
					if(jo.getString("website")!= null){
						String website =jo.getString("website");
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
				if(!JSONUtils.isNull(jo.get(""))){
				}else{
					throw new Exception("字段名不存在");
				}
				String log = jo.getString("log");
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
				if("改名称".contains(log)){
					this.usdateName(poi, jo, langCode);
				}
				//改分类
				if("改分类".contains(log)){
					if(!JSONUtils.isNull(jo.get("kind"))){
						String kind = jo.getString("kind");
						ixPoi.setKindCode(kind);
					}else{
						throw new Exception("二代分类kind字段名不存在");
					}
				}
				//改电话
				if("改电话".contains(log)){
					this.usdateContact(poi, jo);
				}
				//改地址
				if("改地址".contains(log)){
					this.usdateAddress(poi, jo, langCode);
				}
				//改邮编
				if("改邮编".contains(log)){
					if(!JSONUtils.isNull(jo.get("postCode"))){
						String postCode = jo.getString("postCode");
						ixPoi.setPostCode(postCode);
					}else{
						throw new Exception("邮编postCode字段名不存在");
					}
				}
				//改风味类型
				if("改风味类型".contains(log)){
					this.usdateFoodType(poi, jo);
				}
				//改品牌
				if("改品牌".contains(log)){
					if(!JSONUtils.isNull(jo.get("chain"))){
						String chain = jo.getString("chain");
						ixPoi.setChain(chain);
					}else{
						throw new Exception("品牌chain字段名不存在");
					}
				}
				//改等级
				if("改等级".contains(log)){
					if(!JSONUtils.isNull(jo.get("level"))){
						String level = jo.getString("level");
						ixPoi.setLevel(level);
					}else{
						throw new Exception("POI等级level字段名不存在");
					}
				}
				//改24小时
				if("改24小时".contains(log)){
					int open24H =jo.getInt("open24H");
					ixPoi.setOpen24h(open24H);
				}
				//改星级
				if("改星级".contains(log)){
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
				if("改内部POI".contains(log)){
					int indoorType =jo.getInt("indoorType");
					ixPoi.setIndoor(indoorType);
				}
				//改父子关系
				if("改父子关系".contains(log)){
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
			if(jo.getString("address") != null){
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
		//查询IX_POI_RESTAURANT表
		List<IxPoiContact> ixPoiContacts = poi.getIxPoiContacts();
		if(!"[]".equals(contacts)){
			JSONArray ja = JSONArray.fromObject(contacts);
			//保存查询IX_POI_RESTAURANT表
			List<IxPoiContact> contactList = new ArrayList<IxPoiContact>();
			//多源中不存在，但是日库中存在的电话逻辑删除
			for (IxPoiContact contact : ixPoiContacts) {
				for (int i=0;i<ja.size();i++) {
					JSONObject jso = ja.getJSONObject(i);
					//号码number
					if(!JSONUtils.isNull(jo.get("number"))){
						String number = jso.getString("number");
						if(!number.equals(contact.getContact())){
							poi.deleteSubrow(contact);
						}
					}else{
						throw new Exception("号码number字段名不存在");
					}
				}
				contactList.add(contact);
			}
			for (int i=0;i<ja.size();i++) {
				JSONObject jso = ja.getJSONObject(i);
				//号码number
				String number = null;
				if(!JSONUtils.isNull(jo.get("number"))){
					number = jso.getString("number");
				}else{
					throw new Exception("号码number字段名不存在");
				}
				//联系方式类型type
				int type = jso.getInt("type");
				for (IxPoiContact contact : contactList) {
					if(!number.equals(contact.getContact())){
						//不一致，则将多源中电话和电话类型追加到日库中
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}else if(number.equals(contact.getContact())
							&&type!=contact.getContactType()){
						//不一致，则更新电话和电话类型追加到日库中
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}else if(number.equals(contact.getContact())
							&&type == contact.getContactType()){
						//一致，则不处理
					}
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

	
	public boolean importDeleteByJson(IxPoiObj poi,JSONObject jo)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//判断是否已逻辑删除
				if(poi.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
					//已逻辑删除
					throw new Exception("该数据已经逻辑删除");
				}else{
					//该对象逻辑删除
					poi.deleteObj();
					
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

}
