package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameAbb 
* @author code generator
* @date 2017-03-23 07:08:33 
* @Description: TODO
*/
public class ScRoadnameAbb  {
	private Long adminId ;
	private Long nameGroupid ;
	private String name ;
	private Long nameGroupidAbb ;
	private String nameAbb ;
	
	public ScRoadnameAbb (){
	}
	
	public ScRoadnameAbb (Long adminId ,Long nameGroupid,String name,Long nameGroupidAbb,String nameAbb){
		this.adminId=adminId ;
		this.nameGroupid=nameGroupid ;
		this.name=name ;
		this.nameGroupidAbb=nameGroupidAbb ;
		this.nameAbb=nameAbb ;
	}
	public Long getAdminId() {
		return adminId;
	}
	public void setAdminId(Long adminId) {
		this.adminId = adminId;
	}
	public Long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(Long nameGroupid) {
		this.nameGroupid = nameGroupid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getNameGroupidAbb() {
		return nameGroupidAbb;
	}
	public void setNameGroupidAbb(Long nameGroupidAbb) {
		this.nameGroupidAbb = nameGroupidAbb;
	}
	public String getNameAbb() {
		return nameAbb;
	}
	public void setNameAbb(String nameAbb) {
		this.nameAbb = nameAbb;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScRoadnameAbb [adminId=" + adminId +",nameGroupid="+nameGroupid+",name="+name+",nameGroupidAbb="+nameGroupidAbb+",nameAbb="+nameAbb+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adminId == null) ? 0 : adminId.hashCode());
		result = prime * result + ((nameGroupid == null) ? 0 : nameGroupid.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nameGroupidAbb == null) ? 0 : nameGroupidAbb.hashCode());
		result = prime * result + ((nameAbb == null) ? 0 : nameAbb.hashCode());
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
		ScRoadnameAbb other = (ScRoadnameAbb) obj;
		if (adminId == null) {
			if (other.adminId != null)
				return false;
		} else if (!adminId.equals(other.adminId))
			return false;
		if (nameGroupid == null) {
			if (other.nameGroupid != null)
				return false;
		} else if (!nameGroupid.equals(other.nameGroupid))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nameGroupidAbb == null) {
			if (other.nameGroupidAbb != null)
				return false;
		} else if (!nameGroupidAbb.equals(other.nameGroupidAbb))
			return false;
		if (nameAbb == null) {
			if (other.nameAbb != null)
				return false;
		} else if (!nameAbb.equals(other.nameAbb))
			return false;
		return true;
	}
	
	
	
}
