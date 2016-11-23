package com.navinfo.dataservice.dao;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.navicommons.database.Page;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: RdNameImportTest.java
 * @author y
 * @date 2016-7-2下午3:07:28
 * @Description: TODO
 *  
 */
public class RdNameResultsTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-rdname-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void checkResultList(){
		NiValExceptionSelector a = new NiValExceptionSelector();
			JSONObject jsonReq = JSONObject.fromObject("{'pageSize':20,'pageNum':0,'subtaskId':76,'dbId':17}");
			
			//JSONObject data = jsonReq.getJSONObject("data");
			JSONObject jso = JSONObject.fromObject("{'tips':[{'id':'021901d7e8ed4c7c604242a1392291a530fbb2'},{'id':'021901404F5A9DE3AB4ECCACE7B512207BC00B'},{'id':'02190151EEF41E16D34C5C8976B5DD6292DEAC'}]}");
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
//			int dbId = subtask.getDbId();
			
			//FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = jso.getJSONArray("tips");
			// apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			System.out.println(tips.toString());
			Page page = null;
			try {
				 page =a.listCheckResults(jsonReq, tips);
				 System.out.println(page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	

}
