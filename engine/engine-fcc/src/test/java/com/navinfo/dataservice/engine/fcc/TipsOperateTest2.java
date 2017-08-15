package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.fcc.tips.EdgeMatchTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.nirobot.common.utils.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: TipsExportTest.java
 * @author y  
 * @date 2016-11-1 上午10:37:59
 * @Description: 草图预处理tips 接口测试
 *  
 */
public class TipsOperateTest2 extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}

	//private Connection conn;

	public TipsOperateTest2() throws Exception {
	}

	@Test
	public void testCreate() {
		try {
			String  parameter=null;
			//0280029cd052e62f7c4ba4819c6ebc83b83f1a
			parameter="{\"g_location\":{\"type\":\"Point\",\"coordinates\":[116.48153,40.01378]} ,\"user\":10402,\"content\":[{\"geo\":{\"type\":\"LineString\",\"coordinates\":[[116.47896,40.01183],[116.47896,40.01184]],\"style\":\"105000000\"}},{\"geo\":{\"type\":\"LineString\",\"coordinates\":[[116.47884,40.01211],[116.47885,40.01209],[116.47887,40.01208],[116.47897,40.01202],[116.4791,40.01195],[116.47919,40.01191],[116.4793,40.01189],[116.47942,40.01188],[116.47952,40.01188],[116.47965,40.0119],[116.47984,40.01198],[116.47991,40.01202]],\"style\":\"105000000\"}}],\"memo\":\"\"}";
			
			parameter="{\"g_location\":{\"type\":\"Point\",\"coordinates\":[116.48153,40.01378]} ,\"user\":10402,\"content\":[{\"geo\":{\"type\":\"LineString\",\"coordinates\":[[116.47896,40.01183],[116.47896,40.01184]],\"style\":\"105000000\"}},{\"geo\":{\"type\":\"LineString\",\"coordinates\":[[116.47884,40.01211],[116.47885,40.01209],[116.47887,40.01208],[116.47897,40.01202],[116.4791,40.01195],[116.47919,40.01191],[116.4793,40.01189],[116.47942,40.01188],[116.47952,40.01188],[116.47965,40.0119],[116.47984,40.01198],[116.47991,40.01202]],\"style\":\"105000000\"}}]}";
			parameter = "{\"user\":1664,\"g_location\":{\"type\":\"Point\",\"coordinates\":[116.26807,40.20398]},\"content\":[{\"geo\":{\"type\":\"LineString\",\"coordinates\":[[116.26833,40.20398],[116.26853,40.20398],[116.26868,40.20397],[116.26891,40.20395],[116.26928,40.20387],[116.26953,40.20382],[116.26977,40.20374],[116.27005,40.20371],[116.27026,40.20366],[116.27039,40.20362],[116.27048,40.20359],[116.27056,40.20354],[116.27059,40.20354],[116.27063,40.20353],[116.27065,40.20351],[116.27067,40.20349],[116.27071,40.20344],[116.27076,40.20341],[116.27084,40.20335],[116.27089,40.20331],[116.27093,40.20328],[116.27097,40.20318],[116.27099,40.20315],[116.27104,40.20308],[116.27106,40.20303],[116.27114,40.20292],[116.27116,40.20287],[116.27116,40.20282],[116.27116,40.20276],[116.27119,40.20272],[116.27119,40.20264],[116.27121,40.20264],[116.27121,40.20258],[116.27121,40.20246],[116.27121,40.20235],[116.27121,40.20228],[116.27121,40.20217],[116.27121,40.20208],[116.27121,40.20199],[116.27119,40.20185],[116.27114,40.20181],[116.27108,40.20171],[116.27104,40.20161],[116.27076,40.20123],[116.27069,40.20113],[116.27061,40.20103],[116.27052,40.20095],[116.27041,40.20087],[116.27028,40.20081],[116.27001,40.20064],[116.26986,40.20059],[116.26979,40.20059],[116.26968,40.20058],[116.26962,40.20058],[116.26953,40.20058],[116.26949,40.20058],[116.26936,40.20058],[116.26934,40.20058],[116.26932,40.20058],[116.26932,40.20061],[116.26932,40.20064],[116.26932,40.20066],[116.27046,40.20277],[116.27059,40.20302],[116.27067,40.20321],[116.27076,40.20341],[116.27084,40.20359],[116.27093,40.20376],[116.27097,40.20385],[116.27099,40.2039],[116.27101,40.20397],[116.27101,40.20402],[116.27108,40.20408],[116.2711,40.2041],[116.27116,40.20417],[116.27121,40.2042],[116.27121,40.20423],[116.27123,40.20426]]},\"style\":\"105000000\"}],\"memo\":\"\",\"qSubTaskId\":720}";
			
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			/*String validataMsg=validatePars(jsonReq,"g_location","content","user","test");
			
			if(validataMsg!=null){
				
				throw new IllegalArgumentException("参数错误："+validataMsg+" not found");
			}*/

			JSONObject g_location = jsonReq.getJSONObject("g_location");
			
			JSONArray content=jsonReq.getJSONArray ("content");

			int user = jsonReq.getInt("user");

            int qSubTaskId = jsonReq.getInt("qSubTaskId");

			String memo=null;
			
			if(jsonReq.containsKey("memo")){
				
				memo=jsonReq.getString("memo");
			}
			
			if (content==null||content.isEmpty()) {
				
				throw new IllegalArgumentException("参数错误：content不能为空。");
			}
			
			
			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			String rowkey = op.create( g_location, content.toString(), user, memo, qSubTaskId);
			
			System.out.println("创建成功:" + rowkey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void modifyMemo() throws Exception {
		
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"rowkey\":\"028002d45aa3c1b894410bb741105daa050cd5\",\"memo\":\"示例备注信息\",\"user\":2,\"stage\":2}";
		
		if (StringUtils.isEmpty(parameter)) {
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String memo = jsonReq.getString("memo");

		String rowkey = jsonReq.getString("rowkey");
		
		int user = jsonReq.getInt("user");

		if (StringUtils.isEmpty(rowkey)) {
			throw new IllegalArgumentException("参数错误：rowkey不能为空。");
		}
		

		EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();
		
		
		int stage=2; //接边标识和fc预处理都默认为2
		
		op.updateFeedbackMemo(rowkey, user,memo);

		System.out.println("修改成功");
		
		
		
		
		
		
	}
	
	@Test
	public void createPreTips() throws Exception {
		
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		
		try{
            parameter="{\"geometry\":{\"coordinates\":[[116.49153,40.01378],[116.49297,40.01363]],\"type\":\"LineString\"},\"user\":2922,\"sourceType\":\"8001\", \"memo\" :\"testMemo\",\"deep\": {\"fc\":8,\"geo\":null},\"qSubTaskId\":57}";

            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            JSONObject tipsGeometry = jsonReq.getJSONObject("geometry");

            int user = jsonReq.getInt("user");

            String sourceType = jsonReq.getString("sourceType");

            String memo=jsonReq.getString("memo");

            JSONObject deep = jsonReq.getJSONObject("deep"); //tips详细信息


            if (StringUtils.isEmpty(sourceType)) {
                throw new IllegalArgumentException("参数错误：sourceType不能为空。");
            }

            if (tipsGeometry.isNullObject()||tipsGeometry==null) {
                throw new IllegalArgumentException("参数错误：geometry不能为空。");
            }

            int qSubTaskId = jsonReq.getInt("qSubTaskId");

            PretreatmentTipsOperator op = new PretreatmentTipsOperator();

            String rowkey = op.create(sourceType, tipsGeometry, user,deep, memo, qSubTaskId);
		

		System.out.println("创建预处理tips成功" + rowkey);
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	@Test
	public void testInfoSubmit() throws Exception {
		
		try{
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"user\":1664,\"taskId\":593}";
		
		//{"user":1672,"taskId":37}
		
		System.out.println(parameter);
		
		JSONObject jsonReq = JSONObject.fromObject(parameter);

		int user = jsonReq.getInt("user");
		
		int taskId= jsonReq.getInt("taskId");
		
		//int taskType= jsonReq.getInt("taskType");
		
		PretreatmentTipsOperator op = new PretreatmentTipsOperator();
		
		op.submitInfoJobTips2Web( user,taskId);

		System.out.println("预处理提交tips成功");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void modifyMemoAndDeep() throws Exception {
		
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"rowkey\":\"0280015f277b085e8047fd881a0f23fc5dfec6\",\"memo\":\"示例备注信息\",\"user\":10402,\"stage\":2,\"deep\":{\"fc\":4}}";
		
		try{
		if (StringUtils.isEmpty(parameter)) {
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		JSONObject jsonReq = JSONObject.fromObject(parameter);

		String memo = jsonReq.getString("memo");

		String rowkey = jsonReq.getString("rowkey");
		
		int user = jsonReq.getInt("user");
		
		JSONObject deep=null;
		if(jsonReq.containsKey("deep")){
			
			deep = jsonReq.getJSONObject("deep");
			
		}

		if (StringUtils.isEmpty(rowkey)) {
			throw new IllegalArgumentException("参数错误：rowkey不能为空。");
		}
		

		PretreatmentTipsOperator op = new PretreatmentTipsOperator();
		
		
		int stage=2; //接边标识和fc预处理都默认为2
		
		op.updateFeedbackMemoAndDeep(rowkey, user, memo, deep);

		System.out.println("修改成功");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	@Test
	public void testCutLine() throws Exception {
		
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"rowkey\":\"0280017b8ead071595417cb3305ac9d8e49d73\",\"user\":233,\"pointGeo\":{\"type\":\"Point\",\"coordinates\":[115.48297,40.01363]} }";
		parameter="{\"rowkey\":\"02800177931904b83b4dc1acf5bb66ca5fb0cd\",\"pointGeo\":{\"type\":\"Point\",\"coordinates\":[116.47382431497496,40.01010289187674]}}";
		
		if (StringUtils.isEmpty(parameter)) {
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		JSONObject jsonReq = JSONObject.fromObject(parameter);
		
		String rowkey=jsonReq.getString("rowkey");
		
		JSONObject pointGeo = jsonReq.getJSONObject("pointGeo"); //修改改坐标(点几何)
		
		int user = 123;

		if (StringUtils.isEmpty(rowkey)) {
			throw new IllegalArgumentException("参数错误：rowkey不能为空。");
		}
		
		if (pointGeo.isNullObject()||pointGeo==null) {
			throw new IllegalArgumentException("参数错误：pointGeo不能为空。");
		}
		
		PretreatmentTipsOperator op = new PretreatmentTipsOperator();

		op.breakLine(rowkey, pointGeo, user);

		System.out.println("预处理tips打断成功");
	}
	
	
	

	@Test
	public void testMeasureLineCut() throws Exception {
		
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"rowkey\":022001CF4FB458DB484AA798DC7804E2401595,\"user\":123,\"subtaskId\":1,\"jobType\":1,\"pointGeo\":{\"type\":\"Point\",\"coordinates\":[116.47977,40.01272]}}";
		
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");
			
			int user = jsonReq.getInt("user");
			
			JSONObject pointGeo=jsonReq.getJSONObject("pointGeo");
			
			int subTaskId=jsonReq.getInt("subtaskId"); //任务号
			
			int jobType=jsonReq.getInt("jobType"); //任务类型（中线或者是快线的任务号）

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}
			
			if (pointGeo==null||pointGeo.isEmpty()) {
				throw new IllegalArgumentException("参数错误：pointGeo不能为空。");
			}
			
			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			//op.cutMeasuringLineCut(rowkey,pointGeo,user,subTaskId,jobType);

			//return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			e.printStackTrace();

			//return new ModelAndView("jsonView", fail(e.getMessage()));
		}

		System.out.println("测线打断成功");
	}
	
	
	@Test
	public void testEditShape() throws Exception {
		
		//logger.info("/tip/editShape: "+parameter);
		
		String parameter="{\"geometry\":{\"coordinates\":[[115.48153,40.01378],[115.48297,40.01363],[116.49000,40.34567]],\"type\":\"LineString\"},\"user\":2922,\"rowkey\":\"028001713565273810448d8b80b163f336aad0\" }}";
		
		if (StringUtils.isEmpty(parameter)) {
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		JSONObject jsonReq = JSONObject.fromObject(parameter);
		
		String rowkey=jsonReq.getString("rowkey");
		
		JSONObject tipsGeometry = jsonReq.getJSONObject("geometry"); //修改改坐标
		
		//String memo=jsonReq.getString("memo"); //改备注
		
		int user = jsonReq.getInt("user");

		if (StringUtils.isEmpty(rowkey)) {
			throw new IllegalArgumentException("参数错误：rowkey不能为空。");
		}
		
		
		if (tipsGeometry.isNullObject()||tipsGeometry==null) {
			throw new IllegalArgumentException("参数错误：geometry不能为空。");
		}

		PretreatmentTipsOperator op = new PretreatmentTipsOperator();

		op.editGeo(rowkey, tipsGeometry, user);
		
	}
	
	
	
	
	@Test
	public void testSubmitPre() throws Exception {
		
	//	http://192.168.4.188:8000/service/fcc/tip/submitPre/?access_token=00000002IXTSI12VB09E09592F3300CCB65D2A3A59DCB7D0&parameter={"grids":[60560303],"user":123}
		
		String parameter="{\"grids\":[60560303],\"user\":11111}";
		
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

		/*	JSONArray grids = jsonReq.getJSONArray("grids");


			if (grids==null||grids.size()==0) {
                throw new IllegalArgumentException("参数错误:grids不能为空。");
            }*/

			int user = jsonReq.getInt("user");
			
			PretreatmentTipsOperator op = new PretreatmentTipsOperator();
			
			op.submit2Web(user, 1);
			
			System.out.println("提交成功");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	@Test
	public void testSaveOrUpdate() throws Exception {
		
		String parameter= "{\"jsonInfo\":{\"pid\":\"022001f743ba2d2f814271be7cfcb68b6f82c0\",\"rowkey\":\"022001f743ba2d2f814271be7cfcb68b6f82c0\",\"source\":{\"s_featureKind\":2,\"s_project\":\"\",\"s_sourceCode\":7,\"s_sourceId\":\"\",\"s_sourceType\":\"2001\",\"s_sourceProvider\":0,\"s_reliability\":0,\"s_qTaskId\":0,\"s_mTaskId\":2202,\"s_qSubTaskId\":0,\"s_mSubTaskId\":751},\"geometry\":{\"g_location\":{\"type\":\"LineString\",\"coordinates\":[[116.375,39.9181],[116.3766,39.91803]]},\"g_guide\":{\"type\":\"Point\",\"coordinates\":[116.3758,39.91806]}},\"track\":{\"t_lifecycle\":3,\"t_command\":0,\"t_date\":\"20170811125211\",\"t_tipStatus\":1,\"t_dEditStatus\":0,\"t_dEditMeth\":0,\"t_mEditStatus\":0,\"t_mEditMeth\":0,\"t_trackInfo\":[]},\"feedback\":{\"f_array\":[]},\"content\":\"\",\"options\":{},\"geoLiveType\":\"TIPLINKS\",\"code\":\"2001\",\"deep\":{\"id\":\"f743ba2d2f814271be7cfcb68b6f82c0\",\"geo\":{\"type\":\"Point\",\"coordinates\":[116.3758,39.91806]},\"src\":3,\"ln\":1,\"kind\":7,\"len\":137.58633189664096,\"shp\":0,\"prj\":\"\",\"sTime\":0,\"eTime\":0,\"cons\":1,\"time\":\"\",\"sGrip\":0,\"eGrip\":0},\"_originalJson\":{\"pid\":\"022001f743ba2d2f814271be7cfcb68b6f82c0\",\"rowkey\":\"022001f743ba2d2f814271be7cfcb68b6f82c0\",\"source\":{\"s_featureKind\":2,\"s_project\":\"\",\"s_sourceCode\":7,\"s_sourceId\":\"\",\"s_sourceType\":\"2001\",\"s_sourceProvider\":0,\"s_reliability\":0,\"s_qTaskId\":0,\"s_mTaskId\":2202,\"s_qSubTaskId\":0,\"s_mSubTaskId\":751},\"geometry\":{\"g_location\":{\"coordinates\":[[116.375,39.9181],[116.37625,39.91806]],\"type\":\"LineString\"},\"g_guide\":{\"coordinates\":[116.375,39.9181],\"type\":\"Point\"}},\"track\":{\"t_lifecycle\":3,\"t_command\":0,\"t_date\":\"20170811125211\",\"t_tipStatus\":1,\"t_dEditStatus\":0,\"t_dEditMeth\":0,\"t_mEditStatus\":0,\"t_mEditMeth\":0,\"t_trackInfo\":[]},\"feedback\":{\"f_array\":[]},\"content\":\"\",\"options\":{},\"geoLiveType\":\"TIPLINKS\",\"code\":\"2001\",\"deep\":{\"id\":\"f743ba2d2f814271be7cfcb68b6f82c0\",\"geo\":{\"coordinates\":[116.375,39.9181],\"type\":\"Point\"},\"src\":3,\"ln\":1,\"kind\":7,\"len\":106.8152,\"shp\":0,\"prj\":\"\",\"sTime\":0,\"eTime\":0,\"cons\":1,\"time\":\"\",\"sGrip\":0,\"eGrip\":0}},\"_initHooksCalled\":true},\"user\":1664,\"command\":1,\"dbId\":13}";
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			JSONObject jsonInfo=null; //jsonInfo为全量的tips信息，需要符合规格定义
			if(jsonReq.containsKey("jsonInfo")){
				
				jsonInfo = jsonReq.getJSONObject("jsonInfo");
				
			}
			if (jsonInfo==null||jsonInfo.isNullObject()||jsonInfo.keySet().size()==0) {
				throw new IllegalArgumentException("参数错误：jsonInfo不能为空。");
			}
			
			int command = jsonReq.getInt("command"); //command,0：save or:1：update
			
			if (command!=0&&command!=1) {
				throw new IllegalArgumentException("参数错误：command不在范围内【0,1】");
			}
			
			int user = jsonReq.getInt("user");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();
			
			String rowkey= op.saveOrUpdateTips(jsonInfo,command,user,0); //新增或者修改一个tips
			
			System.out.println("修改成功:"+rowkey);
		}catch (Exception e) {
			System.out.println("修改出错");
			e.printStackTrace();
		}
		
	}

    /**
     * 参数验证
     * @param jsonReq
     * @param para
     * @return
     */
	public String validatePars(JSONObject jsonReq, String... para) {
		String notExistsKey = null;
		for (int i = 0; i < para.length; i++) {
			 if(!jsonReq.containsKey(para[i])){
				 notExistsKey=para[i];
				 break;
			}
		}
		return notExistsKey;
	}
	
	@Test
	public void testGetById(){
		String id="02180628db3e8f3431469dbc59b483496b43ca";
		id="02180628db3e8f3431469dbc59b483496b43ca";//能显示的
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator  p=new TipsIndexOracleOperator(conn);
			TipsDao  dao= p.getById(id);
			System.out.println(	dao.getWktLocation());
			//LINESTRING~~~
			Geometry  geo= dao.getWktLocation();
			for (int i = 0; i <geo.getNumGeometries(); i++) {
				Geometry geoN=geo.getGeometryN(i);
				//if(geoN.)
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
		
		
		@Test
		public void testTipsIndexupdate(){
			String id="02180628db3e8f3431469dbc59b483496b43ca";
			id="02180628db3e8f3431469dbc59b483496b43ca";//能显示的
			Connection conn=null;
			try{
				conn = DBConnector.getInstance().getTipsIdxConnection();
				TipsIndexOracleOperator  p=new TipsIndexOracleOperator(conn);
				TipsDao  dao= p.getById(id);
				String wktLocation="";
				System.out.println(dao.getWktLocation());
			}catch (Exception e) {
				// TODO: handle exception
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
			}
			
		}
		
	
	
	
	
	


}
