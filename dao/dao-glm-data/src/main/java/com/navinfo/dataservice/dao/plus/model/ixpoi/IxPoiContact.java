package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiContact 
* @author code generator
* @date 2016-11-18 11:27:23 
* @Description: TODO
*/
public class IxPoiContact extends BasicRow {
	protected long poiPid ;
	protected int contactType=1;
	protected String contact ;
	protected int contactDepart ;
	protected int priority=1 ;
	
	public IxPoiContact (long objPid){
		super(objPid);
		setPoiPid(objPid);
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
	
	public static final String POI_PID = "POI_PID";
	public static final String CONTACT_TYPE = "CONTACT_TYPE";
	public static final String CONTACT = "CONTACT";
	public static final String CONTACT_DEPART = "CONTACT_DEPART";
	public static final String PRIORITY = "PRIORITY";

}
