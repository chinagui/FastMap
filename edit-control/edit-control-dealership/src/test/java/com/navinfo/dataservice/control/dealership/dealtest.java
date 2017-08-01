package com.navinfo.dataservice.control.dealership;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.control.dealership.service.DataConfirmService;
import com.navinfo.dataservice.control.dealership.service.DataEditService;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.dataservice.control.dealership.service.model.InformationExportResult;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class dealtest extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
	}
	
	@Test
	public void testImportSourceExcel() throws Exception{
		DataPrepareService ds = DataPrepareService.getInstance();
//		ds.expTableDiff("4147");
		List<ExpIxDealershipResult> dealerBrandList = ds.searchTableDiff("900D");
		System.out.println("dealerBrandList: "+dealerBrandList.size());
		
		ExportExcel<ExpIxDealershipResult> ex = new ExportExcel<ExpIxDealershipResult>();  
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
            OutputStream out = new FileOutputStream("f://a.xls");  
            ex.exportExcel(headers, dealerBrandList, out);  
            out.close();  
//            JOptionPane.showMessageDialog(null, "导出成功!");  
            System.out.println("excel导出成功！");  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
	}
  	@Test
	public void testSaveData() throws Exception{
		DataEditService ds = DataEditService.getInstance();
				String json = "{\"poiData\":{\"command\":\"INSERT\",\"dbId\":13,\"type\":\"IXPOI\",\"data\":{\"kindCode\":\"888888\",\"chain\":\"4038\",\"level\":\"B1\",\"postCode\":\"123456\",\"names\":[{\"nameGroupid\":1,\"langCode\":\"CHI\",\"nameClass\":1,\"nameType\":2,\"name\":\"达世行凯迪拉克授权售后服务中心2\",\"namePhonetic\":\"\",\"rowId\":\"1\",\"objStatus\":\"INSERT\"}],\"objStatus\":\"INSERT\"}},\"dealershipInfo\":{\"dbId\":399,\"wkfStatus\":\"3\",\"resultId\":455366,\"cfmMemo\":\"备注\"}}";
		JSONObject parameter = JSONObject.fromObject(json);
		ds.saveDataService(parameter,2);
	}
	
		@Test
	public void testCommitDealership() throws Exception{
		DataEditService de = DataEditService.getInstance();
		try {
			Connection conn = null;
			conn = DBConnector.getInstance().getDealershipConnection();
			de.commitDealership("415D", conn, 59);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
		@Test
		public void Test01() throws Exception{
			DataEditService de = DataEditService.getInstance();
			Connection conn = null;
			conn = DBConnector.getInstance().getDealershipConnection();
			//JSONArray data = de.loadWorkListService("4007", conn, 1674, 1);
			JSONObject data=de.diffDetailService(33001, conn);
		}
		
		@Test
		public void test02() throws Exception{
			DataConfirmService confirm = DataConfirmService.getInstance();
			Connection conn = DBConnector.getInstance().getDealershipConnection();
			List<InformationExportResult> informationList = confirm.getOutConfirmList(conn, "");
			ExportExcel<InformationExportResult> excel = new ExportExcel<InformationExportResult>();
			
			try  
	        {  
	            OutputStream out = new FileOutputStream("f://情报下载.xls");  
	            excel.exportExcel(confirm.headers, informationList, out);  
	            out.close();  
	            System.out.println("excel导出成功！");  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        } 
		}
		

		@Test
		public void test03() throws Exception{
			//String localFile = "f://情报下载.xls";
			String xlslocalFile = "C:/Users/fhx/Desktop/release20170621144906.csv";
			DataConfirmService confirm = DataConfirmService.getInstance();
			ExcelReader reader = new ExcelReader(xlslocalFile);
			
			List<Map<String, Object>> importResult = reader.readExcelContent();
			List<String> uniqueKeys = new ArrayList<>();
			for (Map<String, Object> result : importResult) {

				// 若文件中“情报类型”为空，则整个文件不可以上传；
				if (result.get("infoType") == null || result.get("infoType").toString().isEmpty()) {
					throw new Exception("“情报类型”为空，文件不可以上传");
				}

				// 若文件中“UUID”和“情报ID”联合匹配必须唯一，否则整个文件不可导入
				String uniqueKey = result.get("resultId") + "," + result.get("infoId");
				if (uniqueKeys.contains(uniqueKey)) {
					throw new Exception("文件中“UUID”和“情报ID”联合匹配不唯一，文件不可导入");
				} else {
					uniqueKeys.add(uniqueKey);
				}
			}
			String localFile = confirm.xls2csv(importResult,xlslocalFile);
			JSONObject data = confirm.updateResultTable(localFile, (long)1674);
		}
		
		@Test
	public void testFeedback() throws Exception {
		QueryRunner run = new QueryRunner();
		JSONObject obj = new JSONObject();
		obj.put("beginDate", "20170617095512");
		obj.put("endDate", "20170622095512");

		DataConfirmService confirm = DataConfirmService.getInstance();
		// String result = confirm.expInfoFeedbackService(1674, obj);
		// String fileName = confirm.getFeedbackFileName(obj, 1674);
		String filePath = "C://Users/fhx/Desktop/feedback20170615140238.csv";
		Connection conn = DBConnector.getInstance().getDealershipConnection();
		List<Map<String, Object>> feedbackResult = confirm.readCsvFile(filePath, confirm.feedbackHeaders,
				confirm.infoFeedbackHeader());

		for (Map<String, Object> result : feedbackResult) {

			// 文件中字段“情报是否被采纳”+“：”+“情报未采纳原因”+“，”+“情报未采纳或部分采纳备注”+“。”+“关联POI为”+“情报对应的要素外业采集ID”
			String fbContent = result.get("isAdopted") + "：" + result.get("notAdoptedReason") + "，" + result.get("memo")
					+ "。关联POI为" + result.get("cfmPoiNum");
			String sql = String.format(
					"UPDATE IX_DEALERSHIP_RESULT SET WORKFLOW_STATUS = 3, CFM_STATUS = 3, FB_DATE = '%s', FB_CONTENT = '%s', FB_SOURCE = 1 WHERE RESULT_ID = %d",
					result.get("feedbackTime") == null ? "" : result.get("feedbackTime"), fbContent,
					Integer.valueOf(result.get("resultId").toString()));
			run.execute(conn, sql);
		}
		conn.commit();
	}
		
		@Test
		public void testExportToClient() throws Exception{
			DataPrepareService ds = DataPrepareService.getInstance();
			List<ExpClientConfirmResult> clientConfirmResultList = ds.expClientConfirmResultList("4007");//得到客户确认-待发布中品牌数据
			String excelName = "客户确认-待发布列表" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			ExportExcel<ExpClientConfirmResult> ex = new ExportExcel<ExpClientConfirmResult>();
			String[] headers = { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址", "厂商提供电话（销售）",
					"厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编", "厂商提供英文名称", "厂商提供英文地址", "库中PID", "FID", "库中POI名称", "库中POI别名",
					"库中分类", "库中CHAIN", "库中POI地址", "库中电话", "库中邮编", "与库差分结果", "新旧一览表差分结果", "四维确认备注","反馈人ID", "负责人反馈结果",
					"审核意见","反馈时间" };
			try {
				OutputStream out = new FileOutputStream("d://" + excelName + ".xls");
				ex.exportExcel(headers, clientConfirmResultList, out);
				out.close();
				// JOptionPane.showMessageDialog(null, "导出成功!");
				System.out.println("excel导出成功！");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	@Test
	public void testCloseWork() throws Exception {
		DataEditService de = DataEditService.getInstance();
		try {
			ArrayList<Integer> list = new ArrayList<Integer>();
	        list.add(25241);
	        list.add(25395);
	        list.add(25396);
	        list.add(25397);
	        list.add(25398);

			JSONArray resultIds=JSONArray.fromObject(list);
			de.closeWork(130, resultIds);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	

	@Test
	public void getAdminCodeAndProvince() throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		try {
			JSONArray data = manApi.getAdminCodeAndProvince();//得到distinct过后的adminCode列表
			System.out.println(data);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Test
 	public void queryPidListByCon() throws Exception {
 		DataEditService de = DataEditService.getInstance();
 		try {
 			JSONObject jsonObj = new JSONObject();
 			jsonObj.put("poiNum", "");//48903364
 			jsonObj.put("name", "北京中进众旺汽车销售服务有限公司");
 			jsonObj.put("address", "");
 			jsonObj.put("telephone", "");
 			jsonObj.put("location", "");//116.31946,39.93757
 			jsonObj.put("proCode", "11");
 			jsonObj.put("resultId", 1924);
 			jsonObj.put("dbId", 18);
 
 			JSONArray data = de.queryByCon(jsonObj);
 			
 			System.out.println(data);
 		}catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}

	@Test
 	public void testGetChainCodeByLiveUpdate() throws Exception {
 		DataPrepareService dp = DataPrepareService.getInstance();
 		try {
 			System.out.println(dp.getChainCodeByLiveUpdate());
 		}catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
	
	@Test
 	public void testLoadPoiForConflict() throws Exception {
 		DataEditService dp = DataEditService.getInstance();
 		try {
 			JSONObject jsonObj=new JSONObject();
 			jsonObj.put("poiNum","0010061024HYX00212");
 			jsonObj.put("dbId",13);
 			System.out.println(dp.loadPoiForConflict(jsonObj));
 		}catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
}
