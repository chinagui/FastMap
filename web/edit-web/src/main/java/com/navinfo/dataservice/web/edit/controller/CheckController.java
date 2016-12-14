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

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
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

	/**
	 * @Title: listCheck
	 * @Description: (修)返回检察结果列表
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月16日 上午11:14:09 
	 */
	@RequestMapping(value = "/check/list")
	public ModelAndView listCheck(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			logger.info("start check/list");

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int dbId = jsonReq.getInt("dbId");
			
			int subtaskType = jsonReq.getInt("subtaskType");
			int subtaskId =0;
			if(jsonReq.containsKey("subtaskId")){
				subtaskId=jsonReq.getInt("subtaskId");
			}

			JSONArray gridJas = jsonReq.getJSONArray("grids");

			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			NiValExceptionSelector selector = new NiValExceptionSelector(conn);
			
			Set<String> grids = new HashSet<String>();
			for(Object obj:gridJas){
				grids.add(obj.toString());
			}
			if(grids.size()<1){
				logger.info("start check/list manApi");
				ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
				List<Integer> gridList = manApi.getGridIdsBySubtaskId(subtaskId);
				for(Integer obj:gridList){
					grids.add(obj.toString());
				}
				logger.info("end check/list manApi");
			}		
			Page page = selector.list(subtaskType,grids, pageSize, pageNum);
			logger.info("end check/list");

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
	
	/**
	 * @Title: listCheckResults
	 * @Description: 查看检查结果列表(新增)(第七迭代)
	 * @param request   :parameter={"pageNum":0,"pageSize":20,"subtaskId":76}
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月23日 下午1:42:25 
	 */
	@RequestMapping(value = "/check/listRdnResult")
	public ModelAndView listCheckResults(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		logger.debug("listRdnResult:道路名检查结果查询接口:parameter:"+parameter);
		Connection conn = null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId = jsonReq.getInt("subtaskId");
			Integer type = jsonReq.getInt("type");
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			conn = DBConnector.getInstance().getMetaConnection();
			NiValExceptionSelector niValExceptionSelector = new NiValExceptionSelector(conn);
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			//获取子任务范围内的tips
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			logger.debug("获取子任务范围内的tips: "+tips);
			//获取规则号
			JSONArray ruleCodes = CheckService.getInstance().getCkRuleCodes(type);
			logger.debug("获取规则号"+ruleCodes);
			Page page = niValExceptionSelector.listCheckResults(jsonReq,tips,ruleCodes);
			logger.info("end check/list");
			logger.debug(page.getResult());
			logger.debug(page.getTotalCount());
			return new ModelAndView("jsonView", success(page));

		} catch (Exception e) {
			
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			DbUtils.closeQuietly(conn);
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
	 * @Title: updateCheckRdnResult
	 * @Description: 修改道路名检查结果的状态(新增)(第七迭代)
	 * @param request : parameter={"id":"11987","type":2} (id:检查结果id,type:状态 1例外， 2确认不修改， 3确认已修改)
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月23日 下午1:47:32 
	 */
	@RequestMapping(value = "/check/updateRdnResult")
	public ModelAndView updateCheckRdnResult(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String id = jsonReq.getString("id");

			int type = jsonReq.getInt("type");

			conn = DBConnector.getInstance().getMetaConnection();//获取元数据库连接

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
	 * @Title: checkRun
	 * @Description: 
	 * 检查执行
	 * dbId	是	子任务id
	 * type	是	检查类型（0 poi行编，1poi精编, 2道路 , 3道路名）
	 * 根据输入的子任务和检查类型，对任务范围内的数据执行，执行检查。不执行检查结果清理
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月15日 下午10:12:31 
	 */
	@RequestMapping(value = "/check/run")
	public ModelAndView checkRun(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			int subtaskId=jsonReq.getInt("subtaskId");
			int checkType=jsonReq.getInt("checkType");			
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			long jobId=CheckService.getInstance().checkRun(subtaskId,userId,checkType,jsonReq);				
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
	 * @Title: getCkRules
	 * @Description: 获取道路名检察的规则列表(修)(第七迭代)
	 * @param request  type	是	类型（0 POI， 1道路 ,2 道路名）
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月15日 下午9:10:32 
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
	
	
	/**
	 * 清检查结果接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/check/cleanCkResult")
	public ModelAndView cleanCkResult(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			DeepCoreControl deepControl = new DeepCoreControl();
			deepControl.cleanCheck(jsonReq, userId);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
