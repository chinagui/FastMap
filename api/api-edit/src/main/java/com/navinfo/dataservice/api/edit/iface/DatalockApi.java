package com.navinfo.dataservice.api.edit.iface;

import java.util.Collection;
import java.util.Set;

import com.navinfo.dataservice.api.edit.model.FmEditLock;

/** 
* @ClassName: DatalockApiService 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:31:32 
* @Description: TODO
*  
*/
public interface DatalockApi {
	FmEditLock lockGrid(int dbId,int lockObject,Collection<Integer> grids,int lockType,long jobId)throws Exception;
	public int lockGrid(int regionId, int lockObject, Collection<Integer> grids,int lockType,String dbType,long jobId)throws Exception;
	public int unlockGrid(int lockSeq,String dbType)throws Exception;
}
