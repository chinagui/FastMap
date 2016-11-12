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
import com.navinfo.dataservice.engine.man.message.MessageService;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
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

	//获取消息列表
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
	//查询消息
	@RequestMapping(value = "/message/query")
	public ModelAndView query(HttpServletRequest request) {
		try {
			//AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			//long userId = tokenObj.getUserId();
			
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			int msgId = dataJson.getInt("msgId");
			Map<String,Object> msg = MessageService.getInstance().query(msgId);
			
			return new ModelAndView("jsonView", success(msg));
		} catch (Exception e) {
			log.error("获取城市列表失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 根据申请人查询业务申请列表
	 * 消息中心-业务申请(全部角色)
	 * 根据access_token的用户返回该用户的所有申请，即apply_user_id=该用户 的记录
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/listByApplyUserId")
	public ModelAndView getApplyListByApplyUserId(HttpServletRequest request){
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
			Page page = service.getApplyListByApplyUserId(userId,pageNum,pageSize,condition);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 根据审核人查询业务申请列表
	 * 消息中心-业务申请（生管角色）
	 * 根据access_token的用户返回该用户的所有申请，即auditor =该用户 的记录
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/listByAuditor")
	public ModelAndView getApplyListByByAuditor(HttpServletRequest request){
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
			Page page = service.getApplyListByByAuditor(userId,pageNum,pageSize,condition);
			return new ModelAndView("jsonView", success(page));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 业务申请创建
	 * 消息中心-业务申请(全部角色)-新的申请
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/create")
	public ModelAndView createApply(HttpServletRequest request){
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
			if(!paraJson.containsKey("applyType")){
				throw new IllegalArgumentException("parameter参数中applyType不能为空。");
			}
			if(!paraJson.containsKey("severity")){
				throw new IllegalArgumentException("parameter参数中severity不能为空。");
			}
			String msg = service.createApply(userId,paraJson);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 业务申请修改
	 * 消息中心-业务申请(全部角色)-查看申请
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/update")
	public ModelAndView updateApply(HttpServletRequest request){
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
			if(!paraJson.containsKey("applyType")){
				throw new IllegalArgumentException("parameter参数中applyType不能为空。");
			}
			if(!paraJson.containsKey("severity")){
				throw new IllegalArgumentException("parameter参数中severity不能为空。");
			}
			String msg = service.updateApply(userId,paraJson);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 业务申请详细信息查看
	 * 消息中心-业务申请(全部角色)-查看申请
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/query")
	public ModelAndView queryApply(HttpServletRequest request){
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
			if(!paraJson.containsKey("applyId")){
				throw new IllegalArgumentException("parameter参数中applyId不能为空。");
			}
			long applyId = paraJson.getLong("applyId");
			Map<String, Object> msg = service.queryApply(userId,applyId);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("查询失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 批量修改申请删除状态
	 * 消息中心-服务消息(全部角色)
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/updateDeleteFlag")
	public ModelAndView updateDeleteFlag(HttpServletRequest request){
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
			if(!paraJson.containsKey("applyId")){
				throw new IllegalArgumentException("parameter参数中applyId不能为空。");
			}
			if(!paraJson.containsKey("deleteFlag")){
				throw new IllegalArgumentException("parameter参数中deleteFlag不能为空。");
			}
			JSONArray applyIds = paraJson.getJSONArray("applyId");
			long deleteFlag = paraJson.getLong("deleteFlag");
			
			String msg = service.updateDeleteFlag(userId,applyIds,deleteFlag);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * 批量修改申请状态
	 * 消息中心-业务申请(全部角色)/业务审核(生管角色)
	 * @author Han Shaoming
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/apply/updateApplyStatus")
	public ModelAndView updateApplyStatus(HttpServletRequest request){
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
			if(!paraJson.containsKey("applyId")){
				throw new IllegalArgumentException("parameter参数中applyId不能为空。");
			}
			if(!paraJson.containsKey("applyStatus")){
				throw new IllegalArgumentException("parameter参数中deleteFlag不能为空。");
			}
			String msg = service.updateApplyStatus(userId,paraJson);
			return new ModelAndView("jsonView", success(msg));
		}catch(Exception e){
			log.error("修改失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
}
