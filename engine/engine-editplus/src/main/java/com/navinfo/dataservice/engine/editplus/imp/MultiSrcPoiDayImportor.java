package com.navinfo.dataservice.engine.editplus.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.Set;

import com.mysql.fabric.xmlrpc.base.Array;
import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjFactory;
import com.navinfo.dataservice.engine.editplus.model.selector.MultiSrcPoiSelectorConfig;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;
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
public class MultiSrcPoiDayImportor implements JsonImportor {
	private static final Logger log = Logger.getLogger(MultiSrcPoiDayImportor.class);
	protected Map<String,String> errLog=new HashMap<String,String>();
	
	public List<IxPoiObj> importByJsonArray(Connection conn,UploadPois pois)throws Exception{
		if(pois!=null){
			List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
			//新增
			Map<String, JSONObject> addPois = pois.getAddPois();
			for (Map.Entry<String, JSONObject> entry : addPois.entrySet()) {
				JSONObject jo = entry.getValue();
				//日志
				log.info("多源新增json数据"+jo.toString());
				
				try{
					IxPoiObj ixPoiObjAdd = this.improtAdd(conn, jo);
					ixPoiObjList.add(ixPoiObjAdd);
				}catch(Exception e){
					log.error(e.getMessage(),e);
					errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"不为空的属性字段存在null":e.getMessage());
				}
			}
			//删除
			Map<String, JSONObject> deletePois = pois.getDeletePois();
			
			//修改
			Map<String, JSONObject> updatePois = pois.getUpdatePois();
			IxPoiObj ixPoiObjUpdate = this.improtUpdate(conn,updatePois);
			ixPoiObjList.add(ixPoiObjUpdate);
			return ixPoiObjList;
		}
		return null;
	}
	
	/**
	 * 新增数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public IxPoiObj improtAdd(Connection conn,JSONObject jo)throws Exception{
		IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectType.IX_POI);
		//新增数据解析
		this.importAddByJson(poiObj, jo);
		//处理父子关系
		return poiObj;
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
	public IxPoiObj improtUpdate(Connection conn,Map<String, JSONObject> updatePois)throws Exception{
		Collection<Object> fids = new ArrayList<Object>();
		List<JSONObject> joList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : updatePois.entrySet()) {
			JSONObject jso = entry.getValue();
			joList.add(jso);
			fids.add(entry.getKey());
		}
		List<BasicObj> basicObjList = ObjBatchSelector.selectBySpecColumn(conn, ObjectType.IX_POI, MultiSrcPoiSelectorConfig.getInstance(), "POI_NUM", fids, false, false,false);
		//IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectType.IX_POI);
		for (JSONObject jo : joList) {
			//日志
			log.info("多源修改json数据"+jo.toString());
			//判断查询记录
			for (BasicObj basicObj : basicObjList) {
				IxPoiObj queryObj = (IxPoiObj) basicObj;
				IxPoi ixPoi = (IxPoi) queryObj.getMainrow();
				if(ixPoi.getPoiNum().equals(jo.getString("fid"))){
					this.importUpdateByJson(queryObj, jo);
					break;
				}
			}
		}
		return null;
	}
	
	/**
	 * 删除数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public IxPoiObj improtDelete(Connection conn,Map<String,JSONObject> delPois)throws Exception{
		
		return null;
	}
	
	
	 
	@Override
	public boolean importAddByJson(IxPoiObj poi,JSONObject jo)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//POI主表
				IxPoi ixPoi = (IxPoi) poi.getMainrow();
				//显示坐标经度
				double lng = jo.getDouble("lng");
				//显示坐标纬度
				double lat = jo.getDouble("lat");
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
				double guidelon = jo.getDouble("guidelon");
				ixPoi.setXGuide(guidelon);
				//引导坐标纬度--引导Y坐标yGuide
				double guidelat = jo.getDouble("guidelat");
				ixPoi.setYGuide(guidelat);
				//判断是大陆/港澳
				String regionInfo = jo.getString("regionInfo");
				String langCode = null;
				if("D".equals(regionInfo)){
					//大陆
					langCode = "CHI";
				}else if("HM".equals(regionInfo)){
					//港澳
					langCode = "CHT";
				} 
				//名称
				String name = jo.getString("name");
				//IX_POI_NAME表
				IxPoiName ixPoiName = poi.createIxPoiName();
				ixPoiName.setName(name);
				ixPoiName.setNameClass(1);
				ixPoiName.setNameType(2);
				ixPoiName.setLangCode(langCode);
				//二代分类-种别代码 KIND_CODE
				String kind = jo.getString("kind");
				ixPoi.setKindCode(kind);
				//[集合]风味类型
				if(!JSONUtils.isNull(jo.get("foodTypes"))){
					for(Object o:jo.getJSONArray("foodTypes")){
						//...
					}
				}
				String foodTypes = jo.getString("foodTypes");
				if(!"[]".equals(foodTypes)){
					JSONArray ja = JSONArray.fromObject(foodTypes);
					for (int i=0;i<ja.size();i++) {
						JSONObject jso = ja.getJSONObject(i);
						String foodType = jso.getString("foodType");
						//IX_POI_RESTAURANT表
						IxPoiRestaurant ixPoiRestaurant = poi.createIxPoiRestaurant();
						ixPoiRestaurant.setFoodType(foodType);
					}
				}
				//[集合]联系方式
				String contacts = jo.getString("contacts");
				if(!"[]".equals(contacts)){
					JSONArray ja = JSONArray.fromObject(contacts);
					for (int i=0;i<ja.size();i++) {
						JSONObject jso = ja.getJSONObject(i);
						//号码number
						String number = jso.getString("number");
						//联系方式类型type
						int type = jso.getInt("type");
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}
				}
				//邮编--邮政编码 POST_CODE
				String postCode = jo.getString("postCode");
				ixPoi.setPostCode(postCode);
				//地址
				if(!JSONUtils.isNull(jo.get("address"))){
					String address = jo.getString("address");//IX_POI_ADDRESS表
					IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
					ixPoiAddress.setPoiPid(poi.objPid());
					ixPoiAddress.setFullname(address);
					ixPoiAddress.setLangCode(langCode);
				}
				if(jo.getString("address") != null){
					String address = jo.getString("address");
					//IX_POI_ADDRESS表
					IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
					ixPoiAddress.setFullname(address);
					ixPoiAddress.setLangCode(langCode);
				}
				
				//父子关系
				
				//POI等级--POI 等级 LEVEL
				String level = jo.getString("level");
				ixPoi.setLevel(level);
				//内部--内部标识 INDOOR
				int indoorType =jo.getInt("indoorType");
				ixPoi.setIndoor(indoorType);
				//24小时开放--全天营业 OPEN_24H
				int open24H =jo.getInt("open24H");
				ixPoi.setOpen24h(open24H);
				//品牌chain--连锁品牌 CHAIN
				String chain = jo.getString("chain");
				ixPoi.setChain(chain);
				//星级
				if(jo.getInt("rating") >-1){
					int rating =jo.getInt("rating");
					//IX_POI_HOTEL表
					IxPoiHotel ixPoiHotel = poi.createIxPoiHotel();
					ixPoiHotel.setRating(rating);
				}
				//备注
				String remark = jo.getString("remark");
				ixPoi.setPoiMemo(remark);
				//网址
				if(jo.getString("website")!= null){
					String website =jo.getString("website");
					//IX_POI_DETAIL表
					IxPoiDetail ixPoiDetail = poi.createIxPoiDetail();
					ixPoiDetail.setWebSite(website);
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
				String log = jo.getString("log");
				//判断是大陆/港澳
				String regionInfo = jo.getString("regionInfo");
				String langCode = null;
				if("D".equals(regionInfo)){
					//大陆
					langCode = "CHI";
				}else if("HM".equals(regionInfo)){
					//港澳
					langCode = "CHT";
				} 
				//改名称
				if("改名称".contains(log)){
					//名称
					String name = jo.getString("name");
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
				//改分类
				if("改分类".contains(log)){
					String kind = jo.getString("kind");
					ixPoi.setKindCode(kind);
				}
				//改电话
				if("改电话".contains(log)){
					//[集合]联系方式
					String contacts = jo.getString("contacts");
					//查询IX_POI_RESTAURANT表
					List<IxPoiContact> ixPoiContacts = poi.getIxPoiContacts();
					if(!"[]".equals(contacts)){
						JSONArray ja = JSONArray.fromObject(contacts);
						for (IxPoiContact contact : ixPoiContacts) {
							boolean flag = true;
							for (int i=0;i<ja.size();i++) {
								JSONObject jso = ja.getJSONObject(i);
								//号码number
								String number = jso.getString("number");
								//联系方式类型type
								int type = jso.getInt("type");
								if((!number.equals(contact.getContact()))
										||type!=contact.getContactType()){
									//不一致，则将多源中电话和电话类型追加到日库中
									//IX_POI_CONTACT表
									IxPoiContact ixPoiContact = poi.createIxPoiContact();
									ixPoiContact.setContact(number);
									ixPoiContact.setContactType(type);
									flag = false;
								}else if(number.equals(contact.getContact())
										&&type == contact.getContactType()){
									//一致，则不处理
									flag = false;
								}
							}
							if(flag){
								//多源中不存在，但是日库中存在的电话逻辑删除
								poi.deleteSubrow(contact);
							}
						}
					}else if("[]".equals(contacts)){
						//逻辑删除日库中所有电话记录
						for (IxPoiContact ixPoiContact : ixPoiContacts) {
							poi.deleteSubrow(ixPoiContact);
						}
					}
				}
				//改地址
				if("改地址".contains(log)){
					//查询的IX_POI_ADDRESS表
					List<IxPoiAddress> ixPoiAddresses = poi.getIxPoiAddresses();
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
				}
				//改邮编
				if("改邮编".contains(log)){
					String postCode = jo.getString("postCode");
					ixPoi.setPostCode(postCode);
				}
				//改风味类型
				if("改风味类型".contains(log)){
					//查询的IX_POI_RESTAURANT表
					List<IxPoiRestaurant> ixPoiRestaurants = poi.getIxPoiRestaurants();
					//[集合]风味类型
					String foodTypes = jo.getString("foodTypes");
					if(!"[]".equals(foodTypes)){
						JSONArray ja = JSONArray.fromObject(foodTypes);
						for (int i=0;i<ja.size();i++) {
							JSONObject jso = ja.getJSONObject(i);
							String foodType = jso.getString("foodType");
							//IX_POI_RESTAURANT表
							IxPoiRestaurant ixPoiRestaurant = poi.createIxPoiRestaurant();
							ixPoiRestaurant.setFoodType(foodType);
						}
					}if("[]".equals(ixPoiRestaurants)){
						//逻辑删除日库中所有风味类型记录
						for (IxPoiRestaurant ixPoiRestaurant : ixPoiRestaurants) {
							poi.deleteSubrow(ixPoiRestaurant);
						}
						
					}
				}
				//改父子关系
				if("改父子关系".contains(log)){
					
				}
				//改品牌
				if("改品牌".contains(log)){
					String chain = jo.getString("chain");
					ixPoi.setChain(chain);
				}
				//改等级
				if("改等级".contains(log)){
					String level = jo.getString("level");
					ixPoi.setLevel(level);
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
						//IX_POI_HOTEL表
						IxPoiHotel ixPoiHotel = poi.createIxPoiHotel();
						ixPoiHotel.setRating(rating);
					}
				}
				//改内部POI
				if("改内部POI".contains(log)){
					int indoorType =jo.getInt("indoorType");
					ixPoi.setIndoor(indoorType);
				}

				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}

}
