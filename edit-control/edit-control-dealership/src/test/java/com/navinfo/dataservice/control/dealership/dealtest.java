package com.navinfo.dataservice.control.dealership;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.DataConfirmService;
import com.navinfo.dataservice.control.dealership.service.DataEditService;
import com.navinfo.dataservice.control.dealership.service.DataPrepareService;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.dataservice.control.dealership.service.model.InformationExportResult;

import net.sf.json.JSONObject;



public class dealtest extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
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
		
		String json = "{\"poiData\":{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"IXPOI\",\"objId\":410000122,\"data\":{\"chain\":\"\",\"contacts\":[{\"rowId\":\"3F378EC170CD44C3A48F8F56BD86126D\",\"objStatus\":\"DELETE\"},{\"rowId\":\"10D4D158DC6F4C4694C31F1173AF60F8\",\"objStatus\":\"DELETE\"}],\"restaurants\":[{\"foodType\":\"3007\",\"rowId\":\"6115657421A2472F987CDAD1624E9A21\",\"pid\":506000040,\"objStatus\":\"UPDATE\"}],\"rowId\":\"B496AD007CB54A6CA8447D51EF73EB58\",\"pid\":410000122,\"objStatus\":\"UPDATE\"},\"subtaskId\":1},dealershipInfo:{\"wkfStatus\":3,\"dbId\":399,\"resultId\":101,\"cfmMemo\":\"宝马采集\"}}";
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
			JSONArray data = de.loadWorkListService("4007", conn, 1674, 1);
			//JSONObject data=de.diffDetailService(14764, conn);
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
		
		@Test
		public void testExportToClient() throws Exception{
			DataPrepareService ds = DataPrepareService.getInstance();
			List<ExpClientConfirmResult> clientConfirmResultList = ds.expClientConfirmResultList("4007");//得到客户确认-待发布中品牌数据

			ExportExcel<ExpClientConfirmResult> ex = new ExportExcel<ExpClientConfirmResult>();  
			String[] headers =  
		        { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址" ,
		        		"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编" , "厂商提供英文名称",
		        		"厂商提供英文地址", "库中PID","FID","库中POI名称","库中POI别名","库中分类","库中CHAIN","库中POI地址",
		        		"库中电话","库中邮编","与库差分结果","新旧一览表差分结果","四维确认备注"};  
			try  
	        {  
	            OutputStream out = new FileOutputStream("e://a.xls");  
	            ex.exportExcel(headers, clientConfirmResultList, out);  
	            out.close();  
//	            JOptionPane.showMessageDialog(null, "导出成功!");  

	            System.out.println("excel导出成功！");  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        } 
		}
	
}
