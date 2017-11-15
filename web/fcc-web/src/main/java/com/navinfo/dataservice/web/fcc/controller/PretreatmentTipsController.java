package com.navinfo.dataservice.web.fcc.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: PretreatmentTipsController.java
 * @author y
 * @date 2016-11-16 上午11:20:04
 * @Description: 预处理tips controller
 * 
 */

@Controller
public class PretreatmentTipsController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(PretreatmentTipsController.class);

	/**
	 * @Description:预处理tips创建
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/createPre")
	public ModelAndView createPretreatmentTips(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/createPre: " + parameter);

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);

			JSONObject tipsGeometry = jsonReq.getJSONObject("geometry");
			int userId = jsonReq.getInt("user");
			String sourceType = jsonReq.getString("sourceType");
			String memo = jsonReq.getString("memo");
			JSONObject deep = jsonReq.getJSONObject("deep"); // tips详细信息

			if (!jsonReq.containsKey("qSubTaskId")) {
				throw new IllegalArgumentException("参数错误：qSubTaskId不能为空。");
			}
			int qSubTaskId = jsonReq.getInt("qSubTaskId");

			if (StringUtils.isEmpty(sourceType)) {
				throw new IllegalArgumentException("参数错误：sourceType不能为空。");
			}

			if (tipsGeometry.isNullObject() || tipsGeometry == null) {
				throw new IllegalArgumentException("参数错误：geometry不能为空。");
			}

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			String rowkey = op.create(sourceType, tipsGeometry, userId, deep, memo, qSubTaskId);

			return new ModelAndView("jsonView", success(rowkey));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:情报矢量化删除tips
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/pretreatmen/delete")
	public ModelAndView deleteByRowkey(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			// 默认物理删除。0：逻辑删除；1：物理删除 2:不用删除
			int delType = PretreatmentTipsOperator.TIP_PHYSICAL_DELETE;

			int subTaskId = jsonReq.getInt("subTaskId");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			PretreatmentTipsOperator op2 = new PretreatmentTipsOperator();

			delType = op2.getDelTypeByRowkeyAndUserId(rowkey, subTaskId);

			if (delType != PretreatmentTipsOperator.TIP_NOT_DELETE) {
				op2.deleteByRowkey(rowkey, delType, subTaskId);
			}

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:测线打断（情报矢量化测线打断）
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/pretreatmen/measuringLineCut")
	public ModelAndView measuringLineCut(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			int user = jsonReq.getInt("user");

			JSONObject pointGeo = jsonReq.getJSONObject("pointGeo");

			int subTaskId = jsonReq.getInt("subtaskId"); // 任务号

			int taskType = jsonReq.getInt("taskType"); // 任务类型（中线或者是快线的任务号）

			// web传递的是1，或4，需要转成子任务类型
			if (taskType == TaskType.Q_TASK_TYPE) {
				taskType = TaskType.Q_SUB_TASK_TYPE;
			}

			else if (taskType == TaskType.M_TASK_TYPE) {
				taskType = TaskType.M_SUB_TASK_TYPE;
			}

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			if (pointGeo == null || pointGeo.isEmpty()) {
				throw new IllegalArgumentException("参数错误：pointGeo不能为空。");
			}

			int dbId = jsonReq.getInt("dbId");// 大区库id. 打断维护使用

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.cutMeasuringLineCut(rowkey, pointGeo, user, subTaskId, taskType, dbId);

			JSONObject data = new JSONObject();

			data.put("rowkey", rowkey);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * 测线分离
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/tip/pretreatmen/lineSplit")
	public ModelAndView measuringLineSplit(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			JSONArray pids = jsonReq.getJSONArray("pids");
			if (pids == null || pids.size() <= 0) {
				throw new IllegalArgumentException("pids参数不能为空。");
			}

			double distance = jsonReq.getDouble("dis");
			if (distance <= 0) {
				throw new IllegalArgumentException("dis参数必须大于0");
			}

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();
			JSONArray result = op.measuringLineSplit(pids, distance);

			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:预处理tips修改--修形
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/eidtshape")
	public ModelAndView editShape(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/editShape: " + parameter);

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);

			String rowkey = jsonReq.getString("rowkey");

			JSONObject tipsGeometry = jsonReq.getJSONObject("geometry"); // 修改改坐标

			// String memo=jsonReq.getString("memo"); //改备注

			// int user = getUserIdFromRequest(request);

			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			if (tipsGeometry.isNullObject() || tipsGeometry == null) {
				throw new IllegalArgumentException("参数错误：geometry不能为空。");
			}

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.editGeo(rowkey, tipsGeometry, user);

			return new ModelAndView("jsonView", success(rowkey));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:预处理tips-打断
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/cutLine")
	public ModelAndView cutLine(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/cutLine: " + parameter);

			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");

			JSONObject pointGeo = jsonReq.getJSONObject("pointGeo"); // 修改改坐标(点几何)

			// int user = getUserIdFromRequest(request);

			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			if (pointGeo.isNullObject() || pointGeo == null) {
				throw new IllegalArgumentException("参数错误：pointGeo不能为空。");
			}
			//
			// if (!jsonReq.containsKey("qSubTaskId")) {
			// throw new IllegalArgumentException("参数错误：qSubTaskId不能为空。");
			// }
			// int qSubTaskId = jsonReq.getInt("qSubTaskId");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			JSONArray rowkeyArray = op.breakLine(rowkey, pointGeo, user);

			return new ModelAndView("jsonView", success(rowkeyArray));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:fc预处理tips提交(按范围提交)
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/submitPre")
	public ModelAndView submit(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int user = jsonReq.getInt("user");

			int subTaskId = jsonReq.getInt("subTaskId");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.submit2Web(user, subTaskId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:情报矢量化理tips提交(按任务)
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-4-14 下午2:39:49
	 */
	@RequestMapping(value = "/tip/infoTaskSubmit")
	public ModelAndView infoTaskSubmit(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int user = jsonReq.getInt("user");

			int taskId = jsonReq.getInt("taskId");

			/*int taskType= jsonReq.getInt("taskType");*/

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.submitInfoJobTips2Web(user, taskId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:fc预处理同时编辑备注和fc
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/pretreatmen/editMemoAndFc")
	public ModelAndView editMemoAndFc(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String memo = jsonReq.getString("memo");

			JSONObject deep = null;
			if (jsonReq.containsKey("deep")) {

				deep = jsonReq.getJSONObject("deep");

			}

			String rowkey = jsonReq.getString("rowkey");

			// int user = getUserIdFromRequest(request);

			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.updateFeedbackMemoAndDeep(rowkey, user, memo, deep);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:（情报矢量化）tip新增或者修改
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/pretreatmen/saveOrUpdate")
	public ModelAndView saveOrUpdate(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		logger.info("pretreatmen/saveOrUpdate,parameter:" + parameter);
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);

			JSONObject jsonInfo = null; // jsonInfo为全量的tips信息，需要符合规格定义
			if (jsonReq.containsKey("jsonInfo")) {

				jsonInfo = jsonReq.getJSONObject("jsonInfo");

			}
			if (jsonInfo == null || jsonInfo.isNullObject() || jsonInfo.keySet().size() == 0) {
				throw new IllegalArgumentException("参数错误：jsonInfo不能为空。");
			}

			int command = jsonReq.getInt("command"); // command,0：save
														// or:1：update

			if (command != 0 && command != 1) {
				throw new IllegalArgumentException("参数错误：command不在范围内【0,1】");
			}

			int user = jsonReq.getInt("user");

			int dbId = jsonReq.getInt("dbId");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			String rowkey = op.saveOrUpdateTips(jsonInfo, command, user, dbId); // 新增或者修改一个tips

			JSONObject data = new JSONObject();

			data.put("rowkey", rowkey);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:批量新增
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/pretreatmen/batchSave")
	public ModelAndView batchSave(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);

			JSONArray jsonInfoArr = null; // jsonInfo为全量的tips信息，需要符合规格定义
			if (jsonReq.containsKey("jsonInfoArr")) {

				jsonInfoArr = jsonReq.getJSONArray("jsonInfoArr");

			}
			if (jsonInfoArr == null || jsonInfoArr.size() == 0) {
				throw new IllegalArgumentException("参数错误：jsonInfoArr不能为空。");
			}

			int user = jsonReq.getInt("user");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			int command = PretreatmentTipsOperator.COMMAND_INSERT;

			op.batchSaveOrUpdate(jsonInfoArr, user, command); // 新增多个tips

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:从request的token中获取userId
	 * @param request
	 * @return
	 * @author: liya
	 * @time:2017-1-9 下午3:33:49
	 */
	/*	private   int getUserIdFromRequest(HttpServletRequest request) {
			AccessToken  token=(AccessToken)request.getAttribute("token");
			
			int userId=(int)token.getUserId() ;
			return userId;
		}*/

	/** 
	* @Title: subtaskGridStat 
	* @Description: grid标注和统计
	* @param request
	* @return
	* @author Gui Yingen
	* @date 2017年11月14日 下午1:31:00
	*/
	@RequestMapping(value = "/tips/pretreatmen/subtaskGridStat")
	public ModelAndView subtaskGridStat(HttpServletRequest request) {
		String parameter = request.getParameter("parameter");
		logger.info("/pretreatmen/subtaskGridStat, parameter: " + parameter);

		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalAccessException("parameter参数不能为空");
			}
			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);
			int subtaskId = 0;
			if (!jsonReq.containsKey("subtaskId")) {
				throw new IllegalAccessException("参数错误：subtaskId不能为空");
			}
			subtaskId = jsonReq.getInt("subtaskId");

			PretreatmentTipsOperator op = new PretreatmentTipsOperator();
			JSONArray data = op.getSubtaskGridStat(subtaskId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
