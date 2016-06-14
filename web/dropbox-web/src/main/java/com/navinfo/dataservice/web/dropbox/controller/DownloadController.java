package com.navinfo.dataservice.web.dropbox.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.engine.dropbox.manger.DownloadManager;

@Controller
public class DownloadController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(DownloadController.class);

	@RequestMapping(value = "/download/prjbasedata")
	public ModelAndView getBasedata(HttpServletRequest request) {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String prjId = json.getString("prjId");

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getBasedata(prjId);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/download/fmgdblist")
	public ModelAndView getBasedataList(HttpServletRequest request)
			throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONArray data = manager.getBasedataList();

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/download/fmndslist")
	public ModelAndView getNdsList(HttpServletRequest request)
			throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONArray data = manager.getNdsList();

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/download/fmnds")
	public ModelAndView getNds(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String id = json.getString("id");

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getNds(id);

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/download/patternimage")
	public ModelAndView getPatternimg(HttpServletRequest request)
			throws ServletException, IOException {

		try {

			DownloadManager manager = new DownloadManager();

			JSONObject data = manager.getPatternimg();

			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}
}
