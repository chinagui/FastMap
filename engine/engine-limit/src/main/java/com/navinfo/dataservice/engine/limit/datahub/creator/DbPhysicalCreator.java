package com.navinfo.dataservice.engine.limit.datahub.creator;

import com.navinfo.dataservice.engine.limit.datahub.model.DbInfo;
import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;

/** 
 * @ClassName: DbCreator 
 * @author Xiao Xiaowen 
 * @date 2015-12-25 下午2:41:48 
 * @Description: TODO
 */
public interface DbPhysicalCreator {
	public void create(DbInfo db)throws DataHubException;
	public void installGdbModel(DbInfo db, String gdbVersion)throws DataHubException;
}
