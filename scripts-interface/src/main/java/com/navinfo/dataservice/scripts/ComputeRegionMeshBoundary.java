package com.navinfo.dataservice.scripts;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;

/** 
 * @ClassName: ComputeRegionMeshBoundary
 * @author xiaoxiaowen4127
 * @date 2017年7月25日
 * @Description: ComputeRegionMeshBoundary.java
 */
public class ComputeRegionMeshBoundary {
	public static Logger log = Logger.getLogger(SyncTips2Oracle.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			DbInfo tiInfo = DbService.getInstance().getOnlyDbByBizType("fmMan");
			final OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tiInfo.getConnectParam()));
			conn = schema.getPoolDataSource().getConnection();
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			log.info("Over.");
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
