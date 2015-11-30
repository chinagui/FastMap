package com.navinfo.dataservice.expcore.output;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.navinfo.dms.commons.config.Config;
import com.navinfo.dms.commons.database.ColumnMetaData;
import com.navinfo.dms.commons.database.DataBaseUtils;
import com.navinfo.dataservice.expcore.ExporterResult;
import com.navinfo.dataservice.expcore.target.tablerename.TableReName;
import com.navinfo.dataservice.expcore.target.ExportTarget;
import com.navinfo.dms.tools.vm.config.SystemConfig;
import com.navinfo.dms.tools.vm.thread.ThreadLocalContext;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-1-27
 * Time: 上午11:03
 * 数据导出到sqlite
 */
public abstract class AbstractDataOutput implements DataOutput {
	protected Logger log = Logger.getLogger(this.getClass());


	protected List<TableReName> tableReNames = new ArrayList<TableReName>();
	protected ThreadLocalContext ctx;
	protected ExporterResult expResult;

	public AbstractDataOutput(ExporterResult expResult,ThreadLocalContext ctx) {
		this.expResult=expResult;
		this.ctx = ctx;
		log = ctx.getLog();
	}

	public void addTableReName(TableReName tableReName) {
		tableReNames.add(tableReName);
	}

	public void clearTableReName() {
		tableReNames.clear();
	}

	/**
	 * @param resultSet
	 * @param tableName
	 * @param reNameTo
	 * @throws Exception
	 */
	public void output(ResultSet resultSet,
			String tableName,
			String reNameTo) throws Exception {

		// 获取表元数据信息
		List<ColumnMetaData> tmdList = getTableMetaData(resultSet, tableName);
		// 根据元数据生成insert 语句中字段表名信息（非完整的insert）
		tmdList = DataBaseUtils.removeIgnoreColumn(tmdList, null);
		doOutput(resultSet, tableName, reNameTo, tmdList);
	}

	protected abstract void doOutput(ResultSet resultSet,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList
			) throws Exception;

	/**
	 * 自动转换表名
	 * 
	 * @param resultSet
	 * @param tableName
	 * @throws Exception
	 */
	public void output(ResultSet resultSet, String tableName) throws Exception {
		String reNameTable = tableName;
		// logger.debug("开始写表" + tableName + "数据到表" + reNameTable);
		for (TableReName reName : tableReNames) {
			reNameTable = reName.reNameTo(reNameTable);
		}
		// logger.debug("开始写表" + tableName + "数据到表" + reNameTable);
		output(resultSet, tableName, reNameTable);
	}

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
