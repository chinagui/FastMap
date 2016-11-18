package com.navinfo.dataservice.web.man.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.role.RoleService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName RoleController
 * @author Han Shaoming
 * @date 2016年11月12日 上午11:27:03
 * @Description TODO
 */
@Controller
public class RoleController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	/**
	 * 根据角色查询用户列表
	 * 消息中心-业务申请(全部角色)-新的申请/查看申请
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/role/list")
	public ModelAndView queryUserNameByRoleId(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("roleId")){
				throw new IllegalArgumentException("parameter参数中roleId不能为空。");
			}
			JSONObject conditionJson=new JSONObject();
			if(paraJson.containsKey("condition")){
				conditionJson=paraJson.getJSONObject("condition");
			}
			long roleId = paraJson.getLong("roleId");
			List<Map<String,Object>> userNameList = RoleService.getInstance().queryUserNameByRoleId(userId,roleId,conditionJson);
			return new ModelAndView("jsonView", success(userNameList));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
