package com.navinfo.dataservice.datahub.chooser.strategy;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: DbServerStrategy 
 * @author Xiao Xiaowen 
 * @date 2015-12-01 上午9:00:24 
 * @Description: TODO
 */
public abstract class DbServerStrategy {
	
	public static final String USE_REF_DB = "use_ref_db";
	public static final String RELATIVELY_IDLE = "relatively_idle";
	public static final String RANDOM = "random";
	public static final String USE_SPEC_SVR = "use_spec_svr";

	public DbServerStrategy(AbstractStrategyLock strategyLock){
		
	}
	public abstract DbServer getPriorDbServer(String useType,Map<String,Object> params)throws DataHubException;
	public abstract DbServer getPriorDbServer(List<DbServer> dbServers,Map<String,Object> params)throws DataHubException;
}
