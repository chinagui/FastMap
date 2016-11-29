package com.navinfo.dataservice.control.app.upload;

import java.io.FileInputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
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
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
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
	
	private EditApi apiService;
	private QueryRunner runn;
	private Long userId;
	
	protected Logger log = Logger.getLogger(UploadOperation.class);
	
	public UploadOperation(Long userId) {
		this.apiService=(EditApi) ApplicationContextUtil.getBean("editApi");
		runn = new QueryRunner();
		this.userId = userId;
	}
	
	/**
	 * 读取txt，解析，入库
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public JSONObject importPoi(String fileName) throws Exception {
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
	private JSONObject changeData(JSONArray ja) throws Exception {
		JSONObject retObj = new JSONObject();
		List<String> errList = new ArrayList<String>();
		Connection manConn = null;
		Connection conn = null;
		// 获取当前做业季
		String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			MultiMap gridDataMapping = new MultiValueMap();
			for (int i = 0; i < ja.size(); i++) {
				JSONObject jo = ja.getJSONObject(i);

				// 坐标确定grid，grid确定区库ID
				String wkt = jo.getString("geometry");
				Geometry point = new WKTReader().read(wkt);
				Coordinate[] coordinate = point.getCoordinates();
				CompGridUtil gridUtil = new CompGridUtil();
				String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
				gridDataMapping.put(grid, jo);
				
			}
			log.info("计算dbid和上传数据的对应关系");
			MultiMap dbDataMapping = new MultiValueMap();//dbid--jsonlist 对应map
			for (Object grid :gridDataMapping.keySet()){
				String  dbId = calDbDataMapping( manConn, grid.toString());
				log.info("gridId:"+grid+",dbId:"+dbId);
				if (dbId.isEmpty()) {
					JSONObject errObj = new JSONObject();
					errObj.put("reason", "通过poi坐标计算出来的grid："+grid+",无法查询得到对应的大区库");
					errObj.put("pois", gridDataMapping.get(grid));
					errList.add(errObj.toString());
					continue;
				}
				for (Object data: (List)gridDataMapping.get(grid)) {
					dbDataMapping.put(dbId,data);
				}
				
			}
			JSONObject insertObj = new JSONObject();
			JSONObject updateObj = new JSONObject();
			JSONObject deleteObj = new JSONObject();
			//每个db中计算出上传poi对应的fid，lifecycle，urecord，pid
			for(Iterator<String> iter = dbDataMapping.keySet().iterator(); iter.hasNext();){
				String dbId = iter.next();
				List<JSONObject>pois = (List<JSONObject>) dbDataMapping.get(dbId);
				Map<String,PoiWrap> fidPoiMap = new HashMap<String,PoiWrap>();
				for(JSONObject poiJson:pois){
					String fid = poiJson.getString("fid");
					int lifecycle = poiJson.getInt("t_lifecycle");
					PoiWrap poiWrap = new PoiWrap(fid,lifecycle,poiJson);
					fidPoiMap.put(fid, poiWrap);
				}
				log.info("开始计算dbid:"+dbId+",中的数据record和pid");
				calPoiFromDbByFids(conn, dbId,fidPoiMap);
				log.info("把db中的数据，分为增删改");
				List<JSONObject> insertList = new ArrayList<JSONObject>();
				List<JSONObject> updateList = new ArrayList<JSONObject>();
				List<JSONObject> deleteList = new ArrayList<JSONObject>();
				for(String fid:fidPoiMap.keySet()){
					// 判断每一条数据是新增、修改还是删除
					PoiWrap poiWrap = fidPoiMap.get(fid);
					JSONObject poi = poiWrap.getPoiJson();
					int uRecord = poiWrap.getuRecord();
					int lifecycle = poiWrap.getLifecycle();
					if (uRecord != -1) {
						// 能找到，判断lifecycle和u_record
						poi.put("pid", poiWrap.getPid());
						if (lifecycle == 1) {
							deleteList.add(poi);
						} else {
							if (uRecord == 2) {
								JSONObject errObj = new JSONObject();
								errObj.put("fid", fid);
								String errStr = "数据为修改数据，但库中u_record为2";
								errObj.put("reason", errStr);
								errList.add(errObj.toString());
							} else {
								updateList.add(poi);
							}
						}
					} else {
						// 找不到，判断lifecycle是否为1
						if (lifecycle == 1) {
							JSONObject errObj = new JSONObject();
							errObj.put("fid", fid);
							String errStr = "数据在库中找不到对应数据，但lifecycle为1";
							errObj.put("reason", errStr);
							errList.add(errObj.toString());
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
			log.info("增数据入库处理"); 
			JSONObject insertRet = insertData(insertObj, version);
			log.info("改数据入库处理"); 
			JSONObject updateRet = updateData(updateObj, version);
			log.info("删数据入库处理"); 
			JSONObject deleteRet = deleteData(deleteObj);

			int insertCount = insertRet.getInt("success");
			int updateCount = updateRet.getInt("success");
			int deleteCount = deleteRet.getInt("success");

			int total = insertCount + updateCount + deleteCount;
			log.info("处理结果:total"+total+",insert:"+insertCount+",update:"+updateCount+",delete:"+deleteCount); 

			@SuppressWarnings("unchecked")
			List<String> insertFail = (List<String>) insertRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> updateFail = (List<String>) updateRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> deleteFail = (List<String>) deleteRet.get("fail");

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

	private void calPoiFromDbByFids(Connection conn, String dbId,Map<String,PoiWrap>fidPoiMap) throws Exception {
		if (conn != null) {
			DBUtils.closeConnection(conn);
		}
		conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Clob fidClod = null;
		try {
			fidClod = ConnectionUtil.createClob(conn);
			String fidSeq = org.apache.commons.lang.StringUtils.join(fidPoiMap.keySet(), ",");
			log.info("fidSeq"+fidSeq);
			fidClod.setString(1, fidSeq);
			stmt = conn.prepareStatement("SELECT poi_num,u_record,pid FROM ix_poi WHERE poi_num in( select column_value from table(clob_to_table(?)))");
			stmt.setClob(1, fidClod);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int uRecord = rs.getInt("u_record");
				int pid = rs.getInt("pid");
				String fid = rs.getString("poi_num");
				PoiWrap poiWrap = fidPoiMap.get(fid);
				poiWrap.setPid(pid);
				poiWrap.setuRecord(uRecord);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}
	
	private String calDbDataMapping( Connection manConn,String grid) throws SQLException {
		String manQuery = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
		QueryRunner qRunner = new QueryRunner();
		String dbId = qRunner.queryForString(manConn, manQuery, grid);
		return dbId;
		
	}

	// 处理新增数据
	@SuppressWarnings("unchecked")
	private JSONObject insertData(JSONObject insertObj, String version) throws Exception {
		JSONObject retObj = new JSONObject();
		int count = 0;
		List<JSONObject> errList = new ArrayList<JSONObject>();
		try {
			for (Iterator<String> iter = insertObj.keySet().iterator(); iter.hasNext();) {
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) insertObj.get(dbId);
					JSONObject relateParentChildren = new JSONObject();
					for (int i = 0; i < poiList.size(); i++) {
						JSONObject jo = poiList.get(i);
						JSONObject perRetObj = obj2PoiForInsert(jo, version);
						int flag = perRetObj.getInt("flag");
						if (flag == 1) {
							try{
								JSONObject poiObj = perRetObj.getJSONObject("ret");
								if (perRetObj.containsKey("relate")) {
									relateParentChildren.put(poiObj.getString("pid"), perRetObj.getJSONArray("relate"));
								}
								JSONObject json = new JSONObject();
								
								json.put("dbId", dbId);
								json.put("objId", poiObj.getInt("pid"));
								json.put("command", "CREATE");
								json.put("type", "IXPOIUPLOAD");
								json.put("data", poiObj);
								// 调用一次插入
								apiService.run(json);
								apiService.runBatch(json);
								
								// 鲜度验证，POI状态更新
								String rawFields = jo.getString("rawFields");
								upatePoiStatusForAndroid(conn, 0, rawFields,1,poiObj.getInt("pid"));

								conn.commit();
								count++;
							}catch(Exception e){
								DbUtils.rollback(conn);
								log.warn("POI入库错误："+jo.toString());
								log.error(e.getMessage(),e);
								JSONObject errObj = new JSONObject();
								errObj.put("fid", jo.getString("fid"));
								errObj.put("reason", e.getMessage());
								errList.add(errObj);
							}
						} else if (flag == 0) {
							errList.add(perRetObj.getJSONObject("ret"));
						}

					}

					// 最后统一处理父子关系20161011
					if (relateParentChildren.size()>0) {
						insetParent(conn,relateParentChildren);
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
	private JSONObject updateData(JSONObject updateObj, String version) throws Exception {
		JSONObject retObj = new JSONObject();
		int count = 0;
		List<JSONObject> errList = new ArrayList<JSONObject>();
		try {
			for (Iterator<String> iter = updateObj.keySet().iterator(); iter.hasNext();) {
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) updateObj.get(dbId);
					for (int i = 0; i < poiList.size(); i++) {
						JSONObject jo = poiList.get(i);
						JSONObject perRetObj = obj2PoiForUpdate(jo, version, conn);
						int flag = perRetObj.getInt("flag");
						if (flag == 1) {
							try{
								JSONObject poiJson = perRetObj.getJSONObject("ret");
								JSONObject relateParentChildren = new JSONObject();
								if (perRetObj.getJSONObject("relate").size()>0) {
									relateParentChildren.put(poiJson.getString("pid"), perRetObj.getJSONObject("relate"));
								}
								JSONObject commandJson = new JSONObject();
								commandJson.put("dbId", Integer.parseInt(dbId));
								commandJson.put("data", poiJson);
								commandJson.put("objId", poiJson.getInt("pid"));
								commandJson.put("command", "UPDATE");
								commandJson.put("type", "IXPOIUPLOAD");
								// 调用一次更新
								apiService.run(commandJson);
								apiService.runBatch(commandJson);
								
								// 处理一个库中的父子关系20161011
								if (relateParentChildren.size()>0) {
									updateParent(conn,relateParentChildren);
								}

								// 鲜度验证，POI状态更新
								boolean freshFlag = perRetObj.getBoolean("freshFlag");
								String rawFields = jo.getString("rawFields");
								if (freshFlag) {
									upatePoiStatusForAndroid(conn, 1, rawFields,1,poiJson.getInt("pid"));
									
								} else {
									upatePoiStatusForAndroid(conn, 0, rawFields,1,poiJson.getInt("pid"));
								}
								EditApiImpl editApiImpl = new EditApiImpl(conn);
								editApiImpl.updatePoifreshVerified(poiJson.getInt("pid"),"andriod");
								
								conn.commit();
								count++;
							}catch(Exception e){
								DbUtils.rollback(conn);
								log.warn("POI入库错误："+jo.toString());
								log.error(e.getMessage(),e);
								JSONObject errObj = new JSONObject();
								errObj.put("fid", jo.getString("fid"));
								errObj.put("reason", e.getMessage());
								errList.add(errObj);
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
			for (Iterator<String> iter = deleteObj.keySet().iterator(); iter.hasNext();) {
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				try {
					List<JSONObject> poiList = (List<JSONObject>) deleteObj.get(dbId);
					for (int i = 0; i < poiList.size(); i++) {
						JSONObject jo = poiList.get(i);
						int pid = jo.getInt("pid");
						String fid = jo.getString("fid");
						JSONObject poiObj = new JSONObject();
						poiObj.put("dbId", dbId);
						poiObj.put("objId", pid);
						poiObj.put("command", "DELETE");
						poiObj.put("type", "IXPOIUPLOAD");
						try {
							// 调用一次删除
							apiService.run(poiObj);

							// 鲜度验证，POI状态更新
							String rawFields = jo.getString("rawFields");
							IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
							//JSONObject poiRowId = ixPoiSelector.getRowIdById(pid);
							upatePoiStatusForAndroid(conn, 0, rawFields,1,pid);

							conn.commit();
							count++;
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
	 * 
	 * @param jo
	 * @param version
	 * @return
	 * @throws Exception
	 */
	private JSONObject obj2PoiForInsert(JSONObject jo, String version) throws Exception {
		IxPoi poi = new IxPoi();
		String fid = jo.getString("fid");
		JSONObject retObj = new JSONObject();
		try {
			// POI主表
			int pid = PidUtil.getInstance().applyPoiPid();
			log.debug("apply pid:"+pid+" for to be inserted fid:"+fid);
			poi.setPid(pid);
			poi.setKindCode(jo.getString("kindCode"));
			// geometry按SDO_GEOMETRY格式原值转出
			Geometry geometry = new WKTReader().read(jo.getString("geometry"));
			poi.setGeometry(GeoTranslator.transform(geometry, 100000, 5));
			if (jo.getJSONObject("guide").size() > 0) {
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
			// 如果KIND_CODE有修改，则追加“改种别代码”；
			// 如果CHAIN有修改，则追加“改连锁品牌”；
			// 如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
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
			if (fieldState.length() > 0) {
				fieldState = fieldState.substring(0, fieldState.length() - 1);
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
			if (outDoorLog.length() > 0) {
				outDoorLog = outDoorLog.substring(0, outDoorLog.length() - 1);
			}
			poi.setLog(outDoorLog);
			poi.setSportsVenue(jo.getString("sportsVenues"));

			JSONObject indoor = jo.getJSONObject("indoor");
			if (!indoor.isNullObject() && indoor.has("type")) {
				if (indoor.getInt("type") == 3) {
					poi.setIndoor(1);
				} else {
					poi.setIndoor(0);
				}
				
			} else {
				poi.setIndoor(0);
			}

			poi.setVipFlag(jo.getString("vipFlag"));
			poi.setuRecord(1);

			poi.setRowId(UuidUtils.genUuid().toUpperCase());
			
			// 新增卡车标识20160927
			poi.setTruckFlag(jo.getInt("truck"));

			// 名称
			if (!poi.getOldName().isEmpty()) {
				List<IRow> nameList = new ArrayList<IRow>();
				IxPoiName poiName = new IxPoiName();
				int nameId = PidUtil.getInstance().applyPoiNameId();
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
				int addressId = PidUtil.getInstance().applyPoiAddressId();
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
			if (jo.getJSONArray("contacts").size() > 0) {
				JSONArray contactsList = jo.getJSONArray("contacts");
				List<IRow> contacts = new ArrayList<IRow>();
				for (int k = 0; k < contactsList.size(); k++) {
					JSONObject contactObj = contactsList.getJSONObject(k);
					IxPoiContact contact = new IxPoiContact();
					contact.setPoiPid(pid);
					contact.setContactType(contactObj.getInt("type"));
					contact.setContact(contactObj.getString("number"));
					String linkman = contactObj.getString("linkman");
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
					contact.setContactDepart(contactInt);
					contact.setPriority(contactObj.getInt("priority"));
					contact.setRowId(contactObj.getString("rowId"));
					contacts.add(contact);
				}
				poi.setContacts(contacts);
			}

			// 新增POI_MEMO录入20160927
			String poiMemo = "";
			// 照片
			JSONArray attachments = jo.getJSONArray("attachments");
			List<String> photoIdList = new ArrayList<String>();
			List<IRow> photoList = new ArrayList<IRow>();
			for (int k = 0; k < attachments.size(); k++) {
				JSONObject photo = attachments.getJSONObject(k);
				int type = photo.getInt("type");
				if (type == 1) {
					String photoId = photo.getString("id");
					if (!photoIdList.contains(photoId)) {
						photoIdList.add(photoId);
						IxPoiPhoto poiPhoto = new IxPoiPhoto();
						poiPhoto.setPoiPid(pid);
						poiPhoto.setFccPid(photoId);
						poiPhoto.setTag(photo.getInt("tag"));
						poiPhoto.setRowId(photoId);
						photoList.add(poiPhoto);
					}
				} else if (type == 3) {
					poiMemo = photo.getString("content");
				}
			}
			poi.setPoiMemo(poiMemo);
			if (photoList.size() > 0) {
				poi.setPhotos(photoList);
			}

			// 父子关系
			if (jo.getJSONArray("relateChildren").size() > 0) {
				retObj.put("relate", jo.getJSONArray("relateChildren"));
			}

			// 加油站
			if (jo.getJSONObject("gasStation").size() > 0) {
				JSONObject gasObj = jo.getJSONObject("gasStation");
				IxPoiGasstation gasStation = new IxPoiGasstation();
				List<IRow> gasList = new ArrayList<IRow>();
				gasStation.setPid(PidUtil.getInstance().applyPoiGasstationId());
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
			if (jo.getJSONObject("parkings").size() > 0) {
				JSONObject parkingsObj = jo.getJSONObject("parkings");
				IxPoiParking parkings = new IxPoiParking();
				List<IRow> parkingsList = new ArrayList<IRow>();
				parkings.setPid(PidUtil.getInstance().applyPoiParkingsId());
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
				parkings.setVehicle(parkingsObj.getLong("vehicle"));
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
			if (jo.getJSONObject("hotel").size() > 0) {
				JSONObject hotelObj = jo.getJSONObject("hotel");
				IxPoiHotel hotel = new IxPoiHotel();
				List<IRow> hotelList = new ArrayList<IRow>();
				hotel.setPid(PidUtil.getInstance().applyPoiHotelId());
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
			if (jo.getJSONObject("foodtypes").size() > 0) {
				JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
				IxPoiRestaurant foodtypes = new IxPoiRestaurant();
				List<IRow> foodtypesList = new ArrayList<IRow>();
				foodtypes.setPid(PidUtil.getInstance().applyPoiRestaurantId());
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
			
			// 充电站
			if (jo.getJSONObject("chargingStation").size() > 0) {
				JSONObject chargingStationObj = jo.getJSONObject("chargingStation");
				IxPoiChargingStation chargingStation = new IxPoiChargingStation();
				List<IRow> chargingStationList = new ArrayList<IRow>();
				chargingStation.setPid(PidUtil.getInstance().applyPoiChargingstationId());
				chargingStation.setPoiPid(pid);
				chargingStation.setChargingType(chargingStationObj.getInt("type"));
				chargingStation.setChangeBrands(chargingStationObj.getString("changeBrands"));
				chargingStation.setChangeOpenType(chargingStationObj.getString("changeOpenType"));
				chargingStation.setChargingNum(chargingStationObj.getInt("chargingNum"));
				chargingStation.setServiceProv(chargingStationObj.getString("servicePro"));
				chargingStation.setOpenHour(chargingStationObj.getString("openHour"));
				chargingStation.setParkingFees(chargingStationObj.getInt("parkingFees"));
				chargingStation.setParkingInfo(chargingStationObj.getString("parkingInfo"));
				chargingStation.setAvailableState(chargingStationObj.getInt("availableState"));
				chargingStation.setRowId(chargingStationObj.getString("rowId"));
				chargingStationList.add(chargingStation);
				poi.setChargingstations(chargingStationList);
			}
			
			// 充电桩
			if (jo.getJSONArray("chargingPole").size() > 0) {
				JSONArray chargingPoleArray = jo.getJSONArray("chargingPole");
				List<IRow> chargingPoleList = new ArrayList<IRow>();
				for (int i=0;i<chargingPoleArray.size();i++) {
					JSONObject chargingPoleObj = chargingPoleArray.getJSONObject(i);
					IxPoiChargingPlot chargingPole = new IxPoiChargingPlot();
					chargingPole.setPoiPid(pid);
					chargingPole.setGroupId(chargingPoleObj.getInt("groupId"));
					chargingPole.setCount(chargingPoleObj.getInt("count"));
					chargingPole.setAcdc(chargingPoleObj.getInt("acdc"));
					chargingPole.setPlugType(chargingPoleObj.getString("plugType"));
					chargingPole.setPower(chargingPoleObj.getString("power"));
					chargingPole.setVoltage(chargingPoleObj.getString("voltage"));
					chargingPole.setCurrent(chargingPoleObj.getString("current"));
					chargingPole.setMode(chargingPoleObj.getInt("mode"));
					chargingPole.setPlugNum(chargingPoleObj.getInt("plugNum"));
					chargingPole.setPrices(chargingPoleObj.getString("prices"));
					chargingPole.setOpenType(chargingPoleObj.getString("openType"));
					chargingPole.setAvailableState(chargingPoleObj.getInt("availableState"));
					chargingPole.setManufacturer(chargingPoleObj.getString("manufacturer"));
					chargingPole.setFactoryNum(chargingPoleObj.getString("factoryNum"));
					chargingPole.setPlotNum(chargingPoleObj.getString("plotNum"));
					chargingPole.setProductNum(chargingPoleObj.getString("productNum"));
					chargingPole.setParkingNum(chargingPoleObj.getString("parkingNum"));
					chargingPole.setFloor(chargingPoleObj.getInt("floor"));
					chargingPole.setLocationType(chargingPoleObj.getInt("locationType"));
					chargingPole.setPayment(chargingPoleObj.getString("payment"));
					chargingPole.setRowId(chargingPoleObj.getString("rowId"));
					chargingPoleList.add(chargingPole);
				}
				poi.setChargingplots(chargingPoleList);
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
	 * 更新数据解析
	 * 
	 * @param jo
	 * @param version
	 * @param conn
	 * @return
	 */
	private JSONObject obj2PoiForUpdate(JSONObject jo, String version, Connection conn) {
		String fid = jo.getString("fid");
		JSONObject retObj = new JSONObject();
		boolean freshFlag = true;
		try {
			int pid = jo.getInt("pid");
			// 查出旧的POI
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi oldPoi = (IxPoi) ixPoiSelector.loadById(pid, false);
			// parent子表信息需要单独查询20161011
			List<IRow> oldParentArray = new ArrayList<IRow>();
			IxPoiParent oldParentObj = getParentByPid(pid,conn);
			if (oldParentObj != null) {
				oldParentArray.add(oldParentObj);
			}
			oldPoi.setParents(oldParentArray);
			
			// POI主表
			JSONObject poiJson = new JSONObject();
			poiJson.put("pid", pid);
			poiJson.put("kindCode", jo.getString("kindCode"));
			// geometry按SDO_GEOMETRY格式原值转出
			Geometry geometry = new WKTReader().read(jo.getString("geometry"));
			JSONObject geometryObj = GeoTranslator.jts2Geojson(geometry);
			poiJson.put("geometry", geometryObj);
			if (jo.getJSONObject("guide").size() > 0) {
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
			// 如果KIND_CODE有修改，则追加“改种别代码”；
			// 如果CHAIN有修改，则追加“改连锁品牌”；
			// 如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
			String fieldState = "";
			if (!jo.getString("kindCode").equals(oldPoi.getKindCode())) {
				fieldState += "改种别代码|";
			}
			if (!jo.getString("chain").equals(oldPoi.getChain())) {
				fieldState += "改连锁品牌|";
			}
			List<IRow> hotelListIRow = oldPoi.getHotels();
			IxPoiHotel hotelOld = null;
			if (hotelListIRow.size() > 0) {
				hotelOld = (IxPoiHotel) hotelListIRow.get(0);
			}

			if (jo.has("hotel")) {
				JSONObject hotel = jo.getJSONObject("hotel");

				if (!hotel.isNullObject() && hotelOld == null) {
					fieldState += "改酒店星级|";
				} else if (hotel.isNullObject() && hotelOld != null) {
					fieldState += "改酒店星级|";
				} else if (!hotel.isNullObject()) {

					if (hotel == null || hotel.getInt("rating") != hotelOld.getRating()) {
						fieldState += "改酒店星级|";
					}
				}
			}

			if (fieldState.length() > 0) {
				fieldState = fieldState.substring(0, fieldState.length() - 1);
			}
			poiJson.put("fieldState", fieldState);
			poiJson.put("oldName", jo.getString("name"));
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
			if (outDoorLog.length() > 0) {
				outDoorLog = outDoorLog.substring(0, outDoorLog.length() - 1);
			}
			poiJson.put("log", outDoorLog);
			poiJson.put("sportsVenue", jo.getString("sportsVenues"));

			JSONObject indoor = jo.getJSONObject("indoor");
			if (!indoor.isNullObject() && indoor.has("type")) {
				if (indoor.getInt("type") == 3) {
					poiJson.put("indoor", 1);
				} else {
					poiJson.put("indoor", 0);
				}
				
			} else {
				poiJson.put("indoor", 0);
			}
			poiJson.put("vipFlag", jo.getString("vipFlag"));
			poiJson.put("rowId", oldPoi.getRowId());

			poiJson.put("objStatus", ObjStatus.UPDATE.toString());
			
			// 新增卡车标识20160927
			poiJson.put("truckFlag",jo.getInt("truck"));
			
			// 新增poiMemo字段录入20160927
			String poiMemo = "";
			// 照片
			JSONArray attachments = jo.getJSONArray("attachments");
			List<IRow> oldPhotoList = oldPoi.getPhotos();
			List<String> photoIdList = new ArrayList<String>();
			JSONArray photoList = new JSONArray();
			for (IRow oldPhotoIRow : oldPhotoList) {
				IxPoiPhoto oldPhoto = (IxPoiPhoto) oldPhotoIRow;
				photoIdList.add(oldPhoto.getFccPid());
			}
			for (int k = 0; k < attachments.size(); k++) {
				JSONObject photo = attachments.getJSONObject(k);
	
				int type = photo.getInt("type");
				if (type == 1) {
					String photoId = photo.getString("id").toUpperCase();
					if (!photoIdList.contains(photoId)) {
						JSONObject photoObj = new JSONObject();
						photoIdList.add(photoId);
						photoObj.put("objStatus", ObjStatus.INSERT.toString());
						photoObj.put("poiPid", pid);
						photoObj.put("fccPid", photoId);
						photoObj.put("tag", photo.getInt("tag"));
						photoObj.put("rowId", photoId);
						photoList.add(photoObj);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (type == 3) {
					poiMemo = photo.getString("content");
				}
			}
			if (photoList.size() > 0) {
				poiJson.put("photos", photoList);
			}
			poiJson.put("poiMemo", poiMemo);

			// 鲜度验证
			if (oldPoi.fillChangeFields(poiJson)) {
				freshFlag = false;
			}

			// 名称
			List<IRow> oldNameList = oldPoi.getNames();
			String oldNameStr = "";
			IxPoiName oldNameObjChi = new IxPoiName();
			for (IRow oldNameObj : oldNameList) {
				IxPoiName oldName = (IxPoiName) oldNameObj;
				if (oldName.getNameClass() == 1 && oldName.getNameType() == 2 && oldName.getLangCode().equals("CHI")) {
					oldNameStr = oldName.getName();
					oldNameObjChi = oldName;
					break;
				}
			}
			if (!poiJson.getString("oldName").isEmpty() || !oldNameStr.isEmpty()) {
				if (oldNameStr.isEmpty()) {
					JSONArray nameList = new JSONArray();
					JSONObject poiName = new JSONObject();
					int nameId = PidUtil.getInstance().applyPoiNameId();
					poiName.put("objStatus", ObjStatus.INSERT.toString());
					poiName.put("pid", nameId);
					poiName.put("poiPid", pid);
					poiName.put("nameGroupid", 1);
					poiName.put("langCode", "CHI");
					poiName.put("nameClass", 1);
					poiName.put("nameType", 2);
					poiName.put("name", poiJson.getString("oldName"));
					poiName.put("rowId", UuidUtils.genUuid());
					nameList.add(poiName);
					poiJson.put("names", nameList);
					// 鲜度验证
					freshFlag = false;
				} else if (!oldNameStr.equals(poiJson.getString("oldName")) && !StringUtils.isEmpty(poiJson.getString("oldName"))) {
					JSONArray nameList = new JSONArray();
					JSONObject poiName = new JSONObject();
					poiName.put("objStatus", ObjStatus.UPDATE.toString());
					poiName.put("pid", oldNameObjChi.getPid());
					poiName.put("poiPid", oldNameObjChi.getPoiPid());
					poiName.put("nameGroupid", oldNameObjChi.getNameGroupid());
					poiName.put("langCode", oldNameObjChi.getLangCode());
					poiName.put("nameClass", oldNameObjChi.getNameClass());
					poiName.put("nameType", oldNameObjChi.getNameType());
					poiName.put("name", poiJson.getString("oldName"));
					poiName.put("rowId", oldNameObjChi.getRowId());
					nameList.add(poiName);
					poiJson.put("names", nameList);
					// 鲜度验证
					freshFlag = false;
				} else if (StringUtils.isEmpty(poiJson.getString("oldName")) && !StringUtils.isEmpty(oldNameStr)) {
					JSONArray nameList = new JSONArray();
					JSONObject poiName = new JSONObject();
					poiName.put("objStatus", ObjStatus.DELETE.toString());
					poiName.put("pid", oldNameObjChi.getPid());
					poiName.put("poiPid", oldNameObjChi.getPoiPid());
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
			for (IRow oldAddressObj : oldAddressList) {
				IxPoiAddress oldAddress = (IxPoiAddress) oldAddressObj;
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
					int addressId = PidUtil.getInstance().applyPoiAddressId();
					poiAddress.put("objStatus", ObjStatus.INSERT.toString());
					poiAddress.put("pid", addressId);
					poiAddress.put("poiPid", pid);
					poiAddress.put("nameGroupid", 1);
					poiAddress.put("langCode", "CHI");
					poiAddress.put("fullname", poiJson.getString("oldAddress"));
					poiAddress.put("rowId", UuidUtils.genUuid());
					addressList.add(poiAddress);
					poiJson.put("addresses", addressList);
					// 鲜度验证
					freshFlag = false;
				} else if (!oldAddressStr.equals(poiJson.getString("oldAddress")) && !StringUtils.isEmpty(poiJson.getString("oldAddress"))) {
					JSONArray addressList = new JSONArray();
					JSONObject poiAddress = new JSONObject();
					poiAddress.put("objStatus", ObjStatus.UPDATE.toString());
					poiAddress.put("pid", oldAddressObjChi.getPid());
					poiAddress.put("poiPid", oldAddressObjChi.getPoiPid());
					poiAddress.put("nameGroupid", oldAddressObjChi.getNameGroupid());
					poiAddress.put("langCode", oldAddressObjChi.getLangCode());
					poiAddress.put("fullname", poiJson.getString("oldAddress"));
					poiAddress.put("rowId", oldAddressObjChi.getRowId());
					addressList.add(poiAddress);
					poiJson.put("addresses", addressList);
					// 鲜度验证
					freshFlag = false;
				} else if (StringUtils.isEmpty(poiJson.getString("oldAddress")) && !StringUtils.isEmpty(oldAddressStr)) {
					JSONArray addressList = new JSONArray();
					JSONObject poiAddress = new JSONObject();
					poiAddress.put("objStatus", ObjStatus.DELETE.toString());
					poiAddress.put("pid", oldAddressObjChi.getPid());
					poiAddress.put("poiPid", oldAddressObjChi.getPoiPid());
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
				for (IRow irow : oldContactsList) {
					IxPoiContact temp = (IxPoiContact) irow;
					oldArray.add(temp.Serialize(null));
				}

				JSONArray newContactArray = new JSONArray();

				List<String> newRowIdList = new ArrayList<String>();
				for (int k = 0; k < contactsList.size(); k++) {
					JSONObject contactObj = contactsList.getJSONObject(k);
					newRowIdList.add(contactObj.getString("rowId").toUpperCase());

					JSONObject newContact = new JSONObject();
					newContact.put("poiPid", pid);
					newContact.put("contactType", contactObj.getInt("type"));
					newContact.put("contact", contactObj.getString("number"));
					String linkman = contactObj.getString("linkman");
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
					newContact.put("contactDepart", contactInt);
					newContact.put("priority", contactObj.getInt("priority"));
					newContact.put("rowId", contactObj.getString("rowId").toUpperCase());

					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newContact);
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
				JSONArray oldDelJson = getOldDel(oldArray, newRowIdList);

				if (oldDelJson.size() > 0) {
					// 鲜度验证
					freshFlag = false;
				}

				newContactArray.addAll(oldDelJson);

				poiJson.put("contacts", newContactArray);
			}

			JSONObject relate = new JSONObject();
			// 父
			List<IRow> oldParentList = oldPoi.getParents();
			int groupId = 0;
			if (oldParentList.size() > 0) {
				IRow oldParentIRow = oldParentList.get(0);
				IxPoiParent oldParent = (IxPoiParent) oldParentIRow;
				groupId = oldParent.getPid();
			}
			if (!(jo.getJSONArray("relateChildren").size() > 0) || !(oldParentList.size() > 0)) {
				// 新增
				if (jo.getJSONArray("relateChildren").size() > 0 && oldParentList.size() == 0) {
					JSONObject parent = new JSONObject();
					groupId = PidUtil.getInstance().applyPoiGroupId();
					parent.put("objStatus", ObjStatus.INSERT.toString());
					parent.put("pid", groupId);
					parent.put("parentPoiPid", pid);
					parent.put("rowId", UuidUtils.genUuid());
					relate.put("parent", parent);
					// 鲜度验证
					freshFlag = false;
				}
				// 删除
				if (jo.getJSONArray("relateChildren").size() == 0 && oldParentList.size() > 0) {
					JSONObject parent = new JSONObject();
					IRow oldParentIRow = oldParentList.get(0);
					IxPoiParent oldParent = (IxPoiParent) oldParentIRow;
					parent.put("objStatus", ObjStatus.DELETE.toString());
					parent.put("pid", oldParent.getPid());
					parent.put("parentPoiPid", oldParent.getPid());
					parent.put("rowId", oldParent.getRowId());
					relate.put("parent", parent);
					// 鲜度验证
					freshFlag = false;
				}
			}

			// 子
			if (jo.containsKey("relateChildren")) {
				List<IRow> oldChildIRow = oldPoi.getChildren();
				JSONArray oldArray = new JSONArray();
				for (IRow oldChild : oldChildIRow) {
					IxPoiChildren oldPoiChild = (IxPoiChildren) oldChild;
					oldArray.add(oldPoiChild.Serialize(null));
				}
				JSONArray childrenArry = jo.getJSONArray("relateChildren");
				JSONArray newChildrenArray = new JSONArray();
				List<String> newRowIdList = new ArrayList<String>();
				for (int k = 0; k < childrenArry.size(); k++) {
					JSONObject children = childrenArry.getJSONObject(k);
					JSONObject newChildren = new JSONObject();
					newRowIdList.add(children.getString("rowId").toUpperCase());
					newChildren.put("groupId", groupId);
					newChildren.put("relationType", children.getInt("type"));
					newChildren.put("rowId", children.getString("rowId").toUpperCase());

					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newChildren);
					if (ret == 0) {
						newChildren.put("childFid", children.getString("childFid"));
						newChildren.put("objStatus", ObjStatus.INSERT.toString());
						newChildrenArray.add(newChildren);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						newChildren.put("childFid", children.getString("childFid"));
						newChildren.put("objStatus", ObjStatus.UPDATE.toString());
						newChildrenArray.add(newChildren);
						// 鲜度验证
						freshFlag = false;
					}
				}

				// 差分，区分删除的数据
				JSONArray oldDelJson = getOldDel(oldArray, newRowIdList);

				if (oldDelJson.size() > 0) {
					// 鲜度验证
					freshFlag = false;
				}

				newChildrenArray.addAll(oldDelJson);

				relate.put("children", newChildrenArray);
			}
			retObj.put("relate", relate);

			// 加油站
			if (jo.containsKey("gasStation")) {
				List<IRow> gasList = oldPoi.getGasstations();
				JSONObject gasObj = jo.getJSONObject("gasStation");
				JSONArray oldArray = new JSONArray();
				JSONArray newGasArray = new JSONArray();
				if (!gasObj.isEmpty()) {
					for (IRow oldGas : gasList) {
						IxPoiGasstation oldPoiGas = (IxPoiGasstation) oldGas;
						JSONObject oldPoiGasObj = oldPoiGas.Serialize(null);
						oldArray.add(oldPoiGasObj);
					}
					JSONObject newGasStation = new JSONObject();
					newGasStation.put("poiPid", pid);
					newGasStation.put("serviceProv", gasObj.getString("servicePro"));
					newGasStation.put("fuelType", gasObj.getString("fuelType"));
					newGasStation.put("oilType", gasObj.getString("oilType"));
					newGasStation.put("egType", gasObj.getString("egType"));
					newGasStation.put("mgType", gasObj.getString("mgType"));
					newGasStation.put("payment", gasObj.getString("payment"));
					newGasStation.put("service", gasObj.getString("service"));
					newGasStation.put("openHour", gasObj.getString("openHour"));
					newGasStation.put("rowId", gasObj.getString("rowId").toUpperCase());
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newGasStation);
					if (ret == 0) {
						newGasStation.put("pid", PidUtil.getInstance().applyPoiGasstationId());
						newGasStation.put("objStatus", ObjStatus.INSERT.toString());
						newGasArray.add(newGasStation);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						int oldPid = 0;
						for (IRow oldGas : gasList) {
							IxPoiGasstation oldPoiGas = (IxPoiGasstation) oldGas;
							if (oldPoiGas.getRowId().equals(gasObj.getString("rowId").toUpperCase())) {
								oldPid = oldPoiGas.getPid();
								break;
							}
						}
						newGasStation.put("pid", oldPid);
						newGasStation.put("objStatus", ObjStatus.UPDATE.toString());
						newGasArray.add(newGasStation);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (gasList.size() > 0) {
					// 删除的数据
					IxPoiGasstation oldPoiGas = (IxPoiGasstation) gasList.get(0);
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
					for (IRow oldParkings : parkingsList) {
						IxPoiParking oldPoiParkings = (IxPoiParking) oldParkings;
						JSONObject oldPoiParkingsObj = oldPoiParkings.Serialize(null);
						oldArray.add(oldPoiParkingsObj);
					}
					JSONObject newParkings = new JSONObject();
					newParkings.put("poiPid", pid);
					newParkings.put("parkingType", parkingsObj.getString("buildingType"));
					newParkings.put("tollStd", parkingsObj.getString("tollStd"));
					newParkings.put("tollDes", parkingsObj.getString("tollDes"));
					newParkings.put("tollWay", parkingsObj.getString("tollWay"));
					newParkings.put("payment", parkingsObj.getString("payment"));
					newParkings.put("remark", parkingsObj.getString("remark"));
					newParkings.put("openTiime", parkingsObj.getString("openTime"));
					newParkings.put("totalNum", parkingsObj.getInt("totalNum"));
					newParkings.put("resHigh", parkingsObj.getInt("resHigh"));
					newParkings.put("resWidth", parkingsObj.getInt("resWidth"));
					newParkings.put("resWeigh", parkingsObj.getInt("resWeigh"));
					newParkings.put("certificate", parkingsObj.getInt("certificate"));
					newParkings.put("vehicle", parkingsObj.getInt("vehicle"));
					newParkings.put("haveSpecialplace", parkingsObj.getString("haveSpecialPlace"));
					newParkings.put("womenNum", parkingsObj.getInt("womenNum"));
					newParkings.put("handicapNum", parkingsObj.getInt("handicapNum"));
					newParkings.put("miniNum", parkingsObj.getInt("miniNum"));
					newParkings.put("vipNum", parkingsObj.getInt("vipNum"));
					newParkings.put("rowId", parkingsObj.getString("rowId").toUpperCase());
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newParkings);
					if (ret == 0) {
						newParkings.put("pid", PidUtil.getInstance().applyPoiParkingsId());
						newParkings.put("objStatus", ObjStatus.INSERT.toString());
						newParkingsArray.add(newParkings);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						int oldPid = 0;
						for (IRow oldParkings : parkingsList) {
							IxPoiParking oldPoiParkings = (IxPoiParking) oldParkings;
							if (oldPoiParkings.getRowId().equals(parkingsObj.getString("rowId").toUpperCase())) {
								oldPid = oldPoiParkings.getPid();
								break;
							}
						}
						newParkings.put("pid", oldPid);
						newParkings.put("objStatus", ObjStatus.UPDATE.toString());
						newParkingsArray.add(newParkings);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (parkingsList.size() > 0) {
					// 删除的数据
					IxPoiParking oldPoiParking = (IxPoiParking) parkingsList.get(0);
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
					for (IRow oldHotel : hotelList) {
						IxPoiHotel oldPoiHotel = (IxPoiHotel) oldHotel;
						JSONObject oldPoiHotelObj = oldPoiHotel.Serialize(null);
						oldArray.add(oldPoiHotelObj);
					}
					JSONObject newHotel = new JSONObject();
					newHotel.put("poiPid", pid);
					newHotel.put("creditCard", hotelObj.getString("creditCards"));
					newHotel.put("rating", hotelObj.getInt("rating"));
					newHotel.put("checkinTime", hotelObj.getString("checkInTime"));
					newHotel.put("checkoutTime", hotelObj.getString("checkOutTime"));
					newHotel.put("roomCount", hotelObj.getInt("roomCount"));
					newHotel.put("roomType", hotelObj.getString("roomType"));
					newHotel.put("roomPrice", hotelObj.getString("roomPrice"));
					newHotel.put("breakfast", hotelObj.getInt("breakfast"));
					newHotel.put("service", hotelObj.getString("service"));
					newHotel.put("parking", hotelObj.getInt("parking"));
					newHotel.put("longDescription", hotelObj.getString("description"));
					newHotel.put("openHour", hotelObj.getString("openHour"));
					newHotel.put("rowId", hotelObj.getString("rowId").toUpperCase());
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newHotel);
					if (ret == 0) {
						newHotel.put("pid", PidUtil.getInstance().applyPoiHotelId());
						newHotel.put("objStatus", ObjStatus.INSERT.toString());
						newHotelArray.add(newHotel);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						int oldPid = 0;
						for (IRow oldHotel : hotelList) {
							IxPoiHotel oldPoiHotel = (IxPoiHotel) oldHotel;
							if (oldPoiHotel.getRowId().equals(hotelObj.getString("rowId").toUpperCase())) {
								oldPid = oldPoiHotel.getPid();
								break;
							}
						}
						newHotel.put("pid", oldPid);
						newHotel.put("objStatus", ObjStatus.UPDATE.toString());
						newHotelArray.add(newHotel);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (hotelList.size() > 0) {
					// 删除的数据
					IxPoiHotel oldPoiHotel = (IxPoiHotel) hotelList.get(0);
					JSONObject oldDelJson = oldPoiHotel.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());

					newHotelArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}

				poiJson.put("hotels", newHotelArray);
			}

			// 餐馆
			if (jo.containsKey("foodtypes")) {
				List<IRow> foodtypeList = oldPoi.getRestaurants();
				JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
				JSONArray oldArray = new JSONArray();
				JSONArray newFoodtypeArray = new JSONArray();
				if (!foodtypesObj.isEmpty()) {
					for (IRow oldFoodtype : foodtypeList) {
						IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant) oldFoodtype;
						JSONObject oldPoiFoodtypeObj = oldPoiFoodtype.Serialize(null);
						oldArray.add(oldPoiFoodtypeObj);
					}
					JSONObject newFoodtype = new JSONObject();
					newFoodtype.put("poiPid", pid);
					newFoodtype.put("foodType", foodtypesObj.getString("foodtype"));
					newFoodtype.put("creditCard", foodtypesObj.getString("creditCards"));
					newFoodtype.put("avgCost", foodtypesObj.getInt("avgCost"));
					newFoodtype.put("parking", foodtypesObj.getInt("parking"));
					newFoodtype.put("openHour", foodtypesObj.getString("openHour"));
					newFoodtype.put("rowId", foodtypesObj.getString("rowId").toUpperCase());
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newFoodtype);
					if (ret == 0) {
						newFoodtype.put("pid", PidUtil.getInstance().applyPoiRestaurantId());
						newFoodtype.put("objStatus", ObjStatus.INSERT.toString());
						newFoodtypeArray.add(newFoodtype);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						int oldPid = 0;
						for (IRow oldFoodtype : foodtypeList) {
							IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant) oldFoodtype;
							if (oldPoiFoodtype.getRowId().equals(foodtypesObj.getString("rowId").toUpperCase())) {
								oldPid = oldPoiFoodtype.getPid();
								break;
							}
						}
						newFoodtype.put("pid", oldPid);
						newFoodtype.put("objStatus", ObjStatus.UPDATE.toString());
						newFoodtypeArray.add(newFoodtype);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (foodtypeList.size() > 0) {
					// 删除的数据
					IxPoiRestaurant oldPoiFoodtype = (IxPoiRestaurant) foodtypeList.get(0);
					JSONObject oldDelJson = oldPoiFoodtype.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());

					newFoodtypeArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}

				poiJson.put("restaurants", newFoodtypeArray);

			}
			
			// 充电站
			if (jo.containsKey("chargingStation")) {
				List<IRow> chargingStationList = oldPoi.getChargingstations();
				JSONObject chargingStationObj = jo.getJSONObject("chargingStation");
				JSONArray oldArray = new JSONArray();
				JSONArray newChargingStationArray = new JSONArray();
				if (!chargingStationObj.isEmpty()) {
					for (IRow oldChargingStation : chargingStationList) {
						IxPoiChargingStation oldPoiChargingStation = (IxPoiChargingStation) oldChargingStation;
						JSONObject oldPoiChargingStationObj = oldPoiChargingStation.Serialize(null);
						oldArray.add(oldPoiChargingStationObj);
					}
					JSONObject newChargingStation = new JSONObject();
					newChargingStation.put("poiPid", pid);
					newChargingStation.put("chargingType", chargingStationObj.getInt("type"));
					newChargingStation.put("changeBrands", chargingStationObj.getString("changeBrands"));
					newChargingStation.put("changeOpenType", chargingStationObj.getString("changeOpenType"));
					newChargingStation.put("chargingNum", chargingStationObj.getInt("chargingNum"));
					newChargingStation.put("serviceProv", chargingStationObj.getString("servicePro"));
					newChargingStation.put("openHour", chargingStationObj.getString("openHour"));
					newChargingStation.put("parkingFees", chargingStationObj.getInt("parkingFees"));
					newChargingStation.put("parkingInfo", chargingStationObj.getString("parkingInfo"));
					newChargingStation.put("availableState", chargingStationObj.getInt("availableState"));
					newChargingStation.put("rowId", chargingStationObj.getString("rowId").toUpperCase());
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newChargingStation);
					if (ret == 0) {
						newChargingStation.put("pid", PidUtil.getInstance().applyPoiChargingstationId());
						newChargingStation.put("objStatus", ObjStatus.INSERT.toString());
						newChargingStationArray.add(newChargingStation);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						int oldPid = 0;
						for (IRow oldChargingStation : chargingStationList) {
							IxPoiChargingStation oldPoiChargingStation = (IxPoiChargingStation) oldChargingStation;
							if (oldPoiChargingStation.getRowId().equals(chargingStationObj.getString("rowId").toUpperCase())) {
								oldPid = oldPoiChargingStation.getPid();
								break;
							}
						}
						newChargingStation.put("pid", oldPid);
						newChargingStation.put("objStatus", ObjStatus.UPDATE.toString());
						newChargingStationArray.add(newChargingStation);
						// 鲜度验证
						freshFlag = false;
					}
				} else if (chargingStationList.size() > 0) {
					// 删除的数据
					IxPoiChargingStation oldPoiChargingStation = (IxPoiChargingStation) chargingStationList.get(0);
					JSONObject oldDelJson = oldPoiChargingStation.Serialize(null);
					oldDelJson.put("objStatus", ObjStatus.DELETE.toString());
					newChargingStationArray.add(oldDelJson);
					// 鲜度验证
					freshFlag = false;
				}
				poiJson.put("chargingstations", newChargingStationArray);
			}
			
			
			// 充电桩
			if (jo.containsKey("chargingPole")) {
				JSONArray chargingPoleList = jo.getJSONArray("chargingPole");
				List<IRow> oldChargingPoleList = oldPoi.getChargingplots();

				JSONArray oldArray = new JSONArray();
				for (IRow irow : oldChargingPoleList) {
					IxPoiChargingPlot temp = (IxPoiChargingPlot) irow;
					oldArray.add(temp.Serialize(null));
				}

				JSONArray newChargingPoleArray = new JSONArray();

				List<String> newRowIdList = new ArrayList<String>();
				for (int k = 0; k < chargingPoleList.size(); k++) {
					JSONObject chargingPoleObj = chargingPoleList.getJSONObject(k);
					newRowIdList.add(chargingPoleObj.getString("rowId").toUpperCase());

					JSONObject newChargingPole = new JSONObject();
					newChargingPole.put("poiPid", pid);
					newChargingPole.put("groupId", chargingPoleObj.getInt("groupId"));
					newChargingPole.put("count", chargingPoleObj.getInt("count"));
					newChargingPole.put("acdc", chargingPoleObj.getInt("acdc"));
					newChargingPole.put("plugType", chargingPoleObj.getString("plugType"));
					newChargingPole.put("power", chargingPoleObj.getString("power"));
					newChargingPole.put("voltage", chargingPoleObj.getString("voltage"));
					newChargingPole.put("current", chargingPoleObj.getString("current"));
					newChargingPole.put("mode", chargingPoleObj.getInt("mode"));
					newChargingPole.put("plugNum", chargingPoleObj.getInt("plugNum"));
					newChargingPole.put("prices", chargingPoleObj.getString("prices"));
					newChargingPole.put("openType", chargingPoleObj.getString("openType"));
					newChargingPole.put("availableState", chargingPoleObj.getInt("availableState"));
					newChargingPole.put("manufacturer", chargingPoleObj.getString("manufacturer"));
					newChargingPole.put("factoryNum", chargingPoleObj.getString("factoryNum"));
					newChargingPole.put("plotNum", chargingPoleObj.getString("plotNum"));
					newChargingPole.put("productNum", chargingPoleObj.getString("productNum"));
					newChargingPole.put("parkingNum", chargingPoleObj.getString("parkingNum"));
					newChargingPole.put("floor", chargingPoleObj.getInt("floor"));
					newChargingPole.put("locationType", chargingPoleObj.getInt("locationType"));
					newChargingPole.put("payment", chargingPoleObj.getString("payment"));
					newChargingPole.put("rowId", chargingPoleObj.getString("rowId").toUpperCase());

					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newChargingPole);
					if (ret == 0) {
						newChargingPole.put("objStatus", ObjStatus.INSERT.toString());
						newChargingPoleArray.add(newChargingPole);
						// 鲜度验证
						freshFlag = false;
					} else if (ret == 1) {
						newChargingPole.put("objStatus", ObjStatus.UPDATE.toString());
						newChargingPoleArray.add(newChargingPole);
						// 鲜度验证
						freshFlag = false;
					}
				}

				// 差分，区分删除的数据
				JSONArray oldDelJson = getOldDel(oldArray, newRowIdList);
				if (oldDelJson.size() > 0) {
					// 鲜度验证
					freshFlag = false;
				}
				newChargingPoleArray.addAll(oldDelJson);
				poiJson.put("chargingplots", newChargingPoleArray);
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
			for (int i = 0; i < oldArray.size(); i++) {
				String newRowid = newObj.getString("rowId").toUpperCase();
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
			for (int i = 0; i < oldArray.size(); i++) {
				boolean flag = true;
				JSONObject jsonObj = oldArray.getJSONObject(i);
				for (String rowid : newRowIdList) {
					rowid = rowid.toUpperCase();
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

	/**
	 * poi操作修改poi状态为待作业
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void upatePoiStatusForAndroid(Connection conn,  int freshFlag, String rawFields,int status,int pid)
			throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT "+status+" as b," + freshFlag + " as c,'" + rawFields
				+ "' as d," + "sysdate as e,"+ pid + " as f " + "  FROM dual) T2 ");
		sb.append(" ON ( T1.pid=T2.f) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(
				" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.is_upload = 1,T1.raw_fields = T2.d,T1.upload_date = T2.e ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(
				" INSERT (T1.status,T1.fresh_verified,T1.is_upload,T1.raw_fields,T1.upload_date,T1.pid) VALUES(T2.b,T2.c,1,T2.d,T2.e,T2.f)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
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
	
	// 处理新增时的父子关系
	@SuppressWarnings("unchecked")
	private void insetParent(Connection conn,JSONObject relate) throws Exception {
		String sqlParent = "INSERT INTO ix_poi_parent (group_id,parent_poi_pid,tenant_flag,u_record,u_date,row_id) VALUES (?,?,?,?,?,?)";
		
		String sqlChildren = "INSERT INTO ix_poi_children (group_id,child_poi_pid,relation_type,u_record,u_date,row_id) VALUES (?,?,?,?,?,?)";
		
		PreparedStatement pstmtParent = null;
		
		PreparedStatement pstmtChildren = null;

		try {
			pstmtParent = conn.prepareStatement(sqlParent);
			pstmtChildren = conn.prepareStatement(sqlChildren);
			
			Iterator<String> keys = relate.keys();
			
			while(keys.hasNext()) {
				String pid = keys.next();
				try{
					if(checkPoiExists(Long.parseLong(pid),conn)<0){
						log.debug("新增 poi:"+pid+" 没有被正确写入，该父子关系忽略。");
						continue;
					}
					JSONArray relateChildren = relate.getJSONArray(pid);
					
					int groupId = PidUtil.getInstance().applyPoiGroupId();
					
					pstmtParent.setInt(1, groupId);
					pstmtParent.setInt(2, Integer.parseInt(pid));
					pstmtParent.setInt(3, 0);
					pstmtParent.setInt(4, 1);
					pstmtParent.setString(5, StringUtils.getCurrentTime());
					pstmtParent.setString(6, UuidUtils.genUuid());
					pstmtParent.execute();
					
					for (int i=0;i<relateChildren.size();i++) {
						JSONObject children = relateChildren.getJSONObject(i);
						pstmtChildren.setInt(1, groupId);
						int childPid = getPoiNumByPid(children.getString("childFid"),conn);
						pstmtChildren.setInt(2, childPid);
						pstmtChildren.setInt(3, children.getInt("type"));
						pstmtChildren.setInt(4, 1);
						pstmtChildren.setString(5, StringUtils.getCurrentTime());
						pstmtChildren.setString(6, children.getString("rowId"));
						pstmtChildren.execute();
					}
					
					conn.commit();
				}catch(Exception e){
					DbUtils.rollback(conn);
					log.warn("POI父子关系(pid:"+pid+")入库错误。");
					log.error(e.getMessage(),e);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmtParent);
			DBUtils.closeStatement(pstmtChildren);
		}
	}
	
	// 处理更新时的父子关系
	@SuppressWarnings("unchecked")
	private void updateParent(Connection conn,JSONObject relate) throws Exception {
		PreparedStatement pstmtParent = null;
		
		PreparedStatement pstmtChildren = null;
		try {
			Iterator<String> keys = relate.keys();
			
			while(keys.hasNext()) {
				String pid = keys.next();
				JSONObject relateObj = relate.getJSONObject(pid);
				if (relateObj.containsKey("parent")) {
					JSONObject parent = new JSONObject();
					parent = relateObj.getJSONObject("parent");
					String objStatus = parent.getString("objStatus");
					if (objStatus.equals("INSERT")) {
						String sql = "INSERT INTO ix_poi_parent (group_id,parent_poi_pid,tenant_flag,u_record,u_date,row_id) VALUES (?,?,?,?,?,?)";
						pstmtParent = conn.prepareStatement(sql);
						pstmtParent.setInt(1, parent.getInt("pid"));
						pstmtParent.setInt(2, Integer.parseInt(pid));
						pstmtParent.setInt(3, 0);
						pstmtParent.setInt(4, 1);
						pstmtParent.setString(5, StringUtils.getCurrentTime());
						pstmtParent.setString(6, parent.getString("rowId"));
						pstmtParent.execute();
					} else if (objStatus.equals("DELETE")) {
						String sql = "UPDATE ix_poi_parent SET u_record=2 WHERE row_id=:1";
						pstmtParent = conn.prepareStatement(sql);
						pstmtParent.setString(1, parent.getString("rowId"));
						pstmtParent.executeUpdate();
					}
				}
				JSONArray children = relateObj.getJSONArray("children");
				for (int i=0;i<children.size();i++) {
					JSONObject child = children.getJSONObject(i);
					String objStatus = child.getString("objStatus");
					if (objStatus.equals("INSERT")) {
						String sql = "INSERT INTO ix_poi_children (group_id,child_poi_pid,relation_type,u_record,u_date,row_id) VALUES (?,?,?,?,?,?)";
						pstmtChildren = conn.prepareStatement(sql);
						pstmtChildren.setInt(1, child.getInt("groupId"));
						int childPid = getPoiNumByPid(child.getString("childFid"),conn);
						pstmtChildren.setInt(2, childPid);
						pstmtChildren.setInt(3, child.getInt("relationType"));
						pstmtChildren.setInt(4, 1);
						pstmtChildren.setString(5, StringUtils.getCurrentTime());
						pstmtChildren.setString(6, child.getString("rowId"));
						pstmtChildren.execute();
					} else if (objStatus.equals("UPDATE")) {
						String sql = "UPDATE ix_poi_children (group_id,child_poi_pid,relation_type,u_record,u_date) SET (?,?,?,?,?) WHERE row_id=?";
						pstmtChildren = conn.prepareStatement(sql);
						pstmtChildren.setInt(1, child.getInt("groupId"));
						int childPid = getPoiNumByPid(child.getString("childFid"),conn);
						pstmtChildren.setInt(2, childPid);
						pstmtChildren.setInt(3, child.getInt("relationType"));
						pstmtChildren.setInt(4, 1);
						pstmtChildren.setString(5, StringUtils.getCurrentTime());
						pstmtChildren.setString(6, child.getString("rowId"));
						pstmtChildren.executeUpdate();
					} else if (objStatus.equals("DELETE")) {
						String sql = "UPDATE ix_poi_children SET u_record=2 WHERE row_id=:1";
						pstmtChildren = conn.prepareStatement(sql);
						pstmtChildren.setString(1, child.getString("rowId"));
						pstmtChildren.executeUpdate();
					}
				}
				
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmtParent);
			DBUtils.closeStatement(pstmtChildren);
		}
		
		
	}
	
	private int checkPoiExists(long pid,Connection conn)throws Exception{
		String sql="SELECT 1 FROM IX_POI WHERE PID=?";
		return new QueryRunner().queryForInt(conn, sql, pid);
	}
	
	// 通过fid查询pid
	private int getPoiNumByPid(String fid,Connection conn) throws Exception {
		
		String sql = "SELECT pid FROM ix_poi WHERE poi_num=:1";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, fid);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				return resultSet.getInt("pid");
			} else {
				throw new Exception("未找到fid为"+fid+"的子poi");
			}
			
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		
	}
	
	private IxPoiParent getParentByPid(int pid,Connection conn) throws Exception {
		String sql = "SELECT * FROM ix_poi_parent WHERE parent_poi_pid=:1 and u_record !=2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, pid);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				IxPoiParent parent = new IxPoiParent();
				parent.setPid(resultSet.getInt("group_id"));
				parent.setParentPoiPid(resultSet.getInt("parent_poi_pid"));
				parent.setTenantFlag(resultSet.getInt("tenant_flag"));
				parent.setMemo(resultSet.getString("memo"));
				parent.setuRecord(resultSet.getInt("u_record"));
				parent.setuDate(resultSet.getString("u_date"));
				parent.setRowId(resultSet.getString("row_id"));
				return parent;
			} else {
				return null;
			}
			
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	private class PoiWrap{

		private String fid;
		private int lifecycle;
		private JSONObject poiJson;
		private int pid=0;
		private int uRecord=-1;

		public PoiWrap(String fid, int lifecycle, JSONObject poiJson) {
			this.fid = fid;
			this.lifecycle =lifecycle;
			this.poiJson = poiJson;
		}

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public int getuRecord() {
			return uRecord;
		}

		public void setuRecord(int uRecord) {
			this.uRecord = uRecord;
		}

		public String getFid() {
			return fid;
		}

		public int getLifecycle() {
			return lifecycle;
		}

		public JSONObject getPoiJson() {
			return poiJson;
		}
		
		
	}
	
}
