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
		parameter="{\"rowkey\":\"0280017b8ead071595417cb3305ac9d8e49d73\",\"memo\":\"示例备注信息\",\"user\":10402,\"stage\":2}";
		
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
		parameter="{\"geometry\":{\"coordinates\":[[116.48153,40.01378],[116.48297,40.01363]],\"type\":\"LineString\"},\"user\":123,\"sourceType\":\"8001\", \"memo\" :\"testMemo\",\"deep\": {\"fc\":8,\"geo\":null} }}";
		

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
		
		if (StringUtils.isEmpty(parameter)) {
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		JSONObject jsonReq = JSONObject.fromObject(parameter);
		
		String rowkey=jsonReq.getString("rowkey");
		
		JSONObject pointGeo = jsonReq.getJSONObject("pointGeo"); //修改改坐标(点几何)
		
		int user = jsonReq.getInt("user");

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
	
	


}
