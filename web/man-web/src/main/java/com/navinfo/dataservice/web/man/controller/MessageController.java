package com.navinfo.dataservice.web.man.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.message.MessageService;

import net.sf.json.JSONObject;

/** 
 * @ClassName: MessageController
 * @author songdongyan
 * @date 2016年9月14日
 * @Description: MessageController.java
 */
@Controller
public class MessageController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private MessageService service=MessageService.getInstance();


	@RequestMapping(value = "/message/list")
	public ModelAndView list(HttpServletRequest request) {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int status = dataJson.getInt("status");
			Map<String,Object> data = MessageService.getInstance().list(userId, status);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取城市列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

}
