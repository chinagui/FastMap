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


//	@Test
	public void testUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="{\"name\":\"江苏省宿迁市城区_20170612_多源组test1\",\"subtaskId\":558,\"descp\":null,\"planStartDate\":\"20170612\",\"planEndDate\":\"20170612\",\"gridIds\":[50586122,50586123,50586120,50586121,50587200,50586112,50586113,50587210,50585123,50585122,50587113,50585121,50586131,50585131,50586130,50586133,50585133,50586132,50585132,50587102,50587103,50585111,50585112,50585113,50585230,50585231,50584133,50584132,50585101,50585220,50585221,50585103,50584131,50585222,50585102,50584122,50584123,50586102,50585202,50586103,50585203,50585200,50586101,50585201,50585212,50585213,50585210,50586111,50585211,50584230,50584231,50586201,50586200,50584233,50586220,50584330,50584210,50585301,50585300,50584221,50584220,50586210,50586211,50586230],\"workKind\":4,\"hasQuality\":1,\"exeGroupId\":7,\"qualityPlanStartDate\":\"20170612\",\"qualityPlanEndDate\":\"20170612\"}"; 
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
		String parameter ="{\"id1\":99,\"lineWkt\":\"LINESTRING(116.62673950195314 39.824094732204195,116.60785675048828 39.687110247162934)\"}"; 
		//String parameter ="{\"id1\":88,\"id2\":89}"; 
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		int taskId=135;
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
			dataJson.put("exeUserId", 1664);
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
