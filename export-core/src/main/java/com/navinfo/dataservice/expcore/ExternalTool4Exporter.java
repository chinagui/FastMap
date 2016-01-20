package com.navinfo.dataservice.expcore;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.DataBaseUtils;


/** 
 * @ClassName: ExporterExternalTools 
 * @author Xiao Xiaowen 
 * @date 2016-1-20 上午10:10:03 
 * @Description: TODO
 */
public class ExternalTool4Exporter {
	protected Logger log = Logger.getLogger(this.getClass());

	//进一步可以做多线程
	
	public void turnOnPkConstrain(OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			DataBaseUtils.turnOnPkConstraint(conn, tables);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("打开主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void turnOffPkConstrain(OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			DataBaseUtils.turnOffPkConstraint(conn, tables);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void removeDupRecord(OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("打开主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<String>();
	}

}
