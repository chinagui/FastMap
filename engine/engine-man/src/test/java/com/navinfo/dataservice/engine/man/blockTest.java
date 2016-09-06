package com.navinfo.dataservice.engine.man;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.city.CityService;

import net.sf.json.JSONObject;


public class blockTest extends InitApplication{

	public static List<?> TestQueryListByWkt() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"snapshot\":0,\"planningStatus\":0,\"wkt\":\"POLYGON((80.83422302246095 20.518481140136714,120.4135076904297 20.518481140136714,120.4135076904297 50.314989929199214,80.83422302246095 50.314989929199214,80.83422302246095 20.518481140136714))\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		BlockService service = BlockService.getInstance();
		return service.listByWkt(dataJson);	
	}
	
	@Test
	public void  testQueryBlockDetail() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"blockId\":\"130\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		BlockService service = BlockService.getInstance();
		System.out.println(service.query(dataJson).toString());	
	}

	@Override
	@Before
	public void init() {
		initContext();
	}

}
