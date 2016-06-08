package com.navinfo.dataservice.engine.man.block;

/** 
* @ClassName:  Block 
* @author code generator
* @date 2016-06-08 01:32:01 
* @Description: TODO
*/
public class Block  {
	private Integer blockId ;
	private Integer cityId ;
	private String blockName ;
	private Object geometry ;
	private Integer planStatus ;
	
	public Block (){
	}
	
	public Block (Integer blockId ,Integer cityId,String blockName,Object geometry,Integer planStatus){
		this.blockId=blockId ;
		this.cityId=cityId ;
		this.blockName=blockName ;
		this.geometry=geometry ;
		this.planStatus=planStatus ;
	}
	public Integer getBlockId() {
		return blockId;
	}
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public String getBlockName() {
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	public Object getGeometry() {
		return geometry;
	}
	public void setGeometry(Object geometry) {
		this.geometry = geometry;
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
		return "Block [blockId=" + blockId +",cityId="+cityId+",blockName="+blockName+",geometry="+geometry+",planStatus="+planStatus+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result + ((blockName == null) ? 0 : blockName.hashCode());
		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
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
		Block other = (Block) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (blockName == null) {
			if (other.blockName != null)
				return false;
		} else if (!blockName.equals(other.blockName))
			return false;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (planStatus == null) {
			if (other.planStatus != null)
				return false;
		} else if (!planStatus.equals(other.planStatus))
			return false;
		return true;
	}
	
	
	
}
