/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/** 
* @ClassName: RdVariableSpeedSearch 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午5:40:29 
* @Description: TODO
*/
public class RdVariableSpeedSearch implements ISearch {
	
	private Connection conn;

	public RdVariableSpeedSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return (IObj) new AbstractSelector(RdVariableSpeed.class, conn).loadById(pid, false);
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		return null;
	}

}
