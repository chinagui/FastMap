package com.navinfo.dataservice.web.man.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.inforMan.InforManService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: InforManController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class InforManController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired 
	private InforManService service;

/**
 * 规划管理-情报管理-查看及编辑情报信息
 * @param request
 * @return
 */
	@RequestMapping(value = "/inforMan/create")
	public ModelAndView create(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			service.create(dataJson,userId);			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * 规划管理-情报管理-开启情报规划
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/update")
	public ModelAndView update(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			service.update(dataJson);			
			return new ModelAndView("jsonView", success("修改成功"));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * 情报管理--查看及编辑情报信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/close")
	public ModelAndView close(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONArray inforManIds=dataJson.getJSONArray("inforIds");
			service.close(JSONArray.toList(inforManIds));			
			return new ModelAndView("jsonView", success("情报已关闭"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 情报管理--查看及编辑情报信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/inforMan/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			InforMan data = service.query(dataJson.getString("inforId"));
			if(null==data){
				return new ModelAndView("jsonView", fail("该情报规划不存在"));
			}
			return new ModelAndView("jsonView", success(JsonOperation.beanToJson(data)));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
