package com.navinfo.dataservice.engine.limit.datahub.chooser.strategy;

import com.navinfo.dataservice.engine.limit.datahub.model.DbServer;
import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;

import java.util.List;
import java.util.Map;

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
