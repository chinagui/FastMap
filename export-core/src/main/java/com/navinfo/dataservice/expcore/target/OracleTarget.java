package com.navinfo.dataservice.expcore.target;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.model.OracleSchema;
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

	
	public OracleTarget(OracleSchema schema,boolean newTarget){
		super(newTarget);
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
	public void installGdbModel(String gdbVersion)throws ExportException{
		if(!this.isNewTarget()){
			log.info("目标库不是新库，不安装gdb模型。");
			return;
		}
		Connection conn = null;
		try{
			conn = schema.getPoolDataSource().getConnection();
			String schemaCreateFile = "/com/navinfo/dataservice/expcore/resources/"
					+ gdbVersion + "/schema/table_create.sql";
			SqlExec sqlExec = new SqlExec(conn);
			sqlExec.execute(schemaCreateFile);
		}catch(Exception e){
			log.error("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
			throw new ExportException("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
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
