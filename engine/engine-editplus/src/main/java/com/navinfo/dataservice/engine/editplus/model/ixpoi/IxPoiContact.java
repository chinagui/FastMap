package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiContact 
* @author code generator
* @date 2016-11-16 02:34:46 
* @Description: TODO
*/
public class IxPoiContact extends BasicRow {
	protected long poiPid ;
	protected Integer contactType ;
	protected String contact ;
	protected Integer contactDepart ;
	protected Integer priority ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiContact (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public Integer getContactType() {
		return contactType;
	}
	protected void setContactType(Integer contactType) {
		this.contactType = contactType;
	}
	public String getContact() {
		return contact;
	}
	protected void setContact(String contact) {
		this.contact = contact;
	}
	public Integer getContactDepart() {
		return contactDepart;
	}
	protected void setContactDepart(Integer contactDepart) {
		this.contactDepart = contactDepart;
	}
	public Integer getPriority() {
		return priority;
	}
	protected void setPriority(Integer priority) {
		this.priority = priority;
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
		return "IX_POI_CONTACT";
	}
}
