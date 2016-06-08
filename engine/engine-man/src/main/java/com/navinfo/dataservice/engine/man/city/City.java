package com.navinfo.dataservice.engine.man.city;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  City 
* @author code generator
* @date 2016-06-06 08:19:12 
* @Description: TODO
*/
public class City  {
	private Integer cityId ;
	private String cityName ;
	private String provinceName ;
	private Object geometry ;
	private Integer regionId ;
	private Integer planStatus ;
	
	public City (){
	}
	
	public City (Integer cityId ,String cityName,String provinceName,Object geometry,Integer regionId,Integer planStatus){
		this.cityId=cityId ;
		this.cityName=cityName ;
		this.provinceName=provinceName ;
		this.geometry=geometry ;
		this.regionId=regionId ;
		this.planStatus=planStatus ;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public Object getGeometry() {
		return geometry;
	}
	public void setGeometry(Object geometry) {
		this.geometry = geometry;
	}
	public Integer getRegionId() {
		return regionId;
	}
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}
	public Integer getPlanStatus() {
		return planStatus;
	}
	public void setPlanStatus(Integer planStatus) {
		this.planStatus = planStatus;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "City [cityId=" + cityId +",cityName="+cityName+",provinceName="+provinceName+",geometry="+geometry+",regionId="+regionId+",planStatus="+planStatus+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result + ((cityName == null) ? 0 : cityName.hashCode());
		result = prime * result + ((provinceName == null) ? 0 : provinceName.hashCode());
		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
		result = prime * result + ((regionId == null) ? 0 : regionId.hashCode());
		result = prime * result + ((planStatus == null) ? 0 : planStatus.hashCode());
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
		City other = (City) obj;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (cityName == null) {
			if (other.cityName != null)
				return false;
		} else if (!cityName.equals(other.cityName))
			return false;
		if (provinceName == null) {
			if (other.provinceName != null)
				return false;
		} else if (!provinceName.equals(other.provinceName))
			return false;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (regionId == null) {
			if (other.regionId != null)
				return false;
		} else if (!regionId.equals(other.regionId))
			return false;
		if (planStatus == null) {
			if (other.planStatus != null)
				return false;
		} else if (!planStatus.equals(other.planStatus))
			return false;
		return true;
	}
	
	
	
}
