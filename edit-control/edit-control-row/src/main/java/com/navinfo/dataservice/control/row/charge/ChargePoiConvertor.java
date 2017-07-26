package com.navinfo.dataservice.control.row.charge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ChargePoiConvertor
 * @author Han Shaoming
 * @date 2017年7月17日 下午8:25:21
 * @Description TODO
 */
public class ChargePoiConvertor {
	
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private JSONArray errorLog = new JSONArray();
	
	public JSONArray getErrorLog() {
		return errorLog;
	}
	
	//省市城市列表
	private Map<String, Map<String, String>> scPointAdminarea;
	private Map<Long,BasicObj> objsChild;
	private Map<Long, List<Map<String, Object>>> logDatas;
	private Map<Long, Map<String, Object>> freshDatas;
	
	public ChargePoiConvertor(Map<String, Map<String, String>> scPointAdminarea,Map<Long, BasicObj> objsChild, 
			Map<Long, List<Map<String, Object>>> logDatas,Map<Long, Map<String, Object>> freshDatas) {
		this.scPointAdminarea = scPointAdminarea;
		this.objsChild = objsChild;
		this.logDatas = logDatas;
		this.freshDatas = freshDatas;
	}
	/**
	 * 初始化
	 * @author Han Shaoming
	 * @param poi
	 * @return
	 * @throws Exception 
	 */
	public JSONObject initPoi(IxPoiObj poiObj) throws Exception{
		try {
			//获取充电桩数据
			Map<Long, BasicObj> plotMap = this.getChargePlot(poiObj);
			//过滤数据
			boolean filterPoi = this.filterPoi(poiObj,plotMap);
			if(!filterPoi){return null;}
			//处理通用字段
			JSONObject chargePoi = toJson(poiObj,plotMap);
			//处理特殊字段
			//poi的英文名称
			IxPoiName ixPoiName = poiObj.getOfficeOriginEngName();
			String nameEng = "";
			if(ixPoiName != null){
				if(StringUtils.isNotEmpty(ixPoiName.getName())){
					nameEng = ixPoiName.getName();
				}
			}
			chargePoi.put("nameEng", nameEng);
			//poi的英文地址
			String addEng = "";
			IxPoiAddress chAddress = poiObj.getCHAddress();
			if(chAddress != null){
				IxPoiAddress ixPoiAddress = poiObj.getENGAddress(chAddress.getNameGroupid());
				if(ixPoiAddress != null){
					if(StringUtils.isNotEmpty(ixPoiAddress.getFullname())){
						addEng = ixPoiAddress.getFullname();
					}
				}
			}
			chargePoi.put("addEng", addEng);
			//状态
			chargePoi.put("lifecycle", 0);
			//变更履历
			JSONArray editHistory = new JSONArray();
			chargePoi.put("editHistory", editHistory);
			//poi记录在fm第一次建立的时间
			String createDate = this.getCreateDate(poiObj);
			chargePoi.put("createDate", createDate);
			
			return chargePoi;
			
		} catch (Exception e) {
			throw new Exception("充电站pid("+poiObj.objPid()+"),"+e.getMessage(),e);
		}
	}
	/**
	 * 增量
	 * @author Han Shaoming
	 * @param objM 
	 * @param poi
	 * @return
	 * @throws Exception 
	 */
	public JSONObject addPoi(IxPoiObj poiObj, IxPoiObj objM) throws Exception{
		try {
			//获取充电桩数据
			Map<Long, BasicObj> plotMap = this.getChargePlot(poiObj);
			//过滤数据
			boolean filterPoi = this.filterPoiAdd(poiObj);
			if(!filterPoi){return null;}
			//处理通用字段
			JSONObject chargePoi = toJson(poiObj,plotMap);
			//处理特殊字段
			//poi的英文名称
			String nameEng = "";
			//月库中有数据取月库中的
			if(objM != null){
				IxPoiName ixPoiName = objM.getOfficeOriginEngName();
				if(ixPoiName != null){
					if(StringUtils.isNotEmpty(ixPoiName.getName())){
						nameEng = ixPoiName.getName();
					}
				}
			}else{
				IxPoiName ixPoiName = poiObj.getOfficeOriginEngName();
				if(ixPoiName != null){
					if(StringUtils.isNotEmpty(ixPoiName.getName())){
						nameEng = ixPoiName.getName();
					}
				}
			}
			chargePoi.put("nameEng", nameEng);
			//poi的英文地址
			String addEng = "";
			//月库中有数据取月库中的
			if(objM != null){
				IxPoiAddress chAddress = objM.getCHAddress();
				if(chAddress != null){
					IxPoiAddress ixPoiAddress = objM.getENGAddress(chAddress.getNameGroupid());
					if(ixPoiAddress != null){
						if(StringUtils.isNotEmpty(ixPoiAddress.getFullname())){
							addEng = ixPoiAddress.getFullname();
						}
					}
				}
			}else{
				IxPoiAddress chAddress = poiObj.getCHAddress();
				if(chAddress != null){
					IxPoiAddress ixPoiAddress = poiObj.getENGAddress(chAddress.getNameGroupid());
					if(ixPoiAddress != null){
						if(StringUtils.isNotEmpty(ixPoiAddress.getFullname())){
							addEng = ixPoiAddress.getFullname();
						}
					}
				}
			}
			chargePoi.put("addEng", addEng);
			//状态(桩家需要根据增量原则进行处理)
			if(poiObj.isDeleted()){
				//只标记删除记录
				chargePoi.put("lifecycle", 1);
			}else{
				chargePoi.put("lifecycle", 0);
			}
			//变更履历(桩家需要根据增量原则进行处理)
			JSONArray editHistory = new JSONArray();
			chargePoi.put("editHistory", editHistory);
			//poi记录在fm第一次建立的时间(桩家需要根据增量原则进行处理,除新增之外其他不做更新)
			String createDate = this.getCreateDate(poiObj);
			chargePoi.put("createDate", createDate);
			
			return chargePoi;
			
		} catch (Exception e) {
			throw new Exception("充电站pid("+poiObj.objPid()+"),"+e.getMessage(),e);
		}
	}
	
	/**
	 * 通用字段处理
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private JSONObject toJson(IxPoiObj poiObj,Map<Long, BasicObj> plotMap) throws Exception{
		JSONObject chargePoi = new JSONObject();
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		chargePoi.put("pid", pid);
		String kindCode = ixPoi.getKindCode();
		if(kindCode == null || !"230218".equals(kindCode)){return null;}
		//显示坐标
		Geometry geometry = ixPoi.getGeometry();
		double longitude = geometry.getCoordinate().x;
		double latitude = geometry.getCoordinate().y;
		JSONObject location = new JSONObject();
		location.put("longitude", longitude);
		location.put("latitude", latitude);
		chargePoi.put("location", location);
		//引导坐标
		JSONObject guide = new JSONObject();
		guide.put("longitude", ixPoi.getXGuide());
		guide.put("latitude", ixPoi.getYGuide());
		chargePoi.put("guide", guide.toString());
		//名称
		IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
		String name = "";
		if(ixPoiName != null){
			if(StringUtils.isNotEmpty(ixPoiName.getName())){
				name = ixPoiName.getName();
			}
		}
		chargePoi.put("name", name);
		//地址
		IxPoiAddress ixPoiAddress = poiObj.getCHAddress();
		String address = "";
		if(ixPoiAddress != null){
			if(StringUtils.isNotEmpty(ixPoiAddress.getFullname())){
				address = ixPoiAddress.getFullname();
			}
		}
		chargePoi.put("address", address);
		//poi类型
		String chargingType = this.getKindCode(poiObj);
		chargePoi.put("kindCode", chargingType);
		//联系方式
		String telephone = this.getTelephone(poiObj);
		chargePoi.put("telephone", telephone);
		//行政区划
		String adminCode = String.valueOf(poiObj.getAdminId());
		chargePoi.put("adminCode", adminCode);
		//24小时开放;营业时间
		Map<String, Object> openHourAndOpen24H = this.getOpenHourAndOpen24H(poiObj);
		int open24H = (int) openHourAndOpen24H.get("open24H");
		String openHour = (String) openHourAndOpen24H.get("openHour");
		chargePoi.put("open24H", open24H);
		chargePoi.put("openHour", openHour);
		//网址，预留字段
		chargePoi.put("website", "");
		//poi的存在状态
		int state = this.getState(poiObj);
		chargePoi.put("state", state);
		//poi的开放状态;可充电的车品牌
		Map<String, String> plotKindAndCarBrand = this.getPlotKindAndCarBrand(poiObj, plotMap);
		String plotKind = plotKindAndCarBrand.get("plotKind");
		String carBrand = plotKindAndCarBrand.get("carBrand");
		chargePoi.put("plotKind", plotKind);
		chargePoi.put("carBrand", carBrand);
		//充电桩庄主,该字段不更新
		chargePoi.put("owner", "");
		//插座详细信息描述,插座数量描述
		Map<String, Object> socketMap = this.getSockerParamsAndSockerNum(plotMap);
		JSONArray sockerParams = (JSONArray) socketMap.get("sockerParams");
		JSONObject sockerNum = (JSONObject) socketMap.get("sockerNum");
		chargePoi.put("sockerParams", sockerParams);
		chargePoi.put("socker_num", sockerNum);
		//充电桩总数量;停车是否免费信息;服务运营商;停车具体的收费价格
		Map<String, Object> station = this.getStation(poiObj);
		int plotNum = (int) station.get("plotNum");
		int parkingFee = (int) station.get("parkingFee");
		String servicePro = (String) station.get("servicePro");
		String parkingPrice = (String) station.get("parkingPrice");
		chargePoi.put("plotNum", plotNum);
		chargePoi.put("parking_fee", parkingFee);
		chargePoi.put("servicePro", servicePro);
		chargePoi.put("parkingPrice", parkingPrice);
		//支付方式，可多个值，可多选，多个值域
		String payment = this.getPayment(plotMap);
		chargePoi.put("payment", payment);
		//fid
		String fid = "";
		if(StringUtils.isNotEmpty(ixPoi.getPoiNum())){
			fid = ixPoi.getPoiNum();
		}
		chargePoi.put("fid", fid);
		//动态信息
		//是否有动态信息
		chargePoi.put("dynamic", 0);
		//动态数据提供商
		chargePoi.put("dynamic_server", "0");
		//运营商提供的充电站id
		chargePoi.put("serviceStationID", "");
		//邮政编码
		String ZIP = "";
		if(StringUtils.isNotEmpty(ixPoi.getPostCode())){
			ZIP = ixPoi.getPostCode();
		}
		chargePoi.put("ZIP", ZIP);
		//楼层
		String floorLevel = this.getFloorLevel(plotMap);
		chargePoi.put("floorLevel", floorLevel);
		//充电桩所在方位
		String position = this.getPosition(plotMap);
		chargePoi.put("position", position);
		//出入口信息,母库未采集该属性，不转换，赋默认值
		Map<String,Object> entryPointsMap = new HashMap<String,Object>();
		entryPointsMap.put("type", 0);
		entryPointsMap.put("longitude", 0);
		entryPointsMap.put("latitude", 0);
		chargePoi.put("entryPoints", entryPointsMap);
		//是否检查验证过
		chargePoi.put("validated", 1);
		//数据检查、验证的时间,该poi在fm里更新的时间
		Map<String, String> dataMap = this.getValidationDateAndUpdateDate(poiObj);
		String validationDate = dataMap.get("validationDate");
		String updateDate = dataMap.get("updateDate");
		chargePoi.put("validationDate", validationDate);
		chargePoi.put("updateDate", updateDate);
		//数据核实验证方法,数据质量等级
		Map<String, Integer> vqMap = this.getValidationMethodAndQualityLevel(poiObj);
		int validationMethod = vqMap.get("validationMethod");
		int qualityLevel = vqMap.get("qualityLevel");
		chargePoi.put("validationMethod", validationMethod);
		chargePoi.put("qualityLevel", qualityLevel);
		//认证方式
		String authenticationMethod = this.getAuthenticationMethod(poiObj, plotMap);
		chargePoi.put("authenticationMethod", authenticationMethod);
		//省市名称
		Map<String, String> proviceAndCity = this.getProviceAndCity(poiObj);
		String province = proviceAndCity.get("province");
		String city = proviceAndCity.get("city");
		chargePoi.put("province", province);
		chargePoi.put("city", city);
		
		
		return chargePoi;
	}
	
	/**
	 * 获取充电桩
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private Map<Long,BasicObj> getChargePlot(IxPoiObj poiObj) throws Exception{
		try {
			//获取子充电桩pid
			List<Long> childPids = new ArrayList<Long>();
			List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHILDREN");
			if(rows!=null && rows.size()>0){
				for(BasicRow row:rows){
					IxPoiChildren children = (IxPoiChildren) row;
					childPids.add(children.getChildPoiPid());
				}
			}
			Map<Long,BasicObj> objs = new HashMap<Long,BasicObj>();
			for (Long childPid : childPids) {
				//pid=0时不转出
				if(childPid == 0){continue;}
				if(objsChild.containsKey(childPid)){
					objs.put(childPid, objsChild.get(childPid));
				}
			}
			return objs;
		} catch (Exception e) {
			log.error("pid:"+poiObj.objPid()+",获取充电桩出错:"+e.getMessage(),e);
			throw new Exception("获取充电桩出错:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据条件过滤数据
	 * @author Han Shaoming
	 * @param poiObj
	 * @return 
	 */
	private boolean filterPoi(IxPoiObj poiObj,Map<Long, BasicObj> plotMap){
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
		if(pid == 0){return false;}
		//如果站下没有充电桩或站下所有的充电桩均为删除状态，则站及桩均不转出（当IX_POI_CHARGINGSTATION表中的CHARGING_TYPE=2或4时，充电站需要转出）；
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows == null || rows.size() == 0){return false;}
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int type = ixPoiChargingstation.getChargingType();
				if(type != 2 && type != 4){
					List<BasicRow> childs = poiObj.getRowsByName("IX_POI_CHILDREN");
					if(childs == null || childs.size() == 0 || plotMap.size() == 0){return false;}
				}
			}
		}
		return true;
	}
	
	/**
	 * 根据条件过滤数据(增量原则:
	 * 其父充电站未删除，则只将桩家中对应的充电桩插口记录删除，同时更新父充电站下的详情；
	 * -----若桩删除后父充电站下已没有非删除的桩，按照原则日库中的站也应为删除状态，
	 * 如果站此时仍为非删除状态，则建议桩家库中的站暂时保留，防止外业又在站下新增桩记录时无法正常更新
	 * )
	 * 增量原则,有站没有充电桩的也要转入,在桩家(新增时)过滤,
     * 保证原先有站有桩的数据更新为有站无桩时能够保证桩家库数据也更新
	 * @author Han Shaoming
	 * @param poiObj
	 * @return 
	 */
	private boolean filterPoiAdd(IxPoiObj poiObj){
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
		if(pid == 0){return false;}
		//如果站下没有充电桩或站下所有的充电桩均为删除状态，则站及桩均不转出（当IX_POI_CHARGINGSTATION表中的CHARGING_TYPE=2或4时，充电站需要转出）；
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows == null || rows.size() == 0){return false;}
		return true;
	}
	
	/**
	 * 获取POI类型
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private String getKindCode(IxPoiObj poiObj){
		String kindCode = "";
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int type = ixPoiChargingstation.getChargingType();
				if(type == 1 || type == 3){
					kindCode = "1";
				}else if(type == 2){
					kindCode = "2";
				}else if(type == 4){
					kindCode = "4";
				}
			}
			
		}
		return kindCode;
	}
	
	/**
	 * 查询所有的联系方式
	 * @author Han Shaoming
	 * @return
	 */
	private String getTelephone(IxPoiObj poiObj){
		List<String> telephoneList = new ArrayList<String>();
		Map<Integer,List<String>> map = new HashMap<Integer,List<String>>();
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CONTACT");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				IxPoiContact ixPoiContact = (IxPoiContact) row;
				int contactType = ixPoiContact.getContactType();
				if(contactType != 1 && contactType != 2 && contactType != 3 && contactType != 4){
					continue;
				}
				String contact = ixPoiContact.getContact();
				if(StringUtils.isNotEmpty(contact)){
					if(!map.containsKey(contactType)){
						map.put(contactType, new ArrayList<String>());
					}
					map.get(contactType).add(contact);
				}
			}
		}
		//处理数据
		if(map.containsKey(3)){
			telephoneList.addAll(map.get(3));
		}
		if(map.containsKey(4)){
			telephoneList.addAll(map.get(4));
		}
		if(map.containsKey(1)){
			telephoneList.addAll(map.get(1));
		}	
		if(map.containsKey(2)){
			telephoneList.addAll(map.get(2));
		}
		return StringUtils.join(telephoneList, "|");
	}
	
	/**
	 * 查询poi的存在状态
	 * @author Han Shaoming
	 * @return
	 */
	private int getState(IxPoiObj poiObj){
		int state = 0;
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int availableState = ixPoiChargingstation.getAvailableState();
				if(availableState != 0){
					state = 5;
				}
			}
		}
		return state;
	}
	
	
	/**
	 * 查询poi的开放状态和可充电的车品牌
	 * @author Han Shaoming
	 * @return
	 */
	private Map<String,String> getPlotKindAndCarBrand(IxPoiObj poiObj,Map<Long, BasicObj> plotMap){
		Map<String,String> map = new HashMap<String,String>();
		String plotKind = "0";
		String carBrand = "";
		List<BasicRow> childs = poiObj.getRowsByName("IX_POI_CHILDREN");
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				//充电站类型
				int type = ixPoiChargingstation.getChargingType();
				//换电开放限制
				String changeOpenType = ixPoiChargingstation.getChangeOpenType();
				//换电车品牌
				String changeBrands = ixPoiChargingstation.getChangeBrands();
				if(type == 2 || type == 4){
					Set<String> plotKindSet = new HashSet<String>();
					if(childs == null || childs.size() == 0 || plotMap.size() == 0){
						if(StringUtils.isNotEmpty(changeOpenType)){
							String[] openTypes = changeOpenType.split("\\|");
							for (String open : openTypes) {
								if("1".equals(open)){
									//0与其它值域不共存
									plotKindSet.add("0");
									break;
								}else if("2".equals(open)){
									plotKindSet.add("5");
								}else if("3".equals(open)){
									plotKindSet.add("2");
								}else if("4".equals(open)){
									plotKindSet.add("1");
								}else if("5".equals(open)){
									plotKindSet.add("6");
								}else if("6".equals(open)){
									plotKindSet.add("7");
								}
							}
							if(!plotKindSet.contains("0")){
								if(StringUtils.isNotEmpty(changeBrands)){
									carBrand = changeBrands;
									plotKindSet.add("8");
								}
							}
						}
					}
					if(plotKindSet.size() > 0){
						plotKind = StringUtils.join(plotKindSet,"|");
					}
				}
				else if(type != 4){
					Set<String> plotKindSet = new HashSet<String>();
					if(childs != null && childs.size() > 0 && plotMap.size() > 0){
						if(plotMap.size() != 0){
							for (BasicObj obj : plotMap.values()) {
								IxPoiObj plotObj = (IxPoiObj) obj;
								List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
								if(plotRows != null && plotRows.size() > 0){
									for (BasicRow plotRow : plotRows) {
										IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
										//开放状态
										String openType = ixPoiChargingplot.getOpenType();
										if(StringUtils.isNotEmpty(openType)){
											boolean flag = true;
											String[] openTypes = openType.split("\\|");
											for (String open : openTypes) {
												if("1".equals(open)){
													plotKindSet.add("0");
												}else if("2".equals(open)){
													plotKindSet.add("5");
												}else if("3".equals(open)){
													plotKindSet.add("2");
												}else if("4".equals(open)){
													plotKindSet.add("1");
												}else if("5".equals(open)){
													plotKindSet.add("6");
												}else if("6".equals(open)){
													plotKindSet.add("7");
												}else if("7".equals(open)){
													plotKindSet.add("4");
												}
												//判断是否只存在chainId
												if(open.length() != 4){
													flag = false;
												}
											}
											//仅存在CHAINID
											if(flag){
												carBrand = openType;
												plotKindSet.add("8");
											}
										}
									}
								}
							}
						}
					}
					if(plotKindSet.size() > 0){
						//0与其它值域不共存
						if(!plotKindSet.contains("0")){
							plotKind = StringUtils.join(plotKindSet,"|");
						}
					}
				}
			}
		}
		map.put("plotKind", plotKind);
		map.put("carBrand", carBrand);
		return map;
	}
	
	/**
	 * 查询插座的详细信息,插座数量描述
	 * @author Han Shaoming
	 * @param plotMap
	 * @return
	 * @throws Exception 
	 */
	private Map<String,Object> getSockerParamsAndSockerNum(Map<Long, BasicObj> plotMap) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		JSONArray sockerParams = new JSONArray();
		JSONObject sockerNum = new JSONObject();
		//插座总数
		int sockerallNum = 0;
		//快充直流总数
		int sDCquickNum = 0;
		//快充交流总数
		int sACquickNum = 0; 
		//慢充直流总数
		int sDCslowNum = 0;
		//慢充交流总数
		int sACslowNum = 0;
		//插座可用总数(动态)
		int sockableallNum = 0;
		//快充直流可用总数(动态)
		int sDCquickableNum = 0;
		//快充交流可用总数(动态)
		int sACquickableNum = 0;
		//慢充直流可用总数(动态)
		int sDCslowableNum = 0;
		//慢充交流可用总数(动态)
		int sACslowableNum = 0;
		
		//用于插口id
		int i = 1;
		if(plotMap.size() != 0){
			for (BasicObj obj : plotMap.values()) {
				IxPoiObj plotObj = (IxPoiObj) obj;
				IxPoi ixPoi = (IxPoi) plotObj.getMainrow();
				long pid = ixPoi.getPid();
				String fid = ixPoi.getPoiNum();
				List<JSONObject> list = new ArrayList<JSONObject>();
				//单个充电桩插口数量
				int socketSum = 0;
				List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
				if(plotRows != null && plotRows.size() > 0){
					for (BasicRow plotRow : plotRows) {
						try {
							IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
							String plugTypes = ixPoiChargingplot.getPlugType();
							if(ixPoiChargingplot.getPlugNum() > socketSum){
								socketSum = ixPoiChargingplot.getPlugNum();
							}
							if(StringUtils.isEmpty(plugTypes)){
								throw new Exception("插座类型为空");
							}
							String[] plugTypeArray = plugTypes.split("\\|");
							for (String plugType : plugTypeArray) {
								JSONObject jso = new JSONObject();
								//插座的永久ID
								String sockerPid = fid+"_"+plugType+"_"+i;
								jso.put("socker_pid", sockerPid);
								//插座所在充电桩的永久ID
								jso.put("plot_pid", pid);
								//插座所在充电桩的情报ID
								jso.put("plot_fid", fid);
								//插座所在充电桩的充电桩编号
								String chargingplotNum = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getPlotNum())){
									chargingplotNum = ixPoiChargingplot.getPlotNum();
								}
								jso.put("chargingplot_num", chargingplotNum);
								//插座所在充电桩的车位编号
								String parkingNum = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getParkingNum())){
									parkingNum = ixPoiChargingplot.getParkingNum();
								}
								jso.put("parkingNum", parkingNum);
								//插座所在充电桩的出厂编号
								String factoryNum = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getFactoryNum())){
									factoryNum = ixPoiChargingplot.getFactoryNum();
								}
								jso.put("factory_num", factoryNum);
								//插座所在充电桩的设备生产商
								String manufacturer = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getManufacturer())){
									manufacturer = ixPoiChargingplot.getManufacturer();
								}
								jso.put("manufacturer", manufacturer);
								//充电桩产品型号
								String productId = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getProductNum())){
									productId = ixPoiChargingplot.getProductNum();
								}
								jso.put("product_id", productId);
								//充电功率
								String power = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getPower())){
									power = ixPoiChargingplot.getPower();
								}
								jso.put("power", power);
								//交直流充电
								int acdc = ixPoiChargingplot.getAcdc();
								jso.put("acdc", acdc);
								//充电电压
								String voltage = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getVoltage())){
									voltage = ixPoiChargingplot.getVoltage();
								}
								jso.put("voltage", voltage);
								//充电电流
								String current = "";
								if(StringUtils.isNotEmpty(ixPoiChargingplot.getCurrent())){
									current = ixPoiChargingplot.getCurrent();
								}
								jso.put("current", current);
								//充电模式
								int mode = ixPoiChargingplot.getMode();
								jso.put("mode", mode);
								//插头类型
								jso.put("plugType", plugType);
								//该插座的状态
								int sockerState = 0;
								int availableState = ixPoiChargingplot.getAvailableState();
								if(availableState != 0){
									availableState = 2;
								}
								jso.put("sockerState", sockerState);
								//电价;服务费用,目前库中不支持存储电费，接动态信息获取
								jso.put("electric_fee", "");
								jso.put("serve_fee", "");
								//充电是否免费信息
								int chargeFee = 0;
								String prices = ixPoiChargingplot.getPrices();
								if(StringUtils.isNotEmpty(prices)){
									if("免费".equals(prices)){
										chargeFee = 1;
									}
								}
								jso.put("charge_fee", chargeFee);
								//该插孔类型的数量
								jso.put("plugNum", 1);
								//是否可预约
								jso.put("reserve", 0);
								//插口是否有枪,母库中没有该字段，不更新
								jso.put("gun", 2);
								//如果sockerState状态为1，则该数组启用
								JSONArray chargingInfo = new JSONArray();
								jso.put("charging_info", chargingInfo);
								//添加数据
								list.add(jso);
								
								i++;
							}
						} catch (Exception e) {
							log.error("pid("+pid+")的plot记录转换失败"+e.getMessage(),e);
							throw new Exception("pid("+pid+")的plot记录转换失败"+e.getMessage(),e);
						}
					}
				}
				if(list.size() > 0){
					//处理插口类型与插口数量不一致 
					int sum = list.size();
					if(socketSum > sum){
						int diff = socketSum - sum;
						for(int j=0;j<diff;j++){
							if(j >= sum){
								j = 0;
							}
							JSONObject jsonObj = list.get(j);
							JSONObject jso = JSONObject.fromObject(jsonObj);
							//处理插座的永久ID
							String socker_pid = jso.getString("socker_pid");
							int index = socker_pid.lastIndexOf("_");
							String socket = socker_pid.substring(0, index+1);
							jso.put("socker_pid", socket+i);
							list.add(jso);
							i++;
						}
					}else if(socketSum < sum){
						//报log
						errorLog.add("pid("+pid+")的充电桩数据有问题,PLUG_TYPE数量("+sum+")大于PLUG_NUM数量("+socketSum+")");
						list.subList(socketSum, sum).clear();
					}
					//桩poi数据添加到sockerParams
					sockerParams.addAll(list);
				}
			}
		}
		
		//处理插座数量描述
		for (int j = 0; j < sockerParams.size(); j++) {
			JSONObject jso = sockerParams.getJSONObject(j);
			int mode = jso.getInt("mode");
			int acdc = jso.getInt("acdc");
			//处理插口数量数据
			if(mode == 1 && acdc == 1){
				sDCquickNum += 1;
			}else if(mode == 1 && acdc == 0){
				sACquickNum += 1;
			}else if(mode == 0 && acdc == 1){
				sDCslowNum += 1;
			}else if(mode == 0 && acdc == 0){
				sACslowNum += 1;
			}
		}
		sockerallNum = sockerParams.size();
		sockerNum.put("sockerall_num", sockerallNum);
		sockerNum.put("sDCquick_num", sDCquickNum);
		sockerNum.put("sACquick_num", sACquickNum);
		sockerNum.put("sDCslow_num", sDCslowNum);
		sockerNum.put("sACslow_num", sACslowNum);
		sockerNum.put("sockableall_num", sockableallNum);
		sockerNum.put("sDCquickable_num", sDCquickableNum);
		sockerNum.put("sACquickable_num", sACquickableNum);
		sockerNum.put("sDCslowable_num", sDCslowableNum);
		sockerNum.put("sACslowable_num", sACslowableNum);
		
		map.put("sockerParams", sockerParams);
		map.put("sockerNum", sockerNum);
		return map;
		
	}
	
	
	/**
	 * 查询充电桩的总数,停车是否免费,服务提供商,停车具体的收费价格
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private Map<String,Object> getStation(IxPoiObj poiObj){
		Map<String,Object> map = new HashMap<String,Object>();
		int plotNum = 0;
		int parkingFee = 0;
		String servicePro = "0";
		String parkingPrice = "";
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				plotNum = ixPoiChargingstation.getChargingNum();
				parkingFee = ixPoiChargingstation.getParkingFees();
				servicePro = ixPoiChargingstation.getServiceProv();
				if(parkingFee == 1){
					if(StringUtils.isNotEmpty(ixPoiChargingstation.getParkingInfo())){
						parkingPrice = ixPoiChargingstation.getParkingInfo();
					}
				}
			}
		}
		map.put("plotNum", plotNum);
		map.put("parkingFee", parkingFee);
		map.put("servicePro", servicePro);
		map.put("parkingPrice", parkingPrice);
		return map;
	}
	
	/**
	 * 查询营业时间和24小时开放
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private Map<String,Object> getOpenHourAndOpen24H(IxPoiObj poiObj){
		Map<String,Object> map = new HashMap<String,Object>();
		String openHour = "00:00-23:59";
		int open24H = 2;
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		if(ixPoi.getOpen24h() == 0){
			open24H = 2;
		}else{
			open24H = ixPoi.getOpen24h();
		}
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				String hour = ixPoiChargingstation.getOpenHour();
				if(StringUtils.isNotEmpty(hour) && open24H == 2){
					openHour = hour;
				}else{
					if(open24H == 2){
						open24H = 1;
					}
				}
			}
		}
		map.put("open24H", open24H);
		map.put("openHour", openHour);
		return map;
	}
	
	
	/**
	 * 查询支付方式
	 * @author Han Shaoming
	 * @return
	 */
	private String getPayment(Map<Long, BasicObj> plotMap){
		String payment = "0";
		Set<String> paymentSet = new HashSet<String>();
		if(plotMap.size() != 0){
			for (BasicObj obj : plotMap.values()) {
				IxPoiObj plotObj = (IxPoiObj) obj;
				List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
				if(plotRows != null && plotRows.size() > 0){
					for (BasicRow plotRow : plotRows) {
						IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
						//付费方式
						String paymentList = ixPoiChargingplot.getPayment();
						if(StringUtils.isNotEmpty(paymentList)){
							String[] payments = paymentList.split("\\|");
							CollectionUtils.addAll(paymentSet, payments);
						}
					}
				}
			}
		}
		if(paymentSet.size() > 0){
			payment = StringUtils.join(paymentSet,"|");
		}
		return payment;
	}
	
	
	/**
	 * 查询楼层
	 * @author Han Shaoming
	 * @return
	 */
	private String getFloorLevel(Map<Long, BasicObj> plotMap){
		String floorLevel = "";
		Set<Integer> floorLevelSet = new HashSet<Integer>();
		if(plotMap.size() != 0){
			for (BasicObj obj : plotMap.values()) {
				IxPoiObj plotObj = (IxPoiObj) obj;
				List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
				if(plotRows != null && plotRows.size() > 0){
					for (BasicRow plotRow : plotRows) {
						IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
						//楼层
						int floor = ixPoiChargingplot.getFloor();
						floorLevelSet.add(floor);
					}
				}
			}
		}
		if(floorLevelSet.size() > 0){
			floorLevel = StringUtils.join(floorLevelSet,"|");
		}
		return floorLevel;
	}
	
	
	/**
	 * 查询充电桩所在方位
	 * @author Han Shaoming
	 * @return
	 */
	private String getPosition(Map<Long, BasicObj> plotMap){
		String position = "";
		Set<Integer> positionSet = new HashSet<Integer>();
		if(plotMap.size() != 0){
			for (BasicObj obj : plotMap.values()) {
				IxPoiObj plotObj = (IxPoiObj) obj;
				List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
				if(plotRows != null && plotRows.size() > 0){
					for (BasicRow plotRow : plotRows) {
						IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
						//充电桩位置类型
						int locationType = ixPoiChargingplot.getLocationType();
						positionSet.add(locationType);
					}
				}
			}
		}
		if(positionSet.size() > 0){
			position = StringUtils.join(positionSet,"|");
		}
		return position;
	}
	
	
	/**
	 * 查询数据核实验证方法,数据质量等级
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private Map<String, Integer> getValidationMethodAndQualityLevel(IxPoiObj poiObj) throws Exception{
		Map<String,Integer> map = new HashMap<String,Integer>();
		int validationMethod = 0;
		int qualityLevel = 0;
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_FLAG_METHOD");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiFlagMethod ixPoiFlagMethod = (IxPoiFlagMethod) row;
				int verRecord = ixPoiFlagMethod.getVerRecord();
				if(verRecord == 0){
					validationMethod = 6;
				}else if(verRecord == 1){
					validationMethod = 0;
				}else if(verRecord == 2){
					validationMethod = 5;
				}else if(verRecord == 4){
					validationMethod = 1;
				}else if(verRecord == 3 || verRecord == 5){
					//VER_RECORD=3或5时，该条POI转换失败，程序报LOG：VER_RECORD值域为代理店或多源
					errorLog.add("VER_RECORD值域为代理店或多源,pid为:"+poiObj.objPid());
					throw new Exception("VER_RECORD值域为代理店或多源,pid为:"+poiObj.objPid()); 
				}
			}
		}
		//数据质量等级
		int sourceName = 0;
		if(validationMethod == 0){
			qualityLevel = 1;
		}else if(validationMethod == 3 || sourceName == 4){
			qualityLevel = 2;
		}else if(validationMethod == 2 || validationMethod == 5){
			qualityLevel = 3;
		}else if(validationMethod == 1 || sourceName == 2){
			qualityLevel = 4;
		}else if(validationMethod == 4 || validationMethod == 6 || sourceName == 1){
			qualityLevel = 5;
		}
		map.put("validationMethod", validationMethod);
		map.put("qualityLevel", qualityLevel);
		return map;
	}
	
	
	/**
	 * 查询认证方式
	 * @author Han Shaoming
	 * @return
	 */
	private String getAuthenticationMethod(IxPoiObj poiObj,Map<Long, BasicObj> plotMap){
		String authenticationMethod = "0";
		List<BasicRow> childs = poiObj.getRowsByName("IX_POI_CHILDREN");
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				//充电站类型
				int type = ixPoiChargingstation.getChargingType();
				if(type == 2 || type == 4){
					if(childs == null || childs.size() == 0 || plotMap.size() == 0){
						authenticationMethod = "0";
					}
				}
				else if(type != 4){
					Set<String> set = new HashSet<String>();
					if(childs != null && childs.size() > 0 && plotMap.size() > 0){
						if(plotMap.size() != 0){
							for (BasicObj obj : plotMap.values()) {
								IxPoiObj plotObj = (IxPoiObj) obj;
								List<BasicRow> plotRows = plotObj.getRowsByName("IX_POI_CHARGINGPLOT");
								if(plotRows != null && plotRows.size() > 0){
									for (BasicRow plotRow : plotRows) {
										IxPoiChargingplot ixPoiChargingplot = (IxPoiChargingplot) plotRow;
										//付费方式
										String paymentList = ixPoiChargingplot.getPayment();
										if(StringUtils.isNotEmpty(paymentList)){
											String[] payments = paymentList.split("\\|");
											CollectionUtils.addAll(set, payments);
										}
									}
								}
							}
						}
					}
					if(set.size() > 0){
						Set<String> au = new HashSet<String>();
						if(set.contains("1") || set.contains("2") || set.contains("3") || set.contains("4")){
							au.add("1"); 
						}
						if(set.contains("0")){
							au.add("0"); 
						}
						if(set.contains("5")){
							au.add("2");
						}
						if(au.size() > 0){
							authenticationMethod = StringUtils.join(au, "|");
						}
					}
				}
			}
		}
		return authenticationMethod;
	}
	
	
	/**
	 * 查询省市名称
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private Map<String,String> getProviceAndCity(IxPoiObj poiObj) throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		String province = "";
		String city = "";
		try {
			String adminId = String.valueOf(poiObj.getAdminId());
			if(scPointAdminarea.containsKey(adminId)){
				Map<String, String> data = scPointAdminarea.get(adminId);
				if(StringUtils.isNotEmpty(data.get("province"))){
					province = data.get("province");
				}
				if(StringUtils.isNotEmpty(data.get("city"))){
					city = data.get("city");
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("pid:"+poiObj.objPid()+",查询省市名称报错,"+e.getMessage(),e);
			throw new Exception("pid:"+poiObj.objPid()+",查询省市名称报错,"+e.getMessage(),e);
		}
		map.put("province", province);
		map.put("city", city);
		return map;
	}
	
	/**
	 * 数据检查、验证的时间,该poi在fm里更新的时间
	 * @author Han Shaoming
	 * @param poiObj
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private Map<String,String> getValidationDateAndUpdateDate(IxPoiObj poiObj) throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		String validationDate = "20170815000000";
		String updateDate = "20170815000000";
		IxPoi ixPoi =(IxPoi) poiObj.getMainrow();
		long pid = ixPoi.getPid();
		try {
			//获取履历
			List<Map<String, Object>> logData = new ArrayList<Map<String, Object>>();
			if(logDatas.containsKey(pid)){
				logData = logDatas.get(pid);
			}
			if(logData != null && logData.size() > 0){
				Map<String, Object> logMap = logData.get(logData.size()-1);
				String date = (String) logMap.get("date");
				if(StringUtils.isNotEmpty(date)){
					validationDate = date;
					updateDate = date;
				}
			}else{
				//鲜度验证
				if(poiObj.isFreshFlag()){
					Map<String, Object> freshData = new HashMap<String,Object>();
					if(freshDatas.containsKey(pid)){
						freshData = freshDatas.get(pid);
					}
					if(freshData.size() > 0){
						String uploadDate = (String) freshData.get("uploadDate");
						if(StringUtils.isNotEmpty(uploadDate)){
							validationDate = uploadDate;
							updateDate = uploadDate;
						}
					}
				}else if(ixPoi.getOpType().equals(OperationType.INSERT)){
					Map<String, Object> logMap = logData.get(0);
					String date = (String) logMap.get("date");
					if(StringUtils.isNotEmpty(date)){
						validationDate = date;
						updateDate = date;
					}
				}
			}
		} catch (Exception e) {
			log.error("pid:"+pid+",查询履历报错,"+e.getMessage(),e);
			throw new Exception("pid:"+pid+",查询履历报错,"+e.getMessage(),e);
		}
		map.put("validationDate", validationDate);
		map.put("updateDate", updateDate);
		return map;
	} 
	
	/**
	 * 该poi记录在fm第一次建立的时间
	 * @author Han Shaoming
	 * @param poiObj
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private String getCreateDate(IxPoiObj poiObj) throws Exception{
		String createDate = "20170815000000";
		IxPoi ixPoi =(IxPoi) poiObj.getMainrow();
		long pid = ixPoi.getPid();
		try {
			//获取履历
			List<Map<String, Object>> logData = new ArrayList<Map<String, Object>>();
			if(logDatas.containsKey(pid)){
				logData = logDatas.get(pid);
			}
			if(logData != null && logData.size() > 0){
				Map<String, Object> logMap = logData.get(0);
				int operation = (int) logMap.get("operation");
				String date = (String) logMap.get("date");
				//新增
				if(operation == 1){
					if(StringUtils.isNotEmpty(date)){
						createDate = date;
					}
				}
			}else{
				//新增
				if(ixPoi.getOpType().equals(OperationType.INSERT)){
					Map<String, Object> logMap = logData.get(0);
					String date = (String) logMap.get("date");
					if(StringUtils.isNotEmpty(date)){
						createDate = date;
					}
				}
			}
		} catch (Exception e) {
			log.error("pid:"+pid+",查询履历报错,"+e.getMessage(),e);
			throw new Exception("pid:"+pid+",查询履历报错,"+e.getMessage(),e);
		}
		return createDate;
	}
	
	
}
