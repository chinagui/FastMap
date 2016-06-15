package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;

/** 
* @ClassName:  Region 
* @author code generator
* @date 2016-06-08 02:32:17 
* @Description: TODO
*/
public class Region implements Serializable {
	private Integer regionId ;
	private String regionName ;
	private Integer dailyDbId ;
	private Integer monthlyDbId ;
	
	public Region (){
	}
	
	public Region (Integer regionId ,String regionName,Integer dailyDbId,Integer monthlyDbId){
		this.regionId=regionId ;
		this.regionName=regionName ;
		this.dailyDbId=dailyDbId ;
		this.monthlyDbId=monthlyDbId ;
	}
	public Integer getRegionId() {
		return regionId;
	}
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public Integer getDailyDbId() {
		return dailyDbId;
	}
	public void setDailyDbId(Integer dailyDbId) {
		this.dailyDbId = dailyDbId;
	}
	public Integer getMonthlyDbId() {
		return monthlyDbId;
	}
	public void setMonthlyDbId(Integer monthlyDbId) {
		this.monthlyDbId = monthlyDbId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Region [regionId=" + regionId +",regionName="+regionName+",dailyDbId="+dailyDbId+",monthlyDbId="+monthlyDbId+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regionId == null) ? 0 : regionId.hashCode());
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
		Region other = (Region) obj;
		if (regionId == null) {
			if (other.regionId != null)
				return false;
		} else if (!regionId.equals(other.regionId))
			return false;
		return true;
	}
	
	
	
}
