package com.navinfo.dataservice.api.edit.iface;

import java.util.Collection;
import java.util.Set;

/** 
* @ClassName: DatalockApiService 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:31:32 
* @Description: TODO
*  
*/
public interface DatalockApi {
	Set<Integer> query(int prjId,Set<Integer> meshes)throws Exception;
	int lock(int prjId, int userId, Set<Integer> meshes,int lockType)throws Exception;
	int unlock(int prjId,int lockSeq,int lockType)throws Exception;
	void unlock(int prjId, Set<Integer> meshes,int lockType)throws Exception;
	public int lockGrid(int regionId, int lockObject, Collection<Integer> grids,int lockType,String dbType)throws Exception;
	public int unlockGrid(int lockSeq,String dbType)throws Exception;
}
