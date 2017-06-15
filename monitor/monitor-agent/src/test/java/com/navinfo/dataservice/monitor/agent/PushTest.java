package com.navinfo.dataservice.monitor.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
}
