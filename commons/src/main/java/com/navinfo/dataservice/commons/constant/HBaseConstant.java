package com.navinfo.dataservice.commons.constant;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

public class HBaseConstant {

	public static final String linkTileTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.linkTileTableName);
	
	public static final String tipTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.tipsTableName);
	
	public static final String photoTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.photoTableName);
	
	public static final String trackLineTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.trackLinesTableName);
	
	public static final String audioTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.audioTableName);
}
