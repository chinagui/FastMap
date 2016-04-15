package com.navinfo.dataservice.commons.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.navinfo.navicommons.exception.ParseException;


/**
 * 解析excel
 *
 */
public class ExcelReader {
	
	
	 private BufferedReader reader;                                                                                                                      
	                                                                                                                                                            
	 private InputStream is;   
	 
	 private int currSheet;   
	 
	 private int currPosition;  
	 
	 private static final int  START=1;  
	 
	 private static final int  COLUMNS=13;  
	 
	 private int numOfSheets;  
	 
	 Workbook workbook; 
	 
	 List<List> rs=new ArrayList<List>();
	 
	//全角区域
	 public static Pattern fPattern = Pattern.compile("[\\uFF00-\\uFFFF]");
	 
	                                                                                                                                                            
	 public ExcelReader(File inputfile) throws Exception {                                                                                        
	     if(null == inputfile) {                                                                                                 
	         throw new IOException("文件不存在");                                                                                                  
	     }                                                                                                                                                      
	     currPosition = 0;                                                                                                                                      
	     currSheet = 0;                                                                                                                                         
	     is = new FileInputStream(inputfile);  
	     try{
	    	 workbook = new HSSFWorkbook(is);  
	     }catch(Exception e){
	    	 e.printStackTrace();
	    	 //workbook=new XSSFWorkbook(is);
	     }
         numOfSheets = workbook.getNumberOfSheets();                                                                                                        
	                                                                                                                                                            
	 }                                                                                                                                                          
	                                                                                                                                                            

	 public List<List> proccess () throws ParseException{ 
		  HSSFSheet sheet = (HSSFSheet)workbook.getSheetAt(currSheet);
	      StringBuffer buffer = new StringBuffer();                                                                                                             
          HSSFCell cell = null;  
          String cellValue = null;       
          int lastRow = sheet.getPhysicalNumberOfRows();
          try{
	          for(int i = START;i < lastRow;i++){
	        	  currPosition++;
	        	  List line = new ArrayList();
	        	  HSSFRow row = sheet.getRow(i);
	        	  for(int j = row.getFirstCellNum();j < COLUMNS;j++){
		        	  cell = row.getCell(j); 
			          if(null != cell) {   
			        	  cellValue = cell.toString();
			        	  if(containsFullChar(cellValue)){
			        		  cellValue=f2h(cellValue);
			        	  }
			        	  cellValue=trim(cellValue);
			          }else{
			        	  cellValue="";
			          }
			          line.add(cellValue);
		          }
	        	  rs.add(line);
	          }
          }catch(Exception e){
        	  e.printStackTrace();
        	  throw new ParseException("解析excel文件出错");
          }
          if(currPosition > sheet.getLastRowNum()) {                                                                                                         
              currPosition = 0;                                                                                                                              
              while(currSheet != numOfSheets -1){                                                                                                            
                  sheet =(HSSFSheet)workbook.getSheetAt(++currSheet);                                                                                                  
                  return proccess();                                                                                                             
              }
          }
          return rs;
	  }                                                                                                                                                         
	                                                                                                                                                            
	  public void close() {                                                                                                                                     
          try {                                                                                                                                             
        	  if(is != null) {                                                                                                                                      
        		  is.close();                                                                                                                                   
        	  }        
        	  if(reader != null) {
        		  reader.close();     
        	  }
          }catch(IOException e) {                                                                                                                           
              is = null;     
              reader = null; 
          }finally{
        	  rs=null;
          }
	  }
	  
	  /**
	     * 半角转全角
	     *
	     * @param input String.
	     * @return 全角字符串.
	     */
	    public static String h2f(String input) {
	        char c[] = input.toCharArray();
	        for (int i = 0; i < c.length; i++) {
	            if (c[i] == ' ') {
	                c[i] = '\u3000';
	            } else if (c[i] < '\177') {
	                c[i] = (char) (c[i] + 65248);

	            }
	        }
	        return new String(c);
	    }


	    /**
	     * 全角转半角
	     *
	     * @param input String.
	     * @return 半角字符串
	     */
	    public static String f2h(String input) {
	        char c[] = input.toCharArray();
	        for (int i = 0; i < c.length; i++) {
	            if (c[i] == '\u3000') {
	                c[i] = ' ';
	            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
	                c[i] = (char) (c[i] - 65248);

	            }
	        }
	        String returnString = new String(c);
	        return returnString;
	    }

	    public static boolean containsFullChar(String input)
	    {
	        Matcher fMatcher = fPattern.matcher(input);
	        if(fMatcher.find())
	        {
	            return true;
	        }
	        return false;
	    }
	    
	    
	    /**去空格(匹配任意的空白符，包括空格，制表符(Tab)，换行符，中文全角空格)
	     * @param str
	     * @return
	     */
	    public String  trim(String str){
	    	 Pattern pattern = Pattern.compile("[\\s\\p{Zs}]");
	         Matcher re = pattern.matcher(str);
	         str=re.replaceAll("");
	         return str;
	    }
}
