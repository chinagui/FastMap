package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameHwInfo 
* @author code generator
* @date 2017-03-23 07:03:29 
* @Description: TODO
*/
public class ScRoadnameHwInfo  {
	private Integer hwPidUp ;
	private Integer hwPidDw ;
	private Integer nameGroupid ;
	private String memo ;
	private Integer uRecord ;
	private String uFields ;
	
	public ScRoadnameHwInfo (){
	}
	
	public ScRoadnameHwInfo (Integer hwPidUp ,Integer hwPidDw,Integer nameGroupid,String memo,Integer uRecord,String uFields){
		this.hwPidUp=hwPidUp ;
		this.hwPidDw=hwPidDw ;
		this.nameGroupid=nameGroupid ;
		this.memo=memo ;
		this.uRecord=uRecord ;
		this.uFields=uFields ;
	}
	public Integer getHwPidUp() {
		return hwPidUp;
	}
	public void setHwPidUp(Integer hwPidUp) {
		this.hwPidUp = hwPidUp;
	}
	public Integer getHwPidDw() {
		return hwPidDw;
	}
	public void setHwPidDw(Integer hwPidDw) {
		this.hwPidDw = hwPidDw;
	}
	public Integer getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(Integer nameGroupid) {
		this.nameGroupid = nameGroupid;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getURecord() {
		return uRecord;
	}
	public void setURecord(Integer uRecord) {
		this.uRecord = uRecord;
	}
	public String getUFields() {
		return uFields;
	}
	public void setUFields(String uFields) {
		this.uFields = uFields;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScRoadnameHwInfo [hwPidUp=" + hwPidUp +",hwPidDw="+hwPidDw+",nameGroupid="+nameGroupid+",memo="+memo+",uRecord="+uRecord+",uFields="+uFields+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hwPidUp == null) ? 0 : hwPidUp.hashCode());
		result = prime * result + ((hwPidDw == null) ? 0 : hwPidDw.hashCode());
		result = prime * result + ((nameGroupid == null) ? 0 : nameGroupid.hashCode());
		result = prime * result + ((memo == null) ? 0 : memo.hashCode());
		result = prime * result + ((uRecord == null) ? 0 : uRecord.hashCode());
		result = prime * result + ((uFields == null) ? 0 : uFields.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScRoadnameHwInfo other = (ScRoadnameHwInfo) obj;
		if (hwPidUp == null) {
			if (other.hwPidUp != null)
				return false;
		} else if (!hwPidUp.equals(other.hwPidUp))
			return false;
		if (hwPidDw == null) {
			if (other.hwPidDw != null)
				return false;
		} else if (!hwPidDw.equals(other.hwPidDw))
			return false;
		if (nameGroupid == null) {
			if (other.nameGroupid != null)
				return false;
		} else if (!nameGroupid.equals(other.nameGroupid))
			return false;
		if (memo == null) {
			if (other.memo != null)
				return false;
		} else if (!memo.equals(other.memo))
			return false;
		if (uRecord == null) {
			if (other.uRecord != null)
				return false;
		} else if (!uRecord.equals(other.uRecord))
			return false;
		if (uFields == null) {
			if (other.uFields != null)
				return false;
		} else if (!uFields.equals(other.uFields))
			return false;
		return true;
	}
	
	
	
}
