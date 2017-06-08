package com.navinfo.dataservice.web.dealership.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;

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
	
	@RequestMapping(value = "/loadChainList")
	public ModelAndView queryDealerBrand(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			//默认的页码和每页数据设置为1，20
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
	
	@RequestMapping(value = "/loadDiffList")
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
	@RequestMapping(value = "/impTableDiff")
	public ModelAndView impTableDiff(HttpServletRequest request) {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token"); 
			dealerShipService.impTableDiff(request,tokenObj.getUserId());
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/expTableDiff")
	public void expTableDiff(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("octets/stream");
//		response.setHeader("Access-Control-Allow-Origin", "*");
//		response.setHeader("Access-Control-Allow-Methods",
//				"POST, GET, OPTIONS, DELETE,PUT"); 
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			
			List<ExpIxDealershipResult> dealerBrandList = dealerShipService.searchTableDiff(chainCode);

			ExportExcel<ExpIxDealershipResult> ex = new ExportExcel<ExpIxDealershipResult>();  
			
			String excelName = "表表差分结果"+DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			//转码防止乱码  
	        response.addHeader("Content-Disposition", "attachment;filename="+new String( excelName.getBytes("gb2312"), "ISO8859-1" )+".xls");  
	        
			String[] headers =  
		        { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址" ,
		        		"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编" , "厂商提供英文名称",
		        		"厂商提供英文地址", "旧一览表ID", "旧一览表省份" ,
		        		"旧一览表城市", "旧一览表项目", "旧一览表分类", "旧一览表品牌" , "旧一览表名称", "旧一览表简称", "旧一览表地址",
		        		"旧一览表电话（其他）" ,
		        		"旧一览表电话（销售）", "旧一览表电话（服务）", "旧一览表邮编", "旧一览表英文名称" , "旧一览表英文地址", 
		        		"新旧一览表差分结果"  };  
			
			try  
	        {  
	            OutputStream out = response.getOutputStream();  
	            		//new FileOutputStream("f://a.xls");  
	            ex.exportExcel("表表差分结果", headers, dealerBrandList, out, "yyyy-MM-dd");
//	            exportExcel(headers, dealerBrandList, out);  
	            out.close();  
//	            JOptionPane.showMessageDialog(null, "导出成功!");  
	            logger.error("excel导出成功！");  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	            logger.error(e.getMessage());
	        } catch (IOException e) {  
	            e.printStackTrace();  
	            logger.error(e.getMessage());
	        } 
			//return new ModelAndView("jsonView", success("excel导出成功！"));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			//return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/uploadChainExcel")
	public ModelAndView uploadChainExcel(HttpServletRequest request) {
		try {

			dealerShipService.uploadChainExcel(request);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 精编保存接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/dbDiff")
	public ModelAndView tableDbDiff(HttpServletRequest request)
			throws ServletException, IOException {
		
		try {
			String parameter = request.getParameter("parameter");
			JSONObject dataJson = JSONObject.fromObject(parameter);
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			
			
			long jobId=jobApi.createJob("DealershipTableAndDbDiffJob", dataJson, userId,0, "代理店库差分");
			
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
}