package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiNameTone 
* @author code generator
* @date 2016-11-18 11:26:51 
* @Description: TODO
*/
public class IxPoiNameTone extends BasicRow {
	protected long nameId ;
	protected String toneA ;
	protected String toneB ;
	protected String lhA ;
	protected String lhB ;
	protected String jyutp ;
	protected String memo ;

	public IxPoiNameTone (long objPid){
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
	public String getToneA() {
		return toneA;
	}
	public void setToneA(String toneA) {
		if(this.checkValue("TONE_A",this.toneA,toneA)){
			this.toneA = toneA;
		}
	}
	public String getToneB() {
		return toneB;
	}
	public void setToneB(String toneB) {
		if(this.checkValue("TONE_B",this.toneB,toneB)){
			this.toneB = toneB;
		}
	}
	public String getLhA() {
		return lhA;
	}
	public void setLhA(String lhA) {
		if(this.checkValue("LH_A",this.lhA,lhA)){
			this.lhA = lhA;
		}
	}
	public String getLhB() {
		return lhB;
	}
	public void setLhB(String lhB) {
		if(this.checkValue("LH_B",this.lhB,lhB)){
			this.lhB = lhB;
		}
	}
	public String getJyutp() {
		return jyutp;
	}
	public void setJyutp(String jyutp) {
		if(this.checkValue("JYUTP",this.jyutp,jyutp)){
			this.jyutp = jyutp;
		}
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_NAME_TONE";
	}
}
