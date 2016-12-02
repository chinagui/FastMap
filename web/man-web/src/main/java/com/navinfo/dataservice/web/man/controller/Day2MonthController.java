package com.navinfo.dataservice.web.man.controller;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.day2Month.Day2MonthService;
import com.navinfo.navicommons.database.Page;
@Controller
public class Day2MonthController extends BaseController {
	private Logger log=LoggerRepos.getLogger(getClass());

	/**
	 * a-2-2_生产节奏控制
	 * man_config表中，有confKey对应记录，则更新记录；没有则报失败，配置参数错误
	 * by zhangxiaoyi
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/day2Month/update")
	public ModelAndView update(HttpServletRequest request){
		try{
			AccessToken token=(AccessToken) request.getAttribute("token");
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			int confId=dataJson.getInt("confId");
			int status=dataJson.getInt("status");
			long userId=token.getUserId();
			Day2MonthService.getInstance().update(userId,confId,status);
			return new ModelAndView("jsonView",success("success"));
		}catch(Exception e){
			log.error("修改配置错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	@RequestMapping(value="/day2Month/list")
	public ModelAndView list(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			int curPageNum= 1;//默认为第一页
			if (dataJson.containsKey("pageNum")){
				curPageNum = dataJson.getInt("pageNum");
			}
			int curPageSize= 20;//默认为20条记录/页
			if (dataJson.containsKey("pageSize")){
				curPageSize = dataJson.getInt("pageSize");
			}
			JSONObject condition = new JSONObject();	
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			Page result=Day2MonthService.getInstance().list(condition,curPageNum,curPageSize);
			return new ModelAndView("jsonView",success(result));
		}catch(Exception e){
			log.error("查询列表错误", e);
			return new ModelAndView("jsonView",exception(e));
		}
	}

}
