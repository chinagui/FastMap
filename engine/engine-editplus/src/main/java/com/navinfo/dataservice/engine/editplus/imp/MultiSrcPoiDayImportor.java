package com.navinfo.dataservice.engine.editplus.imp;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: MultiSrcPoiDayImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月17日
 * @Description: MultiSrcPoiDayImportor.java
 */
public class MultiSrcPoiDayImportor implements JsonImportor {
	protected Map<Long,String> fatherMap;
	public List<IxPoiObj> importByJsonArray(Connection conn,JSONArray jos)throws Exception{
		if(jos!=null){
			for(Object object:jos){
				JSONObject jo=(JSONObject)object;
				int addFlag = jo.getInt("addFlag");
				int delFlag = jo.getInt("delFlag");
				String log = jo.getString("log");
				if(addFlag == 1){
					//新增
					this.improtAdd(conn, jo);
				}else if(delFlag == 1){
					//删除
				}else if(addFlag!=1 && delFlag!=0 && log != null){
					//修改
				}
				ObjFactory.getInstance();
			}
		}
		return null;
	}
	public IxPoiObj improtAdd(Connection conn,JSONObject jo)throws Exception{
		IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectType.IX_POI, true);
		this.importByJson(poiObj, jo);
		return poiObj;
	}
	public IxPoiObj improtUpdate(Connection conn,JSONObject jo)throws Exception{
		//ObjSelector.selectByPid(conn, ObjectType.IX_POI,MultiSrcPoiSelectorConfig.getInstance() , pid, isOnlyMain, isLock);
		return null;
	}
	public IxPoiObj improtDelete(Connection conn,JSONObject jo)throws Exception{
		return null;
	}
	
	public void handleFatherson(List<BasicObj> objs){
		
	}
	 
	@Override
	public boolean importByJson(BasicObj obj,JSONObject jo)throws Exception {
		if(obj!=null&&jo!=null){
			if(obj instanceof IxPoiObj){
				IxPoiObj poi = (IxPoiObj)obj;
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
				//名称
				String name = jo.getString("name");
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
						//IX_POI_RESTAURANT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}
				}
				//邮编--邮政编码 POST_CODE
				String postCode = jo.getString("postCode");
				ixPoi.setPostCode(postCode);
				//地址
				if(jo.getString("address") != null){
					String address = jo.getString("address");
					//IX_POI_ADDRESS表
					IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
					ixPoiAddress.setFullname(address);
				}
				//父子关系
				if(jo.getString("fatherson") != null){
					String fatherson = jo.getString("fatherson");
				}
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
				//修改履历log
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
			}else{
				throw new ImportException("不支持的对象类型");
			}
			return true;
		}
		return false;
	}

}
