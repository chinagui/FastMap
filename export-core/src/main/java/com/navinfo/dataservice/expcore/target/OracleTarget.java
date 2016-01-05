package com.navinfo.dataservice.expcore.target;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.sql.SqlExec;

/** 
 * @ClassName: OracleTarget 
 * @author Xiao Xiaowen 
 * @date 2015-10-29 下午5:30:04 
 * @Description: TODO
 *  
 */
public class OracleTarget extends AbstractExportTarget {
	private OracleSchema schema;

	
	public OracleTarget(OracleSchema schema){
		this.schema=schema;
	}
	/**
	 * @return the schema
	 */
	public OracleSchema getSchema() {
		return schema;
	}
	/**
	 * @param schema the schema to set
	 */
	public void setSchema(OracleSchema schema) {
		this.schema = schema;
	}
	@Override
	public void release(boolean destroyTarget) {
		log.info("destroy the pooldatasource of the source schema.");
		if(schema!=null){
			schema.closePoolDataSource();
		}
		if(destroyTarget){
			log.info("开始将schema加入清理队列。");
			log.info("当前版本不支持自动清理子版本。");
		}
	}
}
