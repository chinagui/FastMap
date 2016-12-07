package com.navinfo.dataservice.web.sys.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.sys.msg.SysMsg;
import com.navinfo.dataservice.engine.sys.msg.SysMsgService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SysMsgController
 * @author Han Shaoming
 * @date 2016年10月27日 下午6:25:06
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
	 * 查看未读消息详情
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
			List<SysMsg> sysMsgDetail = SysMsgService.getInstance().selectSysMsgDetail(msgId);
			return new ModelAndView("jsonView", success(sysMsgDetail));
		}catch(Exception e){
			log.error("更改消息状态失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 查看已读消息详情
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sysmsg/readDetail/check")
	public ModelAndView checkReadMsgDetail(HttpServletRequest request){
		try{
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(!paraJson.containsKey("msgId")){
				throw new IllegalArgumentException("msgId参数不能为空。");
			}
			long msgId = paraJson.getLong("msgId");
			List<SysMsg> sysMsgDetail = SysMsgService.getInstance().selectSysMsgDetail(msgId);
			return new ModelAndView("jsonView", success(sysMsgDetail));
		}catch(Exception e){
			log.error("查询已读消息详情失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 删除消息
	 * @param request
	 * @return
	 */
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
	
	/**
	 * 查询man管理消息的title
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/message/TitleList")
	public ModelAndView getManMsgTitleList(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			List<String> titleList = SysMsgService.getInstance().getManMsgTitleList(userId);
			return new ModelAndView("jsonView", success(titleList));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 获取man管理消息列表
	 * 消息中心-服务消息（全部角色）
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/message/list")
	public ModelAndView getManMsgList(HttpServletRequest request){
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
			if(!paraJson.containsKey("pageNum")){
				throw new IllegalArgumentException("parameter参数中pageNum不能为空。");
			}
			if(!paraJson.containsKey("pageSize")){
				throw new IllegalArgumentException("parameter参数中pageSize不能为空。");
			}
			if(!paraJson.containsKey("condition")){
				throw new IllegalArgumentException("parameter参数中condition不能为空。");
			}
			int pageNum = paraJson.getInt("pageNum");
			int pageSize = paraJson.getInt("pageSize");
			String condition = paraJson.getString("condition");
			Page page = SysMsgService.getInstance().getManMsgList(userId,pageNum,pageSize,condition);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 批量修改管理信息状态
	 * 消息中心-服务消息（全部角色）
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/message/update")
	public ModelAndView updateManMsg(HttpServletRequest request){
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
			if(!paraJson.containsKey("msgId")){
				throw new IllegalArgumentException("parameter参数中msgId不能为空。");
			}
			if(!paraJson.containsKey("msgStatus")){
				throw new IllegalArgumentException("parameter参数中msgStatus不能为空。");
			}
			JSONArray msgIds = paraJson.getJSONArray("msgId");
			int msgStatus = paraJson.getInt("msgStatus");
			String msg = SysMsgService.getInstance().updateManMsg(userId,msgStatus,msgIds);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("编辑失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 查询未读管理消息列表
	 * 消息中心-服务消息（全部角色）
	 * 返回targetUserId等于当前用户的（即接收人是当前用户）所有未读消息列表和总数
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/message/listUnOperate")
	public ModelAndView getUnAuditapply(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			List<Map<String, Object>> msg = SysMsgService.getInstance().getUnUnOperateMsg(userId);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 全部5天以上的系统/管理消息查询列表
	 * 编辑平台-消息中心
	 * 根据token获取 接收人targetUserId=登陆用户的全部5天以上的系统/管理消息查询列表，不分页
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sysmsg/listHistory")
	public ModelAndView getAllMsgHistory(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			Long userId = tokenObj.getUserId();
			String condition ="{\"isHistory\":2}";
			Map<String, List<Map<String, Object>>> msgs = SysMsgService.getInstance().getAllMsg(userId,condition);
			return new ModelAndView("jsonView", success(msgs));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 全部5天内的系统/管理消息查询列表
	 * 编辑平台-消息中心
	 * 根据token获取 接收人targetUserId=登陆用户的全部5天内的系统/管理消息查询列表，不分页
	 * @author 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sysmsg/listAll")
	public ModelAndView getAllMsg(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			Long userId = tokenObj.getUserId();
			String condition ="{\"isHistory\":1,\"msgType\":1}";
			Map<String, List<Map<String, Object>>> sysMsgs = SysMsgService.getInstance().getAllMsg(userId,condition);
			condition ="{\"isHistory\":1,\"msgType\":2}";
			Map<String, List<Map<String, Object>>> manageMsgs = SysMsgService.getInstance().getAllMsg(userId,condition);
			Map<String,Map<String, List<Map<String, Object>>>> msgs=new HashMap<String, Map<String,List<Map<String,Object>>>>();
			msgs.put("sys", sysMsgs);
			msgs.put("manage", manageMsgs);
			return new ModelAndView("jsonView", success(msgs));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 全部5天以上的系统/管理消息查询列表
	 * 编辑平台-消息中心-历史消息
	 * 根据token获取 接收人targetUserId=登陆用户的全部5天以上的系统/管理消息查询列表，不分页
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	/*@RequestMapping(value = "/sysmsg/listHistory")
	public ModelAndView getDeleteMsgList(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			Long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("pageNum")){
				throw new IllegalArgumentException("parameter参数中pageNum不能为空。");
			}
			if(!paraJson.containsKey("pageSize")){
				throw new IllegalArgumentException("parameter参数中pageSize不能为空。");
			}
			int pageNum = paraJson.getInt("pageNum");
			int pageSize = paraJson.getInt("pageSize");
			Page page = SysMsgService.getInstance().getDeleteMsgList(userId,pageNum,pageSize);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}*/
	
	
	/**
	 * 测试发消息的临时接口
	 */
	@RequestMapping(value = "/sysmsg/sendMessage")
	public ModelAndView sendMessage(HttpServletRequest request){
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(!paraJson.containsKey("msgTitle")){
				throw new IllegalArgumentException("msgTitle参数不能为空。");
			}
			String msgTitle = paraJson.getString("msgTitle");
			if(!paraJson.containsKey("msgContent")){
				throw new IllegalArgumentException("msgContent参数不能为空。");
			}
			String msgContent = paraJson.getString("msgContent");
			if(!paraJson.containsKey("targetUserIds")){
				throw new IllegalArgumentException("targetUserIds参数不能为空。");
			}
			String str = paraJson.getString("targetUserIds");
			String[] split = str.split(",");
			int count = split.length;
			long[] targetUserIds = new long[count];
			for (int i=0;i<count;i++) {
				targetUserIds[i]=Long.valueOf(split[i]);
			}
			SysMsgPublisher.publishMsg(msgTitle, msgContent, userId, targetUserIds, 1, null, null);
			return new ModelAndView("jsonView", success("消息发送成功!"));
		}catch(Exception e){
			log.error("发送失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
}
