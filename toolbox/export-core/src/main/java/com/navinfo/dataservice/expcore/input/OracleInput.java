package com.navinfo.dataservice.expcore.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInputException;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.source.OracleSource;
import com.navinfo.dataservice.expcore.source.parameter.SerializeParameters;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleSql;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleXmlConfigSql;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: OracleInput 
 * @author Xiao Xiaowen 
 * @date 2015-11-2 下午2:03:16 
 * @Description: TODO
 *  
 */
public class OracleInput implements DataInput {
	protected Logger log = LoggerRepos.getLogger(OracleInput.class);
	
	protected OracleSource source;
	protected String exportMode;
	protected String feature;
	protected String condition;
	protected List<String> conditionParams;
	protected String gdbVersion;
	// 排序后且格式化的可以立即执行的导出sql
	protected Map<Integer, List<ExpSQL>> expSqlMap;

	public OracleInput(OracleSchema sourceSchema,String exportMode,String feature,String condition,List<String> conditionParams,String gdbVersion)throws ExportException{
		this.exportMode=exportMode;
		this.feature=feature;
		this.condition=condition;
		this.conditionParams=conditionParams;
		this.gdbVersion=gdbVersion;
		source = new OracleSource(sourceSchema);
	}
	public void initSource()throws ExportException{
		source.init(gdbVersion);
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
		AssembleSql as = new AssembleXmlConfigSql(exportMode,feature,condition,conditionParams);
		expSqlMap = as.assemble(gdbVersion, source.getTempSuffix());

	}
	public void serializeParameters()throws ExportException{
		try{
			//serializeParameters
			log.info("序列化导出参数到数据库临时表中");
			Map<String,List<String>> params = new HashMap<String,List<String>>();
			params.put(condition, conditionParams);

			SerializeParameters serializeParameters = new SerializeParameters();
			serializeParameters.serialize(
					source.getSchema().getPoolDataSource(),
					params,
					source.getTempSuffix(),gdbVersion);
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
