package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SubtaskTest extends InitApplication{


	@Test
	public void testUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="{\"taskId\":135,\"subtaskId\":594,\"name\":\"北京市北京市通州区郊区郊区_20170503_韩雪松\",\"status\":2,\"type\":0,\"referId\":0,\"stage\":0,\"descp\":\"不规则圈\",\"workKind\":1,\"planStartDate\":\"20170712\",\"planEndDate\":\"20170712\",\"exeUserId\":1664,\"hasQuality\":0}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		service.update(dataJson,0);			
	}
	
	@Test
	public void testPaintRefer() throws Exception {
		// TODO Auto-generated constructor stub
		/*
		 * if(condition.containsKey("lineWkt")){
				lineWkt=condition.getString("lineWkt");
			}else{
				id1=condition.getInt("id1");
				id2=condition.getInt("id2");
			}
		 */
		/*
		 * POLYGON ((116.3625 39.50833,116.3625 40.8125, 116.9875 40.8125,116.9875 39.50833,116.3625 39.50833))
		 * LINESTRING(116.6001 40.9001,116.5807 39.78,116.6022 39.5)
		 * 
		 * LINESTRING(116.3001 39.7,117.2 39.7)
		 * 
		 * LINESTRING(116.3001 39.7,116.7 39.7)
		 */
		//String parameter ="{\"id1\":143,\"lineWkt\":\"LINESTRING(116.53369903564453 39.90980207146212,116.52820587158203 39.90822196779971,116.53172492980957 39.90624678696849)\"}"; 
		String parameter ="{\"id1\":148,\"id2\":147}"; 
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		int taskId=78;
		service.paintRefer(taskId, dataJson);	
		System.out.print("end paintRefer");
	}
	
	@Test
	public void testCreate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="{\"taskId\":135,\"name\":\"北京市北京市通州区郊区郊区_20170503_韩雪松\",\"status\":2,\"type\":0,\"referId\":100,\"stage\":0,\"descp\":\"不规则圈\",\"workKind\":1,\"planStartDate\":\"20170712\",\"planEndDate\":\"20170712\",\"exeUserId\":1664,\"hasQuality\":0}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		service.create(0,dataJson);			
	}	
	
	@Test
	public void testPushMsg() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="[572]";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONArray subtaskIds = JSONArray.fromObject(parameter);		
		SubtaskService service = SubtaskService.getInstance();
		String mString=service.pushMsg(Long.valueOf(0), subtaskIds);	
		System.out.print(mString);
	}	
	
//	@Test
	public void testQuery() throws Exception {
		SubtaskService service = SubtaskService.getInstance();
		Map<String, Object> result = service.query(362,1);
		System.out.print(result);
	}
	
	@Test
	public void testListByUser() throws Exception {
		//AccessToken tokenObj=AccessTokenFactory.validate("00000457J3IIA2L1D2F0330FDCAA27180F845D3AAF67B5F3");
		JSONObject dataJson = JSONObject.fromObject("{\"platForm\":1,\"snapshot\":0,\"status\":1,\"pageSize\":1000,\"stage\":0}");
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
		
		//增加平台参数。0：采集端，1：编辑平台2管理平台
		int platForm = 0;//默认采集端
		if(dataJson.containsKey("platForm")){
			platForm = dataJson.getInt("platForm");
			dataJson.remove("platForm");
		}
		if(!dataJson.containsKey("exeUserId")||dataJson.getInt("exeUserId")==0){
			//dataJson.put("exeUserId", (int)tokenObj.getUserId());
			dataJson.put("exeUserId", 1674);
		}
        Page page = SubtaskService.getInstance().listByUserPage(dataJson,snapshot,platForm,pageSize,curPageNum);
        System.out.print(page.getResult());			
	}
		@Override
	@Before
	public void init() {
		initContext();
	}	
		
//	@Test
	public void testUnPlanQualitylist() throws Exception
	{
		try {
			System.out.println(SubtaskService.getInstance().unPlanQualitylist(208));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//删除质检圈(测试)
//	@Test
	public void testQualityDelete() throws Exception
	{
		System.out.println(SubtaskService.getInstance().qualityDelete(3));
	}
	
	//获取质检圈(测试)
//	@Test
	public void testQualityList() throws Exception
	{
		System.out.println(SubtaskService.getInstance().qualitylist(362));
	}
	
//		@Test
	public void testQueryListReferByWkt() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"blockId\":0,\"wkt\":\"POLYGON((80.83422 20.51848,120.41350 20.51848,120.41350 50.31498,80.83422 50.31498,80.83422 20.51848))\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		System.out.println(service.queryListReferByWkt(dataJson)); 
	}
		
	//创建质检圈(测试)
	@Test
	public void testQualityCreate() throws Exception {
		String parameter = "{\"subtaskId\":183,\"geometry\":\"POLYGON((116.33251190185545 39.89419695072923,116.36495590209962 39.88892859714545,116.35242462158202 39.86640182019855,116.32530212402342 39.86890516081786,116.33251190185545 39.89419695072923))\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		JSONObject dataJson = JSONObject.fromObject(parameter);
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		service.qualityCreate(dataJson);
	}
	
	//修改质检圈(测试)
//	@Test
	public void testQualityUpdate() throws Exception {
		String parameter = "{\"qualityId\":27,\"geometry\":\"POLYGON ((80.83422 20.51848, 120.4135 20.51848, 120.4135 50.31498, 80.83422 50.31498, 80.83422 20.51848))\"}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		JSONObject dataJson = JSONObject.fromObject(parameter);
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		service.qualityUpdate(dataJson);
	}
}
