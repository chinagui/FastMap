package com.navinfo.dataservice.web.dealership.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.navinfo.dataservice.control.dealership.service.excelModel.exportWorkResultEntity;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpDbDiffResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;

import net.sf.json.JSONArray;
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
			
			List<Map<String, Object>> dealerBrandList = new ArrayList<>();
			int chainStatus = -1;
			if(dataJson.containsKey("chainStatus")){
				chainStatus = dataJson.getInt("chainStatus");
			}
			
			dealerBrandList = dealerShipService.queryDealerBrand(chainStatus, pageSize, pageNum);
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
			JSONObject jobReq=new JSONObject();
			List<String> chainCodeList=new ArrayList<String>();
			chainCodeList.add(dataJson.getString("chainCode"));
			jobReq.put("chainCodeList", chainCodeList);
			jobReq.put("sourceType", dataJson.getInt("sourceType"));
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			
			long jobId=jobApi.createJob("DealershipTableAndDbDiffJob", jobReq, userId,0, "代理店库差分");
			
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	//客户/外业确认列表接口
	@RequestMapping(value = "/cofirmDataList")
	public ModelAndView cofirmDataList(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("type")){
				throw new Exception("type不能为空");
			}
			
			if(!dataJson.containsKey("cfmStatus")){
				throw new Exception("cfmStatus不能为空");
			}
			
			List<Map<String, Object>> cofirmDataList = dealerShipService.cofirmDataList(dataJson);
			
			return new ModelAndView("jsonView", success(cofirmDataList));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//重启品牌
	@RequestMapping(value = "/openChain")
	public ModelAndView openChain(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			
			if(!dataJson.containsKey("chainCode")){
				throw new Exception("chainCode不能为空");
			}
			List<String> chainList = dataJson.getJSONArray("chainCode");
			for(String chainCode: chainList){
				dealerShipService.openChain(chainCode);
			}
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("开启失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	//作业成果导出
	@RequestMapping(value = "/exportWorkResult")
	public void exportWorkResult(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("octets/stream");
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONArray chains = dataJson.getJSONArray("chains");
			if(chains.size() < 1){
				throw new Exception("chainCode参数不能为空。");
			}
			Map<String, Object> resultMap = dealerShipService.exportWorkResulttList(chains);
			if(resultMap == null){
				throw new Exception("对应chain查询结果为空");
			}
			
			String excelName = resultMap.get("excelName").toString();
			String [] excelTitle = (String[]) resultMap.get("title");
			List<exportWorkResultEntity> exportWorkResultList = (List<exportWorkResultEntity>) resultMap.get("exportWorkResultList");
			ExportExcel<exportWorkResultEntity> ex = new ExportExcel<exportWorkResultEntity>();  
			//转码防止乱码  
	        response.addHeader("Content-Disposition", "attachment;filename="+new String( excelName.getBytes("gb2312"), "ISO8859-1" )+".xls");  
			
			try{  
	            OutputStream out = response.getOutputStream();  
	            ex.exportExcel(excelName, excelTitle, exportWorkResultList, out, "yyyy-MM-dd");
	            out.close();  
	            logger.error("作业成果导出列表excel导出成功！");  
	        }catch(FileNotFoundException e) {  
	            logger.error(e.getMessage());
	            throw e;
	        }catch(IOException e) {  
	            logger.error(e.getMessage());
	            throw e;
	        } 
		}catch(Exception e){
			logger.error("导出失败，原因：" + e.getMessage(), e);
		}
	}
	
	
	
	@RequestMapping(value = "/expDbDiff")
	public void expDbDiff(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("octets/stream");
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			
			List<ExpDbDiffResult> dealerBrandList = dealerShipService.searchDbDiff(chainCode);

			ExportExcel<ExpDbDiffResult> ex = new ExportExcel<ExpDbDiffResult>();  
			
			String excelName = "表库差分结果"+DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			//转码防止乱码  
	        response.addHeader("Content-Disposition", "attachment;filename="+new String( excelName.getBytes("gb2312"), "ISO8859-1" )+".xls");  
	        
			String[] headers =  
		        { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌",
		        		"厂商提供名称", "厂商提供简称", "厂商提供地址" ,"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编" , "厂商提供英文名称","厂商提供英文地址",
		        		"旧一览表ID", "旧一览表省份" ,"旧一览表城市", "旧一览表项目", "旧一览表分类", "旧一览表品牌" , "旧一览表名称", "旧一览表简称", "旧一览表地址",
		        		"旧一览表电话（其他）" ,"旧一览表电话（销售）", "旧一览表电话（服务）", "旧一览表邮编", "旧一览表英文名称" , "旧一览表英文地址", 
		        		"新旧一览表差分结果","表库差分结果","与POI的匹配方式",
		        		"POI1_NUM","POI1_名称" ,"POI1_别名","POI1_分类","POI1_CHAIN","POI1_地址","POI1_电话","POI1_邮编","POI1_差分结果",
		        		"POI2_NUM","POI2_名称" ,"POI2_别名","POI2_分类","POI2_CHAIN","POI2_地址","POI2_电话","POI2_邮编","POI2_差分结果",
		        		"POI3_NUM","POI3_名称" ,"POI3_别名","POI3_分类","POI3_CHAIN","POI3_地址","POI3_电话","POI3_邮编","POI3_差分结果",
		        		"POI4_NUM","POI4_名称" ,"POI4_别名","POI4_分类","POI4_CHAIN","POI4_地址","POI4_电话","POI4_邮编","POI4_差分结果",
		        		"POI5_NUM","POI5_名称" ,"POI5_别名","POI5_分类","POI5_CHAIN","POI5_地址","POI5_电话","POI5_邮编","POI5_差分结果",
		        		//代理店 - 表库差分结果导出原则变更(6882)
		        		"CFM_POI_NUM","CFM_POI_NUM_名称","CFM_POI_NUM_别名","CFM_POI_NUM_分类","CFM_POI_NUM_CHAIN","CFM_POI_NUM_地址","CFM_POI_NUM_电话","CFM_POI_NUM_邮编","CFM_POI_NUM_差分结果",
		        		"POI是否采纳","匹配度","代理店确认时间","大区ID"};
			
			try  
	        {  
	            OutputStream out = response.getOutputStream();  
	            ex.exportExcel("表库差分结果", headers, dealerBrandList, out, "yyyy-MM-dd");
	            out.close();  
	            logger.error("excel导出成功！");  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	            logger.error(e.getMessage());
	        } catch (IOException e) {  
	            e.printStackTrace();  
	            logger.error(e.getMessage());
	        } 
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
		}
	}
	

	@RequestMapping(value = "/exportToClient")
	public void exportToClient(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("octets/stream");
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			
			List<ExpClientConfirmResult> clientConfirmResultList = dealerShipService.expClientConfirmResultList(chainCode);//得到客户确认-待发布中品牌数据

			ExportExcel<ExpClientConfirmResult> ex = new ExportExcel<ExpClientConfirmResult>();  
			
			String excelName = "客户确认-待发布列表"+DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			//转码防止乱码  
	        response.addHeader("Content-Disposition", "attachment;filename="+new String( excelName.getBytes("gb2312"), "ISO8859-1" )+".xls");  
	        
			String[] headers =  
		        { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址" ,
		        		"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编" , "厂商提供英文名称",
		        		"厂商提供英文地址", "库中PID","FID","库中POI名称","库中POI别名","库中分类","库中CHAIN","库中POI地址",
		        		"库中电话","库中邮编","与库差分结果","新旧一览表差分结果","四维确认备注","反馈人ID","负责人反馈结果","审核意见","反馈时间"};  
			
			try  
	        {  
	            OutputStream out = response.getOutputStream();  
	            		//new FileOutputStream("f://a.xls");  
	            ex.exportExcel("客户确认-待发布列表", headers, clientConfirmResultList, out, "yyyy-MM-dd");
//	            exportExcel(headers, dealerBrandList, out);  
	            out.close();  
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
	
	@RequestMapping(value = "/chainUpdate")
	public ModelAndView chainUpdate(HttpServletRequest request) {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			Map<String,Object> result = dealerShipService.chainUpdate(userId);			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error("品牌更新失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
	@RequestMapping(value = "/liveUpdate")
	public ModelAndView liveUpdate(HttpServletRequest request) {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			long jobId = dealerShipService.liveUpdate(userId);			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error("实时更新失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}