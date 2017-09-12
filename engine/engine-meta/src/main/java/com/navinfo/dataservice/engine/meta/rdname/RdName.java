package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 实体类<br>
 * null
 */
public class RdName {
	
	
	
	/**
	 * ------------------------成员变量，共(26)个-------------------------------------
	 */

	/* 主键 */
	protected Integer nameId;

	/* 主键 */
	protected Integer nameGroupid;

	// 语言代码
	protected String langCode;

	/* 道路名 */
	protected String name;
	/*null*/
	protected String nameEng;
	/*null*/
	protected String namePor;

	/* 大街,大道,胡同,环路,隧道等 */
	protected String type;

	/* null */
	protected String base;

	/* 东,西,南,北,中,前,后,左,右等 */
	protected String prefix;

	/* null */
	protected String infix;

	/* 东,西,南,北,中等 */
	protected String suffix;

	/* null */
	protected String namePhonetic;

	/* null */
	protected String prefixPhonetic;

	/* null */
	protected String infixPhonetic;

	/* null */
	protected String suffixPhonetic;

	/* null */
	protected String basePhonetic;

	/* null */
	protected String typePhonetic;

	/* null */
	protected Integer srcFlag;

	/* null */
	protected Integer roadType;

	/* null */
	protected Integer adminId;

	/* null */
	protected Integer codeType;

	/* 外键,引用"AU_MULTIMEDIA",高速,城高名称语音 */
	protected String voiceFile;

	/* 道路名来源履历 */
	protected String srcResume;

	protected Integer paRegionId;

	protected String memo;

	/* NaviMap仅用于高速编号和保留原有RouteID */
	protected Integer routeId;

	/* 增量更新标识 */
	protected Integer changeFlag;

	/* 增量数据的定位查询 */
	protected String changeFields;
	
	/* 作业状态*/
	protected int   processFlag=0;
	/*更新记录*/
	protected int   uRecord = 1;
	/*更新字段*/
	protected String   uFields;
	/*地级市名称 add by wangdongbin*/
	protected String city;
	/*是否操作城市字段 add by wangdongbin*/
	protected boolean isCity=false;
	
	//****zl 2017.04.09 ****
	/*HW 信息标识 默认为 0 如果为1 则需在SC_ROADNAME_HW_INFO表中新增*/
	protected Integer hwInfoFlag;

	public Integer getHwInfoFlag() {
		return hwInfoFlag;
	}

	public void setHwInfoFlag(Integer hwInfoFlag) {
		this.hwInfoFlag = hwInfoFlag;
	}
	//***********************
	
	/**
	 * ------------------------getter,setter方法-------------------------------------
	 */

	public Integer getNameId() {
		return nameId;
	}

	public void setNameId(Integer nameId) {
		this.nameId = nameId;
	}

	public Integer getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(Integer nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*getter:null*/
	public String getNameEng(){
		return this.nameEng;
	}
	/*setter:null*/
	public void setNameEng(String nameEng){
		this.nameEng=nameEng;
	}

	/*getter:null*/
	public String getNamePor(){
		return this.namePor;
	}
	/*setter:null*/
	public void setNamePor(String namePor){
		this.namePor=namePor;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getInfix() {
		return infix;
	}

	public void setInfix(String infix) {
		this.infix = infix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getNamePhonetic() {
		return namePhonetic;
	}

	public void setNamePhonetic(String namePhonetic) {
		this.namePhonetic = namePhonetic;
	}

	public String getPrefixPhonetic() {
		return prefixPhonetic;
	}

	public void setPrefixPhonetic(String prefixPhonetic) {
		this.prefixPhonetic = prefixPhonetic;
	}

	public String getInfixPhonetic() {
		return infixPhonetic;
	}

	public void setInfixPhonetic(String infixPhonetic) {
		this.infixPhonetic = infixPhonetic;
	}

	public String getSuffixPhonetic() {
		return suffixPhonetic;
	}

	public void setSuffixPhonetic(String suffixPhonetic) {
		this.suffixPhonetic = suffixPhonetic;
	}

	public String getBasePhonetic() {
		return basePhonetic;
	}

	public void setBasePhonetic(String basePhonetic) {
		this.basePhonetic = basePhonetic;
	}

	public String getTypePhonetic() {
		return typePhonetic;
	}

	public void setTypePhonetic(String typePhonetic) {
		this.typePhonetic = typePhonetic;
	}

	public Integer getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(Integer srcFlag) {
		this.srcFlag = srcFlag;
	}

	public Integer getRoadType() {
		return roadType;
	}

	public void setRoadType(Integer roadType) {
		this.roadType = roadType;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public Integer getCodeType() {
		return codeType;
	}

	public void setCodeType(Integer codeType) {
		this.codeType = codeType;
	}

	public String getVoiceFile() {
		return voiceFile;
	}

	public void setVoiceFile(String voiceFile) {
		this.voiceFile = voiceFile;
	}

	public String getSrcResume() {
		return srcResume;
	}

	public void setSrcResume(String srcResume) {
		this.srcResume = srcResume;
	}

	public Integer getPaRegionId() {
		return paRegionId;
	}

	public void setPaRegionId(Integer paRegionId) {
		this.paRegionId = paRegionId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Integer getRouteId() {
		return routeId;
	}

	public void setRouteId(Integer routeId) {
		this.routeId = routeId;
	}

	public Integer getChangeFlag() {
		return changeFlag;
	}

	public void setChangeFlag(Integer record) {
		this.changeFlag = record;
	}

	public String getChangeFields() {
		return changeFields;
	}

	public void setChangeFields(String fields) {
		this.changeFields = fields;
	}
	
	
	



	/**
	 * @return the uRecord
	 */
	public int getuRecord() {
		return uRecord;
	}

	/**
	 * @param uRecord the uRecord to set
	 */
	public void setuRecord(int uRecord) {
		this.uRecord = uRecord;
	}

	/**
	 * @return the uFields
	 */
	public String getuFields() {
		return uFields;
	}

	/**
	 * @param uFields the uFields to set
	 */
	public void setuFields(String uFields) {
		this.uFields = uFields;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public boolean isCity() {
		return isCity;
	}

	public void setCity(boolean isCity) {
		this.isCity = isCity;
	}

	/**
	 * @return the processFlag
	 */
	public int getProcessFlag() {
		return processFlag;
	}

	/**
	 * @param processFlag the processFlag to set
	 */
	public void setProcessFlag(int processFlag) {
		this.processFlag = processFlag;
	}

	/* toString() */
	public String toString() {

		return "nameId:" + nameId + ", nameGroupId:" + nameGroupid + ","
				+ "langCode:" + langCode + "," + "name:" + name + "," + "type:"
				+ type + "," + "base:" + base + "," + "prefix:" + prefix + ","
				+ "infix:" + infix + "," + "suffix:" + suffix + ","
				+ "namePhonetic:" + namePhonetic + "," + "typePhonetic:"
				+ typePhonetic + ",basePhonetic:" + basePhonetic + ","
				+ "basePhonetic:" + basePhonetic + ",prefixPhonetic:"
				+ prefixPhonetic + "," + ",infixPhonetic:" + infixPhonetic
				+ ",sufficPhonetic:" + suffixPhonetic + "," + "srcFlag:"
				+ srcFlag + ",roadType:" + roadType + "," + "adminId:"
				+ adminId + "," + "codeType:" + codeType + "," + "voiceFile:"
				+ voiceFile + ",srcResume:" + srcResume + "," + "paRegionId:"
				+ paRegionId + ",memo:" + memo + "," + "routeId:" + routeId
				+ "," + "changeFlag:" + changeFlag + "," + "changeFields:" + changeFields;
	}
	public RdName() {
		
	}
	
	public static Integer ROAD_TYPE_COUNTRY_ROAD = 2;
	public static Integer ROAD_TYPE_DEFAULT = 0;
	//道路类型:铁路
	public static Integer ROAD_TYPE_RAILRODE = 3;
	
	public static Integer DEFAULT_NUM_VALUE = 0;
	public static Integer COUNTRY_CODE = 214;
	
	
	protected String adminName;
	protected String namePhoneticNullFlag;
	protected String nameBaseNullFlag;
	protected String sql;
	
	protected Integer splitFlag=0;//拆分标识
	
	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getNamePhoneticNullFlag() {
		return namePhoneticNullFlag;
	}

	public void setNamePhoneticNullFlag(String namePhoneticNullFlag) {
		this.namePhoneticNullFlag = namePhoneticNullFlag;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getNameBaseNullFlag() {
		return nameBaseNullFlag;
	}

	public void setNameBaseNullFlag(String nameBaseNullFlag) {
		this.nameBaseNullFlag = nameBaseNullFlag;
	}

	/**
	 * 把MAP数据绑定到对象中
	 * 
	 * @param data
	 * @return
	 */
	public void setSearchParameter(Map<String, Object> data) {
		// 名称ID
		Integer nameId = toInteger(data.get("nameId"));
		
		Integer nameGroupId = toInteger(data.get("nameGroupId"));
		
		String name = toString(data.get("name"));
		// 英文名来源
		String langCode = toString(data.get("langCode"));
		// 类型名称
		String type = toString(data.get("type"));
		// 类型名拼音
		String typePhonetic = toString(data.get("typePhonetic"));
		// 基本名称
		String base = toString(data.get("base"));
		// 名称拼音
		String namePhonetic = toString(data.get("namePhonetic"));
		// 前缀名称
		String prefix = toString(data.get("prefix"));
		// 前缀拼音
		String prefixPhonetic = toString(data.get("prefixPhonetic"));
		// 中缀名称
		String infix = toString(data.get("infix"));
		// 中缀拼音
		String infixPhonetic = toString(data.get("infixPhonetic"));
		// 拼音是否为空
		String namePhoneticNullFlag = toString(data.get("namePhoneticNullFlag"));
		// 基本名称是否为空
		String nameBaseNullFlag = toString(data.get("nameBaseNullFlag"));
		// 道路类型
		Integer roadType = toInteger(data.get("roadType"));
		// 国家编号
		Integer codeType = toInteger(data.get("codeType"));
		// 行政区划
		Integer adminId = toInteger(data.get("adminId"));
		String adminName = toString(data.get("adminName"));
		// 用户自定义SQL
		String sql = toString(data.get("sql"));
		//来源履历
		String srcResume = toString(data.get("srcResume"));
		//拆分标识
		Integer splitFlag = toInteger(data.get("splitFlag"));
		
		this.setName(name);
		this.setNameGroupid(nameGroupId);
		this.setNameId(nameId);

		this.setLangCode(langCode);
		this.setType(type);
		this.setTypePhonetic(typePhonetic);
		this.setBase(base);
		this.setNamePhonetic(namePhonetic);
		this.setPrefix(prefix);
		this.setPrefixPhonetic(prefixPhonetic);
		this.setInfixPhonetic(infixPhonetic);
		this.setInfix(infix);

		this.setNamePhoneticNullFlag(namePhoneticNullFlag);
		this.setNameBaseNullFlag(nameBaseNullFlag);
		this.setRoadType(roadType);
		this.setCodeType(codeType);
		this.setAdminId(adminId);
		this.setAdminName(adminName);
		this.setSql(sql);
		this.setSrcResume(srcResume);
		this.setSplitFlag(splitFlag);
	}
	
	protected Integer toInteger(Object value) {
        if (value == null||"".equals(value))
            return null;
        return Integer.valueOf(value.toString());
    }

	protected String toString(Object value) {
    	if (value == null)
    		return null;
    	return value.toString();
    }

	public int getSplitFlag() {
		return splitFlag;
	}

	public void setSplitFlag(Integer splitFlag) {
		this.splitFlag = splitFlag;
	}
	
	
	public RdName mapRow(ResultSet rs) throws SQLException {
		RdName obj = new RdName();
		getRdName(obj,rs);
		return obj;
	}
	
	protected void getRdName(RdName obj, ResultSet rs)
			throws SQLException {
		if(obj!=null){
			obj.setNameId(rs.getInt("name_id"));
			
			obj.setNameGroupid(rs.getInt("name_groupid"));

			obj.setName(rs.getString("name"));
			
			obj.setLangCode(rs.getString("lang_code"));

			obj.setType(rs.getString("type"));

			obj.setBase(rs.getString("base"));
			
			obj.setPrefix(rs.getString("prefix"));

			obj.setInfix(rs.getString("infix"));

			obj.setSuffix(rs.getString("suffix"));

			obj.setNamePhonetic(rs.getString("name_phonetic"));
			
			obj.setBasePhonetic(rs.getString("base_phonetic"));

			obj.setTypePhonetic(rs.getString("type_phonetic"));

			obj.setPrefixPhonetic(rs.getString("prefix_phonetic"));
			
			obj.setInfixPhonetic(rs.getString("infix_phonetic"));

			obj.setSuffixPhonetic(rs.getString("suffix_phonetic"));
			
			obj.setSrcFlag(rs.getInt("src_flag"));
			
			obj.setRoadType(rs.getInt("road_type"));

			obj.setAdminId(rs.getInt("admin_id"));
			
			obj.setCodeType(rs.getInt("code_type"));

			obj.setPaRegionId(rs.getInt("pa_region_id"));

			obj.setSrcResume(rs.getString("src_resume"));
			
			obj.setVoiceFile(rs.getString("voice_file"));

			obj.setMemo(rs.getString("memo"));
			
			obj.setRouteId(rs.getInt("route_id"));
			
			obj.setChangeFlag(rs.getInt("u_record"));
			
			obj.setChangeFields(rs.getString("u_fields"));

			obj.setAdminName(rs.getString("admin_name"));
			
			obj.setSplitFlag(rs.getInt("split_flag"));
			
			obj.setCity(rs.getString("city"));
		}

	}

	
	
}