package com.navinfo.dataservice.commons.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * 解析excel表格工具类
 * 
 * @param  fielpath(String)
 * @return List 包含单元格数据内容的Map对象
 * @author songhe
 */
public class ExcelReader {
	
	private Workbook wb;
	private Sheet sheet;
	private Row row;
	
	/**
	 *判断excel表格 
	 * 
	 * 
	 * */
	public ExcelReader(String filepath) {
		if(filepath==null){
			return;
		}
		String ext = filepath.substring(filepath.lastIndexOf("."));
		try {
			InputStream is = new FileInputStream(filepath);
			if(".xls".equals(ext)){
				wb = new HSSFWorkbook(is);
			}else if(".xlsx".equals(ext)){
				wb = new XSSFWorkbook(is);
			}else{
				wb=null;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * 读取Excel数据内容
	 * 
	 * @param 
	 * @return List 包含单元格数据内容的Map对象
	 * @author songhe
	 */
	public List<Map<String, Object>> readExcelContent() throws Exception{
		if(wb==null){
			throw new Exception("Workbook对象为空！");
		}
		List<Map<String, Object>> BlockPlanList = new ArrayList<>();
		
		sheet = wb.getSheetAt(0);
		// 得到总行数
		int rowNum = sheet.getLastRowNum();
		row = sheet.getRow(0);
		int colNum = row.getPhysicalNumberOfCells();
		// 正文内容应该从第二行开始,第一行为表头的标题
		for (int i = 1; i <= rowNum; i++) {
			row = sheet.getRow(i);
			
			int j = 0;
			Map<String, Object> cellValue = new HashMap<String, Object>();
			
			while (j < colNum ) {
				String obj = getCellFormatValue(row.getCell(j));
				
				if( j == 0){
					cellValue.put("BLOCK_ID", obj);
				}else if(j == 1){
					cellValue.put("BLOCK_NAME", obj);
				}else if(j == 2){
					cellValue.put("CITY_NAME", obj);
				}else if(j == 3){
					cellValue.put("COLLECT_PLAN_START_DATE", obj);
				}else if(j == 4){
					cellValue.put("COLLECT_PLAN_END_DATE", obj);
				}else if(j == 5){
					cellValue.put("ROAD_PLAN_TOTAL", obj);
				}else if(j == 6){
					cellValue.put("POI_PLAN_TOTAL", obj);
				}else if(j == 7){
					cellValue.put("WORK_KIND", obj);
				}else if(j == 8){
					cellValue.put("MONTH_EDIT_PLAN_START_DATE", obj);
				}else if(j == 9){
					cellValue.put("MONTH_EDIT_PLAN_END_DATE", obj);
				}else if(j == 10){
					cellValue.put("PRODUCE_PLAN_END_DATE", obj);
				}else if(j == 11){
					cellValue.put("PRODUCE_PLAN_START_DATE", obj);
				}else if(j == 12){
					cellValue.put("LOT", obj);
				}else if(j == 13){
					cellValue.put("DESCP", obj);
				}else if(j == 14){
					cellValue.put("IS_PLAN", obj);
				}
				j++;
			}
			if(cellValue.containsKey("BLOCK_ID")){
				BlockPlanList.add(cellValue);
			}
		}
		return BlockPlanList;
	}
	
	/**
	 * 读取Excel表头作为map的key值
	 * 
	 * @param 
	 * @return map 单元格第一行的表头内容
	 * @author songhe
	 */
	public Map<String, Object> readExcelTitle() throws Exception{
		if(wb==null){
			throw new Exception("Workbook对象为空！");
		}
		sheet = wb.getSheetAt(0);
		// 得到总行数
		row = sheet.getRow(0);
		int colNum = row.getPhysicalNumberOfCells();
		// 获取第一行表头内容
		row = sheet.getRow(0);
			
		int j = 0;
		Map<String, Object> titleValue = new HashMap<String, Object>();
			
			while (j < colNum) {
				String obj = getCellFormatValue(row.getCell(j));
				titleValue.put(obj, "");
				j++;
			}
		return titleValue;
	}
	
	/**
	 * 
	 * 根据Cell类型设置数据
	 * @param cell
	 * @return
	 * @author songhe
	 */
	private String getCellFormatValue(Cell cell) {
		String cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC
			case Cell.CELL_TYPE_FORMULA: {
				// 判断当前的cell是否为Date
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					cellvalue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
				} else {// 如果是纯数字
					cellvalue = String.valueOf((int) cell.getNumericCellValue());
				}
				break;
			}
			case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
				// 取得当前的Cell字符串
				cellvalue = cell.getRichStringCellValue().getString();
				break;
			default:// 默认的Cell值
				cellvalue = "";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;
	}

}
