package com.navinfo.dataservice.datahub.glm;

/** 
* @ClassName: JobType 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:50:34 
* @Description: TODO
*/
public enum GlmMainTable {
//	IX_POI("IX_POI"),
//	IX_POINTADDRESS("IX_POINTADDRESS"),
//	IX_ANNOTATION("IX_ANNOTATION"),
//	IX_HAMLET("IX_HAMLET"),
//	IX_POSTCODE("IX_POSTCODE"),
//	AD_ADMIN("AD_ADMIN"),
//	AD_FACE("AD_FACE"),
//	ZONE_FACE("ZONE_FACE"),
//	LC_FACE("LC_FACE"),
//	LC_LINK("LC_LINK"),
	RD_NODE("RD_NODE"),
	RD_LINK("RD_LINK");
	private String name;
	GlmMainTable(String name){
		this.name=name;
	}
	public String getName(){
		return name;
	}
	@Override
	public String toString(){
		return name;
	}
	
	public static GlmMainTable getGlmMainTable(String name){
		for (GlmMainTable type:GlmMainTable.values()){
			if(type.getName().equals(name)){
				return type;
			}
		}
		return null;
	}
}
