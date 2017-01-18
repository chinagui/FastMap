/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameSelector;

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
		String parameter = "{\"subtaskId\":76,\"pageNum\":1,\"pageSize\":20,\"sortby\":\"\",\"params\":{\"name\":\"\",\"adminId\":\"\"}}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			
			JSONObject data = selector.searchForWeb(jsonReq,tips);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
