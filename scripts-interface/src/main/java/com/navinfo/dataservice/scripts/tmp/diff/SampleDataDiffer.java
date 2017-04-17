package com.navinfo.dataservice.scripts.tmp.diff;

import java.util.List;

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
	 * fieldVerification  ？？
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
	 * parkings				IX_POI_PARKING//数量不同，则不同；如果数量相同，但是全字段匹配，匹配则ok；否则不ok
	 * hotel				IX_POI_HOTEL //数量不同，则不同；如果数量相同，但是全字段匹配，匹配则ok；否则不ok
	 * chargingStation		IX_POI_CHARGINGSTATION
	 * chargingPole			IX_POI_CHARGINGPLOT
	 * gasStation			IX_POI_GASSTATION
	 * attraction			IX_POI_ATTRACTION
	 * rental				IX_POI_CARRENTAL
	 * hospital				IX_POI.DETAIL.HOSPITAL_CLASS
	 * indoor.type			IX_POI.INDOOR 如果是1，则转为3；否则原值转出；
	 * indoor.floor			IX_POI_ADDRESS.FLOOR
	 * attachments			IX_POI_PHOTO 只比较数量
	 * brands.code			??
	 * freshnessVerification POI_EDIT_STATUS.FRESH_VERIFIED
	 * sourceFlags			??
	 * @param args:
	 * 输入参数
	 * mongodbHost:
	 * mongodbPort:
	 * mongodbDbName:
	 * mongodbCollectioinName
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
	 * 
	 */
	public static void main(String[] args) {
		
		logger.info("解析获取参数");
		Param inParam = parseArgs(args);
		logger.info(inParam);
		SampleDataDiffer differ = new SampleDataDiffer();
		differ.inParam = inParam;
		logger.info("查询 未导入到一体化的fid列表");
		List<String> notImpFidList = differ.findNotImpFids();
		logger.info("相同fid的数据开始差分");
		differ.doDiff(notImpFidList);

	}
	private  void doDiff(List<String> notImpFidList) {
		// TODO 差分主表字段；
		this.diffIxPoi();
		// TODO 差分relateParent；
		// TODO 差分relateChildren；
		
	}
	private void diffIxPoi() {
		logger.info("差分主表属性");
		//将fid对应的poi，从mongo中查询得到一个hashmap(key:fid,value: poiJson)
		//从oracle中查询得到 hashmap(key:fid,value:poiJon)
		
	}
	private  List<String> findNotImpFids() {
		//TODO:获取一体化数据库connection
		//TODO:查询在inParam.diffFidTempTableName 中存在，但是在一体化ix_poi表中不存在的数据；返回fid的列表；
		return null;
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
