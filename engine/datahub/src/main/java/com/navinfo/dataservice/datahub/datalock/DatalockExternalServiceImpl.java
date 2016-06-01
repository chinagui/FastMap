package com.navinfo.dataservice.datahub.datalock;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.ServiceException;
import com.navinfo.dataservice.api.datalock.iface.DatalockExternalService;

/** 
* @ClassName: DataLockService 
* @author Xiao Xiaowen 
* @date 2016年3月22日 下午6:22:20 
* @Description: TODO
*/
@Service("datalockExternalService")
public class DatalockExternalServiceImpl implements DatalockExternalService {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.iface.datalock.IDatalockService#query(int, java.util.Set)
	 */
	@Override
	public Set<Integer> query(int prjId, Set<Integer> meshes) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.iface.datalock.IDatalockService#test(java.lang.String)
	 */
	@Override
	public String test(String name) throws ServiceException {
		// TODO Auto-generated method stub
		return "Hello..."+name;
	}
	
	@Override
	public String help(){
		return "";
	}

}
