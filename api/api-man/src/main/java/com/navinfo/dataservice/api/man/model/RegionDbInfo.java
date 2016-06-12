package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;

/*
 * @author mayunfei
 * 2016年6月8日
 * 描述：RegionInfo的数据
 */
public class RegionDbInfo implements Serializable {
	private static final long serialVersionUID = 9042868857436287677L;
	private Integer regionId ;
	private Integer dailyDbId ;
	private Integer monthlyDbId ;
	
	public RegionDbInfo (){
	}
	public Integer getRegionId() {
		return regionId;
	}
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
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
		return "Region [regionId=" + regionId +",dailyDbId="+dailyDbId+",monthlyDbId="+monthlyDbId+"]";
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
		RegionDbInfo other = (RegionDbInfo) obj;
		if (regionId == null) {
			if (other.regionId != null)
				return false;
		} else if (!regionId.equals(other.regionId))
			return false;
		return true;
	}
	
	
	
}


