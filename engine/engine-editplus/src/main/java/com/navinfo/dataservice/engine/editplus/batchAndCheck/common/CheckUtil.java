package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtil {
	public static boolean blankRuleTable(String word){
		String[] wordList = word.split("");
		for(int i=0;i<wordList.length;i++){
			boolean checkResult=false;
			//空格在word的中间位置
			if(wordList[i].equals(" ") && i>0 && i<wordList.length-1){
				String beforeWord=wordList[i-1];
				String afterWord=wordList[i+1];
				if(afterWord.equals(" ")){continue;}
				if(isChinese(beforeWord)){
					if(isChinese(afterWord) && hasSamePart(wordList, i)){
						checkResult=true;
					}
				}else if(isLetter(beforeWord)){
					if(isLetter(afterWord)||isDigit(afterWord)){
						checkResult=true;
					}
				}else if(isDigit(beforeWord)){
					if(isLetter(afterWord)||isDigit(afterWord)){
						checkResult=true;
					}
				}
				if(checkResult){continue;}
				else{return checkResult;}
			}
		}
		return true;
	}
	
	public static boolean isChinese(String str){
		Pattern p1 = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m1 = p1.matcher(str);
		if (m1.matches()) {
			return true;
		}
		return false;
	}
	
	public static boolean isDigit(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

    public static boolean isLetter(String str){
    	Pattern p1 = Pattern.compile("[a-zA-Z]{1}");
		Matcher m1 = p1.matcher(str);
		if (m1.matches()) {
			return true;
		}
    	return false;
    }
    /**
     * wordList="下铺 下铺"，index=2，返回true；否则false
     * @param wordList
     * @param index
     * @return
     */
    public static boolean hasSamePart(String[] wordList,int index){
    	for(int i=0;i<index;i++){
    		int len=wordList.length;
    		int tt=wordList.length-(index+2);
    		if(i<=tt){
    			String str1="";
    			for(int j=i;j>=0;j--){str1+=wordList[index-1-j];};
    			String str2="";
    			for(int j=0;j<=i;j++){str2+=wordList[index+1+j];};
    			if(str1.equals(str2)){return true;}
    		}
    	}
        return false;
    }
    
    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
    public static String strQ2B(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
        	if (c[i] == '\u3000') {c[i] = ' ';}
        	else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {c[i] = (char) (c[i] - 65248);}
        	}
        String returnString = new String(c);
        return returnString;
    }
}
