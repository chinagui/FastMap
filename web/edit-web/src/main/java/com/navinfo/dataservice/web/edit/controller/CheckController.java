package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.edit.check.CheckService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class CheckController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(CheckController.class);

	@RequestMapping(value = "/check/list")
	public ModelAndView listCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			
			int subtaskType = jsonReq.getInt("subtaskType");

			JSONArray gridJas = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);
			
			Set<String> grids = new HashSet<String>();
			for(Object obj:gridJas){
				grids.add(obj.toString());
			}

			Page page = selector.list(subtaskType,grids, pageSize, pageNum);

			return new ModelAndView("jsonView", success(page));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@RequestMapping(value = "/check/get")
	public ModelAndView getCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			JSONArray data = selector.loadByGrid(grids, pageSize, pageNum);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/check/count")
	public ModelAndView getCheckCount(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			JSONArray grids = jsonReq.getJSONArray("grids");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);

			int data = selector.loadCountByGrid(grids);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping(value = "/check/update")
	public ModelAndView updateCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");

			String id = jsonReq.getString("id");

			int type = jsonReq.getInt("type");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionOperator selector = new NiValExceptionOperator(conn);

			selector.updateCheckLogStatus(id, type);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 检查执行
	 * dbId	是	子任务id
	 * type	是	检查类型（0 poi行编，1poi精编, 2道路）
	 * 根据输入的子任务和检查类型，对任务范围内的数据执行，执行检查。不执行检查结果清理
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/run")
	public ModelAndView checkRun(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId=jsonReq.getInt("subtaskId");
			int checkType=jsonReq.getInt("checkType");
			String ckRules = "";
			if (jsonReq.containsKey("ckRules")) {
				ckRules = jsonReq.getString("ckRules");
			}
			
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			long jobId=CheckService.getInstance().checkRun(subtaskId,userId,checkType,ckRules);				
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 执行检查引擎 前检查
	 * 应用场景：测试
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/runPreCheckEngine")
	public ModelAndView runPreCheckEngine(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn =null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId=jsonReq.getInt("dbId");
			//"command":"UPDATE","type":"RDLINK"
			String objType=jsonReq.getString("type");
			String operType=jsonReq.getString("command");
			JSONArray glmArray=jsonReq.getJSONArray("data");
			logger.info("start runPreCheckEngine:"+dbId+","+objType+","+operType);
			Iterator glmIterator=glmArray.iterator();
			List<IRow> glmList=new ArrayList<IRow>();
			while(glmIterator.hasNext()){
				JSONObject glmTmp=(JSONObject) glmIterator.next();
				String clasStr=glmTmp.getString("classStr");
				JSONObject glmStr=glmTmp.getJSONObject("value");
				IRow glmObj = (IRow) Class.forName(clasStr).newInstance();    //获取对应类  
				glmObj.Unserialize(glmStr);
				glmList.add(glmObj);
			}
			CheckCommand checkCommand = new CheckCommand();			
			checkCommand.setObjType(Enum.valueOf(ObjType.class,objType));
			checkCommand.setOperType(Enum.valueOf(OperType.class,operType));
			// this.checkCommand.setGlmList(this.command.getGlmList());
			conn = DBConnector.getInstance().getConnectionById(42);	
			CheckEngine checkEngine = new CheckEngine(checkCommand, conn);
			String error=checkEngine.preCheck();
			logger.info("end runPreCheckEngine:"+dbId+","+objType+","+operType);
			return new ModelAndView("jsonView", success(error));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 执行检查引擎 后检查
	 * 应用场景：测试
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/runPostCheckEngine")
	public ModelAndView runPostCheckEngine(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn =null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId=jsonReq.getInt("dbId");
			//"command":"UPDATE","type":"RDLINK"
			String objType=jsonReq.getString("type");
			String operType=jsonReq.getString("command");
			JSONArray glmArray=jsonReq.getJSONArray("data");
			logger.info("start runPostCheckEngine:"+dbId+","+objType+","+operType);
			Iterator glmIterator=glmArray.iterator();
			List<IRow> glmList=new ArrayList<IRow>();
			while(glmIterator.hasNext()){
				JSONObject glmTmp=(JSONObject) glmIterator.next();
				String clasStr=glmTmp.getString("classStr");
				JSONObject glmStr=glmTmp.getJSONObject("value");
				IRow glmObj = (IRow) Class.forName(clasStr).newInstance();    //获取对应类  
				glmObj.Unserialize(glmStr);
				glmList.add(glmObj);
			}
			CheckCommand checkCommand = new CheckCommand();			
			checkCommand.setObjType(Enum.valueOf(ObjType.class,objType));
			checkCommand.setOperType(Enum.valueOf(OperType.class,operType));
			// this.checkCommand.setGlmList(this.command.getGlmList());
			conn = DBConnector.getInstance().getConnectionById(42);	
			CheckEngine checkEngine = new CheckEngine(checkCommand, conn);
			checkEngine.postCheck();
			logger.info("end runPostCheckEngine:"+dbId+","+objType+","+operType);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 执行单个检查
	 * 应用场景：测试
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/runCheckRule")
	public ModelAndView runCheckRule(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn =null;
		try {
			//解析参数
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int dbId=jsonReq.getInt("dbId");
			//"command":"UPDATE","type":"RDLINK"
			String objType=jsonReq.getString("type");
			String operType=jsonReq.getString("command");
			String checkType=jsonReq.getString("checkType");
			JSONArray glmArray=jsonReq.getJSONArray("data");
			logger.info("start runCheckRule:"+dbId+","+operType);
			Iterator glmIterator=glmArray.iterator();
			List<IRow> glmList=new ArrayList<IRow>();
			while(glmIterator.hasNext()){
				JSONObject glmTmp=(JSONObject) glmIterator.next();
				String clasStr=glmTmp.getString("classStr");
				JSONObject glmStr=glmTmp.getJSONObject("value");
				IRow glmObj = (IRow) Class.forName(clasStr).newInstance();    //获取对应类  
				glmObj.Unserialize(glmStr);
				glmList.add(glmObj);
			}
			JSONArray ruleArray=jsonReq.getJSONArray("rules");
			//构造检查参数
			CheckCommand checkCommand = new CheckCommand();			
			checkCommand.setObjType(Enum.valueOf(ObjType.class,objType));
			checkCommand.setOperType(Enum.valueOf(OperType.class,operType));
			checkCommand.setGlmList(glmList);
			
			conn = DBConnector.getInstance().getConnectionById(dbId);	
			//执行检查规则
			CheckEngine cEngine=new CheckEngine(checkCommand,conn);
			List<NiValException> checkResultList=cEngine.checkByRules(ruleArray, checkType);	
			//返回检查结果
			if("POST".equals(checkType)){
				logger.info("end runCheckRule:"+dbId+","+operType);
				return new ModelAndView("jsonView", success(checkResultList));}
			if("PRE".equals(checkType)){
				if(checkResultList!=null && checkResultList.size()>0){
					logger.info("end runCheckRule:"+dbId+","+operType);
					return new ModelAndView("jsonView", success(checkResultList.get(0).getInformation()));
					}
				logger.info("end runCheckRule:"+dbId+","+operType);
				return new ModelAndView("jsonView", success());
			}
			logger.info("end runCheckRule:"+dbId+","+operType);
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 检查规则查询
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author wangdongbin
	 */
	@RequestMapping(value = "/check/getCkRules")
	public ModelAndView getCkRules(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn =null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");
			
			int type = jsonReq.getInt("type");
			
			JSONArray result = CheckService.getInstance().getCkRules(pageSize, pageNum, type);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
