package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.GeoUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import oracle.sql.STRUCT;

public class CorwdsSrcPoiDayImportor extends AbstractOperation{
	
	private String actionName=null;
	
	protected List<PoiRelation> parentPid = new ArrayList<PoiRelation>();
	
	public CorwdsSrcPoiDayImportor(Connection conn, OperationResult preResult) {
		super(conn, preResult);
	}

	@Override
	public String getName() {
		return actionName;
	}
	
	public void setName(String actName) {
		actionName=actName;
	}
	
	public List<PoiRelation> getParentPid() {
		return parentPid;
	}
	
	@Override
	public void operate(AbstractCommand cmd) throws Exception {
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
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_HOTEL");
		return tabNames;
	}
	/**
	 * 生成删除数据
	 * @param conn
	 * @param tPoi
	 * @return
	 * @throws Exception 
	 */
	public void importDelPoi(JSONObject tPoi) throws Exception{
		List<IxPoiObj> listPoiObjs = new ArrayList<IxPoiObj>();
		log.info("众包删除json数据" + tPoi.toString());
		String fid = tPoi.getString("FID");
		List<String> fids = Arrays.asList(fid);
		Set<String> tabNames = this.getTabNames();
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,fids,true,true);
		if(objs.containsKey(fid)){
			BasicObj obj = objs.get(fid);
			try{
				IxPoiObj ixPoi = (IxPoiObj)obj;
				if(ixPoi.isDeleted()){
					throw new Exception("该数据已经逻辑删除,FID:" + fid);
				}else{
					// 该对象逻辑删除
					ixPoi.deleteObj();
					// IX_POI_PHOTO
					JSONObject photos = tPoi.getJSONObject("PHOTO");
					if(photos != null && !photos.isEmpty() && !photos.isNullObject()){
						Iterator keys = photos.keys();
						while(keys.hasNext()){
							String key = (String) keys.next();
							String photoName = photos.getString(key);
							String photoPid = photoName.replace(".jpg", "");
							IxPoiPhoto ixPoiPhoto = ixPoi.createIxPoiPhoto();
							ixPoiPhoto.setPid(photoPid);
							if("p1".equals(key)){
								ixPoiPhoto.setTag(3);
							}
							if("p2".equals(key)){
								ixPoiPhoto.setTag(1);
							}
							if("p3".equals(key)){
								ixPoiPhoto.setTag(2);
							}
							if("p4".equals(key)){
								ixPoiPhoto.setTag(100);
							}
							if("p5".equals(key)){
								ixPoiPhoto.setTag(4);
							}
						}
					}
				}
				listPoiObjs.add(ixPoi);
				this.result.putAll(listPoiObjs);
			}catch (Exception e) {
				log.error(e.getMessage(),e);
				throw e;
			}

		}else{
			throw new ImportException("在日库中没有加载到POI,FID:" + fid); 
		}
	}
	
	/**
	 * 生成修改数据
	 * @param conn
	 * @param tPoi
	 * @return
	 */
	public void importUpdatePoi(JSONObject tPoi) throws Exception{
		List<IxPoiObj> listPoiObjs = new ArrayList<IxPoiObj>();
		log.info("众包修改json数据" + tPoi.toString());
		String fid = tPoi.getString("FID");
		JSONArray editHistory = tPoi.getJSONArray("EDITHISTORY");
		if (editHistory.isEmpty()){
			throw new ImportException("修改数据，但无履历内容,FID:" + fid); 
		}
		Set<String> tabNames = this.getTabNames();
		List<String> fids = Arrays.asList(fid);
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,fids,true,true);
		if(objs.containsKey(fid)){
			BasicObj obj = objs.get(fid);
			try{
				IxPoiObj ixPoi = (IxPoiObj)obj;
				if(!ixPoi.isDeleted()){
					IxPoi ixPoiMain = (IxPoi)ixPoi.getMainrow();
					String langCode = "CHI";
					String newName = "";
					IxPoiName ixPoiName = ixPoi.getOfficeOriginCHIName();
					if(ixPoiName != null){
						newName = ixPoiName.getName();
					}
					String kindCode = ixPoiMain.getKindCode();
					MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
					// 遍历履历中变更字段，同时维护日库数据
					for(int i=0;i<editHistory.size();i++){
						JSONObject history = editHistory.getJSONObject(i);
						JSONObject newValue = history.getJSONObject("newValue");
						if(newValue.containsKey("name")){
							newName = ExcelReader.h2f(tPoi.getString("REAUDITNAME"));
							if(ixPoiName != null){
								ixPoiName.setName(newName);
							}else{
								IxPoiName poiName = ixPoi.createIxPoiName();
								poiName.setName(newName);
								poiName.setNameClass(1);
								poiName.setNameType(2);
								poiName.setLangCode(langCode);
							}
						}
						if(newValue.containsKey("kindCode")){
							String newKindCode = tPoi.getString("RECLASSCODE");
							if(newKindCode != null && !newKindCode.equals(kindCode)){
								ixPoiMain.setKindCode(newKindCode);
								// 星级酒店特殊处理 需要新增IX_POI_HOTEL子表
								if("120101".equals(newKindCode)){
									IxPoiHotel hotel = ixPoi.createIxPoiHotel();
									hotel.setRating(1);
								}
								kindCode = newKindCode;
							}
							
						}
						if(newValue.containsKey("address")){
							String newAddress = "";
							if(StringUtils.isNotEmpty(tPoi.getString("REAUDITADDRESS")) && !"null".equals(tPoi.getString("REAUDITADDRESS"))){
								newAddress = ExcelReader.h2f(tPoi.getString("REAUDITADDRESS"));
							}
							if(StringUtils.isNotEmpty(newAddress)){
								IxPoiAddress ixPoiAddress = ixPoi.getCHIAddress();
								if (ixPoiAddress != null){
									ixPoiAddress.setFullname(newAddress);
								}else{
									IxPoiAddress newPoiAddress = ixPoi.createIxPoiAddress();
									newPoiAddress.setFullname(newAddress);
									newPoiAddress.setLangCode(langCode);
								}
							}
						}
						if(newValue.containsKey("contacts")){
							String newAllPhone = "";
							if(StringUtils.isNotEmpty(tPoi.getString("REAUDITPHONE")) && !"null".equals(tPoi.getString("REAUDITPHONE"))){
								newAllPhone = tPoi.getString("REAUDITPHONE");
							}
							if(StringUtils.isNotEmpty(newAllPhone)){
								String[] phones = newAllPhone.split("\\|");
								ixPoi.deleteSubrows("IX_POI_CONTACT");
								for(int j=0;j<phones.length;j++){
									int type = 1;
									String tmpPhone = phones[j];
									// 判断为固话还是移动电话
									if(tmpPhone.startsWith("1") && !tmpPhone.startsWith("0") && !tmpPhone.contains("-")){
										type = 2;
									}
									IxPoiContact ixPoiContact = ixPoi.createIxPoiContact();
									ixPoiContact.setContact(tmpPhone);
									ixPoiContact.setContactType(type);
									ixPoiContact.setPriority(j+1);
								}
							}
						}
						if(newValue.containsKey("location")){
							// 显示坐标取小数点后5位
							double x = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOX"));
							double y = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOY"));
							// 显示坐标经纬度--图幅号码meshId
							String[] meshes = MeshUtils.point2Meshes(x, y);
							if(meshes.length>1){
								throw new ImportException("POI坐标不能在图框线上");
							}
							ixPoiMain.setMeshId(Integer.parseInt(meshes[0]));
							// 显示坐标经纬度--显示坐标
							Geometry geometry = GeoTranslator.point2Jts(x, y);
							ixPoiMain.setGeometry(geometry);
							// 引导坐标和引导link
							Map<Long, Coordinate> pidGuide = getGuideLinkPid(x, y);
							if (!pidGuide.isEmpty()){
								for(long linkPid: pidGuide.keySet()){
									ixPoiMain.setLinkPid(linkPid);
									double xGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).x);
									double yGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).y);
									ixPoiMain.setXGuide(xGuide);
									ixPoiMain.setYGuide(yGuide);
								}
							}else{
								// 没找到引导link
								log.info("没找到引导link，fid:" + fid);
							}
						}
					}
					// TRUCK
					int truck = metadataApi.getCrowdTruck(kindCode);
					if(truck != ixPoiMain.getTruckFlag()){
						ixPoiMain.setTruckFlag(truck);
					}
					// LEVEL
					JSONObject jsonObj=new JSONObject();
					jsonObj.put("dbId", tPoi.getInt("dbId"));
					jsonObj.put("pid",Integer.valueOf(String.valueOf(ixPoiMain.getPid())));
					jsonObj.put("poi_num",fid);
					jsonObj.put("kindCode",kindCode);
					jsonObj.put("chainCode","");
					jsonObj.put("name",newName);
					jsonObj.put("level","");
					// 星级酒店特殊处理
					if("120101".equals(kindCode)){
						jsonObj.put("rating",1);
					}else{
						jsonObj.put("rating",0);
					}
					String level = metadataApi.getLevelForMulti(jsonObj);
					if(level != null && !level.equals(ixPoiMain.getLevel())){
						ixPoiMain.setLevel(level);
					}
					// IX_POI_PHOTO
					JSONObject photos = tPoi.getJSONObject("PHOTO");
					if(photos != null && !photos.isEmpty() && !photos.isNullObject()){
						Iterator keys = photos.keys();
						while(keys.hasNext()){
							String key = (String) keys.next();
							String photoName = photos.getString(key);
							String photoPid = photoName.replace(".jpg", "");
							IxPoiPhoto ixPoiPhoto = ixPoi.createIxPoiPhoto();
							ixPoiPhoto.setPid(photoPid);
//							ixPoiPhoto.setRowId(photoPid);
							if("p1".equals(key)){
								ixPoiPhoto.setTag(3);
							}
							if("p2".equals(key)){
								ixPoiPhoto.setTag(1);
							}
							if("p3".equals(key)){
								ixPoiPhoto.setTag(2);
							}
							if("p4".equals(key)){
								ixPoiPhoto.setTag(100);
							}
							if("p5".equals(key)){
								ixPoiPhoto.setTag(4);
							}
						}
					}
					
				}else{
					throw new Exception("该数据已经逻辑删除");
				}
				listPoiObjs.add(ixPoi);
				this.result.putAll(listPoiObjs);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				throw e;
			}
		}else{
			throw new ImportException("在日库中没有加载到POI,FID:" + fid); 
		}
	}
	
	/**
	 * 生成新增数据
	 * @param conn
	 * @param tPoi
	 * @return newPid
	 * @throws Exception 
	 */
	public long importAddPoi(JSONObject tPoi) throws Exception{
		long newPid = 0;
		List<IxPoiObj> listPoiObjs = new ArrayList<IxPoiObj>();
		log.info("众包新增json数据" + tPoi.toString());
		try{
			IxPoiObj poi = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
			newPid = poi.objPid();
			if(poi!=null){
				if(poi instanceof IxPoiObj){
					// POI主表
					IxPoi ixPoi = (IxPoi) poi.getMainrow();
					// NAME 转全角
					String name = "";
					if(StringUtils.isNotEmpty(tPoi.getString("REAUDITNAME")) && !"null".equals(tPoi.getString("REAUDITNAME"))){
						name = ExcelReader.h2f(tPoi.getString("REAUDITNAME"));
					}
					// PID
					long pid = poi.objPid();
					// POI_NUM
					String fid = tPoi.getString("FID");
					ixPoi.setPoiNum(fid);
					// 显示坐标取小数点后5位
					double x = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOX"));
					double y = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOY"));
					// 显示坐标经纬度--图幅号码meshId
					String[] meshes = MeshUtils.point2Meshes(x, y);
					if(meshes.length>1){
						throw new ImportException("POI坐标不能在图框线上");
					}
					ixPoi.setMeshId(Integer.parseInt(meshes[0]));
					// 显示坐标经纬度--显示坐标
					Geometry geometry = GeoTranslator.point2Jts(x, y);
					ixPoi.setGeometry(geometry);
					// KIND_CODE
					String kindCode = tPoi.getString("RECLASSCODE");
					ixPoi.setKindCode(kindCode);
					// 星级酒店特殊处理 需要新增IX_POI_HOTEL子表
					if("120101".equals(kindCode)){
						IxPoiHotel hotel = poi.createIxPoiHotel();
						hotel.setRating(1);
					}
					// LEVEL
					JSONObject jsonObj=new JSONObject();
					jsonObj.put("dbId", tPoi.getInt("dbId"));
					jsonObj.put("pid",Integer.valueOf(String.valueOf(poi.objPid())));
					jsonObj.put("poi_num",fid);
					jsonObj.put("kindCode",kindCode);
					jsonObj.put("chainCode","");
					jsonObj.put("name",name);
					jsonObj.put("level","");
					// 星级酒店特殊处理
					if("120101".equals(kindCode)){
						jsonObj.put("rating",1);
					}else{
						jsonObj.put("rating",0);
					}
					MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
					String level = metadataApi.getLevelForMulti(jsonObj);
					ixPoi.setLevel(level);
					// TRUCK
					int truck = metadataApi.getCrowdTruck(kindCode);
					ixPoi.setTruckFlag(truck);
					// POI_MEMO
					if(StringUtils.isNotEmpty(tPoi.getString("DESCP")) && !"null".equals(tPoi.getString("DESCP"))){
						ixPoi.setPoiMemo(tPoi.getString("DESCP"));
					}
					// OPEN_24H  2017.8.3 新增数据赋OPEN_24H为2
					ixPoi.setOpen24h(2);
					
					String langCode= "CHI";  // 众包大陆数据
					// IX_POI_NAME
					if(StringUtils.isNotEmpty(name)){
						IxPoiName ixPoiName = poi.createIxPoiName();
						ixPoiName.setName(name);
						ixPoiName.setNameClass(1);
						ixPoiName.setNameType(2);
						ixPoiName.setLangCode(langCode);
					}else{
						throw new Exception("名称name字段为空");
					}
					// IX_POI_ADDRESS
					if(!"null".equals(tPoi.getString("REAUDITADDRESS"))){
						String address = ExcelReader.h2f(tPoi.getString("REAUDITADDRESS"));
						if(StringUtils.isNotEmpty(address)){
							IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
							ixPoiAddress.setFullname(address);
							ixPoiAddress.setLangCode(langCode);
						}
					}
					// IX_POI_CONTACT
					String phoneAll = "";
					if(StringUtils.isNotEmpty(tPoi.getString("REAUDITPHONE")) && !"null".equals(tPoi.getString("REAUDITPHONE"))){
						phoneAll = tPoi.getString("REAUDITPHONE");
					}
					if(StringUtils.isNotEmpty(phoneAll)){
						String[] phones = phoneAll.split("\\|");
						for(int i=0;i<phones.length;i++){
							int type = 1;
							String tmpPhone = phones[i];
							// 判断为固话还是移动电话
							if(tmpPhone.startsWith("1") && !tmpPhone.startsWith("0") && !tmpPhone.contains("-")){
								type = 2;
							}
							IxPoiContact ixPoiContact = poi.createIxPoiContact();
							ixPoiContact.setContact(tmpPhone);
							ixPoiContact.setContactType(type);
							ixPoiContact.setPriority(i+1);
						}
					}
					// IX_POI_FLAG
					// 新增数据取消掉IX_POI_FLAG表
//					String flag = "110000290000";
//					IxPoiFlag ixPoiFlag = poi.createIxPoiFlag();
//					ixPoiFlag.setFlagCode(flag);
					// IX_POI_PHOTO
					JSONObject photos = tPoi.getJSONObject("PHOTO");
					if(photos != null && !photos.isEmpty() && !photos.isNullObject()){
						Iterator keys = photos.keys();
						while(keys.hasNext()){
							String key = (String) keys.next();
							String photoName = photos.getString(key);
							String photoPid = photoName.replace(".jpg", "");
							IxPoiPhoto ixPoiPhoto = poi.createIxPoiPhoto();
							ixPoiPhoto.setPid(photoPid);
//							ixPoiPhoto.setRowId(photoPid);
							if("p1".equals(key)){
								ixPoiPhoto.setTag(3);
							}
							if("p2".equals(key)){
								ixPoiPhoto.setTag(1);
							}
							if("p3".equals(key)){
								ixPoiPhoto.setTag(2);
							}
							if("p4".equals(key)){
								ixPoiPhoto.setTag(100);
							}
							if("p5".equals(key)){
								ixPoiPhoto.setTag(4);
							}
						}	
					}
					// GUIDE_X,GUIDE_Y,LINK_PID
					Map<Long, Coordinate> pidGuide = getGuideLinkPid(x, y);
					if (!pidGuide.isEmpty()){
						for(long linkPid: pidGuide.keySet()){
							ixPoi.setLinkPid(linkPid);
							double xGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).x);
							double yGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).y);
							ixPoi.setXGuide(xGuide);
							ixPoi.setYGuide(yGuide);
						}
					}else{
						// 没找到引导link处理
						log.info("没找到引导link，fid:" + fid);
					}
					
					listPoiObjs.add(poi);
					this.result.putAll(listPoiObjs);
				}else{
					throw new ImportException("不支持的对象类型");
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}
		return newPid;
	}
	
	/**
	 * 生成充电桩新增数据
	 * @param tPoi
	 * @return newPid
	 * @throws Exception 
	 */
	public long importChargeAddPoi(JSONObject tPoi) throws Exception{
		long newPid = 0;
		List<IxPoiObj> listPoiObjs = new ArrayList<IxPoiObj>();
		log.info("众包charge新增json数据" + tPoi.toString());
		try{
			IxPoiObj poi = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
			newPid = poi.objPid();
			if(poi!=null){
				if(poi instanceof IxPoiObj){
					// POI主表
					IxPoi ixPoi = (IxPoi) poi.getMainrow();
					// NAME 转全角
					String name = "";
					if(StringUtils.isNotEmpty(tPoi.getString("NAME")) && !"null".equals(tPoi.getString("NAME"))){
						name = ExcelReader.h2f(tPoi.getString("NAME"));
					}
					// PID
					long pid = poi.objPid();
					// POI_NUM
					String fid = tPoi.getString("FID");
					ixPoi.setPoiNum(fid);
					// 显示坐标取小数点后5位
					double x = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOX"));
					double y = DoubleUtil.keepSpecDecimal(tPoi.getDouble("GEOY"));
					// 显示坐标经纬度--图幅号码meshId
					String[] meshes = MeshUtils.point2Meshes(x, y);
					if(meshes.length>1){
						throw new ImportException("POI坐标不能在图框线上");
					}
					ixPoi.setMeshId(Integer.parseInt(meshes[0]));
					// 显示坐标经纬度--显示坐标
					Geometry geometry = GeoTranslator.point2Jts(x, y);
					ixPoi.setGeometry(geometry);
					// KIND_CODE
					String kindCode = tPoi.getString("KINDCODE");
					ixPoi.setKindCode(kindCode);

					// LEVEL
					JSONObject jsonObj=new JSONObject();
					jsonObj.put("dbId", tPoi.getInt("dbId"));
					jsonObj.put("pid",Integer.valueOf(String.valueOf(poi.objPid())));
					jsonObj.put("poi_num",fid);
					jsonObj.put("kindCode",kindCode);
					jsonObj.put("chainCode","");
					jsonObj.put("name",name);
					jsonObj.put("level","");
					// 星级酒店特殊处理
					if("120101".equals(kindCode)){
						jsonObj.put("rating",1);
					}else{
						jsonObj.put("rating",0);
					}
					MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
					String level = metadataApi.getLevelForMulti(jsonObj);
					ixPoi.setLevel(level);
					// TRUCK
					int truck = metadataApi.getCrowdTruck(kindCode);
					ixPoi.setTruckFlag(truck);
					// POI_MEMO
					if(StringUtils.isNotEmpty(tPoi.getString("MEMO")) && !"null".equals(tPoi.getString("MEMO"))){
						ixPoi.setPoiMemo(tPoi.getString("MEMO"));
					}
					// OPEN_24H  2017.8.3 新增数据赋OPEN_24H为2
					ixPoi.setOpen24h(2);
					
					String langCode= "CHI";  // 众包大陆数据
					// IX_POI_NAME
					if(StringUtils.isNotEmpty(name)){
						IxPoiName ixPoiName = poi.createIxPoiName();
						ixPoiName.setName(name);
						ixPoiName.setNameClass(1);
						ixPoiName.setNameType(2);
						ixPoiName.setLangCode(langCode);
					}
					// IX_POI_ADDRESS
					if(!"null".equals(tPoi.getString("ADDRESS"))){
						String address = ExcelReader.h2f(tPoi.getString("ADDRESS"));
						if(StringUtils.isNotEmpty(address)){
							IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
							ixPoiAddress.setFullname(address);
							ixPoiAddress.setLangCode(langCode);
						}
					}
					// IX_POI_CONTACT
					String phoneAll = "";
					if(StringUtils.isNotEmpty(tPoi.getString("TELEPHONE")) && !"null".equals(tPoi.getString("TELEPHONE"))){
						phoneAll = tPoi.getString("TELEPHONE");
					}
					if(StringUtils.isNotEmpty(phoneAll)){
						String[] phones = phoneAll.split("\\|");
						for(int i=0;i<phones.length;i++){
							int type = 1;
							String tmpPhone = phones[i];
							// 判断为固话还是移动电话
							if(tmpPhone.startsWith("1") && !tmpPhone.startsWith("0") && !tmpPhone.contains("-")){
								type = 2;
							}
							IxPoiContact ixPoiContact = poi.createIxPoiContact();
							ixPoiContact.setContact(tmpPhone);
							ixPoiContact.setContactType(type);
							ixPoiContact.setPriority(i+1);
						}
					}
					// IX_POI_PHOTO
					JSONObject photos = tPoi.getJSONObject("PHOTO");
					if(photos != null && !photos.isEmpty() && !photos.isNullObject()){
						Iterator keys = photos.keys();
						while(keys.hasNext()){
							String key = (String) keys.next();
							JSONArray tmpPhoto = photos.getJSONArray(key);
							if(tmpPhoto != null && !tmpPhoto.isEmpty() && tmpPhoto.size() > 0){
								for(int i=0;i<tmpPhoto.size();i++){
									String photoName = (String) tmpPhoto.getString(i);
									String photoPid = photoName.replace(".jpg", "");
									IxPoiPhoto ixPoiPhoto = poi.createIxPoiPhoto();
									ixPoiPhoto.setPid(photoPid);
									if("p1".equals(key)){
										ixPoiPhoto.setTag(1);
									}
									if("p2".equals(key)){
										ixPoiPhoto.setTag(4);
									}
									if("p3".equals(key)){
										ixPoiPhoto.setTag(100);
									}
									if("p4".equals(key)){
										ixPoiPhoto.setTag(100);
									}
								}
							}
						}	
					}
					// GUIDE_X,GUIDE_Y,LINK_PID
					Map<Long, Coordinate> pidGuide = getGuideLinkPid(x, y);
					if (!pidGuide.isEmpty()){
						for(long linkPid: pidGuide.keySet()){
							ixPoi.setLinkPid(linkPid);
							double xGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).x);
							double yGuide = DoubleUtil.keepSpecDecimal(pidGuide.get(linkPid).y);
							ixPoi.setXGuide(xGuide);
							ixPoi.setYGuide(yGuide);
						}
					}else{
						// 没找到引导link处理
						log.info("没找到引导link，fid:" + fid);
					}
					
					// IX_POI_PARENT   X_POI_CHILDREN
					//处理父子关系
//					if("230218".equals(kindCode)){
//						IxPoiParent ixPoiParent = poi.createIxPoiParent();
//						ixPoiParent.setParentPoiPid(newPid);
//					}
					String fatherson = null;
					if(StringUtils.isNotEmpty(tPoi.getString("FFID")) && "230227".equals(kindCode)){
						fatherson = tPoi.getString("FFID");
						//如果当前poi作为子，则要判断是否设置了父或者取消了父；
						PoiRelation pr = new PoiRelation();
						pr.setFatherFid(fatherson);
						pr.setPid(ixPoi.getPid());
						pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
						parentPid.add(pr);
//						// 根据父fid查询出父表中的groupId
//						List<String> fids = Arrays.asList(fatherson);
//						Set<String> tabNames = new HashSet<>();
//						tabNames.add("IX_POI_PARENT");
//						Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,fids,false,false);
//						IxPoiParent ixPoiParent = null;
//						if(objs.containsKey(fid)){
//							BasicObj obj = objs.get(fid);
//							IxPoiObj parentPoi = (IxPoiObj)obj;
//							if(!parentPoi.isDeleted()){
//								List<IxPoiParent> poiParents = parentPoi.getIxPoiParents();
//								if(poiParents.size()>0){
//									ixPoiParent = poiParents.get(0);
//								}
//							}
//						}
//						if(ixPoiParent != null){
//							IxPoiChildren newIxPoiChildren;
//							newIxPoiChildren = poi.createIxPoiChildren(ixPoiParent.getGroupId());
//							newIxPoiChildren.setChildPoiPid(newPid);
//						}else{
//							throw new ImportException("该桩在日库中未找到其对应的父-站");
//						}
					}
					JSONObject detail = tPoi.getJSONObject("DETAIL");
					// IX_POI_CHARGINGSTATION  IX_POI_CHARGINGPLOT
					if(!JSONUtils.isNull(detail)){
						if("230218".equals(kindCode)){
							IxPoiChargingstation chargeStation = poi.createIxPoiChargingstation();
							chargeStation.setAvailableState(detail.getInt("cs_availableState"));
							if(detail.containsKey("cs_type")){
								chargeStation.setChargingType(detail.getInt("cs_type"));
							}
							if(StringUtils.isNotEmpty(detail.getString("cs_servicePro")) && !"null".equals(detail.getString("cs_servicePro"))){
								chargeStation.setServiceProv(detail.getString("cs_servicePro"));
							}
							chargeStation.setOpenHour(detail.getString("cs_openHour"));
							chargeStation.setParkingFees(detail.getInt("cs_parkingFees"));
							chargeStation.setParkingInfo(detail.getString("cs_parkingInfo"));
						}else if("230227".equals(kindCode)){
							IxPoiChargingplot chargePole = poi.createIxPoiChargingplot();
							chargePole.setAcdc(detail.getInt("cp_acdc"));
							if(StringUtils.isNotEmpty(detail.getString("cp_plugType")) && !"null".equals(detail.getString("cp_plugType"))){
								chargePole.setPlugType(detail.getString("cp_plugType"));
							}
							chargePole.setPower(detail.getString("cp_power"));
							chargePole.setVoltage(detail.getString("cp_voltage"));
							chargePole.setCurrent(detail.getString("cp_current"));
							chargePole.setMode(detail.getInt("cp_mode"));
							chargePole.setFactoryNum(detail.getString("cp_factoryNum"));
							chargePole.setPlotNum(detail.getString("cp_plotNum"));
							chargePole.setProductNum(detail.getString("cp_productNum"));
							chargePole.setParkingNum(detail.getString("cp_parkingNum"));
							chargePole.setFloor(detail.getInt("cp_floor"));
							chargePole.setLocationType(detail.getInt("cp_locationType"));
							if(StringUtils.isNotEmpty(detail.getString("cp_payment")) && !"null".equals(detail.getString("cp_payment"))){
								chargePole.setPayment(detail.getString("cp_payment"));
							}
							chargePole.setAvailableState(detail.getInt("cp_availableState"));
						}else{
							throw new ImportException("kindCode值错误");
						}
					}
					
					listPoiObjs.add(poi);
					this.result.putAll(listPoiObjs);
				}else{
					throw new ImportException("不支持的对象类型");
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}
		return newPid;
	}

	/**
	 * 根据POI的显示坐标计算引导link_pid,X_GUIDE,Y_GUIDE
	 * @param x
	 * @param y
	 * @return
	 * @throws Exception 
	 */
	private Map<Long, Coordinate> getGuideLinkPid(double x, double y) throws Exception{
		Map<Long, Coordinate> pidGuideXY = new HashMap<Long, Coordinate>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		sb.append(" select r.link_pid,r.function_class,r.geometry ");
		sb.append("   from rd_link r, rd_link_form f ");
		sb.append("  where r.link_pid = f.link_pid ");
		sb.append("    and f.form_of_way <> 50 ");
		sb.append("    and SDO_NN(r.GEOMETRY,");
		sb.append("               NAVI_GEOM.CREATEPOINT(:1, :2),");
		sb.append("               'SDO_NUM_RES=1 DISTANCE=1000 UNIT=METER') = 'TRUE' ");
		sb.append("    and r.function_class=5 ");
		sb.append("    and r.u_record <> 2 ");
		sb.append("    and f.u_record <> 2 ");
		sb.append(" UNION ALL ");
		sb.append(" select r.link_pid,r.function_class,r.geometry ");
		sb.append("   from rd_link r, rd_link_form f ");
		sb.append("  where r.link_pid = f.link_pid ");
		sb.append("    and f.form_of_way <> 50 ");
		sb.append("    and SDO_NN(r.GEOMETRY,");
		sb.append("               NAVI_GEOM.CREATEPOINT(:3, :4),");
		sb.append("               'SDO_NUM_RES=1 DISTANCE=1000 UNIT=METER') = 'TRUE'");
		sb.append("    and r.function_class<>5 ");
		sb.append("    and r.u_record <> 2 ");
		sb.append("    and f.u_record <> 2 ");
		try{
			pstmt = this.conn.prepareStatement(sb.toString());
			pstmt.setDouble(1, x);
			pstmt.setDouble(2, y);
			pstmt.setDouble(3, x);
			pstmt.setDouble(4, y);
			rs = pstmt.executeQuery();
			Geometry class5Link = null;
			long class5LinkPid = 0;
			Geometry classNot5Link = null;
			long classNot5LinkPid = 0;
			while(rs.next()){
				int functionClass = rs.getInt("function_class");
				if(functionClass == 5){
					STRUCT class5LinkStruct = (STRUCT)rs.getObject("geometry");
					class5Link = GeoTranslator.struct2Jts(class5LinkStruct);
					class5LinkPid = rs.getLong("link_pid");
				}else{
					STRUCT classNot5LinkStruct = (STRUCT)rs.getObject("geometry");
					classNot5Link = GeoTranslator.struct2Jts(classNot5LinkStruct);
					classNot5LinkPid = rs.getLong("link_pid");
				}
				
			}
			Coordinate location = new Coordinate(x, y);
			if(class5Link != null && classNot5Link == null){
				Coordinate guide = GeometryUtils.GetNearestPointOnLine(location, class5Link);
				pidGuideXY.put(class5LinkPid, guide);
				return pidGuideXY;
			}
			if(class5Link == null && classNot5Link != null){
				Coordinate guide = GeometryUtils.GetNearestPointOnLine(location, classNot5Link);
				pidGuideXY.put(classNot5LinkPid, guide);
				return pidGuideXY;
			}
			if(class5Link != null && classNot5Link != null){
				Coordinate guideClass5 = GeometryUtils.GetNearestPointOnLine(location, class5Link);
				Coordinate guideClassNot5 = GeometryUtils.GetNearestPointOnLine(location, classNot5Link);
				double distanceClass5 = GeometryUtils.getDistance(location, guideClass5);
				double distanceClassNot5 = GeometryUtils.getDistance(location, guideClassNot5);
				if (distanceClass5 < distanceClassNot5){
					pidGuideXY.put(class5LinkPid, guideClass5);
					return pidGuideXY;
				}else{
					pidGuideXY.put(classNot5LinkPid, guideClassNot5);
					return pidGuideXY;
				}
			}
			

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return pidGuideXY;
	}

}
