package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameEngnmQj 
* @author code generator
* @date 2017-03-23 07:07:57 
* @Description: TODO
*/
public class ScRoadnameEngnmQj  {
	private Long id ;
	private String nameQ ;
	private String nameJ ;
	private String langCode ;
	
	public ScRoadnameEngnmQj (){
	}
	
	public ScRoadnameEngnmQj (Long id ,String nameQ,String nameJ,String langCode){
		this.id=id ;
		this.nameQ=nameQ ;
		this.nameJ=nameJ ;
		this.langCode=langCode ;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNameQ() {
		return nameQ;
	}
	public void setNameQ(String nameQ) {
		this.nameQ = nameQ;
	}
	public String getNameJ() {
		return nameJ;
	}
	public void setNameJ(String nameJ) {
		this.nameJ = nameJ;
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
		return "ScRoadnameEngnmQj [id=" + id +",nameQ="+nameQ+",nameJ="+nameJ+",langCode="+langCode+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nameQ == null) ? 0 : nameQ.hashCode());
		result = prime * result + ((nameJ == null) ? 0 : nameJ.hashCode());
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
		ScRoadnameEngnmQj other = (ScRoadnameEngnmQj) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nameQ == null) {
			if (other.nameQ != null)
				return false;
		} else if (!nameQ.equals(other.nameQ))
			return false;
		if (nameJ == null) {
			if (other.nameJ != null)
				return false;
		} else if (!nameJ.equals(other.nameJ))
			return false;
		if (langCode == null) {
			if (other.langCode != null)
				return false;
		} else if (!langCode.equals(other.langCode))
			return false;
		return true;
	}
	
	
	
}
