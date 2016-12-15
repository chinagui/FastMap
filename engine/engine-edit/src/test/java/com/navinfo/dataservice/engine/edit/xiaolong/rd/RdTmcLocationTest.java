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
import com.navinfo.dataservice.dao.glm.search.TmcPointSearch;
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
		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDTMCLOCATION\",\"dbId\":19,\"data\":{\"links\":[{\"locDirect\":1,\"rowId\":\"8737C6A463E24AD9A45A49A5B8FB2803\",\"objStatus\":\"UPDATE\"},{\"locDirect\":1,\"rowId\":\"5ACB9AF3D4CA44A386667255D22336C8\",\"objStatus\":\"UPDATE\"}],\"rowId\":\"375BF7893DAD4AF8880D8B3B95A11B82\",\"pid\":300000057}}";
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

			TmcPointSearch search = new TmcPointSearch(conn);
			
			List<SearchSnapshot> searchDataByTileWithGap = search.searchDataByTileWithGap(107924, 49616, 17, 80);
			
			System.out.println("data:"+ResponseUtils.assembleRegularResult(searchDataByTileWithGap));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
