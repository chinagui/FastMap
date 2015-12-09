package com.navinfo.dataservice.datahub.controller;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.dataservice.datahub.springmvc.BaseController;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: DataHubController 
 * @author Xiao Xiaowen 
 * @date 2015-11-27 上午11:44:30 
 * @Description: TODO
 */
@Controller
public class DataHubController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());
	@RequestMapping(value = "/datahub/getdb/")
	public ModelAndView getDb(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "Hello,Datahub.");
	}
	@RequestMapping(value = "/datahub/getdbcn/")
	public ModelAndView getDbCn(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "你好，数据中心。");
	}
	@RequestMapping(value = "/datahub/createdb/")
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
				throw new IllegalArgumentException("dbName参数不能为空。");
			}
			if(StringUtils.isEmpty(dbType)){
				throw new IllegalArgumentException("dbType参数不能为空。");
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
	@RequestMapping(value = "/datahub/getdbbyname/")
	public ModelAndView getDbByName(HttpServletRequest request){
		try{
			String dbName = URLDecode(request.getParameter("dbName"));
			String dbType = URLDecode(request.getParameter("dbType"));
			if(StringUtils.isEmpty(dbName)){
				throw new IllegalArgumentException("dbName参数不能为空。");
			}
			if(StringUtils.isEmpty(dbType)){
				throw new IllegalArgumentException("dbType参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getDbByName(dbName, dbType);
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/datahub/getdbbyid/")
	public ModelAndView getDbById(HttpServletRequest request){
		try{
			String dbId = URLDecode(request.getParameter("dbId"));
			if(StringUtils.isEmpty(dbId)){
				throw new IllegalArgumentException("dbId参数不能为空。");
			}
			DbManager dbMan = new DbManager();
			UnifiedDb db = dbMan.getDbById(Integer.parseInt(dbId));
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
