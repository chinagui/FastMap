package com.navinfo.dataservice.web.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.sys.msg.SysMsg;
import com.navinfo.dataservice.engine.sys.msg.SysMsgService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONObject;

/** 
 * @ClassName: SysMsgController
 * @author xiaoxiaowen4127
 * @date 2016年9月8日
 * @Description: SysMsgController.java
 */
@Controller
public class SysMsgController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@RequestMapping(value = "/sysmsg/unread/get")
	public ModelAndView getUnread(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			List<SysMsg> msgs = SysMsgService.getInstance().getUnread(userId);
			return new ModelAndView("jsonView", success(msgs));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value = "/sysmsg/read/get")
	public ModelAndView getRead(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			//check
			if(!paraJson.containsKey("pageNum"))throw new IllegalArgumentException("parameter参数中pageNum不能为空。");
			if(!paraJson.containsKey("pageSize"))throw new IllegalArgumentException("parameter参数中pageSize不能为空。");
			int pageNum = paraJson.getInt("pageNum");
			int pageSize = paraJson.getInt("pageSize");
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			Page page = SysMsgService.getInstance().getRead(userId,pageNum,pageSize);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
}
