package com.navinfo.dataservice.expcore.output;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.ColumnMetaData;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.dataservice.expcore.ExporterResult;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-1-27
 * Time: 上午11:03
 * 数据导出到sqlite
 */
public abstract class AbstractDataOutput implements DataOutput {
	protected Logger log = Logger.getLogger(this.getClass());


	protected Map<String,String> tableReNames;
	protected ThreadLocalContext ctx;
	protected ExporterResult expResult;

	public AbstractDataOutput(ExporterResult expResult,ThreadLocalContext ctx) {
		this.expResult=expResult;
		this.ctx = ctx;
		log = ctx.getLog();
	}


	/**
	 * @param resultSet
	 * @param tableName
	 * @param reNameTo
	 * @throws Exception
	 */
	public void output(ResultSet resultSet,
			String tableName) throws Exception {
		//重命名表名
		String reNameTable = tableName;
		if(tableReNames!=null&&tableReNames.containsKey(tableName)){
			reNameTable = tableReNames.get(tableName);
		}
		// 获取表元数据信息和过滤忽略字段
		List<ColumnMetaData> tmdList = getTableMetaData(resultSet, tableName);
		tmdList = DataBaseUtils.removeIgnoreColumn(tmdList, null);
		
		doOutPut(resultSet, tableName, reNameTable, tmdList);
	}

	protected abstract void doOutPut(ResultSet resultSet,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList) throws Exception;


	/**
	 * 获取表元数据信息
	 * 
	 * @param oraRs
	 * @param tableName
	 * @return
	 * @throws java.sql.SQLException
	 */
	protected List<ColumnMetaData> getTableMetaData(ResultSet oraRs,
			String tableName) throws SQLException {
		ResultSetMetaData md = oraRs.getMetaData();
		List<ColumnMetaData> tmdList = DataBaseUtils.getTableMetaData(tableName, md);
		return tmdList;
	}

	protected int calculateBatchSize(List<ColumnMetaData> tmdList) {
		int columnCount = tmdList.size();
		int batchSize = SystemConfig.getSystemConfig().getIntValue("insertBatchSize", 1000);
		if (columnCount > 20) {
			batchSize = 100;
		}
		return batchSize;
	}

}
