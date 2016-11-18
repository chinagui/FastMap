package com.navinfo.dataservice.web.edit.row.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.row.save.PoiSave;

import net.sf.json.JSONObject;

/** 
 * @ClassName: RowMultiSrcController
 * @author xiaoxiaowen4127
 * @date 2016年11月15日
 * @Description: RowMultiSrcController.java
 */
@Controller
public class RowMultiSrcController extends BaseController {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@RequestMapping(value = "/poi/multisrc/uploadApply")
	public ModelAndView run(HttpServletRequest request) throws Exception {
		
		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			//
			return new ModelAndView("jsonView", success(null));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
