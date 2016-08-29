/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import com.navinfo.dataservice.engine.meta.rdname.ScRoadnameTypename;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: RdNameSelectorTest 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午5:49:43 
* @Description: TODO
*/
public class RdNameSelectorTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void testGetRdName()
	{
		String parameter = "{\"pageNum\":1,\"pageSize\":10,\"sortby\":\"\",\"name\":\"快速路\"}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int pageSize = jsonReq.getInt("pageSize");
			int pageNum = jsonReq.getInt("pageNum");
			
			String name = "";
			if (jsonReq.containsKey("name")) {
				name = jsonReq.getString("name");
			}
			
			String sortby = "";
			if (jsonReq.containsKey("sortby")) {
				sortby = jsonReq.getString("sortby");
			}
			
			ScRoadnameTypename typename = new ScRoadnameTypename();
			
			JSONObject data = typename.getNameType(pageNum,pageSize,name,sortby);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
