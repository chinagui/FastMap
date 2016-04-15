package com.navinfo.dataservice.datahub.creator;

import com.navinfo.dataservice.api.datahub.model.DbServerType;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.UnifiedDb;

/** 
 * @ClassName: DbCreator 
 * @author Xiao Xiaowen 
 * @date 2015-12-25 下午2:41:48 
 * @Description: TODO
 */
public interface DbPhysicalCreator {
	public void create(UnifiedDb db)throws DataHubException;
	public void installGdbModel(UnifiedDb db,String gdbVersion)throws DataHubException;
}
