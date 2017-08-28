package com.navinfo.dataservice.commons.constant;


import com.navinfo.dataservice.commons.config.SystemConfigFactory;

public class EsConstant {
    public static final String master = SystemConfigFactory.getSystemConfig().getValue(PropConstant.es_master);
    public static final int port = SystemConfigFactory.getSystemConfig().getIntValue(PropConstant.es_port);
    public static final String cluster = SystemConfigFactory.getSystemConfig().getValue(PropConstant.es_cluster);
    public static final String index_trackpoints = SystemConfigFactory.getSystemConfig().getValue(PropConstant.es_index_trackpoints);
    public static final String geo_trackpoints = "a_geometry";
}