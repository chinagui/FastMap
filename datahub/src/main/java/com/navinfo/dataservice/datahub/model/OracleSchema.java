package com.navinfo.dataservice.datahub.model;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.SerializationConfig;

import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: OracleSchema 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午8:08:01 
 * @Description: TODO
 */
public class OracleSchema extends UnifiedDb {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public UnifiedDb getAdminDb()throws DataHubException{
		return null;
	}
	@Override
	public boolean create(UnifiedDb adminDb)throws DataHubException{
		if(this.isAdminDb()){
			throw new DataHubException("Sys用户不能被创建。");
		}
		return false;
	}
	
	public String getConnectString()throws DataHubException{
		String conString = null;
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
			conString = mapper.writeValueAsString(this);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new DataHubException("将oracleschema实体对象解析成json时发生错误，原因："+e.getMessage(),e);
		}
		return conString;
	}
	
}
