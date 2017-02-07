package com.navinfo.dataservice.engine.meta.scSensitiveWords;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.model.ScSensitiveWordsObj;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

public class ScSensitiveWords {
	
	private Map<Integer, List<ScSensitiveWordsObj>> sensitiveWordMap= new HashMap<Integer, List<ScSensitiveWordsObj>>();

	private static class SingletonHolder {
		private static final ScSensitiveWords INSTANCE = new ScSensitiveWords();
	}

	public static final ScSensitiveWords getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**
	 * select sensitive_word,sensitive_word2,kind_code,admincode,type from SC_SENSITIVE_WORDS
	 * @return Map<Integer, List<ScSensitiveWordsObj>>:key，type;value:ScSensitiveWordsObj列表
	 * @throws Exception
	 */
	public Map<Integer, List<ScSensitiveWordsObj>> scSensitiveWordsMap() throws Exception{
		if (sensitiveWordMap==null||sensitiveWordMap.isEmpty()) {
				synchronized (this) {
					if (sensitiveWordMap==null||sensitiveWordMap.isEmpty()) {
						try {
							String sql = "select sensitive_word,sensitive_word2,kind_code,admincode,type from SC_SENSITIVE_WORDS";
								
							PreparedStatement pstmt = null;
							ResultSet rs = null;
							Connection conn = null;
							try {
								conn = DBConnector.getInstance().getMetaConnection();
								pstmt = conn.prepareStatement(sql);
								rs = pstmt.executeQuery();
								while (rs.next()) {
									String sensitiveWord=rs.getString("sensitive_word");
									String regexSensitiveWord=rs.getString("sensitive_word");
									int regexWordType=0;
									if(regexSensitiveWord!=null&&!regexSensitiveWord.isEmpty()){
										if(regexSensitiveWord.startsWith("<>")){
											regexWordType=1;
											regexSensitiveWord=regexSensitiveWord.replace("<>", "");
										}
										regexSensitiveWord=changWord2Re(regexSensitiveWord);
									}
									String sensitiveWord2=rs.getString("sensitive_word2");
									String regexSensitiveWord2=rs.getString("sensitive_word2");
									int regexWordType2=0;
									if(regexSensitiveWord2!=null&&!regexSensitiveWord2.isEmpty()){
										if(regexSensitiveWord2.startsWith("<>")){
											regexWordType2=1;
											regexSensitiveWord2=regexSensitiveWord2.replace("<>", "");
										}
										regexSensitiveWord2=changWord2Re(regexSensitiveWord2);
									}
									String kindCode=rs.getString("kind_code");
									String regexKindCode=rs.getString("kind_code");
									if(regexKindCode!=null&&!regexKindCode.isEmpty()){regexKindCode=changWord2Re(regexKindCode);}
									String adminCode=rs.getString("admincode");
									String regexAdminCode=rs.getString("admincode");
									if(regexAdminCode!=null&&!regexAdminCode.isEmpty()){regexAdminCode=changWord2Re(regexAdminCode);}
									int type=rs.getInt("type"); 
									ScSensitiveWordsObj senWordsObj=new ScSensitiveWordsObj();
									senWordsObj.setSensitiveWord(sensitiveWord);
									senWordsObj.setSensitiveWord2(sensitiveWord2);
									senWordsObj.setKindCode(kindCode);
									senWordsObj.setAdmincode(adminCode);
									senWordsObj.setType(type);
									senWordsObj.setRegexSensitiveWord(regexSensitiveWord);
									senWordsObj.setRegexWordType(regexWordType);
									senWordsObj.setRegexSensitiveWord2(regexSensitiveWord2);
									senWordsObj.setRegexWordType2(regexWordType2);
									senWordsObj.setRegexKindCode(regexKindCode);
									senWordsObj.setRegexAdmincode(regexAdminCode);
									if(!sensitiveWordMap.containsKey(type)){
										sensitiveWordMap.put(type, new ArrayList<ScSensitiveWordsObj>());
									}
									sensitiveWordMap.get(type).add(senWordsObj);} 
							} catch (Exception e) {
								throw new Exception(e);
							} finally {
								DbUtils.close(conn);
							}
						} catch (Exception e) {
							throw new SQLException("加载sensitiveWordMap失败："+ e.getMessage(), e);
						}
					}
				}
			}
			return sensitiveWordMap;
	}
	
	private String changWord2Re(String word){
        if(!word.contains("%")){
            if(word.equals("连续三位及三位以上数字")){
                return ".*[零一二三四五六七八九十〇0-9０-９]{3,}.*";}
            word="^("+word+")+$";
            return word;}
        if(word.startsWith("%")){word=".*("+word.substring(1);}
        else{word="("+word;}
        if(word.endsWith("%")){word=word.substring(0, word.length()-1)+")+.*";}
        else{word=word+")+$";}
        if(word.contains("%")){word=word.replace("%",")+.*(");}
        return word;
	}
	
}
