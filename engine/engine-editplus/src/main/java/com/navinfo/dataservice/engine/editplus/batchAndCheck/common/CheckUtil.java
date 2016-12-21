package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtil {
	/**
	 * 检查规则的《空格规则表》，详细内容见检查附件
	 * @param word
	 * @return boolean 符合《空格规则表》，返回true；否则返回false
	 */
	public static boolean blankRuleTable(String word){
		char[] wordList = word.toCharArray();
		for(int i=0;i<wordList.length;i++){
			boolean checkResult=false;
			//空格在word的中间位置
			if(String.valueOf(wordList[i]).equals(" ") && i>0 && i<wordList.length-1){
				String beforeWord=String.valueOf(wordList[i-1]);
				String afterWord=String.valueOf(wordList[i+1]);
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
    public static boolean hasSamePart(char[] wordList,int index){
    	for(int i=0;i<index;i++){
    		if(i<=wordList.length-(index+2)){
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
    /**
     * 若存在括号，则
     *   1、括号“（”和“）”要成对出现；
     *   2、括号“（”和“）”中间必须有内容；
     *   3、不允许括号嵌套
     *   调用isRight获取错误信息。若括号符合要求，则返回None
     * @param word
     * @return String 若括号符合规则，则返回null；否则返回字符串
     */
    public static String isRightKuohao(String word){
    	String wordB=strQ2B(word);
    	String wordLeft = wordB.replace("(", "");
    	String wordRight = wordB.replace(")", "");
    	//左右括号数量不一致
    	if(!(wordLeft.length()==wordRight.length())){
    		return "括号需要成对出现";
    	}
    	//不包含括号
    	if(wordLeft.length()==wordB.length()){return null;}
    	//括号是否成对出现，是否嵌套，是否中间有内容
        int lindex=-1;
        int rindex=-1;
        int tmpRIndex=-1;
        while(true){
        	lindex = wordB.indexOf("(",lindex+1);
            rindex = wordB.indexOf(")",rindex+1);
            if(lindex==-1 || rindex==-1){
            	if(!(lindex==-1 && rindex==-1)){return "括号需要成对出现";}
            	break;
            }
            if(lindex>rindex){return "括号需要成对出现";}
            else if(rindex==lindex+1){return "括号中必须存在内容";}
            if(tmpRIndex!=-1){
            	if(lindex<tmpRIndex){return "不能出现括号嵌套括号情况";}
                tmpRIndex=rindex;
            }else{tmpRIndex=rindex;}
        }
        return null;
    }
}
