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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils;

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
	 * 输出：差分json格式如下
	 * {
	 * "fid":"32fdfdf",//差分的主键
	 * "diffResult":[
	 * {"field":"name","mongoValue":"金红果园山庄","oracleValue":"红果园山庄"},	//poi字段属性的差分结果
	 * {"field":"relateChildren","mongoValue":[],"oracleValue":[]},
	 * {"field":"names","mongoValue":[],"oracleValue":[]},
	 * ]
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
			//TODO:查询其他子表
			//执行mongdb和oracle数据的差分比较
			List<JSONObject> outResult = new ArrayList<JSONObject>();
			for (String fid : mongoData.keySet()){
				logger.info("开始比较:fid="+fid);
				List<DiffField> diffFields  =new ArrayList<DiffField>();
				
				logger.info("比较主表属性");
				JSONObject mongoPoi = mongoData.get(fid);
				if (ixPoiMap.containsKey(fid)){
					JSONObject orcleIxPoi = ixPoiMap.get(fid);
					List<DiffField> diffIxPoiResult = diffIxPoi(mongoPoi,orcleIxPoi);
					if(diffIxPoiResult!=null){
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
					if (relateParent != null && relateParent.containsKey("parentFid")) {
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
				
				if (CollectionUtils.isEmpty(diffFields)) continue;
				//TODO:比较其他的子表属性
				
				DiffResult diffResult= new DiffResult(fid,diffFields);
				
				// oracle数据不存在
				if (!ixPoiMap.containsKey(fid)){
					diffResult.setOracleExists(0);
				}
				outResult.add(JSONObject.fromObject(diffResult));
			}
			//TODO:把DiffResult输出到文件
			logger.info("差分结果t输出至txt");
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
	private Map<String, JSONArray> queryIxPoiAddress(Connection conn) {
		// TODO Auto-generated method stub
		return null;
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
					name.put("nameGrpId", resultSet.getInt("NAME_GROUPID"));
					name.put("type", resultSet.getInt("NAME_TYPE"));
					name.put("nameClass", resultSet.getInt("NAME_CLASS"));
					name.put("nameStrPinyin", resultSet.getString("NAME_PHONETIC")!=null?resultSet.getString("NAME_PHONETIC"):"");
					name.put("nameStr", resultSet.getString("NAME")!=null?resultSet.getString("NAME"):"");
					name.put("langCode", resultSet.getString("LANG_CODE")!=null?resultSet.getString("LANG_CODE"):"");
					names.add(name);
					ixPoiNameMap.put(fid, names);
				}else{
					name.put("nameGrpId", resultSet.getInt("NAME_GROUPID"));
					name.put("type", resultSet.getInt("NAME_TYPE"));
					name.put("nameClass", resultSet.getInt("NAME_CLASS"));
					name.put("nameStrPinyin", resultSet.getString("NAME_PHONETIC")!=null?resultSet.getString("NAME_PHONETIC"):"");
					name.put("nameStr", resultSet.getString("NAME")!=null?resultSet.getString("NAME"):"");
					name.put("langCode", resultSet.getString("LANG_CODE")!=null?resultSet.getString("LANG_CODE"):"");
					names.add(name);
					ixPoiNameMap.put(fid, names);
				}
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
					cfids.add(resultSet.getString("cfid"));
					retMap.put(fid, cfids);
				} else {
					cfids.add(resultSet.getString("cfid"));
					retMap.put(fid, cfids);
				}
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
