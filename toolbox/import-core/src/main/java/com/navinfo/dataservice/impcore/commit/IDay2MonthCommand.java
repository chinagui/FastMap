package com.navinfo.dataservice.impcore.commit;

import java.util.Map;

/*
 * @author MaYunFei
 * 2016年6月14日
 * 描述：import-coreIDay2MonthCommand.java
 */
public interface IDay2MonthCommand {
	public Map queryRegionGridMapping() throws Exception;
	public String getStopTime();
	public String getFlushFeatureType();
	public int getLockType();
}

