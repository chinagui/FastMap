package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.engine.man.statics.StaticsOperation;
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
		//String parameter ="{\"id1\":601,\"lineWkt\":\"LINESTRING(116.39915 39.89992,116.39825 39.89959,116.39727 39.89921,116.39714 39.89915,116.39697 39.89916)\"}"; 
		//String parameter ="{\"id1\":859,\"lineWkt\":\"LINESTRING(85.67859999999999 47.41666666666665,85.87909999999998 47.41666666666665,85.8942 47.42971,85.88425 47.45061,85.87499999999999 47.455490000000005,85.85506 47.45363,85.85163 47.44736,85.84991 47.43343,85.84682 47.42321,85.83344 47.42135,85.67791000000001 47.41666666666665)\"}"; 
		String parameter ="{\"lineWkt\":\"LINESTRING(121.41094 28.51425,121.40468 28.49502,121.40768 28.49019,121.41304 28.47797,121.41399 28.47243,121.41498 28.4656,121.41433 28.46062,121.41077 28.45741,121.40717 28.4513,121.42124 28.43451,121.40502000000004 28.416666666666664,121.40176 28.41035,121.41197 28.42009,121.4257 28.42062,121.43995 28.39888,121.43197 28.38053,121.42957 28.37751,121.42845 28.3723,121.39592 28.35326,121.387 28.34684,121.37500000000003 28.34420000000001,121.34022 28.33989,121.31104 28.32735,121.3143 28.34835,121.3143 28.35576,121.29747 28.35953,121.29627 28.3692,121.28941 28.37237,121.26743 28.36769,121.25988 28.36708,121.23756 28.39502)\"}"; 
		//String parameter ="{\"id1\":842,\"id2\":837}"; 
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		int taskId=14362;
		service.paintRefer(taskId, dataJson);	
		System.out.print("end paintRefer");
	}
	
	@Test
	public void testCreate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="{\"taskId\":97,\"status\":2,\"type\":7,\"stage\":2,\"descp\":\"\",\"planStartDate\":\"20170705\",\"planEndDate\":\"20170821\",\"exeGroupId\":\"2\",\"hasQuality\":0}";
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
	public void testClose() throws Exception {
		SubtaskService service = SubtaskService.getInstance();
		service.close(717, 0);			
	}	
	
	@Test
	public void testPushMsg() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter ="[1254]";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONArray subtaskIds = JSONArray.fromObject(parameter);		
		SubtaskService service = SubtaskService.getInstance();
		String mString=service.pushMsg(Long.valueOf(0), subtaskIds);	
		System.out.print(mString);
	}	
	
	@Test
	public void testQuery() throws Exception {
		SubtaskService service = SubtaskService.getInstance();
		Map<String, Object> result = service.query(172,1);
		System.out.print(result);
	}
	@Test
	public void testList() throws Exception {
		String parameter="{\"condition\":{\"taskId\":78},\"pageNum\":1,\"pageSize\":15,\"snapshot\":1}";
		JSONObject dataJson = JSONObject.fromObject(parameter);
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		
		int curPageNum= 1;//默认为第一页
		if(dataJson.containsKey("pageNum")){
			curPageNum = dataJson.getInt("pageNum");
		}
		
		int pageSize = 30;//默认页容量为20
		if(dataJson.containsKey("pageSize")){
			pageSize = dataJson.getInt("pageSize");
		}
		//查询条件
		JSONObject condition = dataJson.getJSONObject("condition");
		SubtaskService service = SubtaskService.getInstance();
		Page result = service.list(condition, pageSize, curPageNum);
		System.out.print(result);
	}
	
	@Test
	public void testListByUser() throws Exception {
		AccessToken tokenObj=AccessTokenFactory.validate("000001XZJ5J8V8XP9D54FECB54D3EAD70774ADACE263BF8B");
		JSONObject dataJson = JSONObject.fromObject("{\"platForm\":1,\"snapshot\":1,\"status\":1,\"pageSize\":1000}");
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
			dataJson.put("exeUserId", (int)tokenObj.getUserId());
			//dataJson.put("exeUserId", 1664);
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
		String parameter = "{\"subtaskId\":184,\"geometry\":\"POLYGON((116.29989624023439 39.891826241725596,116.29714965820311 39.86600654754002,116.35139465332031 39.87338459498892,116.29989624023439 39.891826241725596))\"}";
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
	@Test
	public void testSubTaskStatics() throws Exception{
		String timestamp = "20170803";
		List<Map<String, Object>> personList = StaticsOperation.staticsPersionJob(timestamp);
		System.out.println(personList);
	}
}
