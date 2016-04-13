package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;

public class RdNodeSearch implements ISearch {

	private Connection conn;

	public RdNodeSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdNodeSelector selector = new RdNodeSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Exception {
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		Connection conn = GlmDbPoolManager.getInstance().getConnection(1);
		
		RdNodeSearch s = new RdNodeSearch(conn);
		
		IObj obj = s.searchDataByPid(132837);
		
		System.out.println(obj.Serialize(null));
	}
}
