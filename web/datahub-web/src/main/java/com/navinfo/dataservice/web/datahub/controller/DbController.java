package com.navinfo.dataservice.web.datahub.controller;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.datahub.TestClass;
import com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: DataHubController 
 * @author Xiao Xiaowen 
 * @date 2015-11-27 上午11:44:30 
 * @Description: TODO
 */
@Controller
public class DbController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@RequestMapping(value = "/db/hello/")
	public ModelAndView test(HttpServletRequest request){
		String result = "";
		try{
			TestClass tc = new TestClass();
			result = tc.test("XXX!!!");
		}catch(Exception e){
			result = "调用内部服务出错";
			log.error(e.getMessage(),e);
		}
		return new ModelAndView("jsonView", "data", result);
	}
	@RequestMapping(value = "/db/get/")
	public ModelAndView getDb(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "Hello,Datahub.");
	}
	@RequestMapping(value = "/db/getcn/")
	public ModelAndView getDbCn(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "你好，数据中心！");
	}
	@RequestMapping(value = "/db/create/")
	public ModelAndView createDb(HttpServletRequest request){
		try{
			String dbName = URLDecode(request.getParameter("name"));
			String dbType = URLDecode(request.getParameter("type"));
			String descp = URLDecode(request.getParameter("descp"));
			String strategyType = null;
			//参考db，使用参考策略
			String refDbName = URLDecode(request.getParameter("refname"));
			String refDbType = URLDecode(request.getParameter("reftype"));
			//省份代码，使用按省份分配策略
			String provCode = URLDecode(request.getParameter("provcode"));
			if(StringUtils.isEmpty(dbName)){
				throw new IllegalArgumentException("name参数不能为空。");
			}
			if(StringUtils.isEmpty(dbType)){
				throw new IllegalArgumentException("type参数不能为空。");
			}
			Map<String,String> strategyParam = new HashMap<String,String>();
			if(StringUtils.isNotEmpty(refDbName)&&StringUtils.isNotEmpty(refDbType)){
				strategyType = DbServerStrategy.USE_REF_DB;
				strategyParam.put("refDbName", refDbName);
				strategyParam.put("refDbType", refDbType);
			}else if(StringUtils.isNotEmpty(provCode)){
				strategyType = DbServerStrategy.BY_PROVINCE;
				strategyParam.put("provinceCode", provCode);
			}else{
				//strategyType = DbServerStrategy.RANDOM;
				strategyParam = null;
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.createDb(dbName, dbType, descp,strategyType,strategyParam);

			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("创建db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/db/getbyname/")
	public ModelAndView getDbByName(HttpServletRequest request){
		try{
			String dbName = URLDecode(request.getParameter("name"));
			String dbType = URLDecode(request.getParameter("type"));
			if(StringUtils.isEmpty(dbName)){
				throw new IllegalArgumentException("name参数不能为空。");
			}
			if(StringUtils.isEmpty(dbType)){
				throw new IllegalArgumentException("type参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getDbByName(dbName, dbType);
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/db/getbyid/")
	public ModelAndView getDbById(HttpServletRequest request){
		try{
			String dbId = URLDecode(request.getParameter("id"));
			if(StringUtils.isEmpty(dbId)){
				throw new IllegalArgumentException("id参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getDbById(Integer.parseInt(dbId));
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/db/getonlybyname/")
	public ModelAndView getOnlyDbByName(HttpServletRequest request){
		try{
			String dbName = URLDecode(request.getParameter("name"));
			if(StringUtils.isEmpty(dbName)){
				throw new IllegalArgumentException("name参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getOnlyDbByName(dbName);
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/datahub/getonlybytype/")
	public ModelAndView getOnlyDbByType(HttpServletRequest request){
		try{
			String dbType = URLDecode(request.getParameter("type"));
			if(StringUtils.isEmpty(dbType)){
				throw new IllegalArgumentException("type参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getOnlyDbByType(dbType);
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	public static void main(String[] args){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dbName", "1");
		map.put("dbPasswd", "2");
		map.put("serverIp", "3");
		map.put("serverPort", "4");
		map.put("serverType", "5");
		//new ModelAndView("jsonView", new BaseController().success(map));

    	System.out.println("success");
	}
}
