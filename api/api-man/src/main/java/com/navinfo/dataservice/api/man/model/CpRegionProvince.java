package com.navinfo.dataservice.api.man.model;

/** 
* @ClassName:  Block 
* @author code generator
* @date 2016-06-08 01:32:01 
* @Description: TODO
*/
public class CpRegionProvince  {
	private int regionId ;
	private String ndsRegioncode ;
	private int admincode ;
	private String province ;
	
	public CpRegionProvince (){
	}
	
	public CpRegionProvince (int regionId ,String ndsRegioncode,int admincode,String province){
		this.regionId=regionId;
		this.ndsRegioncode=ndsRegioncode;
		this.admincode=admincode;
		this.province=province;
	}
	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getNdsRegioncode() {
		return ndsRegioncode;
	}

	public void setNdsRegioncode(String ndsRegioncode) {
		this.ndsRegioncode = ndsRegioncode;
	}

	public int getAdmincode() {
		return admincode;
	}

	public void setAdmincode(int admincode) {
		this.admincode = admincode;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CpRegionProvince [regionId=" + regionId +",ndsRegioncode="+ndsRegioncode+",admincode="+admincode+",province="+province+"]";
	}
	
}
