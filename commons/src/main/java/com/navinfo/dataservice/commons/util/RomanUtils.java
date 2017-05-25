/**
 * 
 */
package com.navinfo.dataservice.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** 
 * @ClassName: RomanUtils.java
 * @author y
 * @date 2016-6-24下午4:05:46
 * @Description: 罗马字符处理。
 *  
 */
public class RomanUtils {
	
	//匹配罗马数字的正则,但是由于每一个都可能是0个 空字符串也会被匹配出来 需要后期在程序里再处理
	private static	String regex = "(-| +|^)M{0,9}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})( +|$)";
    private static String ROMAN_REGEX = "[ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ]{1,}";
	
	
	/**
	 * @Description:将字符串中的罗马字符替换为阿拉伯数字
	 * @param value
	 * @return
	 * @author: y
	 * @time:2016-6-24 下午4:15:27
	 */
	public static String replaceAllRoman2RrabicNum(String value){
		Pattern p = Pattern.compile(ROMAN_REGEX);
		Matcher matcher = p.matcher(value);
		
		while (matcher.find()) {
			String srcStr = matcher.group();
			String num=translateRoman(srcStr);
			if(!"0".equals(num)){
				value=value.replace(srcStr,num);
			}
		}

		
		return value;
	}

    private static String translateRoman(String value) {
        value = value.replace("Ⅰ", "1");
        value = value.replace("Ⅱ", "2");
        value = value.replace("Ⅲ", "3");
        value = value.replace("Ⅳ", "4");
        value = value.replace("Ⅴ", "5");
        value = value.replace("Ⅵ", "6");
        value = value.replace("Ⅶ", "7");
        value = value.replace("Ⅷ", "8");
        value = value.replace("Ⅸ", "9");
        value = value.replace("Ⅹ", "10");
        value = value.replace("Ⅺ", "11");
        value = value.replace("Ⅻ", "12");
        value = value.replace("ⅰ", "1");
        value = value.replace("ⅱ", "2");
        value = value.replace("ⅲ", "3");
        value = value.replace("ⅳ", "4");
        value = value.replace("ⅴ", "5");
        value = value.replace("ⅵ", "6");
        value = value.replace("ⅶ", "7");
        value = value.replace("ⅷ", "8");
        value = value.replace("ⅸ", "9");
        value = value.replace("ⅹ", "10");
        return value;
    }
	
	//罗马数字转阿拉伯数字：
    // 从前往后遍历罗马数字，如果某个数比前一个数小，则把该数加入到结果中；
    // 反之，则在结果中两次减去前一个数并加上当前这个数；
    // I、V、X、   L、   C、     D、     M
    // 1．5、10、50、100、500、1000
    private static String r2a(String in){
        int graph[] = new int[400];
        graph['I'] = 1;
        graph['V']=5;
        graph['X']=10;
        graph['L']=50;
        graph['C']=100;
        graph['D']=500;
        graph['M']=1000;
        char[] num = in.toCharArray();
        // 遍历这个数，用sum来总计和
        int sum = graph[num[0]];
        for(int i=0; i<num.length-1; i++){
            // 如果，i比i+1大的话，直接相加
            if(graph[num[i]] >= graph[num[i+1]]){
                sum += graph[num[i+1]];
            }
            // 如果i比i+1小的话，则将总和sum减去i这个地方数的两倍，同时加上i+1
            // 就相当于后边的数比左边的数大，则用右边的数减左边的数
            else{
                sum = sum + graph[num[i+1]] - 2*graph[num[i]];
            }
        }
        return String.valueOf(sum);
    }
    
    
    /**
     * @Description:给一个罗马字符，返回阿拉伯数字
     * @param romanNum
     * @return:
     * @author: y
     * @throws Exception 
     * @time:2016-6-24 下午4:19:07
     */
    public  static  int getRrabicNum(String romanNum) throws Exception{
    	 int num;
    	 if (romanNum.length() == 0)        //输入为空
         {
       	  throw new Exception("罗马字符串不能为空");
         }
   
         romanNum = romanNum.toUpperCase();  //所有罗马数字都转换为大写
         
         int i = 0;       //记录罗马数字每个字符的位置
         int arabic = 0;  //转换后的阿拉伯数字
         
         while (i < romanNum.length()) 
         {
            char letter = romanNum.charAt(i);        // 罗马数字当前位置的字符
            int number = letterToNumber(letter);  // 字符转化为阿拉伯数字
            
            if (number < 0)
            {
           	 throw new RomanUtils().new RomanException("罗马数字中不包含"+letter);
            }
            
            i++;         //移动到字符串的下一个位置
            
            if (i == romanNum.length())   //罗马数字已处理完毕
            {
           	 arabic += number;
            }
            else 
            {
           	 	char nextLetter = romanNum.charAt(i);
           	 	int nextNumber = letterToNumber(nextLetter);
               
  	                if (nextNumber > number)  //后边的字符比前边的大
  	                {  
  		                   int result = nextNumber - number;
  		                   
  		                   if(result == 4 || result == 9 || result == 40 || result == 90 || result == 400 || result == 900)
  		                   {
  		                	   arabic += result;
  		                	   i++;
  		                	   
  		                	   if(i == romanNum.length())     //罗马数字已处理完毕
  		                	   {
  		                		   break;
  		                	   }
  		                	   else
  		                	   {
  		                    	   char  afterNextLetter = romanNum.charAt(i);   
  		                    	   int afterNextNumber = letterToNumber(afterNextLetter);
  		                    	   
  		                    	   if(afterNextNumber > result)
  		                    	   {
  		                    		   throw new RomanUtils().new RomanException("不合法的罗马数字"+letter+nextLetter+afterNextLetter);
  		                    	   }
  		                	   }
  		                   }
  		                   else 
  		                   {
  		                	 throw new RomanUtils().new  RomanException("不合法的罗马数字"+letter+nextLetter);
  		                   }
  	                 }
  	                 else 
  	                 {
  		                	if((number==5 || number==50 ||number==500)&& number == nextNumber)   //V、L、D用于大数右边（相加），使用超过1次。
  			             	{
  		                		throw new RomanUtils().new RomanException("不合法的罗马数字"+letter+nextLetter);
  			             	}
  		                	
  		                	if(number == nextNumber)   
  		                	{
  		                		i++;    //还要再看下一个字符
  		                		
  		                		if (i == romanNum.length())   //罗马数字已处理完毕
  		                        {
  		                           arabic += number+nextNumber;
  		                           break;
  		                        }
  		                		
  		                		char  afterNextLetter = romanNum.charAt(i);   
  		                 	    int afterNextNumber = letterToNumber(afterNextLetter);
  		                 	    
  		                 	    if(afterNextNumber > nextNumber) //I、X、C在在大数左边（即相减时）使用超过2个
  		                 	    {
  		                 	    	throw new RomanUtils().new RomanException("不合法的罗马数字"+letter+nextLetter+afterNextLetter);
  		                 	    }
  		                 	    else if(afterNextNumber == nextNumber)  //出现3个字符都相同的情况，如III
  		                 	    {
  		                 	    	i++;   //还要再看下一个字符,可能会出现IIII这种情况（不允许的，应抛出异常）
  		                 	    	
  		                 	    	if (i == romanNum.length())   //罗马数字已处理完毕
  			                        {
  		                 	    		arabic += number+nextNumber+afterNextNumber;
  			                            break;
  			                        }
  		                 	    	
  		                 	    	char  afterNextNextLetter = romanNum.charAt(i);   
  			                 	    int afterNextNextNumber = letterToNumber(afterNextNextLetter);
  			                 	    
  		                 	    	if(afterNextNextNumber == afterNextNumber)   //出现IIII这种情况
  		                 	    	{
  		                 	    		throw new RomanUtils().new RomanException("不合法的罗马数字"+letter+nextLetter+afterNextLetter+afterNextNextLetter);
  		                 	    	}
  		                 	    	else 
  		                 	    	{
  		                 	    		arabic += number;
  		                 	    		i=i-2;                      //回退2个字符（因为考虑了4个字符）
  		                 	    	}
  		                 	    }
  		                 	    else 
  		                 	    {
  		                 	    	arabic += number+nextNumber;
  		                 	    }
  		                	}
  			                else
  			                {
  			                	arabic += number;
  			                }
  	                 }
            }  
         } 
         
         if (arabic > 3999)
         {
            throw new Exception("输入的数字不能超过3999");
         }
            
         num = arabic;
         return num;
    }
    
    
    /**
     * 罗马字符转换为阿拉伯数字
     * @param letter  罗马字符
     * @return 正常罗马字符，返回阿拉伯数字；否则，返回-1
     */
    private static int letterToNumber(char letter) 
    {
       switch (letter) 
       {
          case 'I':  return 1;
          case 'V':  return 5;
          case 'X':  return 10;
          case 'L':  return 50;
          case 'C':  return 100;
          case 'D':  return 500;
          case 'M':  return 1000;
          default:   return -1;
       }
    }
    
    
    
    class RomanException extends Exception{
    	
   	 private static final long serialVersionUID = 1L;
   	
   	String message;
   	Throwable cause;
   	
   	
   	public RomanException(String message, Throwable cause) {
   		super();
   		this.message = message;
   		this.cause = cause;
   	}
   	
   	
   	public RomanException(String message) {
   		super();
   		this.message = message;
   	}
   	
   	

   }

    public static void main(String[] args) {
        String name="健德门  CI 街道 VI 号";
        name = "ｚｒｍⅠ号ⅡsanⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ测试路";
//        name="健德门  CI 街道 ⅹ 号";
        // 全角转半角(先转换成半角)
        name = ExcelReader.f2h(name);
        name = name.replace("#", "号");
        // 将罗马字符转为阿拉伯数字
        //I V X L C D M
        name = RomanUtils.replaceAllRoman2RrabicNum(name);

        System.out.println(name);
//		System.out.println(RomanUtils.replaceAllRoman2RrabicNum(name));
    }

}
