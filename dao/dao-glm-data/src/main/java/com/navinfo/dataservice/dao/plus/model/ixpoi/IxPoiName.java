package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiName 
* @author code generator
* @date 2016-11-18 11:25:01 
* @Description: TODO
*/
public class IxPoiName extends BasicRow {
	protected long nameId ;
	protected long poiPid ;
	protected long nameGroupid ;
	protected int nameClass ;
	protected int nameType ;
	protected String langCode ;
	protected String name ;
	protected String namePhonetic ;
	protected String keywords ;
	protected String nidbPid ;
	
	public IxPoiName (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	public void setNameId(long nameId) {
		if(this.checkValue("NAME_ID",this.nameId,nameId)){
			this.nameId = nameId;
		}
	}
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(long nameGroupid) {
		if(this.checkValue("NAME_GROUPID",this.nameGroupid,nameGroupid)){
			this.nameGroupid = nameGroupid;
		}
	}
	public int getNameClass() {
		return nameClass;
	}
	public void setNameClass(int nameClass) {
		if(this.checkValue("NAME_CLASS",this.nameClass,nameClass)){
			this.nameClass = nameClass;
		}
	}
	public int getNameType() {
		return nameType;
	}
	public void setNameType(int nameType) {
		if(this.checkValue("NAME_TYPE",this.nameType,nameType)){
			this.nameType = nameType;
		}
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		if(this.checkValue("LANG_CODE",this.langCode,langCode)){
			this.langCode = langCode;
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(this.checkValue("NAME",this.name,name)){
			this.name = name;
		}
	}
	public String getNamePhonetic() {
		return namePhonetic;
	}
	public void setNamePhonetic(String namePhonetic) {
		if(this.checkValue("NAME_PHONETIC",this.namePhonetic,namePhonetic)){
			this.namePhonetic = namePhonetic;
		}
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		if(this.checkValue("KEYWORDS",this.keywords,keywords)){
			this.keywords = keywords;
		}
	}
	public String getNidbPid() {
		return nidbPid;
	}
	public void setNidbPid(String nidbPid) {
		if(this.checkValue("NIDB_PID",this.nidbPid,nidbPid)){
			this.nidbPid = nidbPid;
		}
	}
	
	public boolean isOfficeName(){
		if(this.nameClass==1){return true;}
		else{return false;}
	}
	/**
	 * nameClass 别名 alias
	 * @return
	 */
	public boolean isAliasName(){
		if(this.nameClass==3){return true;}
		else{return false;}
	}
	/**
	 * nameClass 子冠父名 parent
	 * @return
	 */
	public boolean isParentName(){
		if(this.nameClass==9){return true;}
		else{return false;}
	}
	/**
	 * nameClass 站点线路 station
	 * @return
	 */
	public boolean isStationName(){
		if(this.nameClass==8){return true;}
		else{return false;}
	}
	/**
	 * nameClass 菜单 Menu
	 * @return
	 */
	public boolean isMenuName(){
		if(this.nameClass==4){return true;}
		else{return false;}
	}
	/**
	 * nameClass 曾用名
	 * @return
	 */
	public boolean isUsedName(){
		if(this.nameClass==6){return true;}
		else{return false;}
	}
	/**
	 * nameClass 简称
	 * @return
	 */
	public boolean isShortName(){
		if(this.nameClass==5){return true;}
		else{return false;}
	}
	/**
	 * nameClass 古称
	 * @return
	 */
	public boolean isOldName(){
		if(this.nameClass==7){return true;}
		else{return false;}
	}
	
	public boolean isOriginName(){
		if(this.nameType==2){return true;}
		else{return false;}
	}
	
	public boolean isStandardName(){
		if(this.nameType==1){return true;}
		else{return false;}
	}
		
	public boolean isCH(){
		if(this.langCode.equals("CHI")||this.langCode.equals("CHT")){return true;}
		else{return false;}
	}
	
	public boolean isEng(){
		if(this.langCode.equals("ENG")){return true;}
		else{return false;}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_NAME";
	}
	
	//1表示标准，2表示原始
    public String getTypeName(){
        if (1==nameType){return "标准";}
        if (2==nameType){return "原始";}
        return null;
    }

    public String getClassName(){
        if (1==nameClass){return "官方";}
        if (3==nameClass){return "别名";}
        if (4==nameClass){return "菜单";}
        if (5==nameClass){return "简称";}
        if (6==nameClass){return "曾用名";}
        if (7==nameClass){return "古称";}
        if (8==nameClass){return "站点线路名";}
        if (9==nameClass){return "子冠父名";}
        return null;
    }
	
	public static final String NAME_ID = "NAME_ID";
	public static final String POI_PID = "POI_PID";
	public static final String NAME_GROUPID = "NAME_GROUPID";
	public static final String NAME_CLASS = "NAME_CLASS";
	public static final String NAME_TYPE = "NAME_TYPE";
	public static final String LANG_CODE = "LANG_CODE";
	public static final String NAME = "NAME";
	public static final String NAME_PHONETIC = "NAME_PHONETIC";
	public static final String KEYWORDS = "KEYWORDS";
	public static final String NIDB_PID = "NIDB_PID";

}
