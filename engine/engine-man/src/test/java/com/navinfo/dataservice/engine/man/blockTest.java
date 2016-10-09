package com.navinfo.dataservice.engine.man;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByUser;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.exception.ServiceException;

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
	
	@Test
	public void testListByUser() throws ServiceException
	{
		String parameter = "{\"exeUserId\":\"2\",\"stage\":1,\"type\":2,\"snapshot\":0,\"pageNum\":1,\"pageSize\":20}";
				
				JSONObject dataJson = JSONObject.fromObject(parameter);
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		
		int curPageNum= 1;//默认为第一页
		if(dataJson.containsKey("pageNum")){
			curPageNum = dataJson.getInt("pageNum");
			dataJson.remove("pageNum");
		}
		
		int pageSize = 20;//默认页容量为10
		if(dataJson.containsKey("pageSize")){
			pageSize = dataJson.getInt("pageSize");
			dataJson.remove("pageSize");
		}
		
		int snapshot = dataJson.getInt("snapshot");
		dataJson.remove("snapshot");

        Subtask bean = (Subtask)JSONObject.toBean(dataJson, Subtask.class);
        
        Page page = SubtaskService.getInstance().listByUserPage(bean,snapshot,1,pageSize,curPageNum);
        
		System.out.println(page.getResult().toString());
	}
}
