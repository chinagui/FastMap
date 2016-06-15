package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;

/** 
* @ClassName:  CustomisedLayer 
* @author code generator
* @date 2016-06-13 05:53:14 
* @Description: TODO
*/
public class Layer  {
	private Integer layerId ;
	private String layerName ;
	private String geometry ;
	private Integer createUserId ;
	private Timestamp createDate ;
	
	public Layer (){
	}
	
	public Layer (Integer layerId ,String layerName,String geometry,Integer createUserId,Timestamp createDate){
		this.layerId=layerId ;
		this.layerName=layerName ;
		this.geometry=geometry ;
		this.createUserId=createUserId ;
		this.createDate=createDate ;
	}
	public Integer getLayerId() {
		return layerId;
	}
	public void setLayerId(Integer layerId) {
		this.layerId = layerId;
	}
	public String getLayerName() {
		return layerName;
	}
	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}
	public String getGeometry() {
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	public Integer getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CustomisedLayer [layerId=" + layerId +",layerName="+layerName+",geometry="+geometry+",createUserId="+createUserId+",createDate="+createDate+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((layerId == null) ? 0 : layerId.hashCode());
		result = prime * result + ((layerName == null) ? 0 : layerName.hashCode());
		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
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
		Layer other = (Layer) obj;
		if (layerId == null) {
			if (other.layerId != null)
				return false;
		} else if (!layerId.equals(other.layerId))
			return false;
		if (layerName == null) {
			if (other.layerName != null)
				return false;
		} else if (!layerName.equals(other.layerName))
			return false;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (createUserId == null) {
			if (other.createUserId != null)
				return false;
		} else if (!createUserId.equals(other.createUserId))
			return false;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		return true;
	}
	
	
	
}
