package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiName 
* @author code generator
* @date 2016-11-16 02:31:42 
* @Description: TODO
*/
public class IxPoiName extends BasicRow {
	protected long nameId ;
	protected long poiPid ;
	protected long nameGroupid ;
	protected Integer nameClass ;
	protected Integer nameType ;
	protected String langCode ;
	protected String name ;
	protected String namePhonetic ;
	protected String keywords ;
	protected String nidbPid ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiName (long objPid){
		super(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	protected void setNameId(long nameId) {
		this.nameId = nameId;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	protected void setNameGroupid(long nameGroupid) {
		this.nameGroupid = nameGroupid;
	}
	public Integer getNameClass() {
		return nameClass;
	}
	protected void setNameClass(Integer nameClass) {
		this.nameClass = nameClass;
	}
	public Integer getNameType() {
		return nameType;
	}
	protected void setNameType(Integer nameType) {
		this.nameType = nameType;
	}
	public String getLangCode() {
		return langCode;
	}
	protected void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}
	public String getNamePhonetic() {
		return namePhonetic;
	}
	protected void setNamePhonetic(String namePhonetic) {
		this.namePhonetic = namePhonetic;
	}
	public String getKeywords() {
		return keywords;
	}
	protected void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getNidbPid() {
		return nidbPid;
	}
	protected void setNidbPid(String nidbPid) {
		this.nidbPid = nidbPid;
	}
//	public Integer getURecord() {
//		return uRecord;
//	}
//	protected void setURecord(Integer uRecord) {
//		this.uRecord = uRecord;
//	}
//	public String getUFields() {
//		return uFields;
//	}
//	protected void setUFields(String uFields) {
//		this.uFields = uFields;
//	}
//	public String getUDate() {
//		return uDate;
//	}
//	protected void setUDate(String uDate) {
//		this.uDate = uDate;
//	}
	
	@Override
	public String tableName() {
		return "IX_POI_NAME";
	}
}
