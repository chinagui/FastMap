package com.navinfo.dataservice.web.dealership.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;

import net.sf.json.JSONObject;

/**
 * 代理店业务类
 * @author jch
 *
 */
@Controller
public class DataPrepareController extends BaseController {
	private static final Logger logger = Logger.getLogger(DataPrepareController.class);
	private DataPrepareService dealerShipService = DataPrepareService.getInstance();
	
	@RequestMapping(value = "/dealership/loadChainList")
	public ModelAndView queryDealerBrand(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//默认的页码和每页数据设置为1,20
			int pageSize = 1;
			if(dataJson.containsKey("pageSize")){
				pageSize = dataJson.getInt("pageSize");
			}
			int pageNum = 20;
			if(dataJson.containsKey("pageNum")){
				pageNum = dataJson.getInt("pageNum");
			}
			
			int chainStatus = dataJson.getInt("chainStatus");
			List<Map<String, Object>> dealerBrandList = dealerShipService.queryDealerBrand(chainStatus, pageSize, pageNum);
			
			Map<String, Object> result = new HashMap<>();
			result.put("total", dealerBrandList.size());
			result.put("record", dealerBrandList);
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/dealership/loadDiffList")
	public ModelAndView loadDiffList(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			
			List<Map<String, Object>> dealerBrandList = dealerShipService.loadDiffList(chainCode);
			
			return new ModelAndView("jsonView", success(dealerBrandList));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 1.功能描述：表差分结果人工整理完毕后，上传入库
	 * 2.实现逻辑：
	 * 详见需求：一体化代理店业务需求-》表表差分结果导入
	 * 3.使用场景：
	 * 	1）代理店编辑平台-数据准备-表表差分
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dealership/impTableDiff")
	public ModelAndView impTableDiff(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			String upFile= dataJson.getString("upFile");
			
			dealerShipService.impTableDiff(chainCode,upFile);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/dealership/expTableDiff")
	public ModelAndView expTableDiff(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("image/jpeg;charset=GBK");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT"); 
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			
			List<Map<String, Object>> dealerBrandList = dealerShipService.expTableDiff(chainCode);
			
			//处理返回的imageBlob 数据
			InputStream in = imageBlob.getBinaryStream(); // 建立输出流
            int len = (int) imageBlob.length();
            byte[] data = new byte[len]; // 建立缓冲区
            in.read(data);
            in.close();
			response.getOutputStream().write(data);
			return new ModelAndView("jsonView", success(dealerBrandList));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
