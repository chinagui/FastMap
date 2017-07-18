package com.navinfo.dataservice.web.edit.row.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.control.row.charge.RowChargeService;
import net.sf.json.JSONObject;

/**
 * FM到桩家数据转换
 * @ClassName RowChargeController
 * @author Han Shaoming
 * @date 2017年7月17日 下午6:47:10
 * @Description TODO
 */
@Controller
public class RowChargeController extends BaseController{

	protected Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * FM导入桩家库
	 * @author Han Shaoming
	 * @param request
	 * @return
	 * @throws Exception
	 * 参数:type 1:初始化,2:增量
	 * 		time 初始化传0,增量传具体时间"20170717200411"
	 */
	@RequestMapping(value = "/poi/charge/download")
	public ModelAndView run(HttpServletRequest request) throws Exception {
		try {
//			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
//			Long userId = tokenObj.getUserId();
//			Long userId = 0L;
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject paraJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (paraJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(!paraJson.containsKey("type")){
				throw new IllegalArgumentException("parameter参数中type不能为空。");
			}
			if(!paraJson.containsKey("time")){
				throw new IllegalArgumentException("parameter参数中time不能为空。");
			}
			String type = paraJson.getString("type");
			String time = paraJson.getString("time");
			JSONObject data = RowChargeService.getInstance().chargePoiConvertor(type,time);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
