/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.service.MetadataApiImpl;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLine;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLineTree;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcPoint;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcLineSelector;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcPointSelector;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: TmcPointTest
 * @author Zhang Xiaolong
 * @date 2016年11月11日 下午8:22:07
 * @Description: TODO
 */
public class TmcPointTest {

	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testQueryTmcPoint() throws Exception {

		try {
			
			MetadataApiImpl impl = new  MetadataApiImpl();
			
			JSONArray array = impl.queryTmcPoint(107918, 49616, 17, 40);
			
			List<SearchSnapshot> snapshotList = new ArrayList<>();
			
			for(int i = 0;i<array.size();i++)
			{
				JSONObject obj = array.getJSONObject(i);
				
				SearchSnapshot snapshot = new SearchSnapshot();
				
				snapshot.Unserialize(obj);
				
				snapshotList.add(snapshot);
			}
			System.out.println(array);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testQueryTmcLine() throws Exception {

		try {
			
			MetadataApiImpl impl = new  MetadataApiImpl();
			
			JSONArray array = impl.queryTmcLine(107829, 49685, 17, 80);
			
			List<SearchSnapshot> snapshotList = new ArrayList<>();
			
			for(int i = 0;i<array.size();i++)
			{
				JSONObject obj = array.getJSONObject(i);
				
				SearchSnapshot snapshot = new SearchSnapshot();
				
				snapshot.Unserialize(obj);
				
				snapshotList.add(snapshot);
			}
			System.out.println(array.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testQueryTmcTree() throws Exception {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();
			TmcSelector selector = new TmcSelector(conn);
			
			JSONArray array = new JSONArray();
			
			array.add(522000109);
			
			array.add(522000144);
			
			array.add(522000302);
			
			array.add(522000792);
			
			array.add(522002687);
			
			array.add(522004085);
			
			TmcLineTree result = selector.queryTmcTree(array);
			
			System.out.println(result.Serialize(ObjLevel.BRIEF));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testQueryTmcPointById() throws Exception {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();
			TmcPointSelector selector = new TmcPointSelector(conn);
			
			TmcPoint point = selector.loadByTmcPointId(522002094);
			
			System.out.println(point.Serialize(ObjLevel.BRIEF));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DbUtils.close(conn);
		}

	}
	
	@Test
	public void testQueryTmcLineById() throws Exception {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();
			TmcLineSelector selector = new TmcLineSelector(conn);
			
			TmcLine line = selector.loadByTmcLineId(522002093);
			
			System.out.println(line.Serialize(ObjLevel.BRIEF));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DbUtils.close(conn);
		}

	}
}
