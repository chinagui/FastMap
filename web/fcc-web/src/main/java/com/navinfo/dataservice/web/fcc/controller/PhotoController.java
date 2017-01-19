package com.navinfo.dataservice.web.fcc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.photo.HBaseController;
import com.navinfo.dataservice.engine.photo.PhotoGetter;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.navinfo.dataservice.engine.dropbox.dao.DBController;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;

@Controller
public class PhotoController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(PhotoController.class);

	@RequestMapping(value = "/photo/getBySpatial")
	public ModelAndView getBySpatial(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String wkt = jsonReq.getString("wkt");

			PhotoGetter getter = new PhotoGetter();

			JSONArray array = getter.getPhotoBySpatial(wkt);
			
			return new ModelAndView("jsonView", success(array));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/photo/getDetailByRowkey")
	public ModelAndView getDetailByRowkey(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String uuid = jsonReq.getString("rowkey");

			PhotoGetter getter = new PhotoGetter();

			JSONObject data = getter.getPhotoDetailByRowkey(uuid);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/photo/getSnapshotByRowkey")
	public void getSnapshotByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg;charset=GBK");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT");

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String uuid = jsonReq.getString("rowkey");

			String type = jsonReq.getString("type");

			PhotoGetter getter = new PhotoGetter();

			byte[] data = getter.getPhotoByRowkey(uuid, type);

			response.getOutputStream().write(data);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}

	}
	
	@RequestMapping(value = "/photo/getPhotosByRowkey")
	public ModelAndView getPhotosByRowkey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

//		response.setContentType("image/jpeg;charset=GBK");
//
//		response.setHeader("Access-Control-Allow-Origin", "*");
//		response.setHeader("Access-Control-Allow-Methods",
//				"POST, GET, OPTIONS, DELETE,PUT");
		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			JSONArray rowkeys = jsonReq.getJSONArray("rowkeys");
			PhotoGetter getter = new PhotoGetter();
			List<Map<String, Object>> data = getter.getPhotosByRowkey(rowkeys);
			//response.getOutputStream().write(data.toString().getBytes());
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
//			response.getWriter().println(
//					ResponseUtils.assembleFailResult(e.getMessage()));
		}

	}
	/**
	 * 深度信息全貌照片设置
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/photo/deep/setPhoto")
	public ModelAndView setDeepPhoto(HttpServletRequest request) throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		logger.debug("深度信息设置全貌照片");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			logger.debug("parameter="+jsonReq);
			
			JSONObject result = new JSONObject();
			int flag = jsonReq.getInt("flag");
			String oldFccPid = jsonReq.getString("oldFccPid");
			String newFccPid = jsonReq.getString("newFccPid");
			
			PhotoGetter getter = new PhotoGetter();
			
			//新增或修改全貌照片
			if(flag==1){
				//获取照片
				byte[]bytes = getter.getPhotoByRowkey(newFccPid, "origin");
				//设置全貌
				byte[] photo=FileUtils.makeFullViewImage(bytes); 
				//上传全貌照片
				HBaseController hbaseController = new HBaseController();
				InputStream newIn = new ByteInputStream(photo, photo.length);
				//调用hadoop方法传输文件流，获取photo_id
				String photoId = hbaseController.putPhoto(newIn);
				
				result.put("PID", photoId);
			}else{
				//删除全景照片
				//TODO
			}
			
			return new ModelAndView("jsonView", success(result));
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			return new ModelAndView("jsonView",fail(e.getMessage()));
		}
	}
}
