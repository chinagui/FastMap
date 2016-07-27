/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/** 
* @ClassName: AbstractSearch 
* @author Zhang Xiaolong
* @date 2016年7月26日 下午3:56:27 
* @Description: TODO
*/
public class AbstractSearch{
	public IObj searchDataByPid(Class<?> cls,int pid,Connection conn) throws Exception {
		AbstractSelector selector = new AbstractSelector(cls, conn);
		
		return (IObj) selector.loadById(pid, false);
	}

}
