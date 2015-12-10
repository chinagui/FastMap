package com.navinfo.dataservice.web.datahub.manager;


import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
/** 
 * @ClassName: DataHubManager 
 * @author Xiao Xiaowen 
 * @date 2015-12-4 上午10:45:40 
 * @Description: TODO
 */
public class DataHubManager {
	protected Logger log = Logger.getLogger(this.getClass());
	public Map<String,Object> createDb(String dbName,String dbType,String descp)throws DataHubException{
		DbManager dbMan = new DbManager();
		UnifiedDb  db =dbMan.createDb(dbName, dbType, descp);
		
		return null;
	}
}
