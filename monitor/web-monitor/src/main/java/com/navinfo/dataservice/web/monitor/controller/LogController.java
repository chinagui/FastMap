package com.navinfo.dataservice.web.monitor.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.service.monitor.model.FosLog;
import com.navinfo.dataservice.service.monitor.service.LogService;

import net.sf.json.JSONObject;

/** 
 * @ClassName: LogController
 * @author xiaoxiaowen4127
 * @date 2017年8月9日
 * @Description: LogController.java
 */
@Controller
public class LogController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@RequestMapping(value = "/log/hello")
	public ModelAndView hello(HttpServletRequest request){
		log.info("Hello,FastMap!");
		return new ModelAndView("jsonView", "data", "Hello,FastMap!你好，FastMap!");
	}
	@RequestMapping(value = "/log/getbyjobid")
	public ModelAndView getByJobId(HttpServletRequest request){
		try{
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(paraJson.get("jobId")==null){
				throw new IllegalArgumentException("jobId参数不能为空。");
			}
			int jobId = paraJson.getInt("jobId");
			FosLog log = LogService.getInstance().getByJobId(jobId);
			return new ModelAndView("jsonView", success(log));
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/log/getbyreqcode")
	public ModelAndView getByRequestCode(HttpServletRequest request){
		try{
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(paraJson.get("requestCode")==null){
				throw new IllegalArgumentException("requestCode参数不能为空。");
			}
			String rc = paraJson.getString("requestCode");
			
			FosLog log = LogService.getInstance().getByRequestCode(rc);
			
			return new ModelAndView("jsonView", success(log));
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
