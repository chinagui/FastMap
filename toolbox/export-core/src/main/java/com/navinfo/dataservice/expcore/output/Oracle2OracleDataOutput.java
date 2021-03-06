package com.navinfo.dataservice.expcore.output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.database.ColumnMetaData;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.output.AbstractDataOutput;
import com.navinfo.dataservice.expcore.target.OracleTarget;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.database.oracle.ConnectionRegister;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-1-27 Time: 上午11:03
 * 数据导出到sqlite
 */
public class Oracle2OracleDataOutput extends AbstractDataOutput {
	protected OracleTarget target;

	public Oracle2OracleDataOutput(OracleSchema targetSchema,List<String> checkExistTables,String whenExist, Map<String,String> tableReNames,ThreadLocalContext ctx) throws ExportException{
		super(ctx);
		this.checkExistTables=checkExistTables;
		this.whenExist=whenExist;
		this.tableReNames=tableReNames;
		this.target=new OracleTarget(targetSchema);
	}

	public void releaseTarget(boolean destroyTarget){
		target.release(destroyTarget);
		target=null;
	}

	protected int calculateBatchSize(List<ColumnMetaData> tmdList) {
		int columnCount = tmdList.size();
		int batchSize = SystemConfigFactory.getSystemConfig().getIntValue("insertBatchSize", 1000);
		if (columnCount > 20) {
			batchSize = 100;
		}
		return batchSize;
	}
	
	public void doOutPut(ResultSet rs,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList) throws Exception{
		if(checkExistTables!=null&&checkExistTables.contains(tableName)){
			if(ExportConfig.WHEN_EXIST_IGNORE.equals(whenExist)){
				this.doMergeOnlyInsert(rs, tableName, reNameTo, tmdList);
			}else if(ExportConfig.WHEN_EXIST_OVERWRITE.equals(whenExist)){
				this.doMergeFull(rs, tableName, reNameTo, tmdList);
			}else{
				throw new Exception("导出参数中配置了检查是否已存在的表(checkExistTables)，但未配置已存在如何操作(whenExist)。");
			}
		}else{
			this.doInsert(rs, tableName, reNameTo, tmdList);
		}
	}

	protected void doInsert(ResultSet rs,
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
			stmt = conn.prepareStatement(insertSql);
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
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	protected void doMergeFull(ResultSet rs,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList) throws Exception {

        int columnSize = tmdList.size();
        String setArr[] = new String[columnSize];
        String parameters[] = new String[columnSize];
        String columns[] = new String[columnSize];
        for (int i = 0; i < tmdList.size(); i++) {
        	setArr[i] = "\""+tmdList.get(i).getColumnName()+"\"=?";
            parameters[i] = "?";
            columns[i] = "\""+tmdList.get(i).getColumnName()+"\"";
        }
		String pkColumn = null;
		if("LOG_OPERATION".equals(tableName)){
			pkColumn = "OP_ID";
		}else if("RD_NODE".equals(tableName)){
			pkColumn="NODE_PID";
		}else{
			pkColumn = "ROW_ID";
		}
		StringBuilder builder = new StringBuilder("MERGE INTO ");
        builder.append(tableName);
        builder.append(" T USING (SELECT ? " +pkColumn+
        		" FROM DUAL) D ON (T." +pkColumn+
        		"=D." +pkColumn+
        		") WHEN MATCHED THEN UPDATE SET ");

        builder.append(StringUtils.join(setArr, ","));
        
        builder.append(" WHEN NOT MATCHED THEN INSERT VALUES (");
        builder.append(StringUtils.join(columns, ","));
        builder.append(") values(");
        builder.append(StringUtils.join(parameters, ","));
        builder.append(")");


		String sql =  builder.toString();
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		try {
			DataSource dataSource = target.getSchema().getPoolDataSource();
			conn = ConnectionRegister.subThreadGetConnection(ctx, dataSource);
			stmt = conn.prepareStatement(sql);
			while (rs.next()) {
				count++;
				for (int i = 0; i < columnSize; i++) {
					stmt.setObject(1, rs.getObject(pkColumn));//MERGE 条件中使用了
					ColumnMetaData tmd = tmdList.get(i);
					Object value = rs.getObject(tmd.getColumnName());
					if (tmd.isGeometryColumn() && value == null){
						stmt.setNull(i + 2, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
						stmt.setNull(i + 2+columnSize, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					}
					else if (tmd.isClobColumn()) {
						if (value == null) {
							stmt.setCharacterStream(i + 2, null);
							stmt.setCharacterStream(i + 2+columnSize, null);
						} else {
							stmt.setCharacterStream(i + 2, rs.getCharacterStream(tmd.getColumnName()));
							stmt.setCharacterStream(i + 2+columnSize, rs.getCharacterStream(tmd.getColumnName()));
						}
					}

					else {
						stmt.setObject(i + 2, value);
						stmt.setObject(i + 2+columnSize, value);
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
			log.error("doOutput error! mergeSql=" + sql, e);
			throw e;
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	protected void doMergeOnlyInsert(ResultSet rs,
			String tableName,
			String reNameTo,
			List<ColumnMetaData> tmdList) throws Exception {

        int columnSize = tmdList.size();
        String parameters[] = new String[columnSize];
        String columns[] = new String[columnSize];
        for (int i = 0; i < tmdList.size(); i++) {
            parameters[i] = "?";
            columns[i] = "\""+tmdList.get(i).getColumnName()+"\"";
        }
		String pkColumn = null;
		if("LOG_OPERATION".equals(tableName)){
			pkColumn = "OP_ID";
		}else{
			pkColumn = "ROW_ID";
		}
		StringBuilder builder = new StringBuilder("MERGE INTO ");
        builder.append(tableName);
        builder.append(" T USING (SELECT ? " +pkColumn+
        		" FROM DUAL) D ON (T." +pkColumn+
        		"=D." +pkColumn+
        		") WHEN NOT MATCHED THEN INSERT (");
        builder.append(StringUtils.join(columns, ","));
        builder.append(") VALUES (");
        builder.append(StringUtils.join(parameters, ","));
        builder.append(")");


		String sql =  builder.toString();
		Connection conn = null;
		PreparedStatement stmt = null;
		int count = 0;
		try {
			DataSource dataSource = target.getSchema().getPoolDataSource();
			conn = ConnectionRegister.subThreadGetConnection(ctx, dataSource);
			stmt = conn.prepareStatement(sql);
			while (rs.next()) {
				count++;
				stmt.setObject(1, rs.getObject(pkColumn));//MERGE 条件中使用了
				for (int i = 0; i < columnSize; i++) {
					ColumnMetaData tmd = tmdList.get(i);
					Object value = rs.getObject(tmd.getColumnName());
					if (tmd.isGeometryColumn() && value == null){
						stmt.setNull(i + 2, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					}
					else if (tmd.isClobColumn()) {
						if (value == null) {
							stmt.setCharacterStream(i + 2, null);
						} else {
							stmt.setCharacterStream(i + 2, rs.getCharacterStream(tmd.getColumnName()));
						}
					}

					else {
						stmt.setObject(i + 2, value);
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
			log.error("doOutput error! mergeSql=" + sql, e);
			throw e;
		} finally {
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
}
