package com.navinfo.dataservice.commons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.navicommons.geo.computation.CompPointUtil;
import com.navinfo.navicommons.geo.computation.DoublePoint;
/** 
* @ClassName: CompPointUtilTest 
* @author Xiao Xiaowen 
* @date 2016年5月10日 下午9:35:20 
* @Description: TODO
*/
public class CompPointUtilTest {
//	@Test
	public void norm_001(){
		DoublePoint p = new DoublePoint(3.0,4.0);
		System.out.println(CompPointUtil.norm(p));
	}
	@Test
	public void testExcelImport() throws Exception{
//		String filePath ="F://source.xls";
		String filePath ="F://source.xlsx";
		
		ExcelReader excleReader = new ExcelReader(filePath);
		Map<String,String> excelHeader = new HashMap<String,String>();
		excelHeader.put("FID", "cfmPoiNum");
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供简称", "nameShort");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话（销售）", "telSale");
		excelHeader.put("厂商提供电话（维修）", "telService");
		excelHeader.put("厂商提供电话（其他）", "telOther");
		excelHeader.put("厂商提供邮编", "postCode");
		excelHeader.put("厂商提供英文名称", "nameEng");
		excelHeader.put("厂商提供英文地址", "addressEng");
		excelHeader.put("一览表提供时间", "provideDate");
		excelHeader.put("一览表确认时间", "dealCfmDate");
		excelHeader.put("四维确认备注", "cfmMemo");
		/*excelHeader.put("负责人反馈结果", "");
		excelHeader.put("解决人", "");
		excelHeader.put("解决时间", "");
		excelHeader.put("四维差分结果", "");
		excelHeader.put("一览表作业状态", "");
		excelHeader.put("变更履历", "");*/
		excelHeader.put("是否删除记录", "isDeleted");//数字类型
		excelHeader.put("一览表X坐标", "XGuide");
		excelHeader.put("一览表Y坐标", "YGuide");//XGuide,YGuide
		excelHeader.put("已采纳POI分类", "poiKindCode");
		excelHeader.put("已采纳POI品牌", "poiChain");
		excelHeader.put("已采纳POI名称", "poiName");
		excelHeader.put("已采纳POI简称", "poiNameShort");
		excelHeader.put("已采纳POI地址", "poiAddress");
		excelHeader.put("已采纳POI电话", "poiTel");
		excelHeader.put("已采纳POI邮编", "poiPostCode");
		excelHeader.put("已采纳POI显示坐标X", "poiXDisplay");//double
		excelHeader.put("已采纳POI显示坐标Y", "poiYDisplay");//double
		excelHeader.put("已采纳POI引导坐标X", "poiXGuide");//double
		excelHeader.put("已采纳POI引导坐标Y", "poiYGuide");//double
		/*excelHeader.put("", "");
		excelHeader.put("", "");*/
		
//		Object[] sourceObjs = ExcelReader.readDealerdhipSourceExcel(in);
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
	
		System.out.println(sources.size());
	}
}
