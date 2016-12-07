/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.model;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;

import net.sf.json.JSONObject;

/** 
* @ClassName: TmcPointName 
* @author Zhang Xiaolong
* @date 2016年11月16日 下午2:27:58 
* @Description: TODO
*/
public class TmcPointName {
	private int tmcId;
	
	private String transLang;
	
	private String phonetic;
	
	private int nameFlag;
	
	private String translateName;

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public String getTransLang() {
		return transLang;
	}

	public void setTransLang(String transLang) {
		this.transLang = transLang;
	}

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}

	public int getNameFlag() {
		return nameFlag;
	}

	public void setNameFlag(int nameFlag) {
		this.nameFlag = nameFlag;
	}
	
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

		return json;
	}

	public String getTranslateName() {
		return translateName;
	}

	public void setTranslateName(String translateName) {
		this.translateName = translateName;
	}
}
