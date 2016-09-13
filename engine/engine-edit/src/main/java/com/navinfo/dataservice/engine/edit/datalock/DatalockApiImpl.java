package com.navinfo.dataservice.engine.edit.datalock;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;

/** 
* @ClassName: DatalockApiImpl 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午7:31:32 
* @Description: TODO
*  
*/
@Service("datalockApi")
public class DatalockApiImpl implements DatalockApi {

	@Override
	public int lockGrid(int regionId, int lockObject, Collection<Integer> grids,
			int lockType,String dbType,long jobId) throws Exception {
		return GridLockManager.getInstance().lock(regionId, lockObject, grids, lockType,dbType, jobId);
	}

	@Override
	public int unlockGrid(int lockSeq,String dbType) throws Exception {
		return GridLockManager.getInstance().unlock(lockSeq,dbType);
	}

	@Override
	public FmEditLock lockGrid(int dbId, int lockObject, Collection<Integer> grids, int lockType,long jobId) throws Exception {
		return GridLockManager.getInstance().lock(dbId, lockObject, grids, lockType, jobId);
	}

}
