package com.navinfo.dataservice.control.row.quality;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

public class PoiQuality {
	private static final Logger logger = Logger.getLogger(PoiQuality.class);

	public void initQualityData(int qualityId, int dbId) throws Exception {

		Connection conn = null;
		try {
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
