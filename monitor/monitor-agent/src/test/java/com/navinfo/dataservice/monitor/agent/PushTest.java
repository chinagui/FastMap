package com.navinfo.dataservice.monitor.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.monitor.agent.model.StatInfo;
import com.navinfo.dataservice.monitor.agent.utils.AgentUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;


public class PushTest {

	@Test
	public void test01() throws Exception{
		List<StatInfo>  datas = new ArrayList<StatInfo>();
		Date date = new Date();
		int time = (int) date.getTime();
		int j = 5;
		for(int i=0;i<30;i++){
			StatInfo statInfo = new StatInfo();
			statInfo.setEndpoint("192.168.4.110");
			statInfo.setMetric("fos.man.render");
			statInfo.setTimestemp(time);
			statInfo.setStep(5);
			statInfo.setValue(j);
			statInfo.setCounterType("GAUGE");
			statInfo.setTags("service=man,project=render,module=tips");
			datas.add(statInfo);
			if(i<5){
				j += 5;
			}else if(i>10&&i<15){
				j += 10;
			}else{
				j += 20;
			}
			Thread.sleep(5000);
		}
		String data = AgentUtils.pushData(datas);
		System.out.println(data);
		
	}
	
	@Test
	public void test02() throws Exception{
		String url ="http://192.168.4.188:8095/jamon/jamonadmin.jsp?ArraySQL=&RangeName=AllMonitors&TextSize=&action=Refresh&actionSbmt=Go!&displayTypeValue=BasicColumns&formatterValue=#,###&highlight=&instanceName=local&monProxyAction=NoAction&outputTypeValue=xml";
		String invokeByGet = ServiceInvokeUtil.invokeByGet(url);
		System.out.println(invokeByGet);
	}
	
	@Test
	public void test03() throws Exception{
		String str ="{\"type\":\"Point\",\"coordinates\":[116.47199,40.14608]}";
		JSONObject json = JSONObject.fromObject(str);
		Geometry geo = GeoTranslator.geojson2Jts(json);
		System.out.println(geo);
	}
	
	@Test
	public void test04() throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
				new String[] {"applicationContext-quartz.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void test05() throws Exception{
		boolean flag = AgentUtils.tomcatRunSuccess("192.168.4.188", "8084");
		System.out.println(flag);
	}
	
	/**
	 * 获取项目的根目录
	 * @author Han Shaoming
	 * @throws Exception
	 */
	@Test
	public void test06() throws Exception{
		String url1 = PushTest.class.getClassLoader().getResource(".").getPath();
		System.out.println(url1);
		String url2 = PushTest.class.getClassLoader().getResource("").getPath();
		System.out.println(url2);
		String url3 = PushTest.class.getResource("/").getPath();
		System.out.println(url3);
		String url4 = PushTest.class.getResource("").getPath();
		System.out.println(url4);
		
	}
	
	/**
	 * 百分比20.23%
	 * @author Han Shaoming
	 * @throws Exception
	 */
	@Test
	public void test08() throws Exception{
		double usedMemory = 23;
		double maxMemory = 88;
		double usedPercent = (double)Math.round((usedMemory/maxMemory)*10000)/100;
		System.out.println(usedMemory/maxMemory);
		System.out.println(Math.round((usedMemory/maxMemory)*10000));
		System.out.println(usedPercent);
	}
	
	/**
	 * 测试接口超时
	 * @author Han Shaoming
	 * @throws Exception
	 */
	@Test
	public void test07() throws Exception{
		try {
			String url = "http://192.168.4.188:8090";
			String result = ServiceInvokeUtil.invokeByGet(url, null, 2000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("连接超时");
		}
		
	}
}
