package com.navinfo.dataservice.api.man.model;

import com.vividsolutions.jts.geom.Geometry;

/** 
* @ClassName:  Block 
* @author code generator
* @date 2016-06-08 01:32:01 
* @Description: TODO
*/
public class Block  {
	private int blockId ;
	private int cityId ;
	private String blockName ;
	private Geometry geometry ;
	private int planStatus ;
	private Geometry originGeo;
	
	public Block (){
	}
	
	public Block (int blockId ,int cityId,String blockName,Geometry geometry,int planStatus){
		this.blockId=blockId ;
		this.cityId=cityId ;
		this.blockName=blockName ;
		this.geometry=geometry ;
		this.planStatus=planStatus ;
	}
	public int getBlockId() {
		return blockId;
	}
	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getBlockName() {
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public int getPlanStatus() {
		return planStatus;
	}
	public void setPlanStatus(int planStatus) {
		this.planStatus = planStatus;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Block [blockId=" + blockId +",cityId="+cityId+",blockName="+blockName+",geometry="+geometry+",planStatus="+planStatus+"]";
	}

	public Geometry getOriginGeo() {
		return originGeo;
	}

	public void setOriginGeo(Geometry originGeo) {
		this.originGeo = originGeo;
	}
	
	
	
}
