package com.navinfo.dataservice.commons.constant;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

public class HBaseConstant {

	public static final String linkTileTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.linkTileTableName);
	
	public static final String tipTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.tipsTableName);
	
	public static final String photoTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.photoTableName);
	
	//新增mass_photos_poi_idx_pid表配置
	public static final String massPhotosPoiIdxTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.massPhotosPoiIdxTableName);
	
	//新增mass_photos_poi表配置
	public static final String massPhotosPoiTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.massPhotosPoiTableName);
	
	//新增mass_photos_tips_idx_url表配置
	public static final String massPhotosTipsIdxTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.massPhotosTipsIdxTableName);
		
	//新增mass_photos_tips表配置
	public static final String massPhotosTipsTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.massPhotosTipsTableName);
	
	
	public static final String trackLineTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.trackLinesTableName);

    public static final String adasTrackPointsTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.adasTrackPointsTableName);
	
	public static final String audioTab = SystemConfigFactory.getSystemConfig().getValue(PropConstant.audioTableName);
}
