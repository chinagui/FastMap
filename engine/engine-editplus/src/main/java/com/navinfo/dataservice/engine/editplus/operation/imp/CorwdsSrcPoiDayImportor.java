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
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
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
					// 遍历履历中变更字段，同时维护日库数据
					for(int i=0;i<editHistory.size();i++){
						JSONObject history = editHistory.getJSONObject(i);
						JSONObject newValue = history.getJSONObject("newValue");
						if(newValue.containsKey("name")){
							String newName = ExcelReader.h2f(tPoi.getString("REAUDITNAME"));
							IxPoiName ixPoiName = ixPoi.getOfficeOriginCHIName();
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
							String kindCode = tPoi.getString("RECLASSCODE");
							ixPoiMain.setKindCode(kindCode);
						}
						if(newValue.containsKey("address")){
							String newAddress = ExcelReader.h2f(tPoi.getString("REAUDITADDRESS"));
							IxPoiAddress ixPoiAddress = ixPoi.getCHIAddress();
							if (ixPoiAddress != null){
								ixPoiAddress.setFullname(newAddress);
							}else{
								IxPoiAddress newPoiAddress = ixPoi.createIxPoiAddress();
								newPoiAddress.setFullname(newAddress);
								newPoiAddress.setLangCode(langCode);
							}
						}
						if(newValue.containsKey("contacts")){
							String newAllPhone = tPoi.getString("REAUDITPHONE");
							String[] phones = newAllPhone.split("|");
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
						if(newValue.containsKey("photo")){
							// IX_POI_PHOTO暂时不处理
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
					String name = ExcelReader.h2f(tPoi.getString("REAUDITNAME"));
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
					// LEVEL
					JSONObject jsonObj=new JSONObject();
					jsonObj.put("dbId", tPoi.getInt("dbId"));
					jsonObj.put("pid",Integer.valueOf(String.valueOf(poi.objPid())));
					jsonObj.put("poi_num",fid);
					jsonObj.put("kindCode",kindCode);
					jsonObj.put("chainCode","");
					jsonObj.put("name",name);
					jsonObj.put("level","");
					jsonObj.put("rating",0);
					MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
					String level = metadataApi.getLevelForMulti(jsonObj);
					ixPoi.setLevel(level);
					// TRUCK
					int truck = metadataApi.getCrowdTruck(kindCode);
					ixPoi.setTruckFlag(truck);
					// POI_MEMO
					if(StringUtils.isNotEmpty(tPoi.getString("DESCP"))){
						ixPoi.setPoiMemo(tPoi.getString("DESCP"));
					}
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
					String address = ExcelReader.h2f(tPoi.getString("REAUDITADDRESS"));
					if(StringUtils.isNotEmpty(address)){
						IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
						ixPoiAddress.setFullname(address);
						ixPoiAddress.setLangCode(langCode);
					}else{
						throw new Exception("地址address字段为空");
					}
					// IX_POI_CONTACT
					String phoneAll = tPoi.getString("REAUDITPHONE");
					String[] phones = phoneAll.split("|");
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
					// IX_POI_FLAG
					String flag = "110000290000";
					IxPoiFlag ixPoiFlag = poi.createIxPoiFlag();
					ixPoiFlag.setFlagCode(flag);
					// IX_POI_PHOTO
					// TODO
					// GUIDE_X,GUIDE_Y,LINK_PID
					Map<Long, Coordinate> pidGuide = getGuideLinkPid(x, y);
					if (!pidGuide.isEmpty()){
						for(long linkPid: pidGuide.keySet()){
							ixPoi.setLinkPid(linkPid);
							double xGuide = pidGuide.get(linkPid).x;
							double yGuide = pidGuide.get(linkPid).y;
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
		sb.append(" UNION ALL ");
		sb.append(" select r.link_pid,r.function_class,r.geometry ");
		sb.append("   from rd_link r, rd_link_form f ");
		sb.append("  where r.link_pid = f.link_pid ");
		sb.append("    and f.form_of_way <> 50 ");
		sb.append("    and SDO_NN(r.GEOMETRY,");
		sb.append("               NAVI_GEOM.CREATEPOINT(:3, :4),");
		sb.append("               'SDO_NUM_RES=1 DISTANCE=1000 UNIT=METER') = 'TRUE'");
		sb.append("    and r.function_class<>5 ");
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
