package com.navinfo.dataservice.web.edit.row.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.row.multisrc.MultiSrcFmSyncService;
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
	
	/**
	 * 日库多源数据包导入FM申请接口
	 * @author Han Shaoming
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/poi/multisrc/uploadApply")
	public ModelAndView run(HttpServletRequest request) throws Exception {
		try {
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
			if(!paraJson.containsKey("url")){
				throw new IllegalArgumentException("parameter参数中url不能为空。");
			}
			String zipUrl = paraJson.getString("url");
			String msg = MultiSrcFmSyncService.getInstance().applyUploadDay(userId,zipUrl);
			return new ModelAndView("jsonView", success(msg));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
