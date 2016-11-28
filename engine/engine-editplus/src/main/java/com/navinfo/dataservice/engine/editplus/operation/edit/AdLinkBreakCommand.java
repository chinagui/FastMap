package com.navinfo.dataservice.engine.editplus.operation.edit;

/** 
 * @ClassName: AdLinkBreakCommand
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AdLinkBreakCommand.java
 */
public class AdLinkBreakCommand extends EditCommand {

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

}
