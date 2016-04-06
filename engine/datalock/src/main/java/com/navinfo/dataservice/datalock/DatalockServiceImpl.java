package com.navinfo.dataservice.datalock;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.RemoteServiceException;
import com.navinfo.dataservice.api.datalock.iface.DatalockService;

/** 
* @ClassName: DataLockService 
* @author Xiao Xiaowen 
* @date 2016年3月22日 下午6:22:20 
* @Description: TODO
*/
@Service("datalockService")
public class DatalockServiceImpl implements DatalockService {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.iface.datalock.IDatalockService#query(int, java.util.Set)
	 */
	@Override
	public Set<Integer> query(int prjId, Set<Integer> meshes) throws RemoteServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.iface.datalock.IDatalockService#test(java.lang.String)
	 */
	@Override
	public String test(String name) throws RemoteServiceException {
		// TODO Auto-generated method stub
		return "Hello..."+name;
	}

}
