package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameFixedPhrase 
* @author code generator
* @date 2017-03-23 07:06:22 
* @Description: TODO
*/
public class ScRoadnameFixedPhrase  {
	private Long id ;
	private String name ;
	private Integer regionFlag ;
	private String langCode ;
	
	public ScRoadnameFixedPhrase (){
	}
	
	public ScRoadnameFixedPhrase (Long id ,String name,Integer regionFlag,String langCode){
		this.id=id ;
		this.name=name ;
		this.regionFlag=regionFlag ;
		this.langCode=langCode ;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getRegionFlag() {
		return regionFlag;
	}
	public void setRegionFlag(Integer regionFlag) {
		this.regionFlag = regionFlag;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScRoadnameFixedPhrase [id=" + id +",name="+name+",regionFlag="+regionFlag+",langCode="+langCode+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((regionFlag == null) ? 0 : regionFlag.hashCode());
		result = prime * result + ((langCode == null) ? 0 : langCode.hashCode());
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
		ScRoadnameFixedPhrase other = (ScRoadnameFixedPhrase) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (regionFlag == null) {
			if (other.regionFlag != null)
				return false;
		} else if (!regionFlag.equals(other.regionFlag))
			return false;
		if (langCode == null) {
			if (other.langCode != null)
				return false;
		} else if (!langCode.equals(other.langCode))
			return false;
		return true;
	}
	
	
	
}
