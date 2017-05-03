package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.fcc.tips.EdgeMatchTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;

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

			String memo=null;
			
			if(jsonReq.containsKey("memo")){
				
				memo=jsonReq.getString("memo");
			}
			
			if (content==null||content.isEmpty()) {
				
				throw new IllegalArgumentException("参数错误：content不能为空。");
			}
			
			
			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			op.create( g_location, content.toString(), user,memo);
			
			System.out.println("创建成功");
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
		parameter="{\"geometry\":{\"coordinates\":[[116.48153,40.01378],[116.48297,40.01363]],\"type\":\"LineString\"},\"user\":2922,\"sourceType\":\"8001\", \"memo\" :\"testMemo\",\"deep\": {\"fc\":8,\"geo\":null} }}";

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
		
		PretreatmentTipsOperator op = new PretreatmentTipsOperator();

		op.create(sourceType, tipsGeometry, user,deep,memo);
		

		System.out.println("创建预处理tips成功");
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	@Test
	public void testInfoSubmit() throws Exception {
		
		try{
		String  parameter=null;
		//0280017b8ead071595417cb3305ac9d8e49d73
		parameter="{\"user\":1664,\"taskId\":57}";
		
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

			op.cutMeasuringLineCut(rowkey,pointGeo,user,subTaskId,jobType);

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
			
			op.submit2Web(user);
			
			System.out.println("提交成功");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	@Test
	public void testSaveOrUpdate() throws Exception {
		
		String parameter="{\"jsonInfo\":{\"rowkey\":\"021601370d27cad51b49328f13fa1d8c4169a8\",\"deep\":{\"name\":\"测试测试\",\"f_array\":[{\"id\":\"1359079\",\"type\":1},{\"id\":\"1359083\",\"type\":1},{\"id\":\"1359084\",\"type\":1}],\"geo\":{\"coordinates\":[116.37257,40.11223],\"type\":\"Point\"}},\"geometry\":{\"g_location\":{\"type\":\"Polygon\",\"coordinates\":[[[116.37251,40.112022],[116.372505,40.112022],[116.37242,40.112064],[116.37242,40.112064],[116.372375,40.112095],[116.372375,40.1121],[116.37236,40.112118],[116.37235,40.11212],[116.37232,40.112194],[116.37232,40.1122],[116.37232,40.11227],[116.37232,40.112278],[116.372345,40.11232],[116.372345,40.11232],[116.37237,40.11235],[116.37237,40.112354],[116.3724,40.11238],[116.3724,40.112385],[116.37248,40.112427],[116.37248,40.112427],[116.37253,40.11244],[116.372536,40.11244],[116.37262,40.112427],[116.37263,40.112427],[116.37267,40.11242],[116.37268,40.112415],[116.37275,40.112377],[116.37276,40.11237],[116.372795,40.11231],[116.372795,40.11231],[116.37282,40.112267],[116.37282,40.11226],[116.37281,40.112186],[116.3728,40.112183],[116.372765,40.11211],[116.372765,40.112106],[116.372734,40.112076],[116.37271,40.112057],[116.3727,40.112053],[116.37264,40.112034],[116.37264,40.112034],[116.372604,40.112022],[116.3726,40.112022],[116.37251,40.112022]]]},\"g_guide\":{\"type\":\"Point\",\"coordinates\":[116.37257,40.11223]}},\"feedback\":{\"f_array\":[]},\"source\":{\"s_featureKind\":2,\"s_project\":\"\",\"s_sourceCode\":2,\"s_sourceId\":\"\",\"s_sourceType\":\"1601\",\"s_reliability\":100,\"s_sourceProvider\":0,\"s_qTaskId\":0,\"s_mTaskId\":0},\"track\":{\"t_lifecycle\":2,\"t_command\":0,\"t_date\":\"20170223145230\",\"t_cStatus\":1,\"t_dStatus\":0,\"t_mStatus\":0,\"t_inMeth\":0,\"t_pStatus\":0,\"t_dInProc\":0,\"t_mInProc\":0,\"t_fStatus\":0,\"t_trackInfo\":[{\"stage\":1,\"date\":\"20170222180542\",\"handler\":2922}]}},\"user\":2922,\"command\":1}";
		
		
		parameter="{\"jsonInfo\":{\"rowkey\":null,\"pid\":null,\"source\":{\"s_featureKind\":2,\"s_project\":\"\",\"s_sourceCode\":7,\"s_sourceId\":\"\",\"s_sourceType\":\"1507\",\"s_sourceProvider\":0,\"s_reliability\":0,\"s_qTaskId\":0,\"s_mTaskId\":77,\"s_qSubTaskId\":0,\"s_mSubTaskId\":57},\"geometry\":{\"g_location\":{\"type\":\"MultiLineString\",\"coordinates\":[[[116.34488,39.85711],[116.34487,39.85745],[116.34485,39.85778]],[[116.34484325138497,39.85782268913135],[116.34484738111495,39.85778147738756]],[[116.34488,39.85711],[116.34487,39.85745],[116.34485,39.85778]],[[116.34487956762312,39.85711025168359],[116.34484325138497,39.85782268913135]]]},\"g_guide\":{\"type\":\"MultiLineString\",\"coordinates\":[[[116.34488,39.85711],[116.34487,39.85745],[116.34485,39.85778]],[[116.34484325138497,39.85782268913135],[116.34484738111495,39.85778147738756]],[[116.34488,39.85711],[116.34487,39.85745],[116.34485,39.85778]],[[116.34487956762312,39.85711025168359],[116.34484325138497,39.85782268913135]]]}},\"track\":{\"t_lifecycle\":3,\"t_command\":0,\"t_date\":\"\",\"t_cStatus\":0,\"t_fStatus\":0,\"t_pStatus\":0,\"t_dStatus\":0,\"t_mStatus\":0,\"t_dInProc\":0,\"t_mInProc\":0,\"t_inMeth\":0,\"t_trackInfo\":[{\"date\":\"\",\"handler\":1664,\"stage\":6}]},\"feedback\":{\"f_array\":[]},\"content\":\"\",\"options\":{},\"geoLiveType\":\"TIPPEDESTRIANSTREET\",\"code\":\"1507\",\"deep\":{\"name\":\"\",\"gSLoc\":{\"type\":\"Point\",\"coordinates\":[116.34484325138497,39.85782268913135]},\"gELoc\":{\"type\":\"Point\",\"coordinates\":[116.34487956762312,39.85702789224931]},\"f_array\":[{\"id\":17631897,\"type\":1,\"flag\":\"1\"},{\"id\":17610108,\"type\":1,\"flag\":\"2\"},{\"id\":17631896,\"type\":1,\"flag\":\"0\"}],\"time\":\"\"},\"_originalJson\":{\"deep\":{\"name\":\"\",\"gSLoc\":{\"type\":\"Point\",\"coordinates\":[116.34484325138497,39.85782268913135]},\"gELoc\":{\"type\":\"Point\",\"coordinates\":[116.34487956762312,39.85702789224931]},\"f_array\":[{\"id\":17631897,\"type\":1,\"flag\":\"1\"},{\"id\":17610108,\"type\":1,\"flag\":\"2\"},{\"id\":17631896,\"type\":1,\"flag\":\"0\"}],\"time\":\"\"}},\"_initHooksCalled\":true},\"user\":1664,\"command\":0}";
		//parameter="{\"jsonInfo\":{\"rowkey\":null,\"pid\":null,\"source\":{\"s_featureKind\":2,\"s_project\":\"\",\"s_sourceCode\":7,\"s_sourceId\":\"\",\"s_sourceType\":\"1107\",\"s_sourceProvider\":0,\"s_reliability\":null,\"s_qTaskId\":0,\"s_mTaskId\":0},\"geometry\":{\"g_location\":{\"type\":\"Point\",\"coordinates\":[116.45158052444458,39.98805384322511]},\"g_guide\":{\"type\":\"Point\",\"coordinates\":[116.45092887795363,39.98703773419431]}},\"track\":{\"t_lifecycle\":3,\"t_command\":0,\"t_date\":\"\",\"t_cStatus\":0,\"t_fStatus\":0,\"t_pStatus\":0,\"t_dStatus\":0,\"t_mStatus\":0,\"t_dInProc\":0,\"t_mInProc\":0,\"t_inMeth\":0,\"t_trackInfo\":[{\"date\":\"\",\"handel\":2,\"stage\":6}]},\"feedback\":{\"f_array\":[]},\"options\":{},\"geoLiveType\":\"TIPTOLLGATE\",\"code\":\"1107\",\"deep\":{\"id\":\"\",\"in\":{\"id\":19361679,\"type\":1},\"out\":{},\"nId\":0,\"agl\":0,\"tp\":0,\"pNum\":0,\"etc\":[],\"loc\":0,\"name\":\"\"},\"_originalJson\":{\"deep\":{\"id\":\"\",\"in\":{\"id\":19361679,\"type\":1},\"out\":{},\"nId\":0,\"agl\":0,\"tp\":0,\"pNum\":0,\"etc\":[],\"loc\":0,\"name\":\"\"}},\"_initHooksCalled\":true},\"user\":2,\"command\":0}";
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
			
			String rowkey= op.saveOrUpdateTips(jsonInfo,command,user); //新增或者修改一个tips

			//return new ModelAndView("jsonView", success());
			
			System.out.println("修改成功:"+rowkey);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 参数的验证
	 * 
	 * @param response
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
	
	
	public static void main(String[] args) {
		
		
	}
	


}
