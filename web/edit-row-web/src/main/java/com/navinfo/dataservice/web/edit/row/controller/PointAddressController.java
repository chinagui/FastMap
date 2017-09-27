package com.navinfo.dataservice.web.edit.row.controller;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.row.pointaddress.PointAddressService;

import net.sf.json.JSONObject;

/**
 * @Title: PointAddressController
 * @Package: com.navinfo.dataservice.web.edit.row.controller
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月26日
 * @Version: V1.0
 */
@Controller
public class PointAddressController extends BaseController {

	private static final Logger logger = Logger.getLogger(PointAddressController.class);
	private PointAddressService pointAddressService = PointAddressService.getInstance();

	/**
	 * 获取点门牌列表
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/pointaddress/base/list")
	public ModelAndView getPointAddressList(HttpServletRequest request) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		JSONObject jsonReq = JSONObject.fromObject(parameter);
		try {
			if (StringUtils.isEmpty(parameter) || jsonReq == null || jsonReq.isNullObject()) {
				return new ModelAndView("jsonView", fail("parameter参数不能为空"));
			}
			if (jsonReq.has("status")) {
				int status = jsonReq.getInt("status");
				if (!(Arrays.asList(1, 2, 3).contains(status))) {
					return new ModelAndView("jsonView", fail("status参数值域不在[1,2,3]内"));
				}
			}
			JSONObject result = pointAddressService.getPointAddressList(parameter);
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
