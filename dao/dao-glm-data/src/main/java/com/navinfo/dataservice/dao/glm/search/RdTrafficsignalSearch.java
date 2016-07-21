/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

/** 
* @ClassName: RdTrafficsignalSearch 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:45:46 
* @Description: TODO
*/
public class RdTrafficsignalSearch implements ISearch {
	
	private Connection conn;

	public RdTrafficsignalSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdTrafficsignalSelector rdTrafficsignalSelector = new RdTrafficsignalSelector(conn);
		
		return (IObj) rdTrafficsignalSelector.loadById(pid, false);
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
