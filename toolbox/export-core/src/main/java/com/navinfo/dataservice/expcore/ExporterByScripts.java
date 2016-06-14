package com.navinfo.dataservice.expcore;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.expcore.sql.ExecuteSql;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInitException;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.DataOutput;

/**
 * User: Xiao Xiaowen  数据导出的入口，给定参数，实现导出功能
 */
public abstract class ExporterByScripts implements Exporter {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	protected ExportConfig expConfig;
	private OracleInput input;
	private DataOutput output;

	public ExporterByScripts(ExportConfig expConfig) {
		this.expConfig=expConfig;
	}

	public abstract boolean validateExportConfig(ExporterResult result)throws ExportException;
	public abstract OracleInput initDataInput(ExporterResult result)throws ExportException;
	public abstract DataOutput initDataOutput(ExporterResult result)throws ExportException;

	/**
	 * 数据导出,不抛异常，返回导出结果
	 * @return ExporterResult
	 */
	public ExporterResult execute(
			) {

		log.debug("starting FlexibleExporter:exportConfig=" + expConfig.toString());
		ExporterResult result = new ExporterResult();
		long st = System.currentTimeMillis();
		try {
			//验证导出配置是否正确
			validateExportConfig(result);
			//...
			input = initDataInput(result);
			input.serializeParameters();
			input.loadScripts();
			output = initDataOutput(result);
			//有瑕疵，现在还不能确定source是不是应该只有oracle
			ExecuteSql exportSqlExecutor=new ExecuteSql(input,output);
			exportSqlExecutor.execute();
			//...
			result.setStatus(ExporterResult.STATUS_SUCCESS);
			if(expConfig.getTargetDbId()==0){
				String msg = "";
			}
		} catch (Exception e) {
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
			if(input!=null)
			input.releaseSource();
			if(output!=null)
			output.releaseTarget();
			long ft = System.currentTimeMillis();
			result.setTimeConsumingInSec((ft-st)/1000);
		}
		return result;

	}
}
