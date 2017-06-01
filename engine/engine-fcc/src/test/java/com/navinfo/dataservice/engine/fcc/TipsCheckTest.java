package com.navinfo.dataservice.engine.fcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckOperator;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckSelector;
import com.navinfo.dataservice.engine.fcc.check.TipsExtract;

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
				.fromObject("[60566132,60566122,60566120,60566133,60566123,60566112,60566113,60566130,60566131]");

		JSONObject param = new JSONObject();
		param.put("checkTaskId", 26);
		param.put("subTaskId", 25);
		param.put("workerId", 123);
		param.put("workerName", "作业员1");
		param.put("checkerId", 456);
		param.put("checkerName", "质检员1");
		param.put("taskName", "测试任务1");
		param.put("subTaskName", "测试子任务1");
		param.put("grids", gridsList);

		String parameter = param.toString();

		JSONObject result = null;
		try {

			System.out.println("parameter:" + parameter);

			logger.debug("/tip/check/extract:");

			logger.debug("parameter:" + parameter);

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			// 输入：质检任务号、作业子任务号、作业任务范围（grids）、作业员id
			int checkTaskId = jsonReq.getInt("checkTaskId"); // 质检任务号

			int subTaskId = jsonReq.getInt("subTaskId");// 作业任务号

			int workerId = jsonReq.getInt("workerId");// 作业员编号

			String workerName = jsonReq.getString("workerName");// 作业员姓名

			int checkerId = jsonReq.getInt("checkerId");// 质检编号

			String checkerName = jsonReq.getString("checkerName");// 作业员姓名

			String taskName = jsonReq.getString("taskName");// 任务名称

			String subTaskName = jsonReq.getString("subTaskName");// 子任务名称

			JSONArray grids = jsonReq.getJSONArray("grids"); // 作业子任务范围

			if (grids == null || grids.size() == 0) {
				throw new IllegalArgumentException("参数错误:grids不能为空。");
			}

			if (StringUtils.isEmpty(workerName)) {
				throw new IllegalArgumentException("参数错误:workerName不能为空。");
			}

			if (StringUtils.isEmpty(checkerName)) {
				throw new IllegalArgumentException("参数错误:checkerName不能为空。");
			}

			if (StringUtils.isEmpty(taskName)) {
				throw new IllegalArgumentException("参数错误:taskName不能为空。");
			}
			if (StringUtils.isEmpty(subTaskName)) {
				throw new IllegalArgumentException("参数错误:subTaskName不能为空。");
			}

			TipsExtract extract = new TipsExtract();

			// //返回抽取后，抽取总量和tips类型数

			result = extract.doExtract(jsonReq);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(result);

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
	 * @Description:查询质检问题记录测试（按照rowkey）
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testGetCheckWrongByRowkey() {

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

	}

	/**
	 * @Description:新增质检问题记录
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testSaveWrong() {

		JSONObject obj = new JSONObject();
		
		obj.put("checkTaskId", 456);
		obj.put("tipsCode", "1201");
		obj.put("tipsRowkey", "0212014400ac0f42814585b15d2e1f3bdabafb");
		obj.put("quDesc", "错误描述1201");
		obj.put("reason", "错误原因1201 ");
		obj.put("erContent", "错误内容1201");
		obj.put("quRank", "A"); //错误等级
		obj.put("isPrefer", "是"); //是否倾向性
		
		
		JSONObject pa = new JSONObject();
		
		pa.put("data", obj);

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
	public void testUpdateWrong() {

		JSONObject obj = new JSONObject();
		obj.put("quDesc", "错误描述(修改)");
		obj.put("reason", "错误原因 (修改)");
		obj.put("erContent", "错误内容(修改)");
		obj.put("quRank", "A"); //错误等级
		obj.put("isPrefer", 1); //是否倾向性
		
		
		JSONObject pa = new JSONObject();
		
		pa.put("data", obj);
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
			
			JSONObject jsonWrong =jsonReq.getJSONObject("data");

			if (jsonWrong==null) {
				
                throw new IllegalArgumentException("参数错误:data不能为空。");
            }
			
			new TipsCheckOperator().updateCheckWrong(logId,jsonWrong);
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
	
	
	/**
	 * @Description:查询
	 * @author: y
	 * @time:2017-5-31 上午11:25:24
	 */
	@Test
	public void testGetWrongByRowkey() {
		
		JSONObject pa = new JSONObject();
		
		pa.put("rowkey", "0212014400ac0f42814585b15d2e1f3bdabafb");
		
		pa.put("subTaskId", "456");

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
			
			logger.debug("result:"+result);
			

		} catch (Exception e) {
			
			e.printStackTrace();

			logger.error(e.getMessage(), e);

		}

	}
}
