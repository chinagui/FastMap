package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;

public class ScSensitiveWordsUtils {
	/**
	 * 匹配的敏感词
	 * @param name
	 * @param typeD1
	 * @return
	 * type:1-POI 地址,2-POI 名称及其他
	 */
	public static List<ScSensitiveWordsObj> matchSensitiveWords(String word,String kind,int admin,List<ScSensitiveWordsObj> compareList,int type){
		List<ScSensitiveWordsObj> errorWordList=new ArrayList<ScSensitiveWordsObj>();
		for(ScSensitiveWordsObj sensitiveWordTmp:compareList){
			String kindTmp=sensitiveWordTmp.getRegexKindCode();
			if(kindTmp!=null&&!kindTmp.isEmpty()&&!Pattern.matches(kindTmp, kind)){
				continue;
			}
			String adminTmp=sensitiveWordTmp.getRegexAdmincode();
			if(adminTmp!=null&&!adminTmp.isEmpty()&&!Pattern.matches(adminTmp, String.valueOf(admin))){
				continue;
			}
			String word1Tmp=null;
			//type:1-POI 地址,2-POI 名称及其他
			if(type == 1){
				word1Tmp=sensitiveWordTmp.getRegexSensitiveWordAddress();
			}else if(type == 2){
				word1Tmp=sensitiveWordTmp.getRegexSensitiveWord();
			}
			int wordType1Tmp=sensitiveWordTmp.getRegexWordType();
			if(word1Tmp!=null&&!word1Tmp.isEmpty()&&((wordType1Tmp==0 &&!Pattern.matches(word1Tmp, word))
					||(wordType1Tmp==1 &&Pattern.matches(word1Tmp, word)))){
						continue;
			}
			String word2Tmp=sensitiveWordTmp.getRegexSensitiveWord2();
			int wordType2Tmp=sensitiveWordTmp.getRegexWordType2();
			if(word2Tmp!=null&&!word2Tmp.isEmpty()&&((wordType2Tmp==0 &&!Pattern.matches(word2Tmp, word))
					||(wordType2Tmp==1 &&Pattern.matches(word2Tmp, word)))){
						continue;
			}
			errorWordList.add(sensitiveWordTmp);
		}
	    return errorWordList;
	}
}
