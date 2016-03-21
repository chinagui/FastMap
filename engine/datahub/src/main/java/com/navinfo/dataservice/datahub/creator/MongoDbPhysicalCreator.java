package com.navinfo.dataservice.datahub.creator;

import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.UnifiedDb;

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
	public void create(UnifiedDb db) throws DataHubException {
		// do nothing

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.creator.DbPhysicalCreator#installGdbModel(com.navinfo.dataservice.datahub.model.UnifiedDb, java.lang.String)
	 */
	@Override
	public void installGdbModel(UnifiedDb db, String gdbVersion)
			throws DataHubException {
		// do nothing
	}

}
