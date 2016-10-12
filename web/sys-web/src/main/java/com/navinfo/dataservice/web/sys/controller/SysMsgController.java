package com.navinfo.dataservice.web.sys.controller;

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
 * 
 * @ClassName SysMsgController
 * @author Han Shaoming
 * @date 2016年9月22日 上午8:08:24
 * @Description TODO
 */
@Controller
public class SysMsgController extends BaseController {
protected Logger log = Logger.getLogger(this.getClass());
	/**
	 * 查询所有的未读消息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sysmsg/unread/get")
	public ModelAndView getUnread(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			Long userId = tokenObj.getUserId();
			List<SysMsg> msgs = SysMsgService.getInstance().getUnread(userId);
			return new ModelAndView("jsonView", success(msgs));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 查询已读消息
	 * @param request
	 * @return
	 */
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
			Page page = SysMsgService.getInstance().getReadMsg(userId,pageNum,pageSize);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 查看消息详情
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sysmsg/detail/check")
	public ModelAndView checkMsgDetail(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(!paraJson.containsKey("msgId")){
				throw new IllegalArgumentException("msgId参数不能为空。");
			}
			long msgId = paraJson.getLong("msgId");
			SysMsgService.getInstance().updateMsgStatusToRead(msgId, userId);
			return new ModelAndView("jsonView", success("消息查看成功!"));
		}catch(Exception e){
			log.error("更改消息状态失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/sysmsg/message/delete")
	public ModelAndView deleteMsg(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(!paraJson.containsKey("msgId")){
				throw new IllegalArgumentException("msgId参数不能为空。");
			}
			long msgId = paraJson.getLong("msgId");
			SysMsgService.getInstance().deleteMsg(msgId, userId);
			return new ModelAndView("jsonView", success("消息删除成功!"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}

}
