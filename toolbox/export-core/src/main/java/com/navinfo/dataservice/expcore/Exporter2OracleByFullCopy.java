package com.navinfo.dataservice.expcore;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInitException;
import com.navinfo.dataservice.expcore.sql.ExecuteFullCopySql;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleFullCopySql;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.dataservice.commons.util.RandomUtil;

/** 
 * @ClassName: FullCopy2OracleByDbLink 
 * @author Xiao Xiaowen 
 * @date 2016-1-10 上午12:38:48 
 * @Description: TODO
 */
public class Exporter2OracleByFullCopy implements Exporter {
	protected Logger log = Logger.getLogger(this.getClass());
	protected ExportConfig expConfig;
	
	public Exporter2OracleByFullCopy(ExportConfig expConfig){
		this.expConfig=expConfig;
	}

	/* 
	 * 只支持oracle2oracle
	 * 不抛异常
	 */
	@Override
	public ExporterResult execute() {
		log.debug("starting FullCopy:exportConfig=" + expConfig.toString());
		ExporterResult result = new ExporterResult();
		long st = System.currentTimeMillis();
		OracleSchema sourceSchema = null;
		OracleSchema targetSchema = null;
		String dbLinkName = null;
		try{
			//get source&target schema
			try{
				sourceSchema = (OracleSchema)new DbManager().getDbById(expConfig.getSourceDbId());
			}catch(DataHubException e){
				throw new ExportException("初始化导出源时从datahub查询源库出现错误："+e.getMessage(),e);
			}
			if(sourceSchema==null){
				throw new ExportException("导出参数错误，源的dbId不能为空");
			}

			try{
				targetSchema = (OracleSchema)new DbManager().getDbById(expConfig.getTargetDbId());
			}catch(DataHubException e){
				throw new ExportException("初始化目标库时从datahub查询源库出现错误："+e.getMessage(),e);
			}
			if(targetSchema==null){
				throw new ExportException("导出参数错误，目标库的dbId不能为空");
			}
			//create db link
			DbLinkCreator cr = new DbLinkCreator();
			dbLinkName = targetSchema.getDbUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, targetSchema.getDriverManagerDataSource(), sourceSchema.getDbUserName(), sourceSchema.getDbUserPasswd(), sourceSchema.getDbServer().getIp(), String.valueOf(sourceSchema.getDbServer().getPort()), sourceSchema.getDbServer().getServiceName());
			
			//
			AssembleFullCopySql assemble = new AssembleFullCopySql();
			//暂时
			List<ExpSQL> copySqls = assemble.assemble(dbLinkName, sourceSchema, targetSchema, expConfig.getGdbVersion(),expConfig.getSpecificTables(), expConfig.getExcludedTables());
			ExecuteFullCopySql copySqlExecutor = new ExecuteFullCopySql(expConfig,targetSchema);
			ThreadLocalContext ctx = new ThreadLocalContext(log);
			copySqlExecutor.execute(copySqls, ctx);
			//删除dblink
			cr.drop(dbLinkName, false, targetSchema.getDriverManagerDataSource());
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			result.setStatus(ExporterResult.STATUS_FAILED);
			String exceptionMsg = "";
			if (e instanceof ExportInitException){
				exceptionMsg="导出初始化过程出错，可能的原因是导出config不正确。";
			}
			//......
			result.setMsg(exceptionMsg);
			
		} finally {
			//释放source和target
			if(sourceSchema!=null)
				sourceSchema.closePoolDataSource();
			if(targetSchema!=null)
				targetSchema.closePoolDataSource();
			long ft = System.currentTimeMillis();
			result.setTimeConsumingInSec((ft-st)/1000);
		}
		return result;
	}

}
