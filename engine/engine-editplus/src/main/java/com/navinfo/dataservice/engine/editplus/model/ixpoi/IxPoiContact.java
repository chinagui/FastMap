package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiContact 
* @author code generator
* @date 2016-11-18 11:27:23 
* @Description: TODO
*/
public class IxPoiContact extends BasicRow {
	protected long poiPid ;
	protected int contactType ;
	protected String contact ;
	protected int contactDepart ;
	protected int priority ;
	
	public IxPoiContact (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public int getContactType() {
		return contactType;
	}
	public void setContactType(int contactType) {
		if(this.checkValue("CONTACT_TYPE",this.contactType,contactType)){
			this.contactType = contactType;
		}
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		if(this.checkValue("CONTACT",this.contact,contact)){
			this.contact = contact;
		}
	}
	public int getContactDepart() {
		return contactDepart;
	}
	public void setContactDepart(int contactDepart) {
		if(this.checkValue("CONTACT_DEPART",this.contactDepart,contactDepart)){
			this.contactDepart = contactDepart;
		}
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		if(this.checkValue("PRIORITY",this.priority,priority)){
			this.priority = priority;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_CONTACT";
	}
}
