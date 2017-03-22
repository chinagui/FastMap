package com.navinfo.dataservice.engine.man;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.exception.ServiceException;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：engine-manGridSelectorTest.java
 */
public class GridServiceTest {

	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
               // new String[] { "dubbo-consumer-datahub-test.xml"}); 
				new String[] { "dubbo-consumer.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	//@Test
	public void testQueryRegionGridMapping() throws Exception {
		GridService selector = GridService.getInstance();
		List<Integer> gridList = new ArrayList<Integer>();
		gridList.add(39550711);
		gridList.add(3502401);
		MultiValueMap regionGridMapping = (MultiValueMap) (selector.queryRegionGridMapping(gridList));
		Assert.assertNotNull(regionGridMapping);
		Assert.assertTrue(regionGridMapping.containsValue(39550711));
	}
	
	@Test
	public void testSubtask(){
		SubtaskService subtaskService = SubtaskService.getInstance();
		Subtask st = new Subtask();
		try {
			st = subtaskService.queryBySubtaskIdS(537);
			System.out.println(st.getGeometry());
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void queryTaskIdsByGrid() throws Exception{
		try {
			Map<String, Integer> st = GridService.getInstance().queryTaskIdsByGrid("59566421");
			System.out.println(st);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

@Test
	public void testQueryGridOfCity() throws Exception{
	JSONObject conditon= new JSONObject().element("cityId", "17");
	List<Integer> grids = GridService.getInstance().queryListByCondition(conditon);
	assertNotNull(grids);
	
}

}

