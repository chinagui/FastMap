package com.navinfo.dataservice.engine.edit.operation.obj.poi.upload;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperStage;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.operator.poi.index.IxPoiOperator;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command;
import com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UploadOperation {
	/**
	 * 读取txt，解析，入库
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public JSONObject importPoi(String fileName)throws Exception{
		JSONObject retObj = new JSONObject();
		Scanner importPois = new Scanner(new FileInputStream(fileName));
		JSONArray ja = new JSONArray();
		while (importPois.hasNextLine()) {
			try {
				String line = importPois.nextLine();
				JSONObject json = JSONObject.fromObject(line);
				ja.add(json);
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
	private JSONObject changeData(JSONArray ja) throws Exception{
		JSONObject retObj = new JSONObject();
		List<String> errList = new ArrayList<String>();
		Connection manConn = null;
		Connection conn = null;
		// 获取当前做业季
		String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		QueryRunner qRunner = new QueryRunner();
		try {
			Map<String,List<JSONObject>> data = new HashMap<String,List<JSONObject>>();
			manConn = DBConnector.getInstance().getManConnection();
			for (int i=0;i<ja.size();i++) {
				JSONObject jo = ja.getJSONObject(i);
				
				// 坐标确定grid，grid确定区库ID
				String wkt = jo.getString("geometry");
				Geometry point = new WKTReader().read(wkt);
				Coordinate[] coordinate =point.getCoordinates();
				CompGridUtil gridUtil = new CompGridUtil();
				String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
				String manQuery = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
				String dbId = qRunner.queryForString(manConn, manQuery, grid);
				if (dbId.isEmpty()){
					String errstr = "fid:" + jo.getString("fid") + "不在已知grid内";
					errList.add(errstr);
					continue;
				}
				
				if (!data.containsKey(dbId)) {
					List<JSONObject> dataList = new ArrayList<JSONObject>();
					dataList.add(jo);
					data.put(dbId, dataList);
				} else {
					List<JSONObject> dataList = data.get(dbId);
					dataList.add(jo);
					data.put(dbId, dataList);
				}
			}
			
			// 确认每个点属于新增、修改还是删除
			// fid在IX_POI.POI_NUM中查找不到，并且待上传数据lifecycle不为1，为新增
			// fid能找到，并且待上传数据lifecycle不为1且对应的IX_POI.U_RECORD不为2（删除），即为修改
			// fid能找到，并且待上传数据lifecycle为1，即为删除
			String subQuery = "SELECT u_record FROM ix_poi WHERE poi_num=:1";
			JSONObject insertObj= new JSONObject();
			JSONObject updateObj = new JSONObject();
			JSONObject deleteObj = new JSONObject();
			for (Iterator<String> iter = data.keySet().iterator();iter.hasNext();){
				// 创建连接，查找FID是否存在
				String dbId = iter.next();
				if (conn != null) {
					DBUtils.closeConnection(conn);
				}
				conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				List<JSONObject> dataList = data.get(dbId);
				List<JSONObject> insertList = new ArrayList<JSONObject>();
				List<JSONObject> updateList = new ArrayList<JSONObject>();
				List<JSONObject> deleteList = new ArrayList<JSONObject>();
				for (int i=0;i<dataList.size();i++) {
					JSONObject poi = dataList.get(i);
					String fid = poi.getString("fid");
					int lifecycle = poi.getInt("t_lifecycle");
					int uRecord = qRunner.queryForInt(conn, subQuery, fid);
					// 判断每一条数据是新增、修改还是删除
					if (uRecord != -1) {
						// 能找到，判断lifecycle和u_record
						if (lifecycle == 1) {
							deleteList.add(poi);
						} else {
							
							if (uRecord == 2) {
								String errstr = "fid:" + fid + "为修改数据，但库中u_record为2";
								errList.add(errstr);
							} else {
								updateList.add(poi);
							}
						}
					} else {
						// 找不到，判断lifecycle是否为1
						if (lifecycle == 1) {
							String errstr = "fid:" + fid + "在库中找不到对应数据，但lifecycle为1";
							errList.add(errstr);
						} else {
							insertList.add(poi);
						}
					}
				}
				
				// 将数据分为新增、修改和删除三组，key值为区库ID
				insertObj.put(dbId, insertList);
				updateObj.put(dbId, updateList);
				deleteObj.put(dbId, deleteList);
				
			}
			
			// 数据入库处理
			JSONObject insertRet = insertData(insertObj,version);
			JSONObject updateRet = updateData(updateObj,version);
			JSONObject deleteRet = deleteData(deleteObj);
			
			int insertCount = insertRet.getInt("success");
			int updateCount = updateRet.getInt("success");
			int deleteCount = deleteRet.getInt("success");
			
			int total = insertCount + updateCount + deleteCount;
			
			@SuppressWarnings("unchecked")
			List<String> insertFail = (List<String>)insertRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> updateFail = (List<String>)updateRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> deleteFail = (List<String>)deleteRet.get("fail");
			
			errList.addAll(insertFail);
			errList.addAll(updateFail);
			errList.addAll(deleteFail);
			
			retObj.put("success", total);
			retObj.put("fail", errList);
			return retObj;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeConnection(manConn);
		}
	}
	
	// 处理新增数据
	@SuppressWarnings("unchecked")
	private JSONObject insertData(JSONObject insertObj,String version) throws Exception{
		JSONObject retObj = new JSONObject();
		int count = 0;
		List<JSONObject> errList = new ArrayList<JSONObject>();
		try {
			for (Iterator<String> iter = insertObj.keySet().iterator();iter.hasNext();){
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) insertObj.get(dbId);
					for (int i=0;i<poiList.size();i++) {
						JSONObject jo = poiList.get(i);
						IxPoi poi = new IxPoi();
						JSONObject perRetObj = obj2PoiForInsert(jo,version);
						int flag = perRetObj.getInt("flag");
						if (flag == 1) {
							poi.Unserialize(perRetObj.getJSONObject("ret"));
							Result result = new Result();
							result.setOperStage(OperStage.Collect);
							JSONObject poiObj = new JSONObject();
							poiObj.put("dbId", dbId);
							poiObj.put("objId", poi.getPid());
							// 调用一次插入
							CommandForUpload poiCommand = new CommandForUpload(poiObj, null);
							ProcessForUpload poiProcess = new ProcessForUpload(poiCommand);
							result.insertObject(poi, ObjStatus.INSERT, poi.getPid());
							poiProcess.setResult(result);
							poiProcess.run();
							count++;
							
							// 鲜度验证，POI状态更新
							String rawFields = jo.getString("rawFields");
							new IxPoiOperator(conn,poi.getRowId(),0,rawFields) ;
						} else if (flag == 0) {
							errList.add(perRetObj.getJSONObject("ret"));
						}
						
					}
				} catch (Exception e) {
					throw e;
				} finally {
					DBUtils.closeConnection(conn);
				}
			}
			retObj.put("success", count);
			retObj.put("fail", errList);
			return retObj;
		} catch (Exception e) {
			throw e;
		} 
	}
	
	// 处理更新数据
	@SuppressWarnings("unchecked")
	private JSONObject updateData(JSONObject updateObj,String version) throws Exception {
		JSONObject retObj = new JSONObject();
		int count = 0;
		List<JSONObject> errList = new ArrayList<JSONObject>();
		try {
			for (Iterator<String> iter = updateObj.keySet().iterator();iter.hasNext();){
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) updateObj.get(dbId);
					for (int i=0;i<poiList.size();i++) {
						JSONObject jo = poiList.get(i);
						JSONObject perRetObj = obj2PoiForUpdate(jo, version,conn);
						int flag = perRetObj.getInt("flag");
						if (flag == 1) {
							JSONObject poiJson = perRetObj.getJSONObject("ret");
							JSONObject commandJson = new JSONObject();
							commandJson.put("dbId", Integer.parseInt(dbId));
							commandJson.put("data", poiJson);
							commandJson.put("objId", poiJson.getInt("pid"));
							// 调用一次更新
							Command updateCommand = new Command(commandJson,null);
							Process updateProcess = new Process(updateCommand);
							updateProcess.run();
							count++;
							
							// 鲜度验证，POI状态更新
							boolean freshFlag = perRetObj.getBoolean("freshFlag");
							String rawFields = jo.getString("rawFields");
							if (freshFlag) {
								new IxPoiOperator(conn,poiJson.getString("rowId"),1,rawFields) ;
							} else {
								new IxPoiOperator(conn,poiJson.getString("rowId"),0,rawFields) ;
							}
						} else if (flag == 0) {
							errList.add(perRetObj.getJSONObject("ret"));
						}
					}
				} catch (Exception e) {
					throw e;
				} finally {
					DBUtils.closeConnection(conn);
				}
			}
			retObj.put("success", count);
			retObj.put("fail", errList);
			return retObj;
		} catch (Exception e) {
			throw e;
		}
	}


	// 处理删除数据
	@SuppressWarnings("unchecked")
	private JSONObject deleteData(JSONObject deleteObj) throws Exception {
		JSONObject retObj = new JSONObject();
		int count = 0;
		List<JSONObject> errList = new ArrayList<JSONObject>();
		try {
			for (Iterator<String> iter = deleteObj.keySet().iterator();iter.hasNext();){
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) deleteObj.get(dbId);
					for (int i=0;i<poiList.size();i++) {
						JSONObject jo = poiList.get(i);
						int pid = jo.getInt("pid");
						String fid = jo.getString("fid");
						JSONObject poiObj = new JSONObject();
						poiObj.put("dbId", dbId);
						poiObj.put("objId", pid);
						try {
							Result result = new Result();
							result.setOperStage(OperStage.Collect);
							CommandForDelete poiCommand = new CommandForDelete(poiObj, null);
							ProcessForDelete poiProcess = new ProcessForDelete(poiCommand);
							poiProcess.setResult(result);
							poiProcess.run();
							count++;
							
							// 鲜度验证，POI状态更新
							String rawFields = jo.getString("rawFields");
							IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
							JSONObject poiRowId = ixPoiSelector.getRowIdById(pid);
							new IxPoiOperator(conn,poiRowId.getString("rowId"),0,rawFields) ;
						} catch (Exception e) {
							JSONObject errObj = new JSONObject();
							errObj.put("fid", fid);
							errObj.put("reason", e.getMessage());
							errList.add(errObj);
						}
					}
				} catch (Exception e) {
					throw e;
				} finally {
					DBUtils.closeConnection(conn);
				}
				
			}
			retObj.put("success", count);
			retObj.put("fail", errList);
			return retObj;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 新增数据解析
	 * @param jo
	 * @param version
	 * @return
	 * @throws Exception 
	 */
	private JSONObject obj2PoiForInsert(JSONObject jo,String version) throws Exception {
		IxPoi poi = new IxPoi();
		String fid = jo.getString("fid");
		JSONObject retObj = new JSONObject();
		try{
			// POI主表
			int pid = PidService.getInstance().applyPoiPid();
			poi.setPid(pid);
			poi.setKindCode(jo.getString("kindCode"));
			// geometry按SDO_GEOMETRY格式原值转出
			Geometry geometry = new WKTReader().read(jo.getString("geometry"));
			poi.setGeometry(GeoTranslator.transform(geometry, 100000, 5));
			if (jo.getJSONObject("guide").size()>0) {
				poi.setxGuide(jo.getJSONObject("guide").getDouble("longitude"));
				poi.setyGuide(jo.getJSONObject("guide").getDouble("latitude"));
				poi.setLinkPid(jo.getJSONObject("guide").getInt("linkPid"));
			} else {
				poi.setxGuide(0);
				poi.setyGuide(0);
				poi.setLinkPid(0);
			}
			
			poi.setChain(jo.getString("chain"));
			poi.setOpen24h(jo.getInt("open24H"));
			// meshid非0时原值转出；为0时根据几何计算；
			int meshId = jo.getInt("meshid");
			if (meshId == 0) {
				String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
				meshId = Integer.parseInt(meshIds[0]);
			}
			poi.setMesh(meshId);
			poi.setPostCode(jo.getString("postCode"));
			//如果KIND_CODE有修改，则追加“改种别代码”；
			//如果CHAIN有修改，则追加“改连锁品牌”；
			//如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
			String fieldState = "";
			if (!jo.getString("kindCode").isEmpty()) {
				fieldState += "改种别代码|";
			}
			if (!jo.getString("chain").isEmpty()) {
				fieldState += "改连锁品牌|";
			}
			if (jo.has("hotel")) {
				
				JSONObject hotel = jo.getJSONObject("hotel");
				
				if (!hotel.isNullObject() && hotel.has("rating")) {
					fieldState += "改酒店星级|";
				}
			}
			if (fieldState.length()>0) {
				fieldState = fieldState.substring(0, fieldState.length()-1);
			}
			poi.setFieldState(fieldState);
			poi.setOldName(jo.getString("name"));
			poi.setOldAddress(jo.getString("address"));
			poi.setOldKind(jo.getString("kindCode"));
			poi.setPoiNum(jo.getString("fid"));
			poi.setDataVersion(version);
			poi.setCollectTime(jo.getString("t_operateDate"));
			poi.setLevel(jo.getString("level"));
			String outDoorLog = "";
			if (!poi.getOldName().isEmpty()) {
				outDoorLog += "改名称|";
			}
			if (!poi.getOldAddress().isEmpty()) {
				outDoorLog += "改地址|";
			}
			if (!poi.getOldKind().isEmpty()) {
				outDoorLog += "改分类|";
			}
			if (!poi.getLevel().isEmpty()) {
				outDoorLog += "改POI_LEVEL|";
			}
			if (!poi.getGeometry().isEmpty()) {
				outDoorLog += "改RELATION|";
			}
			if (outDoorLog.length()>0) {
				outDoorLog = outDoorLog.substring(0,outDoorLog.length()-1);
			}
			poi.setLog(outDoorLog);
			poi.setSportsVenue(jo.getString("sportsVenues"));
			
			JSONObject indoor = jo.getJSONObject("indoor");
			if (!indoor.isNullObject() && indoor.has("type")) {
				poi.setIndoor(indoor.getInt("type"));
			} else {
				poi.setIndoor(0);
			}
			
			poi.setVipFlag(jo.getString("vipFlag"));
			poi.setuRecord(1);
			
			poi.setRowId(UuidUtils.genUuid().toUpperCase());
			
			// 名称
			if (!poi.getOldName().isEmpty()) {
				List<IRow> nameList = new ArrayList<IRow>();
				IxPoiName poiName = new IxPoiName();
				int nameId = PidService.getInstance().applyPoiNameId();
				poiName.setPid(nameId);
				poiName.setPoiPid(pid);
				poiName.setNameGroupid(1);
				poiName.setLangCode("CHI");
				poiName.setNameClass(1);
				poiName.setNameType(2);
				poiName.setName(poi.getOldName());
				poiName.setRowId(UuidUtils.genUuid());
				nameList.add(poiName);
				poi.setNames(nameList);
			}
			
			// 地址
			if (!poi.getOldAddress().isEmpty()) {
				List<IRow> addressList = new ArrayList<IRow>();
				IxPoiAddress poiAddress = new IxPoiAddress();
				int addressId = PidService.getInstance().applyPoiAddressId();
				poiAddress.setPid(addressId);
				poiAddress.setPoiPid(pid);
				poiAddress.setNameGroupid(1);
				poiAddress.setLangCode("CHI");
				poiAddress.setFullname(poi.getOldAddress());
				poiAddress.setRowId(UuidUtils.genUuid());
				addressList.add(poiAddress);
				poi.setAddresses(addressList);
			}
			
			// 联系方式
			if (jo.getJSONArray("contacts").size()>0) {
				JSONArray contactsList = jo.getJSONArray("contacts");
				List<IRow> contacts = new ArrayList<IRow>();
				for (int k=0;k<contactsList.size();k++) {
					JSONObject contactObj = contactsList.getJSONObject(k);
					IxPoiContact contact = new IxPoiContact();
					contact.setPoiPid(pid);
					contact.setContactType(contactObj.getInt("type"));
					contact.setContact(contactObj.getString("number"));
					String linkman = contactObj.getString("linkman");
					String linkmanNum = "";
					if (linkman.indexOf("总机")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("客服")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("预订")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("销售")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("维修")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("其他")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					int contactInt = Integer.parseInt(linkmanNum, 2);
					contact.setContactDepart(contactInt);
					contact.setPriority(contactObj.getInt("priority"));
					contact.setRowId(contactObj.getString("rowId"));
					contacts.add(contact);
				}
				poi.setContacts(contacts);
			}
			
			// 照片
			JSONArray attachments = jo.getJSONArray("attachments");
			List<String> photoIdList = new ArrayList<String>();
			List<IRow> photoList = new ArrayList<IRow>();
			for (int k=0;k<attachments.size();k++) {
				JSONObject photo = attachments.getJSONObject(k);
				int type = photo.getInt("type");
				if (type==1) {
					String photoId = photo.getString("id");
					if (!photoIdList.contains(photoId)) {
						photoIdList.add(photoId);
						IxPoiPhoto poiPhoto = new IxPoiPhoto();
						poiPhoto.setPoiPid(pid);
						poiPhoto.setPid(photoId);
						poiPhoto.setTag(photo.getInt("tag"));
						poiPhoto.setRowId(photoId);
						photoList.add(poiPhoto);
					}
				}
			}
			if (photoList.size()>0) {
				poi.setPhotos(photoList);
			}
			
			// 父子关系
			if (jo.getJSONArray("relateChildren").size()>0) {
				int groupId = PidService.getInstance().applyPoiGroupId();
				IxPoiParent parent = new IxPoiParent();
				List<IRow> parentList = new ArrayList<IRow>();
				parent.setPid(groupId);
				parent.setParentPoiPid(pid);
				parent.setRowId(UuidUtils.genUuid());
				parentList.add(parent);
				poi.setParents(parentList);
				
				JSONArray relateChildren = jo.getJSONArray("relateChildren");
				List<IRow> childrenList = new ArrayList<IRow>();
				for (int k=0;k<relateChildren.size();k++) {
					JSONObject children = relateChildren.getJSONObject(k);
					IxPoiChildren poiChildren = new IxPoiChildren();
					poiChildren.setGroupId(groupId);
					poiChildren.setChildPoiPid(children.getInt("childPid"));
					poiChildren.setRelationType(children.getInt("type"));
					poiChildren.setRowId(children.getString("rowId"));
					childrenList.add(poiChildren);
				}
				poi.setChildren(childrenList);
			}
			
			// 加油站
			if (jo.getJSONObject("gasStation").size()>0) {
				JSONObject gasObj = jo.getJSONObject("gasStation");
				IxPoiGasstation gasStation = new IxPoiGasstation();
				List<IRow> gasList = new ArrayList<IRow>();
				gasStation.setPid(PidService.getInstance().applyPoiGasstationId());
				gasStation.setPoiPid(pid);
				gasStation.setServiceProv(gasObj.getString("servicePro"));
				gasStation.setFuelType(gasObj.getString("fuelType"));
				gasStation.setOilType(gasObj.getString("oilType"));
				gasStation.setEgType(gasObj.getString("egType"));
				gasStation.setMgType(gasObj.getString("mgType"));
				gasStation.setPayment(gasObj.getString("payment"));
				gasStation.setService(gasObj.getString("service"));
				gasStation.setOpenHour(gasObj.getString("openHour"));
				gasStation.setRowId(gasObj.getString("rowId"));
				gasList.add(gasStation);
				poi.setGasstations(gasList);
			}
			
			// 停车场
			if (jo.getJSONObject("parkings").size()>0) {
				JSONObject parkingsObj = jo.getJSONObject("parkings");
				IxPoiParking parkings = new IxPoiParking();
				List<IRow> parkingsList = new ArrayList<IRow>();
				parkings.setPid(PidService.getInstance().applyPoiParkingsId());
				parkings.setPoiPid(pid);
				parkings.setParkingType(parkingsObj.getString("buildingType"));
				parkings.setTollStd(parkingsObj.getString("tollStd"));
				parkings.setTollDes(parkingsObj.getString("tollDes"));
				parkings.setTollWay(parkingsObj.getString("tollWay"));
				parkings.setPayment(parkingsObj.getString("payment"));
				parkings.setRemark(parkingsObj.getString("remark"));
				parkings.setOpenTiime(parkingsObj.getString("openTime"));
				parkings.setTotalNum(parkingsObj.getInt("totalNum"));
				parkings.setResHigh(parkingsObj.getInt("resHigh"));
				parkings.setResWidth(parkingsObj.getInt("resWidth"));
				parkings.setResWeigh(parkingsObj.getInt("resWeigh"));
				parkings.setCertificate(parkingsObj.getInt("certificate"));
				parkings.setVehicle(parkingsObj.getInt("vehicle"));
				parkings.setHaveSpecialplace(parkingsObj.getString("haveSpecialPlace"));
				parkings.setWomenNum(parkingsObj.getInt("womenNum"));
				parkings.setHandicapNum(parkingsObj.getInt("handicapNum"));
				parkings.setMiniNum(parkingsObj.getInt("miniNum"));
				parkings.setVipNum(parkingsObj.getInt("vipNum"));
				parkings.setRowId(parkingsObj.getString("rowId"));
				parkingsList.add(parkings);
				poi.setParkings(parkingsList);
			}
			
			// 酒店
			if (jo.getJSONObject("hotel").size()>0) {
				JSONObject hotelObj = jo.getJSONObject("hotel");
				IxPoiHotel hotel = new IxPoiHotel();
				List<IRow> hotelList = new ArrayList<IRow>();
				hotel.setPid(PidService.getInstance().applyPoiHotelId());
				hotel.setPoiPid(pid);
				hotel.setCreditCard(hotelObj.getString("creditCards"));
				hotel.setRating(hotelObj.getInt("rating"));
				hotel.setCheckinTime(hotelObj.getString("checkInTime"));
				hotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
				hotel.setRoomCount(hotelObj.getInt("roomCount"));
				hotel.setRoomType(hotelObj.getString("roomType"));
				hotel.setRoomPrice(hotelObj.getString("roomPrice"));
				hotel.setBreakfast(hotelObj.getInt("breakfast"));
				hotel.setService(hotelObj.getString("service"));
				hotel.setParking(hotelObj.getInt("parking"));
				hotel.setLongDescription(hotelObj.getString("description"));
				hotel.setOpenHour(hotelObj.getString("openHour"));
				hotel.setRowId(hotelObj.getString("rowId"));
				hotelList.add(hotel);
				poi.setHotels(hotelList);
			}
			
			// 餐馆
			if (jo.getJSONObject("foodtypes").size()>0) {
				JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
				IxPoiRestaurant foodtypes = new IxPoiRestaurant();
				List<IRow> foodtypesList = new ArrayList<IRow>();
				foodtypes.setPid(PidService.getInstance().applyPoiFoodId());
				foodtypes.setPoiPid(pid);
				foodtypes.setFoodType(foodtypesObj.getString("foodtype"));
				foodtypes.setCreditCard(foodtypesObj.getString("creditCards"));
				foodtypes.setAvgCost(foodtypesObj.getInt("avgCost"));
				foodtypes.setParking(foodtypesObj.getInt("parking"));
				foodtypes.setOpenHour(foodtypesObj.getString("openHour"));
				foodtypes.setRowId(foodtypesObj.getString("rowId"));
				foodtypesList.add(foodtypes);
				poi.setRestaurants(foodtypesList);
			}
			
			retObj.put("flag", 1);
			retObj.put("ret", poi.Serialize(null));
		} catch (Exception e) {
			retObj.put("flag", 0);
			JSONObject errObj = new JSONObject();
			errObj.put("fid", fid);
			String errStr = e.getMessage();
			if (errStr == null) {
				errStr = "";
			}
			errObj.put("reason", errStr);
			retObj.put("ret", errObj);
		}
		return retObj;
	}
	
	/**
	 *  更新数据解析
	 * @param jo
	 * @param version
	 * @param conn
	 * @return
	 */
	private JSONObject obj2PoiForUpdate(JSONObject jo,String version,Connection conn) {
		String fid = jo.getString("fid");
		JSONObject retObj = new JSONObject();
		boolean freshFlag = true;
		try {
			int pid = jo.getInt("pid");
			// 查出旧的POI
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi oldPoi = (IxPoi) ixPoiSelector.loadById(pid, false);
			// POI主表
			JSONObject poiJson = new JSONObject();
			poiJson.put("pid", pid);
			poiJson.put("kindCode", jo.getString("kindCode"));
			// geometry按SDO_GEOMETRY格式原值转出
			Geometry geometry = new WKTReader().read(jo.getString("geometry"));
			JSONObject geometryObj = GeoTranslator.jts2Geojson(geometry);
			poiJson.put("geometry", geometryObj);
			if (jo.getJSONObject("guide").size()>0) {
				poiJson.put("xGuide", jo.getJSONObject("guide").getDouble("longitude"));
				poiJson.put("yGuide", jo.getJSONObject("guide").getDouble("latitude"));
				poiJson.put("linkPid", jo.getJSONObject("guide").getInt("linkPid"));
			} else {
				poiJson.put("xGuide", 0);
				poiJson.put("yGuide", 0);
				poiJson.put("linkPid", 0);
			}
			poiJson.put("chain", jo.getString("chain"));
			poiJson.put("open24h", jo.getInt("open24H"));
			// meshid非0时原值转出；为0时根据几何计算；
			int meshId = jo.getInt("meshid");
			if (meshId == 0) {
				String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
				meshId = Integer.parseInt(meshIds[0]);
			}
			poiJson.put("meshId", meshId);
			poiJson.put("postCode", jo.getString("postCode"));
			//如果KIND_CODE有修改，则追加“改种别代码”；
			//如果CHAIN有修改，则追加“改连锁品牌”；
			//如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
			String fieldState = "";
			if (!jo.getString("kindCode").equals(oldPoi.getKindCode())) {
				fieldState += "改种别代码|";
			}
			if (!jo.getString("chain").equals(oldPoi.getChain())) {
				fieldState += "改连锁品牌|";
			}
			List<IRow> hotelListIRow = oldPoi.getHotels();
			IxPoiHotel hotelOld = null;
			if (hotelListIRow.size()>0) {
				hotelOld = (IxPoiHotel) hotelListIRow.get(0);
			}
			
			if (jo.has("hotel")) {
				JSONObject hotel = jo.getJSONObject("hotel");
				
				if(!hotel.isNullObject() && hotelOld == null){
					fieldState += "改酒店星级|";
				}
				else if(hotel.isNullObject() && hotelOld != null){
					fieldState += "改酒店星级|";
				}
				else if(!hotel.isNullObject()){
					
					if (hotel == null || hotel.getInt("rating") != hotelOld.getRating()) {
						fieldState += "改酒店星级|";
					}
				}
			}
			
			if (fieldState.length()>0) {
				fieldState = fieldState.substring(0, fieldState.length()-1);
			}
			poiJson.put("fieldState", fieldState);
			poiJson.put("oldName",jo.getString("name"));
			poiJson.put("oldAddress", jo.getString("address"));
			poiJson.put("oldKind", jo.getString("kindCode"));
			poiJson.put("poiNum", jo.getString("fid"));
			poiJson.put("dataVersion", version);
			poiJson.put("collectTime", jo.getString("t_operateDate"));
			poiJson.put("level", jo.getString("level"));
			String outDoorLog = "";
			if (!jo.getString("name").equals(oldPoi.getOldName())) {
				outDoorLog += "改名称|";
			}
			if (!jo.getString("address").equals(oldPoi.getOldAddress())) {
				outDoorLog += "改地址|";
			}
			if (!jo.getString("kindCode").equals(oldPoi.getOldKind())) {
				outDoorLog += "改分类|";
			}
			if (!jo.getString("level").equals(oldPoi.getLevel())) {
				outDoorLog += "改POI_LEVEL|";
			}
			if (!geometry.equals(oldPoi.getGeometry())) {
				outDoorLog += "改RELATION|";
			}
			if (outDoorLog.length()>0) {
				outDoorLog = outDoorLog.substring(0,outDoorLog.length()-1);
			}
			poiJson.put("log", outDoorLog);
			poiJson.put("sportsVenue", jo.getString("sportsVenues"));
			
			JSONObject indoor = jo.getJSONObject("indoor");
			if (!indoor.isNullObject() && indoor.has("type")) {
				poiJson.put("indoor", indoor.getInt("type"));
			} else {
				poiJson.put("indoor",0);
			}
			poiJson.put("vipFlag", jo.getString("vipFlag"));
			poiJson.put("rowId", oldPoi.getRowId());
			
			poiJson.put("objStatus", ObjStatus.UPDATE.toString());
			
			// 鲜度验证
			if (oldPoi.fillChangeFields(poiJson)) {
				freshFlag = false;
			}
			
			// 名称
			List<IRow> oldNameList = oldPoi.getNames();
			String oldNameStr = "";
			IxPoiName oldNameObjChi = new IxPoiName();
			for (IRow oldNameObj:oldNameList) {
				IxPoiName oldName = (IxPoiName)oldNameObj;
				if (oldName.getNameClass()==1 && oldName.getNameType()==2 && oldName.getLangCode().equals("CHI")) {
					oldNameStr = oldName.getName();
					oldNameObjChi = oldName;
					break;
				}
			}
			if (!poiJson.getString("oldName").isEmpty() || !oldNameStr.isEmpty()) {
				if (oldNameStr.isEmpty()) {
					JSONArray nameList = new JSONArray();
					JSONObject poiName = new JSONObject();
					int nameId = PidService.getInstance().applyPoiNameId();
					poiName.put("objStatus", ObjStatus.INSERT.toString());
					poiName.put("pid", nameId);
					poiName.put("poiPid", pid);
					poiName.put("nameGroupid", 1);
					poiName.put("langCode", "CHI");
					poiName.put("nameClass",1);
					poiName.put("nameType", 2);
					poiName.put("name", poiJson.getString("oldName"));
					poiName.put("rowId", UuidUtils.genUuid());
					nameList.add(poiName);
					poiJson.put("names", nameList);
					// 鲜度验证
					freshFlag = false;
				} else if (!oldNameStr.equals(poiJson.getString("oldName"))) {
					JSONArray nameList = new JSONArray();
					JSONObject poiName = new JSONObject();
					poiName.put("objStatus", ObjStatus.UPDATE.toString());
					poiName.put("pid", oldNameObjChi.getPid());
					poiName.put("poiPid", oldNameObjChi.getPoiPid());
					poiName.put("nameGroupid", oldNameObjChi.getNameGroupid());
					poiName.put("langCode", oldNameObjChi.getLangCode());
					poiName.put("nameClass",oldNameObjChi.getNameClass());
					poiName.put("nameType", oldNameObjChi.getNameType());
					poiName.put("name", poiJson.getString("oldName"));
					poiName.put("rowId", oldNameObjChi.getRowId());
					nameList.add(poiName);
					poiJson.put("names", nameList);
					// 鲜度验证
					freshFlag = false;
				}
			}
			
			// 地址
			List<IRow> oldAddressList = oldPoi.getAddresses();
			String oldAddressStr = "";
			IxPoiAddress oldAddressObjChi = new IxPoiAddress();
			for (IRow oldAddressObj:oldAddressList) {
				IxPoiAddress oldAddress = (IxPoiAddress)oldAddressObj;
				if (oldAddress.getLangCode().equals("CHI")) {
					oldAddressStr = oldAddress.getFullname();
					oldAddressObjChi = oldAddress;
					break;
				}
			}
			if (!poiJson.getString("oldAddress").isEmpty() || !oldAddressStr.isEmpty()) {
				if (oldAddressStr.isEmpty()) {
					JSONArray addressList = new JSONArray();
					JSONObject poiAddress = new JSONObject();
					int addressId = PidService.getInstance().applyPoiAddressId();
					poiAddress.put("objStatus", ObjStatus.INSERT.toString());
					poiAddress.put("pid", addressId);
					poiAddress.put("poiPid", pid);
					poiAddress.put("nameGroupid", 1);
					poiAddress.put("langCode", "CHI");
					poiAddress.put("fullname",poiJson.getString("oldAddress"));
					poiAddress.put("rowId", UuidUtils.genUuid());
					addressList.add(poiAddress);
					poiJson.put("addresses", addressList);
					// 鲜度验证
					freshFlag = false;
				} else if (!oldAddressStr.equals(poiJson.getString("oldAddress"))) {
					JSONArray addressList = new JSONArray();
					JSONObject poiAddress = new JSONObject();
					poiAddress.put("objStatus", ObjStatus.UPDATE.toString());
					poiAddress.put("pid", oldAddressObjChi.getPid());
					poiAddress.put("poiPid", oldAddressObjChi.getPoiPid());
					poiAddress.put("nameGroupid", oldAddressObjChi.getNameGroupid());
					poiAddress.put("langCode", oldAddressObjChi.getLangCode());
					poiAddress.put("fullname",poiJson.getString("oldAddress"));
					poiAddress.put("rowId", oldAddressObjChi.getRowId());
					addressList.add(poiAddress);
					poiJson.put("addresses", addressList);
					// 鲜度验证
					freshFlag = false;
				}
			}
			
			// 联系方式
			if (jo.containsKey("contacts")) {
				JSONArray contactsList = jo.getJSONArray("contacts");
				List<IRow> oldContactsList = oldPoi.getContacts();
				
				JSONArray oldArray = new JSONArray();
				for (IRow irow:oldContactsList) {
					IxPoiContact temp = (IxPoiContact)irow;
					oldArray.add(temp.Serialize(null));
				}
				
				JSONArray newContactArray = new JSONArray();
				
				List<String> newRowIdList = new ArrayList<String>();
				for (int k=0;k<contactsList.size();k++) {
					JSONObject contactObj = contactsList.getJSONObject(k);
					IxPoiContact contact = new IxPoiContact();
					newRowIdList.add(contactObj.getString("rowId"));
					contact.setPoiPid(pid);
					contact.setContactType(contactObj.getInt("type"));
					contact.setContact(contactObj.getString("number"));
					String linkman = contactObj.getString("linkman");
					String linkmanNum = "";
					if (linkman.indexOf("总机")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("客服")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("预订")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("销售")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("维修")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					if (linkman.indexOf("其他")>=0){
						linkmanNum = "1" + linkmanNum ;
					} else {
						linkmanNum = "0" + linkmanNum ;
					}
					int contactInt = Integer.parseInt(linkmanNum, 2);
					contact.setContactDepart(contactInt);
					contact.setPriority(contactObj.getInt("priority"));
					contact.setRowId(contactObj.getString("rowId"));
					
					JSONObject newContact = contact.Serialize(null);
					
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newContact);
					if (ret == 0) {
						newContact.put("objStatus", ObjStatus.INSERT.toString());
						newContactArray.add(newContact);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						newContact.put("objStatus", ObjStatus.UPDATE.toString());
						newContactArray.add(newContact);
						// 鲜度验证
						freshFlag = false;
					}
				}
				
				// 差分，区分删除的数据
				JSONArray oldDelJson = getOldDel(oldArray,newRowIdList);
				
				if (oldDelJson.size()>0) {
					// 鲜度验证
					freshFlag = false;
				}
				
				newContactArray.addAll(oldDelJson);
				
				poiJson.put("contacts", newContactArray);
			}
			
			// 照片
			JSONArray attachments = jo.getJSONArray("attachments");
			List<IRow> oldPhotoList = oldPoi.getPhotos();
			List<String> photoIdList = new ArrayList<String>();
			JSONArray photoList = new JSONArray();
			for (IRow oldPhotoIRow:oldPhotoList) {
				IxPoiPhoto oldPhoto = (IxPoiPhoto) oldPhotoIRow;
				photoIdList.add(oldPhoto.getPid());
			}
			for (int k=0;k<attachments.size();k++) {
				JSONObject photo = attachments.getJSONObject(k);
				
				int type = photo.getInt("type");
				if (type==1) {
					String photoId = photo.getString("id");
					if (!photoIdList.contains(photoId)) {
						JSONObject photoObj = new JSONObject();
						photoIdList.add(photoId);
						photoObj.put("objStatus", ObjStatus.INSERT.toString());
						photoObj.put("poiPid", pid);
						photoObj.put("pid", photoId);
						photoObj.put("tag", photo.getInt("tag"));
						photoObj.put("rowId", photoId);
						photoList.add(photoObj);
						// 鲜度验证
						freshFlag = false;
					}
				}
			}
			if (photoList.size()>0) {
				poiJson.put("photos", photoList);
			}
			
			// 父
			List<IRow> oldParentList = oldPoi.getParents();
			int groupId = 0;
			if (oldParentList.size()>0) {
				IRow oldParentIRow = oldParentList.get(0);
				IxPoiParent oldParent = (IxPoiParent)oldParentIRow;
				groupId = oldParent.getGroupId();
			} 
			if (!(jo.getJSONArray("relateChildren").size()>0) || !(oldParentList.size()>0)) {
				
				// 新增
				if (jo.getJSONArray("relateChildren").size()>0 && oldParentList.size()==0) {
					JSONObject parent = new JSONObject();
					JSONArray parentList = new JSONArray();
					groupId = PidService.getInstance().applyPoiGroupId();
					parent.put("objStatus", ObjStatus.INSERT.toString());
					parent.put("pid", groupId);
					parent.put("parentPoiPid", pid);
					parent.put("rowId", UuidUtils.genUuid());
					parentList.add(parent);
					poiJson.put("parent", parentList);
					// 鲜度验证
					freshFlag = false;
				}
				// 删除
				if (jo.getJSONArray("relateChildren").size()==0 && oldParentList.size()>0) {
					JSONObject parent = new JSONObject();
					JSONArray parentList = new JSONArray();
					IRow oldParentIRow = oldParentList.get(0);
					IxPoiParent oldParent = (IxPoiParent)oldParentIRow;
					parent.put("objStatus", ObjStatus.DELETE.toString());
					parent.put("pid", oldParent.getPid());
					parent.put("parentPoiPid", oldParent.getPid());
					parent.put("rowId", oldParent.getRowId());
					parentList.add(parent);
					poiJson.put("parent", parentList);
					// 鲜度验证
					freshFlag = false;
				}
			}
			
			// 子
			if (jo.containsKey("relateChildren")) {
				List<IRow> oldChildIRow = oldPoi.getChildren();
				JSONArray oldArray = new JSONArray();
				for (IRow oldChild:oldChildIRow) {
					IxPoiChildren oldPoiChild = (IxPoiChildren) oldChild;
					oldArray.add(oldPoiChild.Serialize(null));
				}
				JSONArray childrenArry = jo.getJSONArray("relateChildren");
				JSONArray newChildrenArray = new JSONArray();
				List<String> newRowIdList = new ArrayList<String>();
				for (int k=0;k<childrenArry.size();k++) {
					JSONObject children = childrenArry.getJSONObject(k);
					IxPoiChildren poiChildren = new IxPoiChildren();
					newRowIdList.add(children.getString("rowId"));
					poiChildren.setGroupId(groupId);
					poiChildren.setChildPoiPid(children.getInt("childPid"));
					poiChildren.setRelationType(children.getInt("type"));
					poiChildren.setRowId(children.getString("rowId"));
					
					JSONObject newChildren = poiChildren.Serialize(null);
					
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newChildren);
					if (ret == 0) {
						newChildren.put("objStatus", ObjStatus.INSERT.toString());
						newChildrenArray.add(newChildren);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						newChildren.put("objStatus", ObjStatus.UPDATE.toString());
						newChildrenArray.add(newChildren);
						// 鲜度验证
						freshFlag = false;
					}
				}
				
				// 差分，区分删除的数据
				JSONArray oldDelJson = getOldDel(oldArray,newRowIdList);
				
				if (oldDelJson.size()>0) {
					// 鲜度验证
					freshFlag = false;
				}
				
				newChildrenArray.addAll(oldDelJson);
				
				poiJson.put("children", newChildrenArray);
			}
			
			// 加油站
			if (jo.containsKey("gasStation")) {
				List<IRow> gasList = oldPoi.getGasstations();
				JSONObject gasObj = jo.getJSONObject("gasStation");
				JSONArray oldArray = new JSONArray();
				JSONArray newGasArray = new JSONArray();
				if (!gasObj.isEmpty()) {
					for (IRow oldGas:gasList) {
						IxPoiGasstation oldPoiGas = (IxPoiGasstation) oldGas;
						JSONObject oldPoiGasObj = oldPoiGas.Serialize(null);
						oldArray.add(oldPoiGasObj);
					}
					List<String> newRowIdList = new ArrayList<String>();
					newRowIdList.add(gasObj.getString("rowId"));
					IxPoiGasstation gasStation = new IxPoiGasstation();
					gasStation.setPoiPid(pid);
					gasStation.setServiceProv(gasObj.getString("servicePro"));
					gasStation.setFuelType(gasObj.getString("fuelType"));
					gasStation.setOilType(gasObj.getString("oilType"));
					gasStation.setEgType(gasObj.getString("egType"));
					gasStation.setMgType(gasObj.getString("mgType"));
					gasStation.setPayment(gasObj.getString("payment"));
					gasStation.setService(gasObj.getString("service"));
					gasStation.setOpenHour(gasObj.getString("openHour"));
					gasStation.setRowId(gasObj.getString("rowId"));
					JSONObject newGasStation = (JSONObject)gasStation.Serialize(null).remove("pid");
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newGasStation);
					if (ret == 0) {
						newGasStation.put("pid", PidService.getInstance().applyPoiGasstationId());
						newGasStation.put("objStatus", ObjStatus.INSERT.toString());
						newGasArray.add(newGasStation);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						String oldRowId = "";
						for (IRow oldGas:gasList) {
							IxPoiGasstation oldPoiGas = (IxPoiGasstation) oldGas;
							if (oldPoiGas.getRowId().equals(gasObj.getString("rowId"))) {
								oldRowId = oldPoiGas.getRowId();
								break;
							}
						}
						newGasStation.put("pid", oldRowId);
						newGasStation.put("objStatus", ObjStatus.UPDATE.toString());
						newGasArray.add(newGasStation);
						// 鲜度验证
						freshFlag = false;
					}
				}else if (gasList.size()>0) {
					// 删除的数据
					IxPoiGasstation oldPoiGas = (IxPoiGasstation)gasList.get(0);
					JSONObject oldDelJson = oldPoiGas.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());
					
					newGasArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}
				
				poiJson.put("gasstations", newGasArray);
			}
			
			// 停车场
			if (jo.containsKey("parkings")) {
				List<IRow> parkingsList = oldPoi.getParkings();
				JSONObject parkingsObj = jo.getJSONObject("parkings");
				JSONArray oldArray = new JSONArray();
				JSONArray newParkingsArray = new JSONArray();
				if (!parkingsObj.isEmpty()) {
					for (IRow oldParkings:parkingsList) {
						IxPoiParking oldPoiParkings = (IxPoiParking) oldParkings;
						JSONObject oldPoiParkingsObj = oldPoiParkings.Serialize(null);
						oldArray.add(oldPoiParkingsObj);
					}
					List<String> newRowIdList = new ArrayList<String>();
					newRowIdList.add(parkingsObj.getString("rowId"));
					IxPoiParking parkings = new IxPoiParking();
					parkings.setPoiPid(pid);
					parkings.setParkingType(parkingsObj.getString("buildingType"));
					parkings.setTollStd(parkingsObj.getString("tollStd"));
					parkings.setTollDes(parkingsObj.getString("tollDes"));
					parkings.setTollWay(parkingsObj.getString("tollWay"));
					parkings.setPayment(parkingsObj.getString("payment"));
					parkings.setRemark(parkingsObj.getString("remark"));
					parkings.setOpenTiime(parkingsObj.getString("openTime"));
					parkings.setTotalNum(parkingsObj.getInt("totalNum"));
					parkings.setResHigh(parkingsObj.getInt("resHigh"));
					parkings.setResWidth(parkingsObj.getInt("resWidth"));
					parkings.setResWeigh(parkingsObj.getInt("resWeigh"));
					parkings.setCertificate(parkingsObj.getInt("certificate"));
					parkings.setVehicle(parkingsObj.getInt("vehicle"));
					parkings.setHaveSpecialplace(parkingsObj.getString("haveSpecialPlace"));
					parkings.setWomenNum(parkingsObj.getInt("womenNum"));
					parkings.setHandicapNum(parkingsObj.getInt("handicapNum"));
					parkings.setMiniNum(parkingsObj.getInt("miniNum"));
					parkings.setVipNum(parkingsObj.getInt("vipNum"));
					parkings.setRowId(parkingsObj.getString("rowId"));
					JSONObject newParkings = (JSONObject) parkings.Serialize(null).remove("pid");
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newParkings);
					if (ret == 0) {
						newParkings.put("pid", PidService.getInstance().applyPoiParkingsId());
						newParkings.put("objStatus", ObjStatus.INSERT.toString());
						newParkingsArray.add(newParkings);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						String oldRowId = "";
						for (IRow oldParkings:parkingsList) {
							IxPoiParking oldPoiParkings = (IxPoiParking) oldParkings;
							if (oldPoiParkings.getRowId().equals(parkingsObj.getString("rowId"))) {
								oldRowId = oldPoiParkings.getRowId();
								break;
							}
						}
						newParkings.put("pid", oldRowId);
						newParkings.put("objStatus", ObjStatus.UPDATE.toString());
						newParkingsArray.add(newParkings);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (parkingsList.size()>0) {
					// 删除的数据
					IxPoiParking oldPoiParking = (IxPoiParking)parkingsList.get(0);
					JSONObject oldDelJson = oldPoiParking.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());
					
					newParkingsArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}
				
				poiJson.put("parkings", newParkingsArray);
			}
			
			// 酒店
			if (jo.containsKey("hotel")) {
				List<IRow> hotelList = oldPoi.getHotels();
				JSONObject hotelObj = jo.getJSONObject("hotel");
				JSONArray oldArray = new JSONArray();
				JSONArray newHotelArray = new JSONArray();
				if (!hotelObj.isEmpty()) {
					for (IRow oldHotel:hotelList) {
						IxPoiHotel oldPoiHotel = (IxPoiHotel) oldHotel;
						JSONObject oldPoiHotelObj = oldPoiHotel.Serialize(null);
						oldArray.add(oldPoiHotelObj);
					}
					List<String> newRowIdList = new ArrayList<String>();
					newRowIdList.add(hotelObj.getString("rowId"));
					IxPoiHotel hotel = new IxPoiHotel();
					hotel.setPoiPid(pid);
					hotel.setCreditCard(hotelObj.getString("creditCards"));
					hotel.setRating(hotelObj.getInt("rating"));
					hotel.setCheckinTime(hotelObj.getString("checkInTime"));
					hotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
					hotel.setRoomCount(hotelObj.getInt("roomCount"));
					hotel.setRoomType(hotelObj.getString("roomType"));
					hotel.setRoomPrice(hotelObj.getString("roomPrice"));
					hotel.setBreakfast(hotelObj.getInt("breakfast"));
					hotel.setService(hotelObj.getString("service"));
					hotel.setParking(hotelObj.getInt("parking"));
					hotel.setLongDescription(hotelObj.getString("description"));
					hotel.setOpenHour(hotelObj.getString("openHour"));
					hotel.setRowId(hotelObj.getString("rowId"));
					JSONObject newHotel = (JSONObject)hotel.Serialize(null).remove("pid");
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newHotel);
					if (ret == 0) {
						newHotel.put("pid", PidService.getInstance().applyPoiHotelId());
						newHotel.put("objStatus", ObjStatus.INSERT.toString());
						newHotelArray.add(newHotel);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						String oldRowId = "";
						for (IRow oldHotel:hotelList) {
							IxPoiHotel oldPoiHotel = (IxPoiHotel) oldHotel;
							if (oldPoiHotel.getRowId().equals(hotelObj.getString("rowId"))) {
								oldRowId = oldPoiHotel.getRowId();
								break;
							}
						}
						newHotel.put("pid", oldRowId);
						newHotel.put("objStatus", ObjStatus.UPDATE.toString());
						newHotelArray.add(newHotel);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (hotelList.size()>0) {
					// 删除的数据
					IxPoiHotel oldPoiHotel = (IxPoiHotel)hotelList.get(0);
					JSONObject oldDelJson = oldPoiHotel.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());
					
					newHotelArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}
				
				poiJson.put("hotels", newHotelArray);
			}
			
			// 餐馆
			if (jo.getJSONObject("foodtypes").size()>0) {
				List<IRow> foodtypeList = oldPoi.getRestaurants();
				JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
				JSONArray oldArray = new JSONArray();
				JSONArray newFoodtypeArray = new JSONArray();
				if (!foodtypesObj.isEmpty()) {
					for (IRow oldFoodtype:foodtypeList) {
						IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant) oldFoodtype;
						JSONObject oldPoiFoodtypeObj = oldPoiFoodtype.Serialize(null);
						oldArray.add(oldPoiFoodtypeObj);
					}
					List<String> newRowIdList = new ArrayList<String>();
					newRowIdList.add(foodtypesObj.getString("rowId"));
					IxPoiRestaurant foodtypes = new IxPoiRestaurant();
					foodtypes.setPoiPid(pid);
					foodtypes.setFoodType(foodtypesObj.getString("foodtype"));
					foodtypes.setCreditCard(foodtypesObj.getString("creditCards"));
					foodtypes.setAvgCost(foodtypesObj.getInt("avgCost"));
					foodtypes.setParking(foodtypesObj.getInt("parking"));
					foodtypes.setOpenHour(foodtypesObj.getString("openHour"));
					foodtypes.setRowId(foodtypesObj.getString("rowId"));
					JSONObject newFoodtype = foodtypes.Serialize(null);
					// 差分,区分新增修改
					int ret = getDifferent(oldArray,newFoodtype);
					if (ret == 0) {
						newFoodtype.put("pid", PidService.getInstance().applyPoiFoodId());
						newFoodtype.put("objStatus", ObjStatus.INSERT.toString());
						newFoodtypeArray.add(newFoodtype);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						String oldRowId = "";
						for (IRow oldFoodtype:foodtypeList) {
							IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant) oldFoodtype;
							if (oldPoiFoodtype.getRowId().equals(foodtypesObj.getString("rowId"))) {
								oldRowId = oldPoiFoodtype.getRowId();
								break;
							}
						}
						newFoodtype.put("pid", oldRowId);
						newFoodtype.put("objStatus", ObjStatus.UPDATE.toString());
						newFoodtypeArray.add(newFoodtype);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (foodtypeList.size()>0) {
					// 删除的数据
					IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant)foodtypeList.get(0);
					JSONObject oldDelJson = oldPoiFoodtype.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());
					
					newFoodtypeArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}
				
				poiJson.put("restaurants", newFoodtypeArray);

			}
			
			retObj.put("flag", 1);
			retObj.put("ret", poiJson);
			retObj.put("freshFlag", freshFlag);
		} catch (Exception e) {
			retObj.put("flag", 0);
			JSONObject errObj = new JSONObject();
			errObj.put("fid", fid);
			String errStr = e.getMessage();
			if (errStr == null) {
				errStr = "";
			}
			errObj.put("reason", errStr);
			retObj.put("ret", errObj);
		}
		
		return retObj;
	}

	// 差分,区分新增修改
	@SuppressWarnings("unchecked")
	private int getDifferent(JSONArray oldArray, JSONObject newObj) throws Exception {
		try {
			int ret = 0;
			boolean theSame = false;
			boolean change = false;
			for (int i=0;i<oldArray.size();i++) {
				String newRowid = newObj.getString("rowId");
				JSONObject old = oldArray.getJSONObject(i);
				if (old.getString("rowId").equals(newRowid)) {
					theSame = true;
					// rowid相同，有其他字段不同的情况下，为修改，返回1；
					// 没有rowid相同的，是新增，返回0
					Iterator<String> it = newObj.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						if (!key.equals("uRecord") && !key.equals("uDate") && !key.equals("pid")) {
							boolean flag = old.getString(key).equals(newObj.getString(key));
							if (flag) {
								continue;
							} else {
								// 修改
								ret = 1;
								change = true;
								break;
							}
						}
						
					}
				} 
			}
			// rowid相同,但无修改字段，不处理，返回2
			if (theSame && !change) {
				ret = 2;
			}
			return ret;
		} catch (Exception e) {
			throw e;
		}
	}
	
	// 差分，区分删除的数据
	private JSONArray getOldDel(JSONArray oldArray, List<String> newRowIdList) throws Exception {
		try {
			JSONArray retArray = new JSONArray();
			for (int i=0;i<oldArray.size();i++) {
				boolean flag = true;
				JSONObject jsonObj = oldArray.getJSONObject(i);
				for (String rowid:newRowIdList) {
					if (rowid.equals(jsonObj.getString("rowId"))) {
						flag = false;
						break;
					}
				}
				// 未找到rowid，为删除数据
				if (flag) {
					jsonObj.put("objStatus", ObjStatus.DELETE.toString());
					retArray.add(jsonObj);
				}
			}
			return retArray;
		} catch (Exception e) {
			throw e;
		}
	}

	
	public JSONObject getUploadInfo(int jobId) throws Exception {

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();

			JSONObject json = new JSONObject();

			String sql = "select * from dropbox_upload where upload_id = :1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, jobId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				String fileName = resultSet.getString("file_name");

				String filePath = resultSet.getString("file_path");

				String md5 = resultSet.getString("file_md5");

				json.put("fileName", fileName);

				json.put("filePath", filePath);

				json.put("md5", md5);

			} else {
				throw new Exception("不存在对应的jobid:" + jobId);
			}

			return json;

		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
}
