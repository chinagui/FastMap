package com.navinfo.dataservice.web.man.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.man.config.ConfigService;
@Controller
public class ConfigController extends BaseController {
	private Logger log=LoggerRepos.getLogger(getClass());

	
	/**
	 * a-2-2_生产节奏控制
	 * man_config表中，有confKey对应记录，则更新记录；没有则报失败，配置参数错误
	 * by zhangxiaoyi
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/config/update")
	public ModelAndView update(HttpServletRequest request){
		try{
			AccessToken token=(AccessToken) request.getAttribute("token");
			String parameter=request.getParameter("parameter");
			if(StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			JSONObject dataJson=JSONObject.fromObject(URLDecode(parameter));
			long userId=token.getUserId();
			ConfigService.getInstance().update(userId,dataJson);
			return new ModelAndView("jsonView",success("success"));
		}catch(Exception e){
			log.error("修改配置错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value="/config/list")
	public ModelAndView list(HttpServletRequest request){
		try{
			List<Map<String, Object>> result=ConfigService.getInstance().list();
			return new ModelAndView("jsonView",success(result));
		}catch(Exception e){
			log.error("查询列表错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * 图幅闸权限验证接口
	 * 原则：
	 * 1.	验证输入的密码是否与sys库配置的man.password相同；相同返回true，不同false
	 * 应用场景：管理平台—图幅管理
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/config/verify")
	public ModelAndView verify(HttpServletRequest request){
		try{
			String parameter=request.getParameter("parameter");
			if(StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			JSONObject dataJson=JSONObject.fromObject(URLDecode(parameter));
			String password=dataJson.getString("password");
			boolean result=ConfigService.getInstance().verify(password);
			return new ModelAndView("jsonView",success(result));
		}catch(Exception e){
			log.error("查询列表错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 图幅闸开关接口
	 * 原则：
	 * 1.	根据openFlag，对图幅进行开关操作
	 * 2.	Action有值，则对action对应的图幅操作
	 * 3.	mediumAction有值，则对meshList图幅操作开关+mediumAction对应的MEDIUM1_FLAG/MEDIUM2_FLAG=1
	 * 4.	mediumAction无值，meshList有值，则对meshList图幅操作开关
	 * 5.	仅openFlag有值，对全部图幅进行操作
	 * 应用场景：管理平台—图幅管理
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/config/mangeMesh")
	public ModelAndView mangeMesh(HttpServletRequest request){
		try{
			String parameter=request.getParameter("parameter");
			if(StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空");
			}
			JSONObject dataJson=JSONObject.fromObject(URLDecode(parameter));
			ConfigService.getInstance().mangeMesh(dataJson);
			return new ModelAndView("jsonView",success());
		}catch(Exception e){
			log.error("查询列表错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
