package com.navinfo.dataservice.web.limit.controller;


import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.limit.glm.model.limit.man.UserInfo;
import com.navinfo.dataservice.engine.limit.user.userinfo.UserInfoService;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


@Controller
public class UserInfoController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	// @Autowired
	// private UserInfoService service;

	@RequestMapping(value = "/userInfo/login")
	public ModelAndView login(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			UserInfo userInfo = (UserInfo) JSONObject.toBean(dataJson, UserInfo.class);

			HashMap<?, ?> data = UserInfoService.getInstance().login(userInfo);

			if (!data.isEmpty()) {
				return new ModelAndView("jsonView", success(data));
			} else {
				return new ModelAndView("jsonView", exception("用户名或密码错误"));
			}

		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson, UserInfo.class);
			UserInfoService.getInstance().create(bean);
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (dataJson.isEmpty()) {
				return new ModelAndView("jsonView", success("无可修改内容"));
			}

			int userId = (int) tokenObj.getUserId();
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson, UserInfo.class);

			bean.setUserId(userId);

			UserInfoService.getInstance().update(bean);
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/delete")
	public ModelAndView delete(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson, UserInfo.class);
			UserInfoService.getInstance().delete(bean);
			return new ModelAndView("jsonView", success("删除成功"));
		} catch (Exception e) {
			log.error("删除失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}


	@RequestMapping(value = "/userInfo/query")
	public ModelAndView query(HttpServletRequest request) {
		try {

			UserInfo bean = null;

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (!JSONUtils.isNull(dataJson)) {

				if (dataJson.isEmpty()) {
					return new ModelAndView("jsonView", success("无请求信息"));
				}
				bean = (UserInfo) JSONObject.toBean(dataJson, UserInfo.class);
			} else {
				AccessToken tokenObj = (AccessToken) request.getAttribute("token");

				long userId = tokenObj.getUserId();

				bean = new UserInfo();

				bean.setUserId((int) userId);
			}

			UserInfo userInfo = UserInfoService.getInstance().query(bean);

			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("userId", userInfo.getUserId());
			data.put("userRealName", userInfo.getUserRealName());
			data.put("userNickName", userInfo.getUserNickName());

			data.put("userEmail", userInfo.getUserEmail());
			data.put("userPhone", userInfo.getUserPhone());

			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
