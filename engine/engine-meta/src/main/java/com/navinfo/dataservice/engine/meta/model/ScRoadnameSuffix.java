package com.navinfo.dataservice.engine.meta.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ScRoadnameSuffix 
* @author code generator
* @date 2017-03-23 07:07:29 
* @Description: TODO
*/
public class ScRoadnameSuffix  {
	private Integer id ;
	private String name ;
	private String py ;
	private String englishname ;
	private Integer regionFlag ;
	private String langCode ;
	
	public ScRoadnameSuffix (){
	}
	
	public ScRoadnameSuffix (Integer id ,String name,String py,String englishname,Integer regionFlag,String langCode){
		this.id=id ;
		this.name=name ;
		this.py=py ;
		this.englishname=englishname ;
		this.regionFlag=regionFlag ;
		this.langCode=langCode ;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPy() {
		return py;
	}
	public void setPy(String py) {
		this.py = py;
	}
	public String getEnglishname() {
		return englishname;
	}
	public void setEnglishname(String englishname) {
		this.englishname = englishname;
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
		return "ScRoadnameSuffix [id=" + id +",name="+name+",py="+py+",englishname="+englishname+",regionFlag="+regionFlag+",langCode="+langCode+"]";
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
		result = prime * result + ((py == null) ? 0 : py.hashCode());
		result = prime * result + ((englishname == null) ? 0 : englishname.hashCode());
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
		ScRoadnameSuffix other = (ScRoadnameSuffix) obj;
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
		if (py == null) {
			if (other.py != null)
				return false;
		} else if (!py.equals(other.py))
			return false;
		if (englishname == null) {
			if (other.englishname != null)
				return false;
		} else if (!englishname.equals(other.englishname))
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
