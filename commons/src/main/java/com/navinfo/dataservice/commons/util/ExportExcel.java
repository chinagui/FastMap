package com.navinfo.dataservice.commons.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
  
/** 
 * 利用开源组件POI3.0.2动态导出EXCEL文档 转载时请保留以下信息，注明出处！ 
 *  
 * @author leno 
 * @version v1.0 
 * @param <T> 
 *            应用泛型，代表任意一个符合javabean风格的类 
 *            注意这里为了简单起见，boolean型的属性xxx的get器方式为getXxx(),而不是isXxx() 
 *            byte[]表jpg格式的图片数据 
 */  
@SuppressWarnings("deprecation")
public class ExportExcel<T>  
{  
    public void exportExcel(Collection<T> dataset, OutputStream out)  
    {  
        exportExcel("测试POI导出EXCEL文档", null, dataset, out, "yyyy-MM-dd",null,null,null);  
    }  
  
    public void exportExcel(String[] headers, Collection<T> dataset,  
            OutputStream out)  
    {  
        exportExcel("测试POI导出EXCEL文档", headers, dataset, out, "yyyy-MM-dd",null,null,null);  
    }  
  
    public void exportExcel(String[] headers, Collection<T> dataset,  
            OutputStream out, String pattern)  
    {  
        exportExcel("测试POI导出EXCEL文档", headers, dataset, out, pattern,null,null,null);  
    }  
  
     
    @SuppressWarnings("unchecked")  
    public void exportExcel(String title, String[] headers,  
            Collection<T> dataset, OutputStream out, String pattern,Map<String,Integer> colorMap,
            Map<String,String> mergeMap,String[] mergeHeaders)  
    {  
        // 声明一个工作薄   
        HSSFWorkbook workbook = new HSSFWorkbook();  
        createSheet(title, workbook, headers, dataset, out, pattern,colorMap,mergeMap,mergeHeaders);
        try  
        {  
            workbook.write(out);  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }  
    
    /** 
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上 
     *  
     * @param title 
     *            表格标题名 
     * @param headers 
     *            表格属性列名数组 
     * @param dataset 
     *            需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的 
     *            javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据) 
     * @param out 
     *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中 
     * @param pattern 
     *            如果有时间数据，设定输出格式。默认为"yyy-MM-dd" 
     */ 
    @SuppressWarnings("unchecked")  
    public void exportExcel(String title, String[] headers,  
            Collection<T> dataset, OutputStream out, String pattern)  
    {  
        // 声明一个工作薄   
        HSSFWorkbook workbook = new HSSFWorkbook();  
        createSheet(title, workbook, headers, dataset, out, pattern,null,null,null);
        try  
        {  
            workbook.write(out);  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }  
    
    /**
     * colorMap 背景色
     * mergeMap 合并单元格行列map
     * headers2 合并单元格标题
     * @param title
     * @param workbook
     * @param headers
     * @param dataset
     * @param out
     * @param pattern
     * @param colorMap
     * @param mergeMap
     */
    public void createSheet(String title,HSSFWorkbook workbook,String[] headers,  
            Collection<T> dataset, OutputStream out, String pattern,Map<String,Integer> colorMap,
            Map<String,String> mergeMap,String[] mergeHeaders){
    	// 生成一个表格   
        HSSFSheet sheet = workbook.createSheet(title);  
        // 设置表格默认列宽度为15个字节   
        sheet.setDefaultColumnWidth((short) 15);  
        // 生成一个样式   
        HSSFCellStyle style = workbook.createCellStyle();  
        // 设置这些样式   
//        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);  
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        
        //设置标题颜色
        if(null!=colorMap&&(!colorMap.isEmpty())){
        	 HSSFPalette palette = workbook.getCustomPalette();  
        	 Integer red = colorMap.get("red");
        	 Integer green = colorMap.get("green");
        	 Integer blue = colorMap.get("blue");
             palette.setColorAtIndex((short)11, (byte) (red.intValue()), (byte) (green.intValue()), (byte) (blue.intValue()));
             style.setFillPattern((short)11);
             style.setFillForegroundColor((short)11);
             style.setFillBackgroundColor((short)11);
        }
        
       
        // 生成一个字体   
        HSSFFont font = workbook.createFont();  
//        font.setColor(HSSFColor.VIOLET.index);  
        font.setFontHeightInPoints((short) 10);  
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
        // 把字体应用到当前的样式   
        style.setFont(font);  
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        // 生成并设置另一个样式   
        HSSFCellStyle style2 = workbook.createCellStyle();  
//        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);  
//        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 生成另一个字体   
        HSSFFont font2 = workbook.createFont();  
        font2.setColor(HSSFColor.BLACK.index);
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);  
        // 把字体应用到当前的样式   
        style2.setFont(font2);  
        
        //bug:8123(预处理平台_代理店_数据准备_表表差分结果导出：数据量较大时，导出报错)
        HSSFFont font3 = workbook.createFont();  
        font3.setColor(HSSFColor.BLUE.index);
  
        // 声明一个画图的顶级管理器   
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();  
        // 定义注释的大小和位置,详见文档   
        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,  
                0, 0, 0, (short) 4, 2, (short) 6, 5));  
        // 设置注释内容   
        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));  
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.   
        comment.setAuthor("leno");  
  
        // 产生表格标题行   
        HSSFRow row = sheet.createRow(0); 
        int index = 0;
        if(null==mergeMap||mergeMap.isEmpty()){
    	  for (short i = 0; i < headers.length; i++)  
          {  
              HSSFCell cell = row.createCell(i);  
              cell.setCellStyle(style);  
              HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
              cell.setCellValue(text);  
              
          }  
        }else{
        	index = Integer.parseInt((String)mergeMap.get("rowNum"))-1;
        	String[] colIndexArray = ((String)mergeMap.get("colIndex")).split(",");
        	int firstMergeIndex = Integer.parseInt(colIndexArray[0]);
        	int lastMergeIndex  = Integer.parseInt(colIndexArray[colIndexArray.length-1]);
        	HSSFRow row1 = sheet.createRow(1);  
        	for (int i=0;i<colIndexArray.length;i++)  
            {   
        		if(i==colIndexArray.length-1){break;}
        		int colIndex = Integer.parseInt(colIndexArray[i]);
        		int nextColIndex = Integer.parseInt(colIndexArray[i+1]);
                HSSFCell cell = row.createCell(colIndex);  
                // 生成并设置另一个样式   
                cell.setCellStyle(style);  
                HSSFRichTextString text = new HSSFRichTextString(mergeHeaders[i]);  
                cell.setCellValue(text);
                CellRangeAddress cra = new CellRangeAddress(0, 0, colIndex, nextColIndex-1);
                sheet.addMergedRegion(cra); 
                setRegionStyle(sheet, cra, style);
                for (int j=colIndex;j<nextColIndex;j++) {
                	HSSFCell cell1 =  row1.createCell(j);
                    cell1.setCellStyle(style);  
                    HSSFRichTextString text1 = new HSSFRichTextString(headers[j]);  
                    cell1.setCellValue(text1);
				}
                setRegionStyle(sheet, cra, style);
            }  
           	for (int i = 0; i < firstMergeIndex; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        	for (int i = lastMergeIndex; i < headers.length; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        }	
        	
   
        	
  
        // 遍历集合数据，产生数据行   
        Iterator<T> it = dataset.iterator();  
        while (it.hasNext())  
        {  
            index++;  
            row = sheet.createRow(index);  
            T t = (T) it.next();  
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值   
            Field[] fields = t.getClass().getDeclaredFields();  
            for (short i = 0; i < fields.length; i++)  
            {  
                HSSFCell cell = row.createCell(i);  
                cell.setCellStyle(style2);  
                Field field = fields[i];  
                String fieldName = field.getName();  
                System.out.println("fieldName:  "+fieldName);
                String getMethodName = "get"  
                        + fieldName.substring(0, 1).toUpperCase()  
                        + fieldName.substring(1);  
                try  
                {  
                    Class tCls = t.getClass();  
                    Method getMethod = tCls.getMethod(getMethodName,  
                            new Class[]  
                            {});  
                    Object value = getMethod.invoke(t, new Object[]  
                    {});  
                    // 判断值的类型后进行强制类型转换   
                    String textValue = null;  
                    if(value != null){
                    	
                    
                     if (value instanceof Integer) {   
                     int intValue = (Integer) value;   
                     cell.setCellValue(intValue);   
                     } else if (value instanceof Float) {   
                     float fValue = (Float) value;   
                     textValue = new HSSFRichTextString(String.valueOf(fValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Double) {   
                     double dValue = (Double) value;   
                     textValue = new HSSFRichTextString(   
                     String.valueOf(dValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Long) {   
                     long longValue = (Long) value;   
                     cell.setCellValue(longValue);   
                     }   
                /*    if (value instanceof Boolean)  
                    {  
                        boolean bValue = (Boolean) value;  
                        textValue = "男";  
                        if (!bValue)  
                        {  
                            textValue = "女";  
                        }  
                    } */ 
                    else if (value instanceof Date)  
                    {  
                        Date date = (Date) value;  
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);  
                        textValue = sdf.format(date);  
                    }  
                    else if (value instanceof byte[])  
                    {  
                        // 有图片时，设置行高为60px;   
                        row.setHeightInPoints(60);  
                        // 设置图片所在列宽度为80px,注意这里单位的一个换算   
                        sheet.setColumnWidth(i, (short) (35.7 * 80));  
                        // sheet.autoSizeColumn(i);   
                        byte[] bsValue = (byte[]) value;  
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,  
                                1023, 255, (short) 6, index, (short) 6, index);  
                        anchor.setAnchorType(2);  
                        patriarch.createPicture(anchor, workbook.addPicture(  
                                bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));  
                    }  
                    else  
                    {  
                        // 其它数据类型都当作字符串简单处理   
                        textValue = value.toString();  
                    }
                    }else{
                    	textValue="";
                    }
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成   
                    if (textValue != null)  
                    {  
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");  
                        Matcher matcher = p.matcher(textValue);  
                        if (matcher.matches())  
                        {  
                            // 是数字当作double处理   
                            cell.setCellValue(Double.parseDouble(textValue));  
                        }  
                        else  
                        {  
                            HSSFRichTextString richString = new HSSFRichTextString(  
                                    textValue);  
//                            HSSFFont font3 = workbook.createFont();  
//                            font3.setColor(HSSFColor.BLUE.index);  
                            richString.applyFont(font3);  
                            cell.setCellValue(richString);  
                        }  
                    }  
                }  
                catch (SecurityException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (NoSuchMethodException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (IllegalArgumentException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (IllegalAccessException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (InvocationTargetException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                finally  
                {  
                    // 清理资源   
                }  
            }  
        }  
       
    }
    
    
    /**
     * @Title: createSheet
     * @Description: 增加默认值的导出excel
     * @param title
     * @param workbook
     * @param headers
     * @param dataset
     * @param out
     * @param pattern
     * @param colorMap
     * @param mergeMap
     * @param mergeHeaders
     * @param defaultStr  void
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年8月9日 下午7:23:38 
     */
    public void createSheet(String title,HSSFWorkbook workbook,String[] headers,  
            Collection<T> dataset, OutputStream out, String pattern,Map<String,Integer> colorMap,
            Map<String,String> mergeMap,String[] mergeHeaders,String defaultStr){
    	// 生成一个表格   
        HSSFSheet sheet = workbook.createSheet(title);  
        // 设置表格默认列宽度为15个字节   
        sheet.setDefaultColumnWidth((short) 15);  
        // 生成一个样式   
        HSSFCellStyle style = workbook.createCellStyle();  
        // 设置这些样式   
//        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);  
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        
        //设置标题颜色
        if(null!=colorMap&&(!colorMap.isEmpty())){
        	 HSSFPalette palette = workbook.getCustomPalette();  
        	 Integer red = colorMap.get("red");
        	 Integer green = colorMap.get("green");
        	 Integer blue = colorMap.get("blue");
             palette.setColorAtIndex((short)11, (byte) (red.intValue()), (byte) (green.intValue()), (byte) (blue.intValue()));
             style.setFillPattern((short)11);
             style.setFillForegroundColor((short)11);
             style.setFillBackgroundColor((short)11);
        }
        
       
        // 生成一个字体   
        HSSFFont font = workbook.createFont();  
//        font.setColor(HSSFColor.VIOLET.index);  
        font.setFontHeightInPoints((short) 10);  
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
        // 把字体应用到当前的样式   
        style.setFont(font);  
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        // 生成并设置另一个样式   
        HSSFCellStyle style2 = workbook.createCellStyle();  
//        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);  
//        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 生成另一个字体   
        HSSFFont font2 = workbook.createFont();  
        font2.setColor(HSSFColor.BLACK.index);
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);  
        // 把字体应用到当前的样式   
        style2.setFont(font2);  
        
        //bug:8123(预处理平台_代理店_数据准备_表表差分结果导出：数据量较大时，导出报错)
        HSSFFont font3 = workbook.createFont();  
        font3.setColor(HSSFColor.BLUE.index);
  
        // 声明一个画图的顶级管理器   
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();  
        // 定义注释的大小和位置,详见文档   
        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,  
                0, 0, 0, (short) 4, 2, (short) 6, 5));  
        // 设置注释内容   
        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));  
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.   
        comment.setAuthor("leno");  
  
        // 产生表格标题行   
        HSSFRow row = sheet.createRow(0); 
        int index = 0;
        if(null==mergeMap||mergeMap.isEmpty()){
    	  for (short i = 0; i < headers.length; i++)  
          {  
              HSSFCell cell = row.createCell(i);  
              cell.setCellStyle(style);  
              HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
              cell.setCellValue(text);  
              
          }  
        }else{
        	index = Integer.parseInt((String)mergeMap.get("rowNum"))-1;
        	String[] colIndexArray = ((String)mergeMap.get("colIndex")).split(",");
        	int firstMergeIndex = Integer.parseInt(colIndexArray[0]);
        	int lastMergeIndex  = Integer.parseInt(colIndexArray[colIndexArray.length-1]);
        	HSSFRow row1 = sheet.createRow(1);  
        	for (int i=0;i<colIndexArray.length;i++)  
            {   
        		if(i==colIndexArray.length-1){break;}
        		int colIndex = Integer.parseInt(colIndexArray[i]);
        		int nextColIndex = Integer.parseInt(colIndexArray[i+1]);
                HSSFCell cell = row.createCell(colIndex);  
                // 生成并设置另一个样式   
                cell.setCellStyle(style);  
                HSSFRichTextString text = new HSSFRichTextString(mergeHeaders[i]);  
                cell.setCellValue(text);
                CellRangeAddress cra = new CellRangeAddress(0, 0, colIndex, nextColIndex-1);
                sheet.addMergedRegion(cra); 
                setRegionStyle(sheet, cra, style);
                for (int j=colIndex;j<nextColIndex;j++) {
                	HSSFCell cell1 =  row1.createCell(j);
                    cell1.setCellStyle(style);  
                    HSSFRichTextString text1 = new HSSFRichTextString(headers[j]);  
                    cell1.setCellValue(text1);
				}
                setRegionStyle(sheet, cra, style);
            }  
           	for (int i = 0; i < firstMergeIndex; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        	for (int i = lastMergeIndex; i < headers.length; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        }	
        // 遍历集合数据，产生数据行   
        Iterator<T> it = dataset.iterator();  
        while (it.hasNext())  
        {  
            index++;  
            row = sheet.createRow(index);  
            T t = (T) it.next();  
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值   
            Field[] fields = t.getClass().getDeclaredFields();  
            for (short i = 0; i < fields.length; i++)  
            {  
                HSSFCell cell = row.createCell(i);  
                cell.setCellStyle(style2);  
                Field field = fields[i];  
                String fieldName = field.getName();  
                System.out.println("fieldName:  "+fieldName);
                String getMethodName = "get"  
                        + fieldName.substring(0, 1).toUpperCase()  
                        + fieldName.substring(1);  
                try  
                {  
                    Class tCls = t.getClass();  
                    Method getMethod = tCls.getMethod(getMethodName,  
                            new Class[]  
                            {});  
                    Object value = getMethod.invoke(t, new Object[]  
                    {});  
                    // 判断值的类型后进行强制类型转换   
                    String textValue = null;  
                    if(value != null){
                    	
                    
                     if (value instanceof Integer) {   
                     int intValue = (Integer) value;   
                     cell.setCellValue(intValue);   
                     } else if (value instanceof Float) {   
                     float fValue = (Float) value;   
                     textValue = new HSSFRichTextString(String.valueOf(fValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Double) {   
                     double dValue = (Double) value;   
                     textValue = new HSSFRichTextString(   
                     String.valueOf(dValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Long) {   
                     long longValue = (Long) value;   
                     cell.setCellValue(longValue);   
                     }   
                /*    if (value instanceof Boolean)  
                    {  
                        boolean bValue = (Boolean) value;  
                        textValue = "男";  
                        if (!bValue)  
                        {  
                            textValue = "女";  
                        }  
                    } */ 
                    else if (value instanceof Date)  
                    {  
                    	if(value != null){
                    		Date date = (Date) value;  
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);  
                            textValue = sdf.format(date);
                    	}else{
                    		textValue = defaultStr;
                    	}
                          
                    }  
                    else if (value instanceof byte[])  
                    {  
                        // 有图片时，设置行高为60px;   
                        row.setHeightInPoints(60);  
                        // 设置图片所在列宽度为80px,注意这里单位的一个换算   
                        sheet.setColumnWidth(i, (short) (35.7 * 80));  
                        // sheet.autoSizeColumn(i);   
                        byte[] bsValue = (byte[]) value;  
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,  
                                1023, 255, (short) 6, index, (short) 6, index);  
                        anchor.setAnchorType(2);  
                        patriarch.createPicture(anchor, workbook.addPicture(  
                                bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));  
                    }  
                    else  
                    {  
                        // 其它数据类型都当作字符串简单处理   
                    	if(value != null){
                    		textValue = value.toString(); 
                    	}else{
                    		textValue = defaultStr;
                    	}
                         
                    }
                    }else{
                    	textValue=defaultStr;
                    }
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成   
                    if (textValue != null)  
                    {  
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");  
                        Matcher matcher = p.matcher(textValue);  
                        if (matcher.matches())  
                        {  
                            // 是数字当作double处理   
                            cell.setCellValue(Double.parseDouble(textValue));  
                        }  
                        else  
                        {  
                            HSSFRichTextString richString = new HSSFRichTextString(  
                                    textValue);  
//                            HSSFFont font3 = workbook.createFont();  
//                            font3.setColor(HSSFColor.BLUE.index);  
                            richString.applyFont(font3);  
                            cell.setCellValue(richString);  
                        }  
                    }  
                }  
                catch (SecurityException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (NoSuchMethodException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (IllegalArgumentException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (IllegalAccessException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (InvocationTargetException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                finally  
                {  
                    // 清理资源   
                }  
            }  
        }  
       
    }

    public void createXLSByTemplate(String sheetName, HSSFWorkbook workbook,
                            Collection<Map<Integer, Object>> dataset, String pattern,Map<String,Integer> colorMap,
                            String defaultStr){
        // 生成一个表格
        HSSFSheet sheet = workbook.getSheet(sheetName);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 15);
        // 生成一个样式
        HSSFCellStyle style = workbook.createCellStyle();
        // 设置这些样式
//        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        //设置标题颜色
        if(null!=colorMap&&(!colorMap.isEmpty())){
            HSSFPalette palette = workbook.getCustomPalette();
            Integer red = colorMap.get("red");
            Integer green = colorMap.get("green");
            Integer blue = colorMap.get("blue");
            palette.setColorAtIndex((short)11, (byte) (red.intValue()), (byte) (green.intValue()), (byte) (blue.intValue()));
            style.setFillPattern((short)11);
            style.setFillForegroundColor((short)11);
            style.setFillBackgroundColor((short)11);
        }


        // 生成一个字体
        HSSFFont font = workbook.createFont();
//        font.setColor(HSSFColor.VIOLET.index);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        // 把字体应用到当前的样式
        style.setFont(font);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
//        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
//        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();
        font2.setColor(HSSFColor.BLACK.index);
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        //bug:8123(预处理平台_代理店_数据准备_表表差分结果导出：数据量较大时，导出报错)
        HSSFFont font3 = workbook.createFont();
        font3.setColor(HSSFColor.BLUE.index);

        // 声明一个画图的顶级管理器
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        // 定义注释的大小和位置,详见文档
        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,
                0, 0, 0, (short) 4, 2, (short) 6, 5));
        // 设置注释内容
        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
        comment.setAuthor("leno");

        // 标题行根据模板生成
        int index = sheet.getLastRowNum();
        HSSFRow row = null;

        // 遍历集合数据，产生数据行
        Iterator<Map<Integer,Object>> it = dataset.iterator();
        while (it.hasNext())
        {
            index++;
            row = sheet.createRow(index);
            Map t = (Map) it.next();
//            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
//            Field[] fields = t.getClass().getDeclaredFields();
            for (short i = 0; i < t.size(); i++)
            {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
//                Field field = fields[i];
//                String fieldName = field.getName();
//                System.out.println("fieldName:  "+fieldName);
//                String getMethodName = "get"
//                        + fieldName.substring(0, 1).toUpperCase()
//                        + fieldName.substring(1);
                try
                {
//                    Class tCls = t.getClass();
//                    Method getMethod = tCls.getMethod(getMethodName,
//                            new Class[]
//                                    {});
                    Object value = t.get(Integer.valueOf(i));
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    if(value != null){


                        if (value instanceof Integer) {
                            int intValue = (Integer) value;
                            cell.setCellValue(intValue);
                        } else if (value instanceof Float) {
                            float fValue = (Float) value;
                            textValue = new HSSFRichTextString(String.valueOf(fValue)).toString();
                            cell.setCellValue(textValue);
                        } else if (value instanceof Double) {
                            double dValue = (Double) value;
                            textValue = new HSSFRichTextString(
                                    String.valueOf(dValue)).toString();
                            cell.setCellValue(textValue);
                        } else if (value instanceof Long) {
                            long longValue = (Long) value;
                            cell.setCellValue(longValue);
                        }
                /*    if (value instanceof Boolean)
                    {
                        boolean bValue = (Boolean) value;
                        textValue = "男";
                        if (!bValue)
                        {
                            textValue = "女";
                        }
                    } */
                        else if (value instanceof Date)
                        {
                            if(value != null){
                                Date date = (Date) value;
                                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                                textValue = sdf.format(date);
                            }else{
                                textValue = defaultStr;
                            }

                        }
                        else if (value instanceof byte[])
                        {
                            // 有图片时，设置行高为60px;
                            row.setHeightInPoints(60);
                            // 设置图片所在列宽度为80px,注意这里单位的一个换算
                            sheet.setColumnWidth(i, (short) (35.7 * 80));
                            // sheet.autoSizeColumn(i);
                            byte[] bsValue = (byte[]) value;
                            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,
                                    1023, 255, (short) 6, index, (short) 6, index);
                            anchor.setAnchorType(2);
                            patriarch.createPicture(anchor, workbook.addPicture(
                                    bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));
                        }
                        else
                        {
                            // 其它数据类型都当作字符串简单处理
                            if(value != null){
                                textValue = value.toString();
                            }else{
                                textValue = defaultStr;
                            }

                        }
                    }else{
                        textValue=defaultStr;
                    }
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
                    if (textValue != null)
                    {
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                        Matcher matcher = p.matcher(textValue);
                        if (matcher.matches())
                        {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        }
                        else
                        {
                            HSSFRichTextString richString = new HSSFRichTextString(
                                    textValue);
//                            HSSFFont font3 = workbook.createFont();
//                            font3.setColor(HSSFColor.BLUE.index);
                            richString.applyFont(font3);
                            cell.setCellValue(richString);
                        }
                    }
                }
                catch (SecurityException e)
                {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
                finally
                {
                    // 清理资源
                }
            }
        }

    }
    
    /**
     * colorMap 背景色
     * mergeMap 合并单元格行列map
     * headers2 合并单元格标题
     * @param title
     * @param workbook
     * @param headers
     * @param dataset
     * @param out
     * @param pattern
     * @param colorMap
     * @param mergeMap
     */
    public void createSheetForList(String title,HSSFWorkbook workbook,String[] headers,  
            Collection<List<Object>> dataset, OutputStream out, String pattern,Map<String,Integer> colorMap,
            Map<String,String> mergeMap,String[] mergeHeaders){
    	// 生成一个表格   
        HSSFSheet sheet = workbook.createSheet(title);  
        // 设置表格默认列宽度为15个字节   
        sheet.setDefaultColumnWidth((short) 15);  
        // 生成一个样式   
        HSSFCellStyle style = workbook.createCellStyle();  
        // 设置这些样式   
//        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);  
//        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        
        //设置标题颜色
        if(null!=colorMap&&(!colorMap.isEmpty())){
        	 HSSFPalette palette = workbook.getCustomPalette();  
        	 Integer red = colorMap.get("red");
        	 Integer green = colorMap.get("green");
        	 Integer blue = colorMap.get("blue");
             palette.setColorAtIndex((short)11, (byte) (red.intValue()), (byte) (green.intValue()), (byte) (blue.intValue()));
             style.setFillPattern((short)11);
             style.setFillForegroundColor((short)11);
             style.setFillBackgroundColor((short)11);
        }
        
       
        // 生成一个字体   
        HSSFFont font = workbook.createFont();  
//        font.setColor(HSSFColor.VIOLET.index);  
        font.setFontHeightInPoints((short) 12);  
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
        // 把字体应用到当前的样式   
        style.setFont(font);  
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        // 生成并设置另一个样式   
        HSSFCellStyle style2 = workbook.createCellStyle();  
//        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);  
//        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
//        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);  
//        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 生成另一个字体   
        HSSFFont font2 = workbook.createFont();  
        font2.setColor(HSSFColor.BLACK.index);
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);  
        // 把字体应用到当前的样式   
        style2.setFont(font2);  
        
        //bug:8123(预处理平台_代理店_数据准备_表表差分结果导出：数据量较大时，导出报错)
        HSSFFont font3 = workbook.createFont();  
        font3.setColor(HSSFColor.BLUE.index);
  
        // 声明一个画图的顶级管理器   
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();  
        // 定义注释的大小和位置,详见文档   
        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0,  
                0, 0, 0, (short) 4, 2, (short) 6, 5));  
        // 设置注释内容   
        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));  
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.   
        comment.setAuthor("leno");  
  
        // 产生表格标题行   
        HSSFRow row = sheet.createRow(0); 
        int index = 0;
        if(null==mergeMap||mergeMap.isEmpty()){
    	  for (short i = 0; i < headers.length; i++)  
          {  
              HSSFCell cell = row.createCell(i);  
              cell.setCellStyle(style);  
              HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
              cell.setCellValue(text);  
              
          }  
        }else{
        	index = Integer.parseInt((String)mergeMap.get("rowNum"))-1;
        	String[] colIndexArray = ((String)mergeMap.get("colIndex")).split(",");
        	int firstMergeIndex = Integer.parseInt(colIndexArray[0]);
        	int lastMergeIndex  = Integer.parseInt(colIndexArray[colIndexArray.length-1]);
        	HSSFRow row1 = sheet.createRow(1);  
        	for (int i=0;i<colIndexArray.length;i++)  
            {   
        		if(i==colIndexArray.length-1){break;}
        		int colIndex = Integer.parseInt(colIndexArray[i]);
        		int nextColIndex = Integer.parseInt(colIndexArray[i+1]);
                HSSFCell cell = row.createCell(colIndex);  
                // 生成并设置另一个样式   
                cell.setCellStyle(style);  
                HSSFRichTextString text = new HSSFRichTextString(mergeHeaders[i]);  
                cell.setCellValue(text);
                CellRangeAddress cra = new CellRangeAddress(0, 0, colIndex, nextColIndex-1);
                sheet.addMergedRegion(cra); 
                setRegionStyle(sheet, cra, style);
                for (int j=colIndex;j<nextColIndex;j++) {
                	HSSFCell cell1 =  row1.createCell(j);
                    cell1.setCellStyle(style);  
                    HSSFRichTextString text1 = new HSSFRichTextString(headers[j]);  
                    cell1.setCellValue(text1);
				}
                setRegionStyle(sheet, cra, style);
            }  
           	for (int i = 0; i < firstMergeIndex; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        	for (int i = lastMergeIndex; i < headers.length; i++)  
            {  
        		
                HSSFCell cell = row.createCell(i);  
                CellRangeAddress cra = new CellRangeAddress(0, index, i, i);
                sheet.addMergedRegion(cra); 
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);  
                cell.setCellValue(text);
                setRegionStyle(sheet, cra, style);
            } 
        }	
        	
   
        	
  
        // 遍历集合数据，产生数据行   
        Iterator<List<Object>> it = dataset.iterator();  
        while (it.hasNext())  
        {  
            index++;  
            row = sheet.createRow(index);  
            List<Object> list = (List<Object>) it.next();  
            //遍历集合,给每个单元格赋值
            for (short i = 0; i < list.size(); i++)  
            {  
                HSSFCell cell = row.createCell(i);  
                cell.setCellStyle(style2);  
                try  
                {  
                    Object value = list.get(i);  
                    // 判断值的类型后进行强制类型转换   
                    String textValue = null;  
                    if(value != null){
                    	
                    
                     if (value instanceof Integer) {   
                     int intValue = (Integer) value;   
                     cell.setCellValue(intValue);   
                     } else if (value instanceof Float) {   
                     float fValue = (Float) value;   
                     textValue = new HSSFRichTextString(String.valueOf(fValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Double) {   
                     double dValue = (Double) value;   
                     textValue = new HSSFRichTextString(   
                     String.valueOf(dValue)).toString();   
                     cell.setCellValue(textValue);   
                     } else if (value instanceof Long) {   
                     long longValue = (Long) value;   
                     cell.setCellValue(longValue);   
                     }   
                /*    if (value instanceof Boolean)  
                    {  
                        boolean bValue = (Boolean) value;  
                        textValue = "男";  
                        if (!bValue)  
                        {  
                            textValue = "女";  
                        }  
                    } */ 
                    else if (value instanceof Date)  
                    {  
                        Date date = (Date) value;  
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);  
                        textValue = sdf.format(date);  
                    }  
                    else if (value instanceof byte[])  
                    {  
                        // 有图片时，设置行高为60px;   
                        row.setHeightInPoints(60);  
                        // 设置图片所在列宽度为80px,注意这里单位的一个换算   
                        sheet.setColumnWidth(i, (short) (35.7 * 80));  
                        // sheet.autoSizeColumn(i);   
                        byte[] bsValue = (byte[]) value;  
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,  
                                1023, 255, (short) 6, index, (short) 6, index);  
                        anchor.setAnchorType(2);  
                        patriarch.createPicture(anchor, workbook.addPicture(  
                                bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));  
                    }  
                    else  
                    {  
                        // 其它数据类型都当作字符串简单处理   
                        textValue = value.toString();  
                    }
                    }else{
                    	textValue="";
                    }
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成   
                    if (textValue != null)  
                    {  
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");  
                        Matcher matcher = p.matcher(textValue);  
                        if (matcher.matches())  
                        {  
                            // 是数字当作double处理   
                            cell.setCellValue(Double.parseDouble(textValue));  
                        }  
                        else  
                        {  
                            HSSFRichTextString richString = new HSSFRichTextString(  
                                    textValue);  
//                            HSSFFont font3 = workbook.createFont();  
//                            font3.setColor(HSSFColor.BLUE.index);  
                            richString.applyFont(font3);  
                            cell.setCellValue(richString);  
                        }  
                    }  
                }  
                catch (SecurityException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                catch (IllegalArgumentException e)  
                {  
                    e.printStackTrace();  
                    System.out.println(e.getMessage());
                }  
                finally  
                {  
                    // 清理资源   
                }  
            }  
        }  
       
    }
    
    
    
	public static void setRegionStyle(HSSFSheet sheet, CellRangeAddress region, HSSFCellStyle cs) {
		for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
			HSSFRow row = HSSFCellUtil.getRow(i, sheet);
			for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
				HSSFCell cell = HSSFCellUtil.getCell(row, (short) j);
				cell.setCellStyle(cs);
			}
		}
	}
}

