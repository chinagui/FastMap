package com.navinfo.dataservice.engine.fcc;

import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckOperator;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckSelector;
import com.navinfo.dataservice.engine.fcc.check.TipsExtract;
import com.navinfo.dataservice.engine.fcc.service.FccApiImpl;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;

/**
 * @ClassName: TipsCheckTest.java
 * @author y
 * @date 2017-5-31 上午11:23:38
 * @Description: TODO
 * 
 */
public class TipsCheckTest extends InitApplication {

	Logger logger = Logger.getLogger(TipsCheckTest.class);

	@Override
	@Before
	public void init() {
		initContext();
	}

	/**
	 * @Description:抽检测试
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testExtact() {

		JSONArray gridsList = JSONArray
				.fromObject("[59564432,59564433,59564410,59565402,59565401,59565400,59565313,59564423,59564332,59564422,59564333,59564421,59565410,59565411,59564420,59565420,59565421,59565303,59564323,59564431,59564430]");

		JSONObject param = new JSONObject();
		param.put("subTaskId", 283);
		param.put("checkerId", 456);
		param.put("checkerName", "质检员1");
		param.put("grids", gridsList);

		//String parameter = param.toString();
		//238
		String parameter="{\"subTaskId\":238,\"checkerId\":2699,\"checkerName\":\"冯佳\",\"grids\":[60560233]}";

		try {

			System.out.println("parameter:" + parameter);

			  if (StringUtils.isEmpty(parameter)) {
	                throw new IllegalArgumentException("parameter参数不能为空。");
	            }
				JSONObject jsonReq = JSONObject.fromObject(parameter);
				//输入：质检任务号、作业子任务号、作业任务范围（grids）、作业员id
				int checkTaskId=jsonReq.getInt("subTaskId"); //质检任务号
				
				int checkerId=jsonReq.getInt("checkerId");//质检编号
				
				String checkerName=jsonReq.getString("checkerName");//作业员姓名

				JSONArray grids = jsonReq.getJSONArray("grids"); //作业子任务范围
				
				if (grids==null||grids.size()==0) {
	                throw new IllegalArgumentException("参数错误:grids不能为空。");
	            }
				
				if (StringUtils.isEmpty(checkerName)) {
	                throw new IllegalArgumentException("参数错误:checkerName不能为空。");
	            }
				
				TipsExtract extract = new TipsExtract();
				
				////返回抽取后，抽取总量和tips类型数
				JSONObject result=extract.doExtract(checkTaskId,checkerId,checkerName,grids);
				
				System.out.println("result:"+result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/**
	 * @Description:关闭质检任务
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testCLoseTask() {

		JSONObject obj = new JSONObject();

		obj.put("taskId", 26);

		try {
			String parameter = obj.toString();

			System.out.println(parameter);

			logger.debug("/tip/check/close:");

			logger.debug("parameter:" + parameter);

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int checkTaskId = jsonReq.getInt("taskId"); // 质检任务号
			TipsCheckOperator operate = new TipsCheckOperator();

			operate.closeTask(checkTaskId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * @Description:统计
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testGetStats() {

	}

	/**
	 * @Description:列表
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testGetSnapot() {
		
		JSONObject obj=new JSONObject();
		obj.put("grids", JSONArray.fromObject("[59566311,59566322,59566321,59566332,59566333,59566331,59566312]"));
		obj.put("type", "1301");
		obj.put("dbId", 13);
		obj.put("subtaskId", 283);
		obj.put("workStatus", 2);
		
		String parameter = obj.toString();

		try {
		    
		    if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
		    
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray grids = jsonReq.getJSONArray("grids");
			if (grids==null||grids.size()==0) {
                throw new IllegalArgumentException("参数错误:grids不能为空。");
            }

			String type = jsonReq.getString("type");
            if (StringUtils.isEmpty(type)) {
                throw new IllegalArgumentException("参数错误:type不能为空。");
            }

			int dbId = jsonReq.getInt("dbId");
            if (dbId == 0) {
                throw new IllegalArgumentException("参数错误:dbId不能为空。");
            }
			
			int subtaskId = jsonReq.getInt("subtaskId");
            if (subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

			TipsSelector selector = new TipsSelector();
			JSONArray array = selector.getSnapshot(parameter);
			
			System.out.println("result:"+ResponseUtils.assembleRegularResult(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			e.printStackTrace();
		}

	}

	
	
	/**
	 * @Description:新增质检问题记录
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testSaveWrong() {

		JSONObject obj = new JSONObject();
		
		obj.put("checkTaskId", 283);
		obj.put("tipsCode", "8002");
		obj.put("tipsRowkey", "0280020f4d849b6c614766afb99d0dd744bf6c");
		obj.put("quDesc", "错误描述1201");
		obj.put("reason", "错误原因1201 ");
		obj.put("erContent", "错误内容1201");
		obj.put("quRank", "A"); //错误等级
		obj.put("isPrefer", "1"); //是否倾向性
		obj.put("checker", "质检员001"); //是否倾向性
		
		JSONObject pa = new JSONObject();
		
		pa.put("data", obj);
		
		pa=JSONObject.fromObject("{\"data\":{\"checkTaskId\":284,\"quDesc\":\"vbv bnghj\",\"reason\":\"录入错误\",\"erContent\":\"道路种别\",\"quRank\":\"C\",\"isPrefer\":0,\"checker\":\"冯佳2699\",\"tipsCode\":\"1201\",\"tipsRowkey\":\"021201fdff891e87234be387a2ffb39210f352\"}}");
		

		String parameter = pa.toString();

		logger.debug("/tip/check/saveWrong:");

		logger.debug("parameter:" + parameter);

		try {

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONObject jsonWrong = jsonReq.getJSONObject("data");

			if (jsonWrong == null) {

				throw new IllegalArgumentException("参数错误:data不能为空。");
			}

			CheckWrong wrongResult = new TipsCheckOperator()
					.saveCheckWrong(jsonWrong);

			logger.debug("result:" + JSONObject.fromObject(wrongResult));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @Description:修改质检问题记录
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testUpdateStatus() {

		
		JSONObject pa = new JSONObject();
		pa.put("checkStatus", 1);
		pa.put("rowkey", "11170110935773");

		String parameter = pa.toString();

		try {
			logger.debug("/tip/check/updateStatus:");
			
			System.out.println("parameter:"+parameter);
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int  workStatus=jsonReq.getInt("checkStatus");
			
			String rowkey = jsonReq.getString("rowkey");

			TipsCheckOperator op = new TipsCheckOperator();
			
			op.updateTipsCheckStatus(rowkey,workStatus);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Description:删除
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testDelWrong() {
		
		JSONObject pa = new JSONObject();
		
		pa.put("logId", "41f602a115e94edcabd5443f578e6c11");

		String parameter = pa.toString();
		
		try {
			logger.debug("/tip/check/updateWrong:");
			
			logger.debug("parameter:"+parameter);
			
		    if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			String logId = jsonReq.getString("logId");//主键
			
			if (StringUtils.isEmpty(logId)) {
				throw new IllegalArgumentException("参数错误：logId不能为空");
			}
			
			new TipsCheckOperator().deleteWrong(logId);
			

		} catch (Exception e) {
			
			e.printStackTrace();

			logger.error(e.getMessage(), e);

		}

	}
	
	@Test
	public void getCollectIdsBySubTaskId(){
        ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
        try {
			Set<Integer> taskSet = manApi.getCollectTaskIdByDaySubtask(183);
			for (Integer collectTaskId : taskSet) {
				System.out.println(collectTaskId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		
		
	}
	
	
	/**
	 * @Description:查询
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testGetWrongByRowkey() {
		
		JSONObject pa = new JSONObject();
		
		pa.put("rowkey", "0220019609FB3AFDD047EE9FE53BEF56496AAE");
		
		pa.put("subTaskId", "188");

		String parameter = pa.toString();
		
		try {
			logger.debug("/tip/check/queryWrong:");
			
			logger.debug("parameter:"+parameter);
			
			if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int checkTaskId=jsonReq.getInt("subTaskId"); //质检任务号
			
			String rowkey =jsonReq.getString("rowkey");//tips rowkey

			if (StringUtils.isEmpty(rowkey)) {
				
                throw new IllegalArgumentException("参数错误:rowkey不能为空。");
            }
			
			TipsCheckSelector selector=new TipsCheckSelector();
			
			////返回当前rowkey下的 错误问题记录（用于界面显示或者修改）
			JSONObject result=selector.queryWrongByRowkey(checkTaskId,rowkey);
			
			System.out.println("result:"+result);
			

		} catch (Exception e) {
			
			e.printStackTrace();

			logger.error(e.getMessage(), e);

		}

	}
	
	
	@Test
	public void testFccApi() {
		
		FccApiImpl imp=new FccApiImpl();
		try {
			Map<String,Integer> resultMap=imp.getCheckTaskCount(555);
			Set<String> keys=resultMap.keySet();
			for (String key : keys) {
				System.out.println(key+":"+resultMap.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
