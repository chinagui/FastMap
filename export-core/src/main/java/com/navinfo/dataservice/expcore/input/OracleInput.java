package com.navinfo.dataservice.expcore.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInputException;
import com.navinfo.dataservice.expcore.model.OracleSchema;
import com.navinfo.dataservice.expcore.source.OracleSource;
import com.navinfo.dataservice.expcore.source.parameter.ScriptsConfigManager;
import com.navinfo.dataservice.expcore.source.parameter.SerializeParameters;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.commons.log.DSJobLogger;

/** 
 * @ClassName: OracleInput 
 * @author Xiao Xiaowen 
 * @date 2015-11-2 下午2:03:16 
 * @Description: TODO
 *  
 */
public class OracleInput implements DataInput {
	protected Logger log = Logger.getLogger(ScriptsConfigManager.class);
	
	//导入参数
	protected ExportConfig expConfig;
	protected OracleSource source;
	// 排序后且格式化的可以立即执行的导出sql
	protected Map<Integer, List<ExpSQL>> expSqlMap;

	public OracleInput(ExportConfig expConfig)throws ExportException{
		log = DSJobLogger.getLogger(log);
		this.expConfig=expConfig;
		initSource();
	}
	public void initSource()throws ExportException{
		OracleSchema schema = new OracleSchema(expConfig.getSourceUserName(),
				expConfig.getSourcePassword(),
				expConfig.getSourceIp(),
				expConfig.getSourcePort(),
				expConfig.getSourceServiceName(),
				expConfig.getSourceTablespaceName());
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
		if(expConfig.isFastCopy()){
			return;
		}
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
