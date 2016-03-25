package com.navinfo.dataservice.expcore.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInputException;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.source.OracleSource;
import com.navinfo.dataservice.expcore.source.parameter.SerializeParameters;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleSql;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleXmlConfigSql;
import com.navinfo.dataservice.commons.log.JobLogger;
import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: OracleInput 
 * @author Xiao Xiaowen 
 * @date 2015-11-2 下午2:03:16 
 * @Description: TODO
 *  
 */
public class OracleInput implements DataInput {
	protected Logger log = Logger.getLogger(OracleInput.class);
	
	//导入参数
	protected ExportConfig expConfig;
	protected OracleSource source;
	// 排序后且格式化的可以立即执行的导出sql
	protected Map<Integer, List<ExpSQL>> expSqlMap;

	public OracleInput(ExportConfig expConfig)throws ExportException{
		log = JobLogger.getLogger(log);
		this.expConfig=expConfig;
		initSource();
	}
	public void initSource()throws ExportException{
		OracleSchema schema = null;
		try{
			schema = (OracleSchema)new DbManager().getDbById(expConfig.getSourceDbId());
		}catch(DataHubException e){
			throw new ExportException("初始化导出源时从datahub查询源库出现错误："+e.getMessage(),e);
		}
		if(schema==null){
			throw new ExportException("导出参数错误，源的dbId不能为空");
		}
		this.source=new OracleSource(schema);
		source.init(expConfig.getGdbVersion());
	}
	public void releaseSource(){
		source.release();
		source=null;
	}
	
	public Map<Integer, List<ExpSQL>> getExpSqlMap() {
		return expSqlMap;
	}
	
	
	
	public void setExpSqlMap(Map<Integer, List<ExpSQL>> expSqlMap) {
		this.expSqlMap = expSqlMap;
	}
	
	
	
	
	/**
	 * @return the source
	 */
	public OracleSource getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(OracleSource source) {
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.expcore.input.DataInput#loadScripts()
	 */
	@Override
	public void loadScripts() throws ExportInputException, Exception {
		AssembleSql as = new AssembleXmlConfigSql(expConfig.getExportMode(),expConfig.getFeature(),expConfig.getCondition(),expConfig.getConditionParams());
		expSqlMap = as.assemble(expConfig.getGdbVersion(), source.getTempSuffix());

	}
	public void serializeParameters()throws ExportException{
		try{
			//serializeParameters
			log.info("序列化导出参数到数据库临时表中");
			Map<String,Set<String>> params = new HashMap<String,Set<String>>();
			params.put(expConfig.getCondition(), expConfig.getConditionParams());

			SerializeParameters serializeParameters = new SerializeParameters();
			serializeParameters.serialize(
					source.getSchema().getPoolDataSource(),
					params,
					source.getTempSuffix(),expConfig.getGdbVersion());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ExportException("序列化导出参数到数据库临时表中时发生错误。",e);
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.expcore.input.DataInput#input()
	 */
	@Override
	public void input() throws ExportInputException, Exception {
		// TODO Auto-generated method stub

	}

}
