package com.navinfo.dataservice.engine.edit.datalock;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;

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
	public Set<Integer> query(int prjId, Set<Integer> meshes) throws Exception {
		return MeshLockManager.getInstance().query(prjId, meshes);
	}

	@Override
	public int lock(int prjId, int userId, Set<Integer> meshes, int lockType) throws Exception {
		return MeshLockManager.getInstance().lock(prjId, userId, meshes, lockType);
	}

	@Override
	public int unlock(int prjId, int lockSeq, int lockType) throws Exception {
		
		return MeshLockManager.getInstance().unlock(prjId, lockSeq, lockType);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.edit.iface.DatalockApi#unlock(int, java.util.Set, int)
	 */
	@Override
	public void unlock(int prjId, Set<Integer> meshes, int lockType) throws Exception {
		MeshLockManager.getInstance().unlock(prjId, meshes, lockType);
	}

}
