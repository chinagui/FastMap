package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameHwCode 
* @author code generator
* @date 2017-03-23 07:08:12 
* @Description: TODO
*/
public class ScRoadnameHwCode  {
	private String roadname ;
	private String roadcode ;
	
	public ScRoadnameHwCode (){
	}
	
	public ScRoadnameHwCode (String roadname ,String roadcode){
		this.roadname=roadname ;
		this.roadcode=roadcode ;
	}
	public String getRoadname() {
		return roadname;
	}
	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}
	public String getRoadcode() {
		return roadcode;
	}
	public void setRoadcode(String roadcode) {
		this.roadcode = roadcode;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScRoadnameHwCode [roadname=" + roadname +",roadcode="+roadcode+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roadname == null) ? 0 : roadname.hashCode());
		result = prime * result + ((roadcode == null) ? 0 : roadcode.hashCode());
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
		ScRoadnameHwCode other = (ScRoadnameHwCode) obj;
		if (roadname == null) {
			if (other.roadname != null)
				return false;
		} else if (!roadname.equals(other.roadname))
			return false;
		if (roadcode == null) {
			if (other.roadcode != null)
				return false;
		} else if (!roadcode.equals(other.roadcode))
			return false;
		return true;
	}
	
	
	
}
