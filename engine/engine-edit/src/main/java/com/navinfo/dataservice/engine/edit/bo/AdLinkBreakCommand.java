package com.navinfo.dataservice.engine.edit.bo;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

/** 
 * @ClassName: AdLinkBreakCommand
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AdLinkBreakCommand.java
 */
public class AdLinkBreakCommand extends AbstractCommand {

	private int linkPid;
	
	private double longitude;
	
	private double latitude;

	
	public int getLinkPid() {
		return linkPid;
	}


	public void setLinkPid(int linkPid) {
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


	@Override
	public void validate() throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ObjType getObjType() {
		// TODO Auto-generated method stub
		return null;
	}

}
