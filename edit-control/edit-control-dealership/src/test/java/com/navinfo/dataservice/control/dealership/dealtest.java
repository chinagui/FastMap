package com.navinfo.dataservice.control.dealership;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
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
	public void testGetResultIdListByChain() throws Exception{
		DataEditService de = DataEditService.getInstance();
		try {
			Connection conn = null;
			conn = DBConnector.getInstance().getDealershipConnection();
			de.commitDealership("900D", conn, 130);
			
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
			String localFile = "e://release20170613152630.csv";
			DataConfirmService confirm = DataConfirmService.getInstance();
			
			List<Map<String, Object>> importResult = confirm.readCsvFile(localFile);
			List<String> uniqueKeys = new ArrayList<>();
			for (Map<String, Object> result : importResult) {

				// 若文件中“情报类型”为空，则整个文件不可以上传；
				//if (result.get("infoType") == null || result.get("infoType").toString().isEmpty()) {
				//	throw new Exception("“情报类型”为空，文件不可以上传");
				//}

				// 若文件中“UUID”和“情报ID”联合匹配必须唯一，否则整个文件不可导入
				String uniqueKey = result.get("resultId") + "," + result.get("infoId");
				if (uniqueKeys.contains(uniqueKey)) {
					throw new Exception("文件中“UUID”和“情报ID”联合匹配不唯一，文件不可导入");
				} else {
					uniqueKeys.add(uniqueKey);
				}
			}
			JSONObject data = confirm.updateResultTable(localFile, (long)1674);
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
	 			jsonObj.put("poiNum", "");
	 			jsonObj.put("name", "华都");
	 			jsonObj.put("address", "");
	 			jsonObj.put("telephone", "");
	 			jsonObj.put("location", "");//116.47199,40.14608
	 			jsonObj.put("proCode", "");
	 			jsonObj.put("resultId", 33022);
	 			jsonObj.put("dbId", 13);
	 
	 			JSONArray data = de.queryByCon(jsonObj);
	 			
	 			System.out.println(data);
	 		}catch (Exception e) {
	 			System.out.println(e.getMessage());
	 		}
	 	}

}
