package com.navinfo.dataservice.scripts.tmp.diff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SampleDataDiffer {
	Param inParam;
	private static Logger logger = LoggerRepos.getLogger(SampleDataDiffer.class);
	/**
	 * 1.根据临时表中的fid，进行修改差分。修改差分逻辑如下
	 * mongodb记录的是Fastmap-POI生产系统中作业的poi； mongodb规格参考：https://192.168.0.72:8443/svn/ArchitectureTeam/FastMap/01Trunk/01设计（文档）/03规格/01模型/17夏/FastMap规格_17sum_20161209.xlsx
	 * oracle库中是一体化系统作业的poi；oracle的规格参考 GLM模型
	 * 将oracle中的数据和对应的mongodb中的数据进行字段级比较；
	 * 1)poi基础属性比较：
	 * mongdb             oracle
	 * location
	 * guide
	 * fid
	 * meshid
	 * postcode
	 * kindcode
	 * level
	 * open24h
	 * adminReal
	 * imporance
	 * airportCode
	 * lifecycle		  ix_poi.U_RECORD 如果一个是删除，另外一个不是删除； 则算不一致；否则算一致；
	 * name               取官方原始中文名称；
	 * address            取官方原始中文地址；
	 * adminCode          通过regionid 关联查询 AD_ADMIN.region得到 admin_id: select admin_id from ad_admin where region_id=?
	 * website		      ix_poi_detail.website
	 * hwEntryExit        ix_poi_detail.HW_ENTRYEXIT
	 * truck			  ix_poi.TRUCK_FLAG
	 * rawFields		  poi_edit_status.raw_fields
	 * sportsVenues			IX_POI.SPORTS_VENUE
	 * vipFlag				IX_POI.VIP_FLAG
	 * verifyFlags.record	IX_POI.VERIFIED_FLAG
	 * relateParent			IX_POI_PARENT 对应的fid  //根据mongo中relateParent.fid 和 对应oracle库中poi的ix_poi_parent对应的fid进行比较
	 * 2)poi子表比较（以下子表，oracle删除的数据除外）
	 * [下文中oracle的子表，可以参考192.168.3.107 gdb_vm/gdb_vm 中关于poi子表的逻辑配置
	 * 有逻辑主键的用下面的sql去查询；
	 * select *
		  from glm_table_meta t
		 where table_name = 'IX_POI_CONTACT'
		   AND IS_PK = 1
	   如果没有逻辑主键，则全字段匹配
	 *  ]
	 * relateChildren		IX_POI_CHILDREN //如果数量不同，则不同；如果数量相同，但是包含的fid不相同，则不同；
	 * names				IX_POI_NAMES //如果数量不同，则不同；如果数量相同，但是包含的逻辑主键 “poi_pid,name_class,name_type,lang_code,name” 不相同，则不同；如果mongo，oracle 的““poi_pid,name_class,name_type,lang_code,name”” 相同，但是“name_phonetic或者name-groupid”不同，则不同； 
	 * addresses			IX_POI_ADDRESS //如果数量不同，则不同；如果数量相同，但是逻辑主键“poi_pid,fullname,lang_code”不同，则不同；否则，判断逻辑主键之外的字段不同（name,u_record,u_fields,u_date,row_id除外），则不同；
	 * contacts			  	IX_POI_CONTACT //如果数量不同，则不同；如果数量相同，但是逻辑主键“poi_pid,contact_type,contact”不同，则不同；否则判断“pririoty”是否相同；
	 * businessTime       	IX_POI_BUSINESSTIME //数量不同，则不同；如果数量相同，但是DAY_SRT,DAY_END,TIME_SRT,TIME_DUR,MON_SRT,MON_END,WEEK_IN_YEAR_SRT,WEEK_IN_YEAR_END,WEEK_IN_MONTH_SRT,WEEK_IN_MONTH_END,VALID_WEEK 全字段匹配
	 * foodtypes			IX_POI_RESTAURANT//数量不同，则不同；如果数量相同，但是FOOD_TYPE,CREDIT_CARD,PARKING,AVG_COST,OPEN_HOUR全字段匹配，匹配则ok；否则不ok
	 * parkings				IX_POI_PARKING//数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * hotel				IX_POI_HOTEL //数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * chargingStation		IX_POI_CHARGINGSTATION//数量不同，则不同；如果数量相同，匹配逻辑主键“POI_PID，CHARGING_TYPE，CHANGE_BRANDS，CHANGE_OPEN_TYPE，CHARGING_NUM，SERVICE_PROV，OPEN_HOUR，PARKING_FEES，PARKING_INFO，AVAILABLE_STATE”，匹配则ok；否则不ok
	 * chargingPole			IX_POI_CHARGINGPLOT//数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * gasStation			IX_POI_GASSTATION //数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * attraction			IX_POI_ATTRACTION//数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * rental				IX_POI_CARRENTAL//数量不同，则不同；如果数量相同，但是全字段匹配（以mongo字段为参照），匹配则ok；否则不ok
	 * hospital				IX_POI_DETAIL.HOSPITAL_CLASS
	 * indoor.type			IX_POI.INDOOR 如果是1，则转为3；否则原值转出；
	 * indoor.floor			IX_POI_ADDRESS.FLOOR
	 * attachments			IX_POI_PHOTO 只比较数量
	 * brands.code			在oralce元数据chain的列表中； oracle poi中对应的chain的列表 查询sql： select chain  from SC_POINT_BRAND_FOODTYPE t where poikind=?;//?是oracle中的ix_poi.kind_code
	 * freshnessVerification POI_EDIT_STATUS.FRESH_VERIFIED
	 * @param args:
	 * 输入参数
	 * mongodbHost:
	 * mongodbPort:
	 * mongodbDbName:
	 * mongodbCollectionName
	 * orclHost:
	 * orclPort:
	 * orclSid:
	 * orclUser:
	 * orclPwd:
	 * diffFidTempTableName: 差分的fid的临时表
	 * outputFile: 输出文件全路径
	 * 输出：差分json格式如下
	 * {
	 * "fid":"32fdfdf",//差分的主键
	 * "diffResult":[
	 * {"field":"name","mongoValue":"金红果园山庄","oracleValue":"红果园山庄"},	//poi字段属性的差分结果
	 * {"field":"relateChildren","mongoValue":[],"oracleValue":[]},
	 * {"field":"names","mongoValue":[],"oracleValue":[]},
	 * ]
	 * "oracleExists":1 存在， 0不存在
	 * }
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		
		logger.info("解析获取参数");
		Param inParam = parseArgs(args);
		logger.info(inParam);
		SampleDataDiffer differ = new SampleDataDiffer();
		differ.inParam = inParam;
		logger.info("相同fid的数据开始差分");
		differ.doDiff();
		logger.info("数据差分完成，输出路径："+ differ.inParam.getOutputFile());

	}
	private  void doDiff() throws Exception {
		//将fid对应的poi，从mongo中查询得到一个hashmap(key:fid,value: poiJson)
		Map<String,JSONObject> mongoData = this.queryFromMongo();
		//从oracle中查询得到 hashmap(key:fid,value:poiJon)
		java.sql.Connection conn = getConnection();
		try{
			//查询主表信息 key是fid
			Map<String,JSONObject> ixPoiMap = queryIxPoi(conn);
			//查询父表信息，key=当前poi的fid，得到其父表对应的fid
			Map<String,String> ixPoiParentMap = queryIxPoiParent(conn);
			//查询children表，得到子表对应的fid
			Map<String,List<String>> ixPoiChildrenMap = queryIxPoiChildren(conn);
			Map<String,JSONArray> ixPoiNameMap = queryIxPoiName(conn);
			Map<String,JSONArray> ixPoiAddressMap = queryIxPoiAddress(conn);
			Map<String,JSONArray> ixPoiBusinessTimeMap = queryBusinessTimes(conn);
			Map<String,JSONArray> ixPoiContactMap=queryIxPoiContact(conn);
			//TODO:查询其他子表
			Map<String,JSONObject> ixPoiChargingStation = queryIxPoiCharigngStation(conn);
			Map<String,JSONArray> ixPoiChargingPlots = queryIxPoiChargingPlot(conn);
			Map<String,JSONObject> ixPoiGasStation = queryIxPoiGasStation(conn);
			Map<String,JSONObject> ixPoiAttraction = queryIxPoiAttraction(conn);
			Map<String,JSONObject> ixPoiRental = queryIxPoiRental(conn);
			Map<String,JSONObject> ixPoiHospital = queryIxPoiHospital(conn);
			//执行mongdb和oracle数据的差分比较
			List<JSONObject> outResult = new ArrayList<JSONObject>();
			for (String fid : mongoData.keySet()){
				logger.info("开始比较:fid="+fid);
				List<DiffField> diffFields  =new ArrayList<DiffField>();
				
				logger.info("比较主表属性");
				try{
					JSONObject mongoPoi = mongoData.get(fid);
					if (ixPoiMap.containsKey(fid)){
						JSONObject orcleIxPoi = ixPoiMap.get(fid);
						List<DiffField> diffIxPoiResult = diffIxPoi(mongoPoi,orcleIxPoi);
						if(!diffIxPoiResult.isEmpty()){
							diffFields.addAll(diffIxPoiResult);
						}
					}
					
					logger.info("比较relateParent属性");
					if (ixPoiParentMap.containsKey(fid)) {
						List<DiffField> diffIxPoiParentResult = diffIxPoiParent(mongoPoi,ixPoiParentMap.get(fid));
						if(diffIxPoiParentResult!=null){
							diffFields.addAll(diffIxPoiParentResult);
						}
					} else {
						JSONObject relateParent = mongoPoi.getJSONObject("relateParent");
						if (!relateParent.isNullObject() && relateParent.containsKey("parentFid")) {
							String parentFid = relateParent.getString("parentFid");
							if (parentFid != null && !parentFid.isEmpty()) {
								diffFields.add(setDiffField("relateParent",parentFid,null));
							}
						}
					}
					logger.info("比较relateChildren属性");
					if (ixPoiChildrenMap.containsKey(fid)) {
						List<DiffField> diffIxPoiChildResult = diffIxPoiChildren(mongoPoi,ixPoiChildrenMap.get(fid));
						if(diffIxPoiChildResult!=null){
							diffFields.addAll(diffIxPoiChildResult);
						}
					} else {
						JSONArray relateChildren = mongoPoi.getJSONArray("relateChildren");
						if (relateChildren.size()>0) {
							diffFields.add(setDiffField("relateChildren",relateChildren,null));
						}
					}
					logger.info("比较names属性");
					if(ixPoiNameMap.containsKey(fid)){
						List<DiffField> diffIxPoiNamesResult = diffIxPoiNames(mongoPoi,ixPoiNameMap.get(fid));
						if (diffIxPoiNamesResult!=null){
							diffFields.addAll(diffIxPoiNamesResult);
						}
					}else{
						JSONArray mongoNames = mongoPoi.getJSONArray("names");
						if (mongoNames.size()>0){
							diffFields.add(setDiffField("names", mongoNames, null));
						}
					}
					logger.info("比较addresses属性");
					if(ixPoiAddressMap.containsKey(fid)){
						List<DiffField> diffIxPoiAddressesResult = diffIxPoiAddress(mongoPoi,ixPoiAddressMap.get(fid));
						if (diffIxPoiAddressesResult!=null){
							diffFields.addAll(diffIxPoiAddressesResult);
						}
					}else{
						JSONArray mongoAddresses = mongoPoi.getJSONArray("addresses");
						if (mongoAddresses.size()>0){
							diffFields.add(setDiffField("addresses", mongoAddresses, null));
						}
					}
					logger.info("比较businessTime属性");
					if(ixPoiBusinessTimeMap.containsKey(fid)){
						List<DiffField> diffIxPoiBusinessTimeResult = diffIxPoiBusinessTime(mongoPoi,ixPoiBusinessTimeMap.get(fid));
						if (diffIxPoiBusinessTimeResult!=null){
							diffFields.addAll(diffIxPoiBusinessTimeResult);
						}
					}else{
						JSONArray mongoBusinessTimes = mongoPoi.getJSONArray("businessTime");
						if (mongoBusinessTimes.size()>0){
							diffFields.add(setDiffField("businessTime", mongoBusinessTimes, null));
						}
					}
					logger.info("比较CONTACT子表");
					if(ixPoiContactMap.containsKey(fid)){
						List<DiffField> diffIxPoiContactResult=diffIxPoiContact(mongoPoi,ixPoiContactMap.get(fid));
						if(diffIxPoiContactResult!=null){
							diffFields.addAll(diffIxPoiContactResult);
						}
					}else{
						JSONArray relateContact=mongoPoi.getJSONArray("contacts");
						if(relateContact.size()>0){
							diffFields.add(setDiffField("contacts", relateContact,null));
						}
					}
					//TODO:比较其他的子表属性
					logger.info("比较chargingStation属性");
					if (ixPoiChargingStation.containsKey(fid)) {
						List<DiffField> diffIxPoiChargingStationResult = diffIxPoiChargingStation(mongoPoi,ixPoiChargingStation.get(fid));
						if(diffIxPoiChargingStationResult!=null){
							diffFields.addAll(diffIxPoiChargingStationResult);
						}
					} else {
						JSONObject chargingStation = mongoPoi.getJSONObject("chargingStation");
						if (!chargingStation.isNullObject() && !chargingStation.isEmpty()) {
							diffFields.add(setDiffField("chargingStation",chargingStation,null));
						}
					}
					logger.info("比较chargingPlots属性");
					if (ixPoiChargingPlots.containsKey(fid)) {
						List<DiffField> diffIxPoiChargingPlotsResult = diffIxPoiChargingPlots(mongoPoi,ixPoiChargingPlots.get(fid));
						if(diffIxPoiChargingPlotsResult!=null){
							diffFields.addAll(diffIxPoiChargingPlotsResult);
						}
					} else {
						JSONObject chargingPlots = mongoPoi.getJSONObject("chargingPole");
						if (!chargingPlots.isNullObject() && !chargingPlots.isEmpty()) {
							diffFields.add(setDiffField("chargingPole",chargingPlots,null));
						}
					}
					logger.info("比较gasstation属性");
					if (ixPoiGasStation.containsKey(fid)) {
						List<DiffField> diffIxPoiGasStationResult = diffIxPoiGasStation(mongoPoi,ixPoiGasStation.get(fid));
						if(diffIxPoiGasStationResult!=null){
							diffFields.addAll(diffIxPoiGasStationResult);
						}
					} else {
						JSONObject gasStation = mongoPoi.getJSONObject("gasStation");
						if (!gasStation.isNullObject() && !gasStation.isEmpty()) {
							diffFields.add(setDiffField("gasStation",gasStation,null));
						}
					}
					logger.info("比较attraction属性");
					if (ixPoiAttraction.containsKey(fid)) {
						List<DiffField> diffIxPoiAttractionResult = diffIxPoiAttraction(mongoPoi,ixPoiAttraction.get(fid));
						if(diffIxPoiAttractionResult!=null){
							diffFields.addAll(diffIxPoiAttractionResult);
						}
					} else {
						JSONObject attraction = mongoPoi.getJSONObject("attraction");
						if (!attraction.isNullObject() && !attraction.isEmpty()) {
							diffFields.add(setDiffField("attraction",attraction,null));
						}
					}
					logger.info("比较rental属性");
					if (ixPoiRental.containsKey(fid)) {
						List<DiffField> diffIxPoiRentalResult = diffIxPoiRental(mongoPoi,ixPoiRental.get(fid));
						if(diffIxPoiRentalResult!=null){
							diffFields.addAll(diffIxPoiRentalResult);
						}
					} else {
						JSONObject rental = mongoPoi.getJSONObject("rental");
						if (!rental.isNullObject() && !rental.isEmpty()) {
							diffFields.add(setDiffField("rental",rental,null));
						}
					}
					logger.info("比较hospital属性");
					if (ixPoiHospital.containsKey(fid)) {
						List<DiffField> diffIxPoiHospitalResult = diffIxPoiHospital(mongoPoi,ixPoiHospital.get(fid));
						if(diffIxPoiHospitalResult!=null){
							diffFields.addAll(diffIxPoiHospitalResult);
						}
					} else {
						JSONObject hospital = mongoPoi.getJSONObject("hospital");
						if (!hospital.isNullObject() && !hospital.isEmpty()) {
							diffFields.add(setDiffField("hospital",hospital,null));
						}
					}

					
					if (CollectionUtils.isEmpty(diffFields)) continue;
					
					
					DiffResult diffResult= new DiffResult(fid,diffFields);
					
					// oracle数据不存在
					if (!ixPoiMap.containsKey(fid)){
						diffResult.setOracleExists(0);
					}
					outResult.add(JSONObject.fromObject(diffResult));
				}catch (Exception e){
					System.out.println("fid:" + fid);
					e.printStackTrace();
					logger.error(e.getMessage());
				}
			}
			//TODO:把DiffResult输出到文件
			logger.info("差分结果输出至txt");
			writeDiffResult(outResult);
		}finally{
			if(conn!=null){conn.close();}
		}
		
	}
	
	private void writeDiffResult(List<JSONObject> outResult) {
//		String txtPath = "W:\\test\\POI.txt";
		String txtPath = this.inParam.getOutputFile();
		CollectConvertUtils.writeJSONObject2TxtFile(txtPath, outResult);
	}
	private List<DiffField> diffIxPoiContact(JSONObject mongoPoi, JSONArray contactLists) {
		List<DiffField> diffList = new ArrayList<DiffField>();

		JSONArray relateContacts = mongoPoi.getJSONArray("contacts");
		if (relateContacts.size() == 0) {
			diffList.add(setDiffField("contacts", null, contactLists));
			return diffList;
		} else {
			if (relateContacts.size() != contactLists.size()) {
				// 数量不相等
				diffList.add(setDiffField("contacts", relateContacts, contactLists));
				return diffList;
			} else {
				
				for (int i = 0; i < relateContacts.size(); i++) {
					JSONObject relateObj = relateContacts.getJSONObject(i);
					boolean isSame = false;
					for (int j = 0; j < contactLists.size(); j++) {
						JSONObject contactObj = contactLists.getJSONObject(i);

						// 判断联系方式和联系方式类型，如果相同，判断优先选择
						if (isStringSame(relateObj.getString("number"), contactObj.getString("number"))
								&& relateObj.getInt("type") == contactObj.getInt("type")) {
							if (relateObj.getInt("priority") == contactObj.getInt("priority")) {
								isSame = true;
								break;
							}
						}
					} // 内层for
					if(!isSame){
						diffList.add(setDiffField("contacts", relateContacts, contactLists));
						return diffList;
					}
				} // 外层for
			}
		} // 外层else

		return null;
	}
	private List<DiffField> diffIxPoiBusinessTime(JSONObject mongoPoi, JSONArray orcleBusinessTimes){
		JSONArray mongoBusinessTimes = mongoPoi.getJSONArray("businessTime");
		if (mongoBusinessTimes.size() != orcleBusinessTimes.size()){
			// 数量不相等
			List<DiffField> diffResult = new ArrayList<DiffField>();
			diffResult.add(setDiffField("businessTime", mongoBusinessTimes, orcleBusinessTimes));
			return diffResult;
		}else{
			for (int i=0;i<mongoBusinessTimes.size();i++) {
				boolean isSame = false;
				JSONObject mongoBusinessTime = mongoBusinessTimes.getJSONObject(i);
				for (int j=0;j<orcleBusinessTimes.size();j++){
					JSONObject orcleBusinessTime = orcleBusinessTimes.getJSONObject(j);
					//如果数量相同，但是逻辑主键“poi_pid,fullname,lang_code”不同，则不同；
					//否则，判断逻辑主键之外的字段不同（name,u_record,u_fields,u_date,row_id除外），则不同；
					if (isStringSame(mongoBusinessTime.getString("validWeek"),orcleBusinessTime.getString("validWeek")) &&
							isStringSame(mongoBusinessTime.getString("weekStartMonth"),orcleBusinessTime.getString("weekStartMonth")) &&
						    isStringSame(mongoBusinessTime.getString("timeDuration"),orcleBusinessTime.getString("timeDuration")) &&
						    isStringSame(mongoBusinessTime.getString("timeStart"),orcleBusinessTime.getString("timeStart")) &&
							isStringSame(mongoBusinessTime.getString("monEnd"),orcleBusinessTime.getString("monEnd")) &&
							isStringSame(mongoBusinessTime.getString("weekStartYear"),orcleBusinessTime.getString("weekStartYear")) &&
							isStringSame(mongoBusinessTime.getString("monStart"),orcleBusinessTime.getString("monStart")) &&
						    isStringSame(mongoBusinessTime.getString("dayEnd"),orcleBusinessTime.getString("dayEnd")) &&
							isStringSame(mongoBusinessTime.getString("weekEndYear"),orcleBusinessTime.getString("weekEndYear")) &&
							isStringSame(mongoBusinessTime.getString("dayStart"),orcleBusinessTime.getString("dayStart")) &&
						    isStringSame(mongoBusinessTime.getString("weekEndMonth"),orcleBusinessTime.getString("weekEndMonth"))){
						isSame = true;
						break;
					}
				}	
				if (!isSame){
					List<DiffField> diffResult = new ArrayList<DiffField>();
					diffResult.add(setDiffField("businessTime", mongoBusinessTimes, orcleBusinessTimes));
					return diffResult;
				}
			}
		}
		return null;
	}
	private List<DiffField> diffIxPoiAddress(JSONObject mongoPoi, JSONArray orcleAddresses){
		JSONArray mongoAddresses = mongoPoi.getJSONArray("addresses");
		if (mongoAddresses.size() != orcleAddresses.size()){
			// 数量不相等
			List<DiffField> diffResult = new ArrayList<DiffField>();
			diffResult.add(setDiffField("addresses", mongoAddresses, orcleAddresses));
			return diffResult;
		}else{
			
			for (int i=0;i<mongoAddresses.size();i++) {
				boolean isSame = false;
				JSONObject mongoAddress = mongoAddresses.getJSONObject(i);
				for (int j=0;j<orcleAddresses.size();j++){
					JSONObject orcleAddress = orcleAddresses.getJSONObject(j);
					//如果数量相同，但是逻辑主键“poi_pid,fullname,lang_code”不同，则不同；
					//否则，判断逻辑主键之外的字段不同（name,u_record,u_fields,u_date,row_id除外），则不同；
					if (isStringSame(mongoAddress.getString("langCode"),orcleAddress.getString("langCode")) &&
							isStringSame(mongoAddress.getString("fullName"),orcleAddress.getString("fullName")) &&
						    isStringSame(mongoAddress.getString("fullNamePinyin"),orcleAddress.getString("fullNamePinyin")) &&
						    isStringSame(mongoAddress.getString("roadName"),orcleAddress.getString("roadName")) &&
							isStringSame(mongoAddress.getString("roadNamePinyin"),orcleAddress.getString("roadNamePinyin")) &&
							isStringSame(mongoAddress.getString("addrName"),orcleAddress.getString("addrName")) &&
						    isStringSame(mongoAddress.getString("addrNamePinyin"),orcleAddress.getString("addrNamePinyin"))){
						isSame = true;
						break;
					}
				}	
				if (!isSame){
					List<DiffField> diffResult = new ArrayList<DiffField>();
					diffResult.add(setDiffField("addresses", mongoAddresses, orcleAddresses));
					return diffResult;
				}
			}
		}
		return null;
	}
	private List<DiffField> diffIxPoiNames(JSONObject mongoPoi, JSONArray orcleNames){
		JSONArray mongoNames = mongoPoi.getJSONArray("names");
		if (mongoNames.size() != orcleNames.size()){
			// 数量不相等
			List<DiffField> diffResult = new ArrayList<DiffField>();
			diffResult.add(setDiffField("names", mongoNames, orcleNames));
			return diffResult;
		}else{
			
			for (int i=0;i<mongoNames.size();i++) {
				boolean isSame = false;
				JSONObject mongoName = mongoNames.getJSONObject(i);
				for (int j=0;j<orcleNames.size();j++){
					JSONObject orcleName = orcleNames.getJSONObject(j);
					//如果数量相同，但是包含的逻辑主键 “poi_pid,name_class,name_type,lang_code,name” 不相同，则不同；
					//如果mongo，oracle 的““poi_pid,name_class,name_type,lang_code,name”” 相同，
					//但是“name_phonetic或者name-groupid”不同，则不同
					if (mongoName.getInt("nameClass")==orcleName.getInt("nameClass") && 
							mongoName.getInt("type")==orcleName.getInt("type") && 
							isStringSame(mongoName.getString("langCode"),orcleName.getString("langCode")) &&
							isStringSame(mongoName.getString("nameStr"),orcleName.getString("nameStr"))){
						if (isStringSame(mongoName.getString("nameStrPinyin"),orcleName.getString("nameStrPinyin")) || mongoName.getInt("nameGrpId")==orcleName.getInt("nameGrpId")){
							isSame = true;
							break;
						}
					}
				}
				if (!isSame){
					List<DiffField> diffResult = new ArrayList<DiffField>();
					diffResult.add(setDiffField("names", mongoNames, orcleNames));
					return diffResult;
				}
			}
		}
		return null;
	}
	private List<DiffField> diffIxPoiParent(JSONObject mongoPoi, String parentFid) {
		JSONObject relateParent = mongoPoi.getJSONObject("relateParent");
		if (relateParent == null || relateParent.isEmpty() || !relateParent.containsKey("parentFid") || relateParent.getString("parentFid") == null) {
			List<DiffField> diffList = new ArrayList<DiffField>();
			diffList.add(setDiffField("relateParent",null,parentFid));
			return diffList;
		} else {
			String mongoParentFid = relateParent.getString("parentFid");
			if (!mongoParentFid.equals(parentFid)) {
				List<DiffField> diffList = new ArrayList<DiffField>();
				diffList.add(setDiffField("relateParent",mongoParentFid,parentFid));
				return diffList;
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiChildren(JSONObject mongoPoi, List<String> childFids) {
		JSONArray relateChildren = mongoPoi.getJSONArray("relateChildren");
		if (relateChildren.size() == 0) {
			List<DiffField> diffList = new ArrayList<DiffField>();
			diffList.add(setDiffField("relateChildren",null,childFids));
			return diffList;
		} else {
			if (relateChildren.size() != childFids.size()) {
				// 数量不相等
				List<DiffField> diffList = new ArrayList<DiffField>();
				diffList.add(setDiffField("relateChildren",relateChildren,childFids));
				return diffList;
			} else {
				for (int i=0;i<relateChildren.size();i++) {
					// 判断fid是否相等
					JSONObject childObj = relateChildren.getJSONObject(i);
					if (!childObj.containsKey("childFid")) {
						List<DiffField> diffList = new ArrayList<DiffField>();
						diffList.add(setDiffField("relateChildren",relateChildren,childFids));
						return diffList;
					}
					String childFid = childObj.getString("childFid");
					if (!childFids.contains(childFid)) {
						List<DiffField> diffList = new ArrayList<DiffField>();
						diffList.add(setDiffField("relateChildren",relateChildren,childFids));
						return diffList;
					}
				}
			}
		}
		return null;
	}
		private List<DiffField> diffIxPoiChargingStation(JSONObject mongoPoi, JSONObject chargingStation) throws Exception {
		JSONObject chargingStationObj = mongoPoi.getJSONObject("chargingStation");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("chargingStation",chargingStationObj,chargingStation));
		if (chargingStationObj.isNullObject() || chargingStationObj.isEmpty()) {
			return diffList;
		} else {
			if (chargingStationObj.getInt("type") != chargingStation.getInt("chargingType") || !isStringSame(chargingStationObj.getString("changeBrands"),chargingStation.getString("changeBrands"))
				|| !isStringSame(chargingStationObj.getString("changeOpenType"),chargingStation.getString("changeOpenType")) || chargingStationObj.getInt("chargingNum") != chargingStation.getInt("chargingNum") 
				|| !isStringSame(chargingStationObj.getString("servicePro"),chargingStation.getString("serviceProv")) || !isStringSame(chargingStationObj.getString("openHour"),chargingStation.getString("openHour"))
				|| chargingStationObj.getInt("parkingFees") != chargingStation.getInt("parkingFees") || !isStringSame(chargingStationObj.getString("parkingInfo"),chargingStation.getString("parkingInfo"))
				|| chargingStationObj.getInt("availableState") != chargingStation.getInt("availableState")) {
				return diffList;
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiChargingPlots(JSONObject mongoPoi, JSONArray chargingPlots) throws Exception {
		JSONArray chargingPole = mongoPoi.getJSONArray("chargingPole");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("chargingPole",chargingPole,chargingPlots));
		if (chargingPole.size() != chargingPlots.size()) {
			// 数量不相等
			return diffList;
		} else {
			for (int i=0;i<chargingPole.size();i++) {
				boolean flag = true;
				JSONObject poleObj = chargingPole.getJSONObject(i);
				for (int j=0;j<chargingPlots.size();j++) {
					JSONObject plotsObj = chargingPlots.getJSONObject(j);
					if (isStringSame(poleObj.getString("plugType"),plotsObj.getString("plugType")) && isStringSame(poleObj.getString("productNum"),plotsObj.getString("productNum"))
						&& isStringSame(poleObj.getString("power"),plotsObj.getString("power")) && poleObj.getInt("floor") == plotsObj.getInt("floor")
						&& isStringSame(poleObj.getString("factoryNum"),plotsObj.getString("factoryNum")) && poleObj.getInt("locationType") == plotsObj.getInt("locationType")
						&& isStringSame(poleObj.getString("parkingNum"),plotsObj.getString("parkingNum")) && poleObj.getInt("acdc") == plotsObj.getInt("acdc")
						&& isStringSame(poleObj.getString("payment"),plotsObj.getString("payment")) && isStringSame(poleObj.getString("current"),plotsObj.getString("current"))
						&& isStringSame(poleObj.getString("plotNum"),plotsObj.getString("plotNum")) && poleObj.getInt("plugNum") == plotsObj.getInt("plugNum")
						&& poleObj.getInt("mode") == plotsObj.getInt("mode") && isStringSame(poleObj.getString("prices"),plotsObj.getString("prices"))
						&& isStringSame(poleObj.getString("openType"),plotsObj.getString("openType")) && poleObj.getInt("availableState") == plotsObj.getInt("availableState")
						&& isStringSame(poleObj.getString("manufacturer"),plotsObj.getString("manufacturer")) && poleObj.getInt("voltage") == plotsObj.getInt("voltage")) {
						flag = false;
						break;
					}
				}
				if (flag) {
					return diffList;
				}
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiGasStation(JSONObject mongoPoi, JSONObject gasStationObj) throws Exception {
		JSONObject gasStation = mongoPoi.getJSONObject("gasStation");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("gasStation",gasStation,gasStationObj));
		if (gasStation.isNullObject() || gasStation.isEmpty()) {
			return diffList;
		} else {
			if (!isStringSame(gasStation.getString("servicePro"),gasStationObj.getString("servicePro")) || !isStringSame(gasStation.getString("service"),gasStationObj.getString("service"))
				|| !isStringSame(gasStation.getString("openHour"),gasStationObj.getString("openHour")) || !isStringSame(gasStation.getString("egType"),gasStationObj.getString("egType")) 
				|| !isStringSame(gasStation.getString("fuelType"),gasStationObj.getString("fuelType")) || !isStringSame(gasStation.getString("payment"),gasStationObj.getString("payment"))
				|| !isStringSame(gasStation.getString("mgType"),gasStationObj.getString("mgType")) || !isStringSame(gasStation.getString("oilType"),gasStationObj.getString("oilType"))) {
				return diffList;
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiAttraction(JSONObject mongoPoi, JSONObject attractionObj) throws Exception {
		JSONObject attraction = mongoPoi.getJSONObject("attraction");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("attraction",attraction,attractionObj));
		if (attraction.isNullObject() || attraction.isEmpty()) {
			return diffList;
		} else {
			if (!isStringSame(attraction.getString("ticketPrice"),attractionObj.getString("ticketPrice")) || attraction.getInt("sightLevel") != attractionObj.getInt("sightLevel")
				|| !isStringSame(attraction.getString("openHour"),attractionObj.getString("openHour")) || !isStringSame(attraction.getString("description"),attractionObj.getString("description"))
				|| attraction.getInt("parking") != attractionObj.getInt("parking")) {
				return diffList;
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiRental(JSONObject mongoPoi, JSONObject rentalObj) throws Exception {
		JSONObject rental = mongoPoi.getJSONObject("rental");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("rental",rental,rentalObj));
		if (rental.isNullObject() || rental.isEmpty()) {
			return diffList;
		} else {
			if (!isStringSame(rental.getString("openHour"),rentalObj.getString("openHour")) || !isStringSame(rental.getString("adressDes"),rentalObj.getString("adressDes"))
				|| !isStringSame(rental.getString("howToGo"),rentalObj.getString("howToGo"))) {
				return diffList;
			}
		}
		return null;
	}
	
	private List<DiffField> diffIxPoiHospital(JSONObject mongoPoi, JSONObject hospitalObj) throws Exception {
		JSONObject hospital = mongoPoi.getJSONObject("hospital");
		List<DiffField> diffList = new ArrayList<DiffField>();
		diffList.add(setDiffField("hospital",hospital,hospitalObj));
		if (hospital.isNullObject() || hospital.isEmpty()) {
			return diffList;
		} else {
			if (hospital.getInt("rating") != hospitalObj.getInt("rating")) {
				return diffList;
			}
		}
		return null;
	}
	
	private DiffField setDiffField(String field,Object mongoValue,Object oracleValue) {
		DiffField diffField = new DiffField();
		diffField.setField(field);
		diffField.setMongoValue(mongoValue);
		diffField.setOracleValue(oracleValue);
		return diffField;
	}
    private boolean isStringSame(String str1, String str2) {
        boolean flag = false;

        if (str1 != null && str2 != null && str1.equals(str2)) {
            flag = true;
        } else if (str1 == null && str2 == null) {
            flag = true;
        } else if (str1 == null && "".equals(str2)){
        	flag = true;
        } else if ("".equals(str1) && str2 == null){
        	flag = true;
        }

        return flag;
    }
	private List<DiffField> diffIxPoi(JSONObject mongoPoi, JSONObject orcleIxPoi) {
		// TODO Auto-generated method stub
		List<DiffField> diffList = new ArrayList<DiffField>();
		JSONObject mongoLocation = mongoPoi.getJSONObject("location");
		JSONObject orcleLocation = orcleIxPoi.getJSONObject("location");
		if((mongoLocation.getDouble("longitude")!=orcleLocation.getDouble("longitude")) || (mongoLocation.getDouble("latitude")!=orcleLocation.getDouble("latitude"))){
			diffList.add(setDiffField("location", mongoLocation, orcleLocation));
		}
		String mongoMeshId = mongoPoi.getString("meshid");
		String oracleMeshId = orcleIxPoi.getString("meshid");
		if (!isStringSame(mongoMeshId, oracleMeshId)){
			diffList.add(setDiffField("meshid", mongoMeshId, oracleMeshId));
		}
		String mongoPostCode = mongoPoi.getString("postCode");
		String orclePostCode = orcleIxPoi.getString("postCode");
		if (!isStringSame(mongoPostCode, orclePostCode)){
			diffList.add(setDiffField("postCode", mongoPostCode, orclePostCode));
		}
		String mongoKindCode = mongoPoi.getString("kindCode");
		String orcleKindCode = orcleIxPoi.getString("kindCode");
		if (!isStringSame(mongoKindCode, orcleKindCode)){
			diffList.add(setDiffField("kindCode", mongoKindCode, orcleKindCode));
		}
		String mongoLevel = mongoPoi.getString("level");
		String orcleLevel = orcleIxPoi.getString("level");
		if (!isStringSame(mongoLevel, orcleLevel)){
			diffList.add(setDiffField("level",mongoLevel, orcleLevel));
		}
		int mongoOpen24H = mongoPoi.getInt("open24H");
		int orcleOpen24H = orcleIxPoi.getInt("open24H");
		if (mongoOpen24H != orcleOpen24H){
			diffList.add(setDiffField("open24H",mongoOpen24H, orcleOpen24H));
		}
		int mongoAdminReal = mongoPoi.getInt("adminReal");
		int orcleAdminReal = orcleIxPoi.getInt("adminReal");
		if (mongoAdminReal != orcleAdminReal){
			diffList.add(setDiffField("adminReal",mongoAdminReal, orcleAdminReal));
		}
		int mongoImportance = mongoPoi.getInt("importance");
		int orcleImportance = orcleIxPoi.getInt("importance");
		if (mongoImportance != orcleImportance){
			diffList.add(setDiffField("importance",mongoImportance, orcleImportance));
		}
		String mongoAirPort = mongoPoi.getString("airportCode");
		String orcleAirPort = orcleIxPoi.getString("airportCode");
		if (!isStringSame(mongoAirPort, orcleAirPort)){
			diffList.add(setDiffField("airportCode",mongoAirPort, orcleAirPort));
		}
		String mongoVipFlag = mongoPoi.getString("vipFlag");
		String orcleVipFlag = orcleIxPoi.getString("vipFlag");
		if (!isStringSame(mongoVipFlag, orcleVipFlag)){
			diffList.add(setDiffField("vipFlag",mongoVipFlag, orcleVipFlag));
		}
		int mongoTruck = mongoPoi.getInt("truck");
		int orcleTruck = orcleIxPoi.getInt("truck");
		if (mongoTruck != orcleTruck){
			diffList.add(setDiffField("truck",mongoTruck, orcleTruck));
		}
		JSONObject mongoSportsVenues = mongoPoi.getJSONObject("sportsVenues");
		String orcleSportsVenues = orcleIxPoi.getString("sportsVenues");
		if (mongoSportsVenues.isEmpty() || mongoSportsVenues.isNullObject() || !mongoSportsVenues.containsKey("buildingType")){
			diffList.add(setDiffField("sportsVenues",null,orcleSportsVenues));
		}else{
			String sportsVenues = mongoSportsVenues.getString("buildingType");
			if (!isStringSame(sportsVenues, orcleSportsVenues)){
				diffList.add(setDiffField("sportsVenues",sportsVenues,orcleSportsVenues));
			}
		}
		JSONObject mongoVerifyFlags = mongoPoi.getJSONObject("verifyFlags");
		String orcleVerifyFlags = orcleIxPoi.getString("verifyFlags");
		if (mongoVerifyFlags.isEmpty() || mongoVerifyFlags.isNullObject() || !mongoVerifyFlags.containsKey("record")){
			diffList.add(setDiffField("verifyFlags",null,orcleVerifyFlags));
		}else{
			String verifyFlags = mongoVerifyFlags.getString("record");
			if (!isStringSame(verifyFlags, orcleVerifyFlags)){
				diffList.add(setDiffField("verifyFlags",verifyFlags,orcleVerifyFlags));
			}
		}
		String mongoName = mongoPoi.getString("name");
		String orcleName =  orcleIxPoi.getString("name");
		if (!isStringSame(mongoName, orcleName)){
			diffList.add(setDiffField("name",mongoName,orcleName));
		}
		String mongoAdd = mongoPoi.getString("address");
		String orcleAdd =  orcleIxPoi.getString("address");
		if (!isStringSame(mongoAdd, orcleAdd)){
			diffList.add(setDiffField("address",mongoAdd,orcleAdd));
		}
		String mongoAdminCode = mongoPoi.getString("adminCode");
		String orcleAdminCode =  orcleIxPoi.getString("adminCode");
		if (!isStringSame(mongoAdminCode, orcleAdminCode)){
			diffList.add(setDiffField("adminCode",mongoAdminCode,orcleAdminCode));
		}
		String mongoWebsite = mongoPoi.getString("website");
		String orcleWebsite =  orcleIxPoi.getString("website");
		if (!isStringSame(mongoWebsite, orcleWebsite)){
			diffList.add(setDiffField("website",mongoWebsite,orcleWebsite));
		}
		int mongoHwEntryExit = mongoPoi.getInt("hwEntryExit");
		int orcleHwEntryExit = orcleIxPoi.getInt("hwEntryExit");
		if (mongoHwEntryExit != orcleHwEntryExit){
			diffList.add(setDiffField("hwEntryExit",mongoHwEntryExit,orcleHwEntryExit));
		}
		
		return diffList;
	}
	//处理联系方式表
	private Map<String, JSONArray> queryIxPoiContact(Connection conn) throws Exception {
		Map<String, JSONArray> ixPoiContactMap = new HashMap<String, JSONArray>();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT P.POI_NUM fid,N.* FROM IX_POI POI,IX_POI_CONTACT N," + this.inParam.getDiffFidTempTableName()
				+ " C");
		sb.append("WHERE C.FID = P.POI_NUM AND P.PID = N.POI_PID AND P.U_RECORD <> 2 AND N.U_RECORD <> 2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String fid = resultSet.getString("FID");
				JSONArray contacts = new JSONArray();
				JSONObject contact = new JSONObject();
				if (ixPoiContactMap.containsKey(fid)) {
					contacts = ixPoiContactMap.get(fid);
				}
				contact.put("number", resultSet.getString("CONTACT"));
				contact.put("type", resultSet.getInt("CONTACT_TYPE"));
				contact.put("priority", resultSet.getInt("PRIORITY"));
				contacts.add(contact);
				ixPoiContactMap.put(fid, contacts);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
		}

		return null;
	}
	private Map<String, JSONArray> queryBusinessTimes(Connection conn) throws SQLException {
		Map<String, JSONArray> ixPoiBusinessTimesMap = new HashMap<String, JSONArray>();
		StringBuilder sb = new StringBuilder();
		sb.append("select p.poi_num fid,");
		sb.append(" nvl(b.MON_SRT,'') MON_SRT,nvl(b.MON_END, '') MON_END,nvl(b.WEEK_IN_YEAR_SRT, '') WEEK_IN_YEAR_SRT,nvl(b.WEEK_IN_YEAR_END, '') WEEK_IN_YEAR_END,nvl(b.WEEK_IN_MONTH_SRT, '') WEEK_IN_MONTH_SRT,nvl(b.WEEK_IN_MONTH_END, '') WEEK_IN_MONTH_END,nvl(b.VALID_WEEK, '') VALID_WEEK,nvl(b.DAY_SRT, '') DAY_SRT,nvl(b.DAY_END, '') DAY_END,nvl(b.TIME_SRT, '') TIME_SRT,nvl(b.TIME_DUR, '') TIME_DUR ");
		sb.append(" from ix_poi p, IX_POI_BUSINESSTIME b," + this.inParam.getDiffFidTempTableName() + " c");
		sb.append(" where c.fid = p.poi_num");
		sb.append(" and p.pid = b.poi_pid");
		sb.append(" and p.u_record != 2");
		sb.append(" and b.u_record != 2");
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String fid = resultSet.getString("fid");
				String monStart = resultSet.getString("MON_SRT");
				String monEnd = resultSet.getString("MON_END");
				String weekStartYear = resultSet.getString("WEEK_IN_YEAR_SRT");
				String weekEndYear = resultSet.getString("WEEK_IN_YEAR_END");
				String weekStartMonth = resultSet.getString("WEEK_IN_MONTH_SRT");
				String weekEndMonth = resultSet.getString("WEEK_IN_MONTH_END");
				String validWeek = resultSet.getString("VALID_WEEK");
				String dayStart = resultSet.getString("DAY_SRT");
				String dayEnd = resultSet.getString("DAY_END");
				String timeStart = resultSet.getString("TIME_SRT");
				String timeDuration = resultSet.getString("TIME_DUR");
				JSONArray businessTimes = new JSONArray();
				JSONObject businessTime = new JSONObject();
				if (ixPoiBusinessTimesMap.containsKey(fid)){
					businessTimes = ixPoiBusinessTimesMap.get(fid);
				}
				businessTime.put("monStart", monStart);
				businessTime.put("monEnd", monEnd);
				businessTime.put("weekStartYear", weekStartYear);
				businessTime.put("weekEndYear", weekEndYear);
				businessTime.put("weekStartMonth", weekStartMonth);
				businessTime.put("weekEndMonth", weekEndMonth);
				businessTime.put("validWeek", validWeek);
				businessTime.put("dayStart", dayStart);
				businessTime.put("dayEnd", dayEnd);
				businessTime.put("timeStart", timeStart);
				businessTime.put("timeDuration", timeDuration);
				businessTimes.add(businessTime);
				ixPoiBusinessTimesMap.put(fid, businessTimes);
				
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if(pstmt != null){pstmt.close();}
			if(resultSet != null){resultSet.close();}
		}
		return ixPoiBusinessTimesMap;
	}
	private Map<String, JSONArray> queryIxPoiAddress(Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		Map<String, JSONArray> ixPoiAddressMap = new HashMap<String, JSONArray>();
		StringBuilder sb = new StringBuilder();
		sb.append("select p.poi_num fid,");
		sb.append(" nvl(d.LANG_CODE,'') LANG_CODE,nvl(d.FULLNAME, '') FULLNAME,nvl(d.PROVINCE, '') PROVINCE,nvl(d.CITY, '') CITY,nvl(d.COUNTY, '') COUNTY,nvl(d.TOWN, '') TOWN,nvl(d.PLACE, '') PLACE,nvl(d.STREET, '') STREET,nvl(d.LANDMARK, '') LANDMARK,nvl(d.PREFIX, '') PREFIX,nvl(d.HOUSENUM, '') HOUSENUM,nvl(d.TYPE, '') TYPE,nvl(d.SUBNUM, '') SUBNUM,nvl(d.SURFIX, '') SURFIX,nvl(d.ESTAB, '') ESTAB,nvl(d.BUILDING, '') BUILDING,nvl(d.FLOOR, '') FLOOR,nvl(d.UNIT, '') UNIT,nvl(d.ROOM, '') ROOM,nvl(d.ADDONS, '') ADDONS,");
		sb.append(" nvl(d.FULLNAME_PHONETIC, '') FULLNAME_PHONETIC,nvl(d.PROV_PHONETIC, '') PROV_PHONETIC,nvl(d.CITY_PHONETIC, '') CITY_PHONETIC,nvl(d.COUNTY_PHONETIC, '') COUNTY_PHONETIC,nvl(d.TOWN_PHONETIC, '') TOWN_PHONETIC,nvl(d.STREET_PHONETIC, '') STREET_PHONETIC,nvl(d.PLACE_PHONETIC, '') PLACE_PHONETIC,nvl(d.LANDMARK_PHONETIC, '') LANDMARK_PHONETIC,nvl(d.PREFIX_PHONETIC, '') PREFIX_PHONETIC,nvl(d.HOUSENUM_PHONETIC, '') HOUSENUM_PHONETIC,nvl(d.TYPE_PHONETIC, '') TYPE_PHONETIC,nvl(d.SUBNUM_PHONETIC, '') SUBNUM_PHONETIC,nvl(d.SURFIX_PHONETIC, '') SURFIX_PHONETIC,nvl(d.ESTAB_PHONETIC, '') ESTAB_PHONETIC,nvl(d.BUILDING_PHONETIC, '') BUILDING_PHONETIC,nvl(d.FLOOR_PHONETIC, '') FLOOR_PHONETIC,nvl(d.UNIT_PHONETIC, '') UNIT_PHONETIC,nvl(d.ROOM_PHONETIC, '') ROOM_PHONETIC,nvl(d.ADDONS_PHONETIC, '') ADDONS_PHONETIC ");
		sb.append(" from ix_poi p, ix_poi_address d," + this.inParam.getDiffFidTempTableName() + " c");
		sb.append(" where c.fid = p.poi_num");
		sb.append(" and p.pid = d.poi_pid");
		sb.append(" and p.u_record != 2");
		sb.append(" and d.u_record != 2");
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String fid = resultSet.getString("fid");
				JSONArray addresses = new JSONArray();
				JSONObject address = new JSONObject();
				String roadName = resultSet.getString("PROVINCE")+"|"+resultSet.getString("CITY")+"|"+resultSet.getString("COUNTY")+"|"
									+resultSet.getString("TOWN")+"|"+resultSet.getString("PLACE")+"|"+resultSet.getString("STREET");
				if ("|||||".equals(roadName)){roadName="";}
				String roadNamePinyin = resultSet.getString("PROV_PHONETIC")+"|"+resultSet.getString("CITY_PHONETIC")+"|"+resultSet.getString("COUNTY_PHONETIC")+"|"
										+resultSet.getString("TOWN_PHONETIC")+"|"+resultSet.getString("PLACE_PHONETIC")+"|"+resultSet.getString("STREET_PHONETIC");
				if ("|||||".equals(roadNamePinyin)){roadNamePinyin="";}
				String addrName = resultSet.getString("LANDMARK")+"|"+resultSet.getString("PREFIX")+"|"+resultSet.getString("HOUSENUM")+"|"
									+resultSet.getString("TYPE")+"|"+resultSet.getString("SUBNUM")+"|"+resultSet.getString("SURFIX")+"|"
									+resultSet.getString("ESTAB")+"|"+resultSet.getString("BUILDING")+"|"+resultSet.getString("FLOOR")+"|"
									+resultSet.getString("UNIT")+"|"+resultSet.getString("ROOM")+"|"+resultSet.getString("ADDONS");
				if ("|||||||||||".equals(addrName)){addrName="";}
				String addrNamePinyin = resultSet.getString("LANDMARK_PHONETIC")+"|"+resultSet.getString("PREFIX_PHONETIC")+"|"+resultSet.getString("HOUSENUM_PHONETIC")+"|"
										+resultSet.getString("TYPE_PHONETIC")+"|"+resultSet.getString("SUBNUM_PHONETIC")+"|"+resultSet.getString("SURFIX_PHONETIC")+"|"
										+resultSet.getString("ESTAB_PHONETIC")+"|"+resultSet.getString("BUILDING_PHONETIC")+"|"+resultSet.getString("FLOOR_PHONETIC")+"|"
										+resultSet.getString("UNIT_PHONETIC")+"|"+resultSet.getString("ROOM_PHONETIC")+"|"+resultSet.getString("ADDONS_PHONETIC");
				if ("|||||||||||".equals(addrNamePinyin)){addrNamePinyin="";}
				if (ixPoiAddressMap.containsKey(fid)){
					addresses = ixPoiAddressMap.get(fid);
				}
				address.put("langCode", resultSet.getString("LANG_CODE"));
				address.put("fullName", resultSet.getString("FULLNAME"));
				address.put("fullNamePinyin", resultSet.getString("FULLNAME_PHONETIC"));
				address.put("roadName", roadName);
				address.put("roadNamePinyin", roadNamePinyin);
				address.put("addrName", addrName);
				address.put("addrNamePinyin", addrNamePinyin);
				addresses.add(address);
				ixPoiAddressMap.put(fid, addresses);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(pstmt != null){pstmt.close();}
			if(resultSet != null){resultSet.close();}
		}
		return ixPoiAddressMap;
	}
	private Map<String, JSONArray> queryIxPoiName(Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		Map<String, JSONArray> ixPoiNameMap = new HashMap<String, JSONArray>();
		StringBuilder sb = new StringBuilder();
		sb.append("select p.poi_num fid, n.*");
		sb.append(" from ix_poi p, ix_poi_name n," + this.inParam.getDiffFidTempTableName() + " c");
		sb.append(" where c.fid = p.poi_num");
		sb.append(" and p.pid = n.poi_pid");
		sb.append(" and p.u_record != 2");
		sb.append(" and n.u_record != 2");
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String fid = resultSet.getString("fid");
				JSONArray names = new JSONArray();
				JSONObject name = new JSONObject();
				if (ixPoiNameMap.containsKey(fid)){
					names = ixPoiNameMap.get(fid);
				}
				name.put("nameGrpId", resultSet.getInt("NAME_GROUPID"));
				name.put("type", resultSet.getInt("NAME_TYPE"));
				name.put("nameClass", resultSet.getInt("NAME_CLASS"));
				name.put("nameStrPinyin", resultSet.getString("NAME_PHONETIC")!=null?resultSet.getString("NAME_PHONETIC"):"");
				name.put("nameStr", resultSet.getString("NAME")!=null?resultSet.getString("NAME"):"");
				name.put("langCode", resultSet.getString("LANG_CODE")!=null?resultSet.getString("LANG_CODE"):"");
				names.add(name);
				ixPoiNameMap.put(fid, names);
			}		
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if(pstmt != null){pstmt.close();}
			if(resultSet != null){resultSet.close();}
		}
		return ixPoiNameMap;
	}
	private Map<String, List<String>> queryIxPoiChildren(Connection conn) throws Exception {
		Map<String, List<String>> retMap = new HashMap<String,List<String>>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select i.poi_num fid,(select poi_num from ix_poi where pid=c.child_poi_pid) cfid");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+"  f,ix_poi i,ix_poi_parent p,ix_poi_children c");
		sb.append(" where f.fid=i.poi_num");
		sb.append(" and i.pid=p.parent_poi_pid");
		sb.append(" and p.group_id=c.group_id");
		sb.append(" and i.u_record != 2");
		sb.append(" and p.u_record != 2");
		sb.append(" and c.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String fid = resultSet.getString("fid");
				List<String> cfids = new ArrayList<String>();
				if (retMap.containsKey(fid)) {
					cfids = retMap.get(fid);
				}
				cfids.add(resultSet.getString("cfid"));
				retMap.put(fid, cfids);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, String> queryIxPoiParent(Connection conn) throws Exception {
		
		Map<String, String> retMap = new HashMap<String,String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select i.poi_num fid,(select poi_num from ix_poi where pid=p.parent_poi_pid) pfid");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+"  f,ix_poi i,ix_poi_children c,ix_poi_parent p");
		sb.append(" where f.fid=i.poi_num");
		sb.append(" and i.pid=c.child_poi_pid");
		sb.append(" and c.group_id=p.group_id");
		sb.append(" and i.u_record != 2");
		sb.append(" and c.u_record != 2");
		sb.append(" and p.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				retMap.put(resultSet.getString("fid"), resultSet.getString("pfid"));
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONObject> queryIxPoi(Connection conn) throws SQLException {
		Map<String, JSONObject> IxPoiMap = new HashMap<String, JSONObject>();
		StringBuilder sb = new StringBuilder();
		sb.append("select p.geometry.sdo_point.x locationX,p.geometry.sdo_point.y locationY,p.*,");
		sb.append(" (select a.admin_id from ad_admin a where p.region_id=a.region_id) admin_id,");
		sb.append(" (select n.name from ix_poi_name n where n.poi_pid=p.pid and n.name_class=1 and n.name_type = 2 and n.lang_code = 'CHI'and n.u_record != 2) name,");
		sb.append(" (select ad.fullname from ix_poi_address ad where ad.poi_pid=p.pid and ad.lang_code='CHI' and ad.u_record!=2) address,");
		sb.append(" (select de.web_site from ix_poi_detail de where de.poi_pid=p.pid and de.u_record!=2) web_site,");
		sb.append(" (select de.hw_entryexit from ix_poi_detail de where de.poi_pid=p.pid and de.u_record!=2) hw_entryexit");
		sb.append(" from ix_poi p,"+this.inParam.getDiffFidTempTableName() + " b");
		sb.append(" where p.poi_num = b.fid");
		sb.append(" and p.u_record != 2");
				
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				String fid = resultSet.getString("poi_num");
				JSONObject poi = new JSONObject();
				//location
				JSONObject location = new JSONObject();
				location.put("longitude", resultSet.getDouble("locationX"));
				location.put("latitude", resultSet.getDouble("locationY"));
				poi.put("location", location);
//				//guide 先不比较
//				JSONObject guide = new JSONObject();
//				guide.put("linkPid", resultSet.getInt("link_pid"));
//				guide.put("longitude", resultSet.getDouble("x_guide"));
//				guide.put("latitude", resultSet.getDouble("y_guide"));
//				poi.put("guide", guide);
				//fid
				poi.put("fid", fid);
				//meshid
				poi.put("meshid", resultSet.getString("MESH_ID")!=null?resultSet.getString("MESH_ID"):"");
				//postCode
				poi.put("postCode", resultSet.getString("POST_CODE")!=null?resultSet.getString("POST_CODE"):"");
				//kindCode
				poi.put("kindCode", resultSet.getString("KIND_CODE")!=null?resultSet.getString("KIND_CODE"):"");
				//level
				poi.put("level", resultSet.getString("LEVEL")!=null?resultSet.getString("LEVEL"):"");
				//open24H
				poi.put("open24H", resultSet.getInt("OPEN_24H"));
				//adminReal
				poi.put("adminReal", resultSet.getInt("ADMIN_REAL"));
				//importance
				poi.put("importance", resultSet.getInt("IMPORTANCE"));
				//airportCode
				poi.put("airportCode", resultSet.getString("AIRPORT_CODE")!=null?resultSet.getString("AIRPORT_CODE"):"");
				//vipFlag
				poi.put("vipFlag", resultSet.getString("VIP_FLAG")!=null?resultSet.getString("VIP_FLAG"):"");
				//truck
				poi.put("truck", resultSet.getInt("TRUCK_FLAG"));
				//sportsVenues
				poi.put("sportsVenues", resultSet.getString("SPORTS_VENUE")!=null?resultSet.getString("SPORTS_VENUE"):"");
				//verifyFlags
				poi.put("verifyFlags", resultSet.getString("VERIFIED_FLAG")!=null?resultSet.getString("VERIFIED_FLAG"):"");
				//name
				poi.put("name", resultSet.getString("NAME")!=null?resultSet.getString("NAME"):"");
				//address
				poi.put("address", resultSet.getString("ADDRESS")!=null?resultSet.getString("ADDRESS"):"");
				//adminCode
				poi.put("adminCode", resultSet.getString("ADMIN_ID")!=null?resultSet.getString("ADMIN_ID"):"");
				//website
				poi.put("website", resultSet.getString("WEB_SITE")!=null?resultSet.getString("WEB_SITE"):"");
				//hwEntryExit
				poi.put("hwEntryExit", resultSet.getInt("HW_ENTRYEXIT"));

				IxPoiMap.put(fid, poi);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			if (pstmt != null){
				pstmt.close();
			}
			if (resultSet != null){
				resultSet.close();
			}
		}
		return IxPoiMap;
	}
	
	private Map<String, JSONObject> queryIxPoiCharigngStation(Connection conn) throws Exception {
		Map<String, JSONObject> retMap = new HashMap<String,JSONObject>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,s.charging_type,s.change_brands,s.change_open_type,s.charging_num,s.service_prov,s.open_hour");
		sb.append(",s.parking_fees,s.parking_info,s.available_state");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_chargingstation s");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=s.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and s.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("chargingType", resultSet.getInt("charging_type"));
				jsonObj.put("changeBrands", resultSet.getString("change_brands"));
				jsonObj.put("changeOpenType", resultSet.getString("change_open_type"));
				jsonObj.put("chargingNum", resultSet.getInt("charging_num"));
				jsonObj.put("serviceProv", resultSet.getString("service_prov"));
				jsonObj.put("openHour", resultSet.getString("openHour"));
				jsonObj.put("parkingFees", resultSet.getInt("parking_fees"));
				jsonObj.put("parkingInfo", resultSet.getString("parking_info"));
				jsonObj.put("availableState", resultSet.getInt("available_state"));
				retMap.put(resultSet.getString("fid"), jsonObj);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONArray> queryIxPoiChargingPlot(Connection conn) throws Exception {
		Map<String, JSONArray> retMap = new HashMap<String,JSONArray>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,p.*");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_chargingplot p");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=p.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and p.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("plugType", resultSet.getString("plug_type"));
				jsonObj.put("productNum", resultSet.getString("product_num"));
				jsonObj.put("power", resultSet.getString("power"));
				jsonObj.put("floor", resultSet.getInt("floor"));
				jsonObj.put("factoryNum", resultSet.getString("factory_num"));
				jsonObj.put("locationType", resultSet.getInt("location_type"));
				jsonObj.put("parkingNum", resultSet.getString("parking_num"));
				jsonObj.put("acdc", resultSet.getInt("acdc"));
				jsonObj.put("payment", resultSet.getString("payment"));
				jsonObj.put("current", resultSet.getString("current"));
				jsonObj.put("plotNum", resultSet.getString("plot_num"));
				jsonObj.put("plugNum", resultSet.getInt("plug_num"));
				jsonObj.put("mode", resultSet.getInt("mode"));
				jsonObj.put("prices", resultSet.getString("prices"));
				jsonObj.put("openType", resultSet.getString("open_type"));
				jsonObj.put("availableState", resultSet.getInt("available_state"));
				jsonObj.put("manufacturer", resultSet.getString("manufacturer"));
				jsonObj.put("voltage", resultSet.getInt("voltage"));
				String fid = resultSet.getString("fid");
				JSONArray plots = new JSONArray();
				if (retMap.containsKey(fid)) {
					plots = retMap.get(fid);
				}
				plots.add(jsonObj);
				retMap.put(fid, plots);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONObject> queryIxPoiGasStation(Connection conn) throws Exception {
		Map<String, JSONObject> retMap = new HashMap<String,JSONObject>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,g.* ");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_gasstation g");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=g.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and g.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("servicePro", resultSet.getString("service_prov"));
				jsonObj.put("service", resultSet.getString("service"));
				jsonObj.put("openHour", resultSet.getString("open_hour"));
				jsonObj.put("egType", resultSet.getString("eg_type"));
				jsonObj.put("fuelType", resultSet.getString("fuel_type"));
				jsonObj.put("payment", resultSet.getString("payment"));
				jsonObj.put("mgType", resultSet.getString("mg_type"));
				jsonObj.put("oilType", resultSet.getString("oil_type"));
				retMap.put(resultSet.getString("fid"), jsonObj);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONObject> queryIxPoiAttraction(Connection conn) throws Exception {
		Map<String, JSONObject> retMap = new HashMap<String,JSONObject>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,a.ticket_price,a.sight_level,a.open_hour,a.long_description,a.parking");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_attraction a");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=a.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and a.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("ticketPrice", resultSet.getString("ticket_price"));
				jsonObj.put("sightLevel", resultSet.getInt("sight_level"));
				jsonObj.put("openHour", resultSet.getString("open_hour"));
				jsonObj.put("description", resultSet.getString("long_description"));
				jsonObj.put("parking", resultSet.getInt("parking"));
				retMap.put(resultSet.getString("fid"), jsonObj);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONObject> queryIxPoiRental(Connection conn) throws Exception {
		Map<String, JSONObject> retMap = new HashMap<String,JSONObject>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,r.open_hour,r.address,r.how_to_go");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_carrental r");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=r.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and r.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("openHour", resultSet.getString("ticket_price"));
				jsonObj.put("adressDes", resultSet.getString("sight_level"));
				jsonObj.put("howToGo", resultSet.getString("open_hour"));
				retMap.put(resultSet.getString("fid"), jsonObj);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String, JSONObject> queryIxPoiHospital(Connection conn) throws Exception {
		Map<String, JSONObject> retMap = new HashMap<String,JSONObject>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c.fid,d.hospital_class");
		sb.append(" from "+this.inParam.getDiffFidTempTableName()+" c,ix_poi i,ix_poi_detail d");
		sb.append(" where c.fid=i.poi_num");
		sb.append(" and i.pid=d.poi_pid");
		sb.append(" and i.u_record != 2");
		sb.append(" and d.u_record != 2");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("rating", resultSet.getInt("hospital_class"));
				retMap.put(resultSet.getString("fid"), jsonObj);
			}
			return retMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}
	
	private Map<String,JSONObject> queryFromMongo() throws Exception {
		// TODO 从mongodb中查询获取poi数据；
		logger.info("Get diff fidList");
		List<String> fidList = getDiffFidList();
		logger.info("MongoDB init start");
		MongoClient mongoClient = new MongoClient(this.inParam.getMongodbHost(), this.inParam.getMongoPort());
		MongoCollection<Document> mongoDB = mongoClient.getDatabase(this.inParam.mongodbDbName).getCollection(this.inParam.mongodbCollectionName);
		logger.info("MongoDB init success");
		BasicDBObject condition = new BasicDBObject();
		condition.put("fid",new BasicDBObject(QueryOperators.IN, fidList));
		logger.info("MongoDB find data");
		MongoCursor<Document> cur = mongoDB.find(condition).iterator();
		
		Map<String,JSONObject> mongoData = new HashMap<String,JSONObject>();
		while(cur.hasNext()){
			JSONObject poi = JSONObject.fromObject(cur.next());
			String fid = poi.getString("fid");
			int pid = poi.getInt("pid");
			if (StringUtils.isNotEmpty(fid)){
				mongoData.put(fid, poi);
			}else{
				System.out.println("Mongo库中数据fid为空，不进行差分比较，pid:"+ pid);
				logger.info("Mongo库中数据fid为空，不进行差分比较，pid:"+ pid);
			}
		}
		return mongoData;
	}
	private List<String> getDiffFidList() throws Exception{
		Connection conn = getConnection();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		String sql = "select c.fid from " + this.inParam.getDiffFidTempTableName() +" c";
		List<String> fidList = new ArrayList<String>();
		try{
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				String fid = resultSet.getString("fid");
				if (StringUtils.isNotEmpty(fid)){
					fidList.add(fid);
				}
			}
		}catch(Exception e){
			conn.rollback();
			e.printStackTrace();
		}finally{
			if (conn != null){
				conn.close();
			}
			if (pstmt != null){
				pstmt.close();
			}
			if (resultSet != null){
				resultSet.close();
			}
		}
		return fidList;
	}

	private Connection getConnection() throws Exception {
		String driver = "oracle.jdbc.driver.OracleDriver";  
        String url = "jdbc:Oracle:thin:@"+this.inParam.getOralceHost()+":"+this.inParam.getOraclePort()+":"+this.inParam.getOracleSid();
        Class.forName(driver);  
        return DriverManager.getConnection(url, this.inParam.getOracleUser(), this.inParam.getOraclePwd());
	}
	private static Param parseArgs(String[] args) {
		Param param = new Param();
		param.setMongodbHost(args[0]);//
		param.setMongoPort(Integer.parseInt(args[1]));//
		param.setMongodbDbName(args[2]);//
		param.setMongodbCollectionName(args[3]);
		//TODO:解析其他参数
		param.setOralceHost(args[4]);
		param.setOraclePort(args[5]);
		param.setOracleSid(args[6]);
		param.setOracleUser(args[7]);
		param.setOraclePwd(args[8]);
		param.setDiffFidTempTableName(args[9]);
		param.setOutputFile(args[10]);
		return param;
		
	}
	

}
