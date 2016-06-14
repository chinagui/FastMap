package com.navinfo.dataservice.expcore.target;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;

/** 
 * @ClassName: OracleTarget 
 * @author Xiao Xiaowen 
 * @date 2015-10-29 下午5:30:04 
 * @Description: TODO
 *  
 */
public class OracleTarget implements ExportTarget {
	protected Logger log = Logger.getLogger(getClass());
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
