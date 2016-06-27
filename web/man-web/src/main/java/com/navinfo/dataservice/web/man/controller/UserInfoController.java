package com.navinfo.dataservice.web.man.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.man.model.UserDevice;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;

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

			UserDevice userDevice = new UserDevice();

			if (dataJson.containsKey("deviceToken")
					&& dataJson.containsKey("devicePlatform")
					&& dataJson.containsKey("deviceVersion")) {
				userDevice.setDeviceToken(dataJson.getString("deviceToken"));
				userDevice.setDevicePlatform(dataJson
						.getString("devicePlatform"));
				userDevice
						.setDeviceVersion(dataJson.getString("deviceVersion"));
				dataJson.remove("deviceToken");
				dataJson.remove("devicePlatform");
				dataJson.remove("deviceVersion");
			}

			UserInfo userInfo = (UserInfo) JSONObject.toBean(dataJson,
					UserInfo.class);

			HashMap<?, ?> data = service.login(userInfo, userDevice);

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
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson,
					UserInfo.class);
			service.create(bean);
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

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (dataJson.isEmpty()) {
				return new ModelAndView("jsonView", success("无可修改内容"));
			}

			int userId = (int) tokenObj.getUserId();
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson,
					UserInfo.class);

			bean.setUserId(userId);

			service.update(bean);
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/delete")
	public ModelAndView delete(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			UserInfo bean = (UserInfo) JSONObject.toBean(dataJson,
					UserInfo.class);
			service.delete(bean);
			return new ModelAndView("jsonView", success("删除成功"));
		} catch (Exception e) {
			log.error("删除失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/list")
	public ModelAndView list(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if (dataJson.isEmpty()) {
				return new ModelAndView("jsonView", success("无请求信息"));
			}
			Integer group_Id = Integer.valueOf(dataJson.getString("groupId"));

			UserGroup bean = new UserGroup();
			bean.setGroupId(group_Id);

			List<UserInfo> userInfoList = service.list(bean);
			HashMap<String, Object> data = new HashMap<String, Object>();
			Integer userSize = userInfoList.size();
			data.put("total", userSize);
			data.put("rows", userInfoList);
			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/userInfo/query")
	public ModelAndView query(HttpServletRequest request) {
		try {

			UserInfo bean = null;

			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (!JSONUtils.isNull(dataJson)) {

				if (dataJson.isEmpty()) {
					return new ModelAndView("jsonView", success("无请求信息"));
				}
				bean = (UserInfo) JSONObject.toBean(dataJson,
						UserInfo.class);
			}
			else{
				AccessToken tokenObj = (AccessToken) request.getAttribute("token");
				
				long userId = tokenObj.getUserId();
				
				bean = new UserInfo();
				
				bean.setUserId((int) userId);
			}
			
			UserInfo userInfo = service.query(bean);

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
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/user/getUploadTime")
	public ModelAndView getUploadTime(HttpServletRequest request) {
		try {

			String tokenString = request.getParameter("access_token");
			String deviceId = request.getParameter("deviceId");
			if (tokenString == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken token = AccessTokenFactory.validate(tokenString);
			Integer use_id = (int) token.getUserId();
			// ------------

			UserInfo bean = new UserInfo();
			bean.setUserId(use_id);

			List<String> poi_tips_time = service.getUploadTime(bean, deviceId);
			HashMap<String, String> data = new HashMap<String, String>();
			String poi_time = "";
			String tips_time = "";
			if (poi_tips_time.size() > 0) {
				poi_time = poi_tips_time.get(0);
				tips_time = poi_tips_time.get(1);
			}
			data.put("poi", poi_time);
			data.put("tips", tips_time);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {
			log.error("获取列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
