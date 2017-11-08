package com.navinfo.dataservice.engine.man;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.engine.man.city.CityService;

import net.sf.json.JSONObject;


public class cityTest {

	public static List TestQueryListByWkt() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"planningStatus\":0,\"wkt\":\"POLYGON((80.83422302246095 20.518481140136714,120.4135076904297 20.518481140136714,120.4135076904297 50.314989929199214,80.83422302246095 50.314989929199214,80.83422302246095 20.518481140136714))\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		CityService service = CityService.getInstance();
		return service.queryListByWkt(null);			
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println(cityTest.TestQueryListByWkt());
	}

}
