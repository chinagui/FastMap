package com.navinfo.dataservice.web.fcc.photo.mass.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.photo.mass.TipsPhotoGetter;

import net.sf.json.JSONObject;

@Controller
public class TipsPhotoController extends BaseController {
	private static final Logger logger = Logger.getLogger(PoiPhotoController.class);
	
	//Tips三代查历史
	 @RequestMapping(value = "/tips/getFmTipsHisPhoto")
	 public ModelAndView getTipsPhoto(HttpServletRequest request,HttpServletResponse response
	    ) throws ServletException, IOException {
		 
		 String parameter = request.getParameter("parameter");	
		 JSONObject jsonReq = JSONObject.fromObject(parameter);
	     String rowkey = jsonReq.getJSONArray("rowkey").toString();
		 TipsPhotoGetter tipsPhoto = new TipsPhotoGetter();
		 
		 //根据输入的rowkey获取到该tips对应的三代历史照片
		 
		 try {
			 List<Map<String, Object>> tipsHisPhoto4Fm = tipsPhoto.getTipsHisPhotoByRowkey(rowkey); 				
				return new ModelAndView("jsonView", success(tipsHisPhoto4Fm));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return new ModelAndView("jsonView", fail(e.getMessage()));
			}	
		 
	 }
	 
	 
	//供mark的二代查历史调用
	 @RequestMapping(value = "/tips/getMarkHisPhoto")
	 public void getMarkHisPhoto(HttpServletRequest request,HttpServletResponse response
	    ) throws ServletException, IOException {
		 
		 response.setContentType("image/jpeg;charset=GBK");

			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods",
					"POST, GET, OPTIONS, DELETE,PUT");

			String parameter = request.getParameter("parameter");

			try {
				JSONObject jsonReq = JSONObject.fromObject(parameter);

				String uuid = jsonReq.getString("rowkey");

				String type = jsonReq.getString("type");
				
				TipsPhotoGetter getter = new TipsPhotoGetter();

				byte[] data = getter.getMarkHisPhotoByRowkey(uuid, type);

				response.getOutputStream().write(data);

			} catch (Exception e) {

				logger.error(e.getMessage(), e);

				response.getWriter().println(
						ResponseUtils.assembleFailResult(e.getMessage()));
			}
		 
	 }
	 
}
