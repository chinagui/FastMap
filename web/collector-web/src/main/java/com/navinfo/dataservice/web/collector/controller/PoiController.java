package com.navinfo.dataservice.web.collector.controller;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;

import net.sf.json.JSONObject;

/** 
 * @ClassName: DataHubController 
 * @author Xiao Xiaowen 
 * @date 2015-11-27 上午11:44:30 
 * @Description: TODO
 */
@Controller
public class PoiController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());

	@RequestMapping(value = "/poi/help")
	public ModelAndView getDb(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "Hello,Datahub.你好，数据中心！");
	}
	
	@RequestMapping(value = "/poi/upload")
	public ModelAndView createDb(HttpServletRequest request){
		try{
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(paraJson.get("jobId")==null){
				throw new IllegalArgumentException("jobId参数不能为空。");
			}
			int jobId = paraJson.getInt("jobId");

			int subtaskId = 0;
			if(paraJson.containsKey("subtaskId")){
				subtaskId = paraJson.getInt("subtaskId");
			}

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			Long userId = tokenObj.getUserId();
//			JSONObject res=PoiService.getInstance().importPoi(jobId, subtaskId,userId);
			return new ModelAndView("jsonView", success(null));
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	public static void main(String[] args){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dbName", "1");
		map.put("dbPasswd", "2");
		map.put("serverIp", "3");
		map.put("serverPort", "4");
		map.put("serverType", "5");
		//new ModelAndView("jsonView", new BaseController().success(map));

    	System.out.println("success");
	}
}
