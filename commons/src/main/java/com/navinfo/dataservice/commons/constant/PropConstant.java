package com.navinfo.dataservice.commons.constant;

public class PropConstant {

	public static final String pmIp = "pm_ip";

	public static final String pmPort = "pm_port";

	public static final String pmServiceName = "pm_service_name";

	public static final String pmUsername = "pm_username";

	public static final String pmPassword = "pm_password";

	public static final String hbaseAddress = "hbase.address";
	
	public static final String tipsTableName = "hbase.tablename.tips";
	
	//新增mass_photos_poi_idx_pid表配置
	public static final String massPhotosPoiIdxTableName = "hbase.tablename.mass_photos_poi_idx_pid";
	
	//新增mass_photos_poi表配置
	public static final String massPhotosPoiTableName = "hbase.tablename.mass_photos_poi";
	
	//新增mass_photos_tips_idx_url表配置
	public static final String massPhotosTipsIdxTableName = "hbase.tablename.mass_photos_tips_idx_url";
	
	//新增mass_photos_tips表配置
	public static final String massPhotosTipsTableName = "hbase.tablename.mass_photos_tips";
	
	public static final String trackLinesTableName = "hbase.tablename.tracklines";

	public static final String adasTrackPointsTableName = "hbase.tablename.adasTrackPoints";
	
	public static final String photoTableName = "hbase.tablename.photo";
	
	public static final String audioTableName = "hbase.tablename.audio";
	
	public static final String linkTileTableName = "hbase.tablename.linktile";

	public static final String solrAddress = "solr.address";
	
	public static final String solrCloudAddress = "solr.cloud.address";

	public static final String pidManagerIp = "pid_manager_ip";

	public static final String pidManagerPort = "pid_manager_port";

	public static final String pidManagerServiceName = "pid_manager_service_name";

	public static final String pidManagerUsername = "pid_manager_username";

	public static final String pidManagerPassword = "pid_manager_password";
	
	public static final String oracleDriver = "oracle.jdbc.driver.OracleDriver";
	
	public static final String metaIp = "meta_ip";

	public static final String metaPort = "meta_port";

	public static final String metaServiceName = "meta_service_name";

	public static final String metaUsername = "meta_username";

	public static final String metaPassword = "meta_password";
	
	public static final String uploadPath = "dropbox.upload.path";
	
	public static final String uploadPathCustom = "dropbox.upload.path.custom";
	
	public static final String downloadFilePathRoot="dropbox.download.filepath.root";
	
	public static final String downloadUrlPathRoot="dropbox.download.urlpath.root";
	
	public static final String downloadFilePathTips = "dropbox.download.filepath.tips";
	
	public static final String downloadFilePathNds = "dropbox.download.filepath.nds";
	
	public static final String downloadFilePathBasedata = "dropbox.download.filepath.basedata";
	
	public static final String downloadFilePathPatternimg = "dropbox.download.filepath.patternimg";
	
	public static final String downloadUrlPathTips = "dropbox.download.urlpath.tips";
	
	public static final String downloadUrlPathNds = "dropbox.download.urlpath.nds";
	
	public static final String downloadUrlPathBasedata = "dropbox.download.urlpath.basedata";
	
	public static final String downloadUrlPathPatternimg = "dropbox.download.urlpath.patternimg";
	
	public static final String downloadFilePathPoi = "editsupport.poi.download.filepath.poi";
	
	public static final String downloadUrlPathPoi = "editsupport.poi.download.urlpath.poi";
	
	public static final String serverUrl = "dropbox.url";
	
	public static final String gdbVersion = "gdb.version";
	
	public static final String inforUploadUrl = "mapspotter.infor.upload.url";
	
	public static final String inforTimeOut = "mapspotter.infor.upload.timeout";
	
	public static final String dataSourceType="datasource.sql.type";
	
	public static final String dataSourceStatEnable="datasource.sql.stat.enable";
	
	public static final String pidServers="pid.connection.string";
	public static final String cmsUrl="cms.url";
	
	public static final String rticServers = "rtic.connection.string";
	
	public static final String fmStat="fm_stat";
	public static final String mongoHost="mongo_host";
	public static final String mongoPort="mongo_port";
	
	public static final String multisrcDaySyncUrl="multisrc.day.sync.url";
	public static final String multisrcDayNotifyUrl="multisrc.day.notify.url";
	public static final String seasonVersion="SEASON.VERSION";
	public static final String productConvert="product.convert";
	public static final String day2mounthGetLock="day2mounth.getLock";
	public static final String day2mounthReleaseLock="product.releaseLock";
	public static final String FMBAT20110ThreadParameter="FMBAT20110.threadParameter";
	
	public static final String valueSmtp="VALUE_SMTP";
	public static final String sendEmail="SEND_EMAil";
	public static final String sendUser="SEND_USER";
	public static final String sendPwd="SEND_PWD";
	
	public static final String mapspotterCrowdUrl="mapspotter.crowd.url";
	public static final String smapMailUrl="smap.mail.url";
	
	public static final String mapspotterInfoPass="mapspotter.info.pass.url";
	public static final String mapspotterInfoFeedBack="mapspotter.info.feedback.url";
	
	public static final String manPassword="man.password";
	
	public static final String gdbSqlitePassword="gdbsqlite.password";
	
	public static final String baiduGeocoding="baiduGeocoding";

	//es相关配置
	public static final String es_master = "es.master";
	public static final String es_port = "es.port";
	public static final String es_cluster ="es.cluster";
	public static final String es_index_trackpoints ="es.index.trackpoints";

    //20170911 adas hbase库独立
    public static final String adasHbaseAddress = "hbase.adas.address";
    
    //深度信息质检率系数
    public static final String deepQcQualityRateRatio = "extract.deep.qc.quality.rate.ratio";
    
    //日出品统计接口
    public static final String dayProduceStatUrl = "day.produce.stat.url";
    
    //点门牌下载
    public static final String downloadFilePathPa = "editsupport.pa.download.filepath.pa";
	
	public static final String downloadUrlPathPa = "editsupport.pa.download.urlpath.pa";
}
