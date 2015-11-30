package com.navinfo.dataservice.expcore.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dms.commons.database.ColumnMetaData;
import com.navinfo.dms.commons.database.DataBaseUtils;
import com.navinfo.dms.commons.utils.RandomUtil;
import com.navinfo.dms.commons.utils.StringUtils;
import com.navinfo.dataservice.expcore.ExporterResult;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.model.OracleSchema;
import com.navinfo.dataservice.expcore.output.AbstractDataOutput;
import com.navinfo.dataservice.expcore.target.OracleTarget;
import com.navinfo.dms.tools.vm.config.SystemConfig;
import com.navinfo.dms.tools.vm.database.ConnectionRegister;
import com.navinfo.dms.tools.vm.thread.ThreadLocalContext;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-1-27 Time: 上午11:03
 * 数据导出到sqlite
 */
public class Oracle2OracleDataOutput extends AbstractDataOutput {
	protected ExportConfig expConfig;
	protected OracleTarget target;

	public Oracle2OracleDataOutput(ExportConfig expConfig,ExporterResult expResult, ThreadLocalContext ctx) throws ExportException{
		super(expResult,ctx);
		this.expConfig=expConfig;
		initTarget();
	}
	public void initTarget()throws ExportException{
		if(expConfig.isNewTarget()){
			if(StringUtils.isEmpty(expConfig.getTargetUserName())
					||StringUtils.isEmpty(expConfig.getTargetPassword())){
				String userNameStr="VM_" + RandomUtil.nextString(10);
				expConfig.setTargetUserName(userNameStr);
				expConfig.setTargetPassword(userNameStr);
			}
		}
		OracleSchema schema = new OracleSchema(expConfig.getTargetUserName(),
				expConfig.getTargetPassword(),
				expConfig.getTargetIp(),
				expConfig.getTargetPort(),
				expConfig.getTargetServiceName(),
				expConfig.getTargetTablespaceName());
		if(expConfig.isNewTarget()){
			try{	
				schema.create(expConfig.getTargetSysName(),expConfig.getTargetSysPassword());
			}catch(SQLException e){
				log.error("创建目标库时失败。",e);
				throw new ExportException("创建目标库时失败。",e);
			}
		}
		this.target=new OracleTarget(schema,expConfig.isNewTarget());
		target.init(expConfig.getGdbVersion());

		expResult.setNewTargetUserName(target.getSchema().getUserName());
		expResult.setNewTargetPassword(target.getSchema().getPassword());
	}
	public void releaseTarget(){
		target.release(expConfig.isDestroyTarget());
		target=null;
	}

	protected int calculateBatchSize(List<ColumnMetaData> tmdList) {
		int columnCount = tmdList.size();
		int batchSize = SystemConfig.getSystemConfig().getIntValue("insertBatchSize", 1000);
		if (columnCount > 20) {
			batchSize = 100;
		}
		return batchSize;
	}

	protected void doOutput(ResultSet rs,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList) throws Exception {
		String insertSql = DataBaseUtils.generateInsertSql(reNameTo, tmdList, null);
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		try {
			DataSource dataSource = target.getSchema().getPoolDataSource();
			conn = ConnectionRegister.subThreadGetConnection(ctx, dataSource);
			// logger.debug(insertSql+" tableName="+tableName+" reNameTo="+reNameTo);
			// logger.debug(insertSql);
			/*
			 * if (insertSql.indexOf("IX") > -1) {
			 * 
			 * logger.debug(insertSql+" tableName="+tableName+" reNameTo="+reNameTo
			 * ); }
			 */
			stmt = conn.prepareStatement(insertSql);
			// 在执行数据导入前，先删除所有数据
			/*
			 * if (((OracleTarget) exportTarget).isTruncateData()) { //删除表数据 //
			 * logger.debug("truncate table "+reNameTo+" version info "+
			 * version.getDataBase().); truncateOuputTable(conn, reNameTo); }
			 */
			while (rs.next()) {
				count++;
				for (int i = 0; i < tmdList.size(); i++) {
					ColumnMetaData tmd = tmdList.get(i);
					Object value = rs.getObject(tmd.getColumnName());
					if (tmd.isGeometryColumn() && value == null)
						stmt.setNull(i + 1, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					else if (tmd.isClobColumn()) {
						if (value == null) {
							stmt.setCharacterStream(i + 1, null);
						} else {
							stmt.setCharacterStream(i + 1, rs.getCharacterStream(tmd.getColumnName()));
						}
					}

					else {
						stmt.setObject(i + 1, value);
					}

				}
				stmt.addBatch();
				// 没n条提交一次，如果n非常大，会报错
				if (count % calculateBatchSize(tmdList) == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}

			}
			stmt.executeBatch();
			stmt.clearBatch();
			// logger.debug(reNameTo + ":" + count);
		} catch (Exception e) {
			log.error("doOutput error! insertSql=" + insertSql, e);
			throw e;
			// context.shutdownApp(
			// new ThreadExecuteException("输出内容到oracle异常，请查看日志文件",
			// e));e
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
}
