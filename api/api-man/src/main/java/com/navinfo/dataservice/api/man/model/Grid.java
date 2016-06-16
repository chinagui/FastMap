package com.navinfo.dataservice.api.man.model;


/** 
* @ClassName:  Grid 
* @author code generator
* @date 2016-06-14 08:32:25 
* @Description: TODO
*/
public class Grid  {
	private Integer gridId ;
	private Integer regionId ;
	private Integer cityId ;
	private Integer blockId ;
	
	public Grid (){
	}
	
	public Grid (Integer gridId ,Integer regionId,Integer cityId,Integer blockId){
		this.gridId=gridId ;
		this.regionId=regionId ;
		this.cityId=cityId ;
		this.blockId=blockId ;
	}
	public Integer getGridId() {
		return gridId;
	}
	public void setGridId(Integer gridId) {
		this.gridId = gridId;
	}
	public Integer getRegionId() {
		return regionId;
	}
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public Integer getBlockId() {
		return blockId;
	}
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Grid [gridId=" + gridId +",regionId="+regionId+",cityId="+cityId+",blockId="+blockId+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gridId == null) ? 0 : gridId.hashCode());
		result = prime * result + ((regionId == null) ? 0 : regionId.hashCode());
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
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
		Grid other = (Grid) obj;
		if (gridId == null) {
			if (other.gridId != null)
				return false;
		} else if (!gridId.equals(other.gridId))
			return false;
		if (regionId == null) {
			if (other.regionId != null)
				return false;
		} else if (!regionId.equals(other.regionId))
			return false;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		return true;
	}
	
	
	
}
