package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.api.man.model.UserDevice;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.Page;

/** 
* @ClassName: UserInfoController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class UserInfoController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired 
	private UserInfoService service;

	@RequestMapping(value = "/userInfo/login")
	public ModelAndView login(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			UserDevice userDevice = new UserDevice();
			
			if(dataJson.containsKey("deviceToken")){
				userDevice.setDeviceToken(dataJson.getString("deviceToken"));
				dataJson.remove("deviceToken");
			}
			if(dataJson.containsKey("devicePlatform")){
				userDevice.setDevicePlatform(dataJson.getString("devicePlatform"));
				dataJson.remove("devicePlatform");
			}
			if(dataJson.containsKey("deviceVersion")){
				userDevice.setDeviceVersion(dataJson.getString("deviceVersion"));
				dataJson.remove("deviceVersion");
			}
			
			UserInfo  userInfo = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);	
			
			HashMap<?, ?> data = service.login(userInfo,userDevice);
		
			if(!data.isEmpty()){
				return new ModelAndView("jsonView", success("data"));
			}else{
				return new ModelAndView("jsonView",success("用户不存在"));
			}
	
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/userInfo/create")
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
			UserInfo  bean = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);	
			service.create(bean);			
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/userInfo/update")
	public ModelAndView update(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo  bean = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);
			service.update(bean);			
			return new ModelAndView("jsonView", success("修改成功"));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/userInfo/delete")
	public ModelAndView delete(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo  bean = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);
			service.delete(bean);			
			return new ModelAndView("jsonView", success("删除成功"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/userInfo/list")
	public ModelAndView list(HttpServletRequest request){
		try{			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int curPageNum= 1;//默认为第一页
			String curPage= request.getParameter("page");
			if (StringUtils.isNotEmpty(curPage)){
				curPageNum = Integer.parseInt(curPage);
			}
			UserInfo  bean = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);
			Page data = service.list(bean,curPageNum);			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/userInfo/query")
	public ModelAndView query(HttpServletRequest request){
		try{

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo  bean = (UserInfo)JSONObject.toBean(dataJson, UserInfo.class);
			UserInfo  userInfo = service.query(bean);
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("userId", userInfo.getUserId());
			data.put("userRealName", userInfo.getUserRealName());
			data.put("userNickName", userInfo.getUserNickName());
			data.put("userIcon", userInfo.getUserIcon());
			data.put("userEmail", userInfo.getUserEmail());
			data.put("userPhone", userInfo.getUserPhone());
			data.put("userLevel", userInfo.getUserLevel());
			data.put("userScore", userInfo.getUserScore());
			data.put("userGpsId", userInfo.getUserGpsid());
			
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取明细失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
