package com.navinfo.dataservice.web.edit.row.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.row.pointaddress.PointAddressSave;

import net.sf.json.JSONObject;

/**
 * @Title:IndexController
 * @Package:com.navinfo.dataservice.web.edit.row.controller
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年9月29日
 */
@Controller
public class IndexController extends BaseController {

	private static final Logger logger = Logger.getLogger(PointAddressController.class);
	
	
	/**
	 * 点门牌保存接口
	 * 
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/index/rowDataSave")
	public ModelAndView poiSave(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		try {
			if (StringUtils.isEmpty(parameter)) {
				return new ModelAndView("jsonView", fail("parameter参数不能为空"));
			}
			
			JSONObject result = PointAddressSave.getInstance().save(parameter, tokenObj.getUserId());

			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}