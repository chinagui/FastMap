package com.navinfo.dataservice.web.datahub.controller;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.datahub.service.DbService;

/** 
 * @ClassName: DataHubController 
 * @author Xiao Xiaowen 
 * @date 2015-11-27 上午11:44:30 
 * @Description: TODO
 */
@Controller
public class DbController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());

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
			String serverType = URLDecode(request.getParameter("serverType"));
			Assert.notNull(serverType, "serverType不能为空");
			String dbName = URLDecode(request.getParameter("dbName"));
//			Assert.notNull(dbName, "dbName不能为空");
			String userName = URLDecode(request.getParameter("userName"));
//			Assert.notNull(userName, "userName不能为空");
			String userPasswd = URLDecode(request.getParameter("userPasswd"));
//			Assert.notNull(userPasswd, "userPasswd不能为空");
			String type = URLDecode(request.getParameter("bizType"));
			Assert.notNull(type, "type不能为空");
			String descp = URLDecode(request.getParameter("descp"));
			String gdbVersion = URLDecode(request.getParameter("gdbVersion"));
			//参考db，使用参考策略
			String refDbIdStr = URLDecode(request.getParameter("refDbId"));
			int refDbId = StringUtils.isEmpty(refDbIdStr)?0:Integer.valueOf(refDbIdStr);
			//省份代码，使用按省份分配策略
			//String provCode = URLDecode(request.getParameter("provcode"));
			
			DbInfo db = DbService.getInstance().createDb(serverType,dbName,userName,userPasswd,type, descp,gdbVersion,refDbId,0);

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
			DbInfo db = DbService.getInstance().getDbByName(dbName, dbType);
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
			DbInfo db = DbService.getInstance().getDbById(Integer.parseInt(dbId));
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
			DbInfo db = DbService.getInstance().getOnlyDbByName(dbName);
			return new ModelAndView("jsonView", success(db.getConnectParam()));
		}catch(Exception e){
			log.error("获取db失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/db/getonlybytype/")
	public ModelAndView getOnlyDbByType(HttpServletRequest request){
		try{
			String bizType = URLDecode(request.getParameter("type"));
			if(StringUtils.isEmpty(bizType)){
				throw new IllegalArgumentException("type参数不能为空。");
			}
			DbInfo db = DbService.getInstance().getOnlyDbByBizType(bizType);
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
