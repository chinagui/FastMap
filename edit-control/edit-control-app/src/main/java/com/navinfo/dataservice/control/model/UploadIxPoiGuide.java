package com.navinfo.dataservice.control.model;


/** 
* @ClassName:  IxPoiChildren 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoiGuide{
	private Integer linkPid;
	private double  longitude;
	private double  latitude;
	
	public Integer getLinkPid() {
		return linkPid;
	}
	public void setLinkPid(Integer linkPid) {
		this.linkPid = linkPid;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
}
