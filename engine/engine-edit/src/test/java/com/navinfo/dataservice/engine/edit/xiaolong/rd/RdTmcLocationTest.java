/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.rd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.search.RdGscSearch;
import com.navinfo.dataservice.dao.glm.search.RdTmcLocationSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;

/** 
* @ClassName: RdTmcLocationTest 
* @author Zhang Xiaolong
* @date 2016年11月17日 下午7:22:17 
* @Description: TODO
*/
public class RdTmcLocationTest extends InitApplication{
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void testRdTmcLocation()
	{
		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDTMCLOCATION\",\"dbId\":17,\"data\":{\"tmcId\":\"522001558\",\"direct\":\"1\",\"locDirect\":\"1\",\"loctableId\":\"32\",\"linkPids\":[565603,565604,681841,681842,611966,678760,678759,15481020,15481019,49043863,671528,681844,606299,671718,685606,685605,671456,12566922,12566924,12566923,671526,88075237,88075238,682639,682640,583413,682642,682643,682657,682658]}}";
		Transaction t = new Transaction(parameter);
		try {
			String msg = t.run();
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByPid() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);

			System.out.println(p.searchDataByPid(ObjType.RDTMCLOCATION, 36475).Serialize(ObjLevel.BRIEF));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetByPids() {
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			SearchProcess p = new SearchProcess(conn);
			
			List<Integer> tmcPidList = new ArrayList<>();
			
			tmcPidList.add(36475);
			
			tmcPidList.add(36477);
			
			JSONArray pids = new JSONArray();
			
			pids.add(36477);
			
			pids.add(36475);
			
			List<? extends IObj> objs = p.searchDataByPids(ObjType.RDTMCLOCATION, pids);
			
			for(IObj obj : objs)
			{
				System.out.println(obj.Serialize(ObjLevel.FULL));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRwRender()
	{
		Connection conn;
		try {
			conn = DBConnector.getInstance().getConnectionById(17);

			RdTmcLocationSearch search = new RdTmcLocationSearch(conn);
			
			List<SearchSnapshot> searchDataByTileWithGap = search.searchDataByTileWithGap(215852, 99235, 18, 80);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(searchDataByTileWithGap));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
