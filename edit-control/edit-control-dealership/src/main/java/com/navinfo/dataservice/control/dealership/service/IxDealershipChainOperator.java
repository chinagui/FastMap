package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class IxDealershipChainOperator {
	private static Logger log = LoggerRepos.getLogger(DataEditService.class);
	/**
	 * 表表差分后，修改IX_DEALERSHIP_CHAIN表状态
	 * @param conn
	 * @param chainCode
	 * @throws SQLException
	 */
	public static void changeChainStatus(Connection conn, String chainCode,int status) throws SQLException {
		log.info("start 表表差分修改chain表状态");
		String sql="UPDATE IX_DEALERSHIP_CHAIN SET WORK_STATUS = "+status+" WHERE CHAIN_CODE = '"+chainCode+"'";
		QueryRunner run=new QueryRunner();
		run.update(conn, sql);
	}
}
