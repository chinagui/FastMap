/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.engine.meta.service.MetadataApiImpl;
import com.navinfo.dataservice.engine.meta.tmc.TmcLineTree;
import com.navinfo.dataservice.engine.meta.tmc.TmcSelector;

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
			
			JSONArray array = impl.queryTmcPoint(107829, 49685, 17, 80);
			
			List<SearchSnapshot> snapshotList = new ArrayList<>();
			
			for(int i = 0;i<array.size();i++)
			{
				JSONObject obj = array.getJSONObject(i);
				
				SearchSnapshot snapshot = new SearchSnapshot();
				
				snapshot.Unserialize(obj);
				
				snapshotList.add(snapshot);
			}
			System.out.println(snapshotList.toString());
			
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
			
			int[] tmcIds = {522001001};
			
			TmcLineTree result = selector.queryTmcTree(tmcIds);
			
			System.out.println(result.Serialize(ObjLevel.BRIEF));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
