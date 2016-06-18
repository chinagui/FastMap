package com.navinfo.dataservice.web.man.controller;

import java.io.BufferedReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.engine.man.layer.LayerService;

/**
 * @ClassName: CustomisedLayerController
 * @author code generator
 * @date 2016年4月6日 下午6:25:24
 * @Description: TODO
 */
@Controller
public class LayerController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired
	private LayerService service;

	@RequestMapping(value = "/layer/create")
	public ModelAndView create(HttpServletRequest request) {
		try {
			//AccessToken tokenObj = (AccessToken) request.getAttribute("token");

			StringBuffer sb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					sb.append(line);
			} catch (Exception e) {
				throw new IllegalArgumentException("读取POST参数失败。");
			}

			String data = sb.toString();
			
			JSONObject dataJson = JSONObject.fromObject(data);
			
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			//long userId = tokenObj.getUserId();
			long userId=2;
			//log.info(tokenObj);
			
			service.create(userId, dataJson.getString("layerName"),dataJson.getString("wkt"));
			return new ModelAndView("jsonView", success("创建成功"));
		} catch (Exception e) {
			log.error("创建失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/layer/update")
	public ModelAndView update(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.update(dataJson.getString("layerId"),
					dataJson.getString("wkt"));
			return new ModelAndView("jsonView", success("修改成功"));
		} catch (Exception e) {
			log.error("修改失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/layer/delete")
	public ModelAndView delete(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			service.delete(dataJson.getString("layerId"));
			return new ModelAndView("jsonView", success("删除成功"));
		} catch (Exception e) {
			log.error("删除失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	@RequestMapping(value = "/layer/listByWkt")
	public ModelAndView query(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request
					.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("param参数不能为空。");
			}
			List data = service.listByWkt(dataJson.getString("wkt"));
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			log.error("获取明细失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
