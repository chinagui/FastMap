package com.navinfo.dataservice.scripts.tmp.diff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

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
	 * orclHost:
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
			Map<String,JSONArray> ixPoiChildrenMap = queryIxPoiChildren(conn);
			Map<String,JSONArray> ixPoiNameMap = queryIxPoiName(conn);
			Map<String,JSONArray> ixPoiAddressMap = queryIxPoiAddress(conn);
			//TODO:查询其他子表
			//执行mongdb和oracle数据的差分比较
			for (String fid : mongoData.keySet()){
				logger.info("开始比较:fid="+fid);
				List<DiffField> diffFields  =new ArrayList<DiffField>();
				logger.info("比较主表属性");
				JSONObject mongoPoi = mongoData.get(fid);
				JSONObject orcleIxPoi = ixPoiMap.get(fid);
				List<DiffField> diffIxPoiResult = diffIxPoi(mongoPoi,orcleIxPoi);
				if(diffIxPoiResult!=null){
					diffFields.addAll(diffIxPoiResult);
				}
				logger.info("比较relateParent属性");
				List<DiffField> diffIxPoiParentResult = diffIxPoiParent(mongoPoi,orcleIxPoi);
				if(diffIxPoiResult!=null){
					diffFields.addAll(diffIxPoiParentResult);
				}
				if (CollectionUtils.isEmpty(diffFields)) continue;
				//TODO:比较其他的子表属性
				DiffResult diffResult= new DiffResult(fid,diffFields);
				//TODO:把DiffResult输出到文件
				writeDiffResult(diffResult);
			}
			
		}finally{
			if(conn!=null){conn.close();}
		}
		
	}
	
	private void writeDiffResult(DiffResult diffResult) {
		
		
	}
	private List<DiffField> diffIxPoiParent(JSONObject mongoPoi, JSONObject orcleIxPoi) {
		// TODO Auto-generated method stub
		return null;
	}
	private List<DiffField> diffIxPoi(JSONObject mongoPoi, JSONObject orcleIxPoi) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, JSONArray> queryIxPoiAddress(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, JSONArray> queryIxPoiName(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, JSONArray> queryIxPoiChildren(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, String> queryIxPoiParent(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, JSONObject> queryIxPoi(Connection conn) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String,JSONObject> queryFromMongo() {
		// TODO 从mongodb中查询获取poi数据；
		return null;
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
		//TODO:解析其他参数
		return param;
		
	}
	

}
