package com.navinfo.dataservice.datahub.creator;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: MongoDbPhysicalCreator 
 * @author Xiao Xiaowen 
 * @date 2015-12-25 下午4:41:03 
 * @Description: TODO
 */
public class MongoDbPhysicalCreator implements DbPhysicalCreator {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.creator.DbPhysicalCreator#create(com.navinfo.dataservice.datahub.model.UnifiedDb, java.lang.String)
	 */
	@Override
	public void create(DbInfo db) throws DataHubException {
		// do nothing

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.creator.DbPhysicalCreator#installGdbModel(com.navinfo.dataservice.datahub.model.UnifiedDb, java.lang.String)
	 */
	@Override
	public void installGdbModel(DbInfo db, String gdbVersion)
			throws DataHubException {
		// do nothing
	}

}
