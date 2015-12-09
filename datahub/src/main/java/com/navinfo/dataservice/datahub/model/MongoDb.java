package com.navinfo.dataservice.datahub.model;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: MongoDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午1:47:59 
 * @Description: TODO
 */
public class MongoDb extends UnifiedDb {
	protected Logger log = Logger.getLogger(this.getClass());
	public MongoDb(){
		super();
	}
	public MongoDb(String dbName,String dbType){
		super(dbName,null,-1,null,dbType);
	}
	public MongoDb(String dbName,String dbType,DbServer dbServer){
		super(-1,dbName,null,-1,null,dbType,dbServer);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.model.AbstractDb#getAdminDb()
	 */
	@Override
	public UnifiedDb getAdminDb() throws DataHubException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.model.AbstractDb#create(com.navinfo.dataservice.datahub.model.AbstractDb)
	 */
	@Override
	public boolean create(UnifiedDb adminDb) throws DataHubException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConnectString()throws DataHubException {
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
