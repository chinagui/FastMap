package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

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
	
	@Override
	public String tableName() {
		return "IX_POI_NAME";
	}
}
