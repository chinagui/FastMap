package com.navinfo.dataservice.web.fcc.check.controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckWrongSelector;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckOperator;
import com.navinfo.dataservice.engine.fcc.check.TipsCheckSelector;
import com.navinfo.dataservice.engine.fcc.check.TipsExtract;

/** 
 * @ClassName: TipsCheckController.java
 * @author y
 * @date 2017-5-26 上午10:35:50
 * @Description: TODO
 *  
 */
@Controller
public class TipsCheckController extends BaseController {

	private static final Logger logger = Logger.getLogger(TipsCheckController.class);
	
	
	
	/**
	 * @Description:tip抽取
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/extract")
	public ModelAndView checkExtract(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/extract:");
			
			logger.debug("parameter:"+parameter);
			
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
			
			if (StringUtils.isEmpty(checkerName)) {
                throw new IllegalArgumentException("参数错误:checkerName不能为空。");
            }
			
			TipsExtract extract = new TipsExtract();
			
			////返回抽取后，抽取总量和tips类型数
			JSONObject result=extract.doExtract(checkTaskId,checkerId,checkerName,grids);
			
			logger.debug("result:"+result);
			
			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * @Description:tip抽取
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/close")
	public ModelAndView checkTaskClose(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/close:");
			
			logger.debug("parameter:"+parameter);
			
		    if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int checkTaskId=jsonReq.getInt("taskId"); //质检任务号
			TipsCheckOperator operate=new TipsCheckOperator();
			operate.closeTask(checkTaskId);
			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * @Description:质检tips统计
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 下午6:39:06
	 */
	@RequestMapping(value = "/tip/check/getStats")
	@Deprecated
	public ModelAndView getStats(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			
			logger.debug("/tip/check/getStats:");
			
			logger.debug("parameter:"+parameter);
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int checkTaskId=jsonReq.getInt("checkTaskId");

			int  workStatus=jsonReq.getInt("workStatus");
			
			int checkerId = jsonReq.getInt("checkerId");

			int  workerId = jsonReq.getInt("workerId");

			TipsCheckSelector selector = new TipsCheckSelector();
			
			JSONObject data = selector.getStats(checkerId,workerId,checkTaskId,workStatus);
			

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * @Description:质检列表查询
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 下午6:38:52
	 */
	@RequestMapping(value = "/tip/check/getSnapot")
	@Deprecated
	public ModelAndView getSnapot(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/getSnapot:");
			
			logger.debug("parameter:"+parameter);
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int checkTaskId=jsonReq.getInt("checkTaskId");

			int  workStatus=jsonReq.getInt("workStatus");
			
			int checkerId = jsonReq.getInt("checkerId");

			int  workerId = jsonReq.getInt("checkerId");
			
			String type = jsonReq.getString("type");
            if (StringUtils.isEmpty(type)) {
                throw new IllegalArgumentException("参数错误:type不能为空。");
            }

			int dbId = jsonReq.getInt("dbId");
            if (dbId == 0) {
                throw new IllegalArgumentException("参数错误:dbId不能为空。");
            }

			TipsCheckSelector selector = new TipsCheckSelector();
			
			JSONArray data = selector.getSnapot(checkerId,workerId,checkTaskId,workStatus,dbId,type);
			
			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	
	/**
	 * @Description:质检问题记录查询(根据质检任务号，tipsRowkey查询质检问题记录)
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/queryWrong")
	public ModelAndView queryWrongByRowkey(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/queryWrong:");
			
			logger.debug("parameter:"+parameter);
			
		    if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int checkTaskId=jsonReq.getInt("subTaskId"); //质检任务号
			
			String objectId =jsonReq.getString("objectId");//tips rowkey

			if (StringUtils.isEmpty(objectId)) {
				
                throw new IllegalArgumentException("参数错误:objectId不能为空。");
            }
			
			TipsCheckSelector selector=new TipsCheckSelector();
			
			////返回当前rowkey下的 错误问题记录（用于界面显示或者修改）
			JSONObject result=selector.queryWrongByRowkey(checkTaskId,objectId);
			
			logger.debug("result:"+result);
			
			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * @Description:质检问题记录保存
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/saveWrong")
	public ModelAndView saveWrong(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/saveWrong:");
			
			logger.debug("parameter:"+parameter);
			
		    if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }
			JSONObject jsonReq = JSONObject.fromObject(parameter);
		/*	
			int command = jsonReq.getInt("command"); //command,0：save or:1：update
			
			if (command!=0&&command!=1) {
				throw new IllegalArgumentException("参数错误：command不在范围内【0,1】");
			}
			*/
			
			JSONObject jsonWrong =jsonReq.getJSONObject("data");

			if (jsonWrong==null) {
				
                throw new IllegalArgumentException("参数错误:data不能为空。");
            }
			
			CheckWrong wrongResult=new TipsCheckOperator().saveCheckWrong(jsonWrong);
			
			logger.debug("result:"+JSONObject.fromObject(wrongResult));
			
			return new ModelAndView("jsonView", success(JSONObject.fromObject(wrongResult)));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	/**
	 * @Description:质检问题记录查询(根据质检任务号，tipsRowkey查询质检问题记录)
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/updateWrong")
	public ModelAndView updateWrong(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

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
			
			
			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	/**
	 * @Description:删除质检问题记录
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 上午10:41:40
	 */
	@RequestMapping(value = "/tip/check/deleteWrong")
	public ModelAndView deleteWrong(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/deleteWrong:");
			
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
			
			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	/**
	 * @Description:tips质检状态流传
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2017-5-26 下午6:38:52
	 */
	@RequestMapping(value = "/tip/check/updateStatus")
	public ModelAndView updateStatus(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			logger.debug("/tip/check/updateStatus:");
			
			logger.debug("parameter:"+parameter);
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			
			int  workStatus=jsonReq.getInt("checkStatus");
			
			String rowkey = jsonReq.getString("rowkey");

			TipsCheckOperator op = new TipsCheckOperator();
			
			op.updateTipsCheckStatus(rowkey,workStatus);
			
			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 查询道路质检问题记录
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/rd/check/getQualityProblem")
	public ModelAndView getQualityProblem(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn = null;
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String id = jsonReq.getString("id");
			
			JSONObject data = CheckWrongSelector.getByLogId(id);

			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
