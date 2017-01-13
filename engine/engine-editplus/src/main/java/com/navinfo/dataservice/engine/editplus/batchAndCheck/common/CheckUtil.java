package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.search.AdAdminSearch;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;

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
	
	/**
	 * 检查规则的《空格规则表》，返回对应的提示信息
	 * @param word
	 * @return boolean 符合《空格规则表》，返回true；否则返回false
	 */
	public static String blankRuleErrStr(String word){
		if (word == null) {
			return null;
		}
		int spaceIndex = word.indexOf(' ');
		if (spaceIndex > 0 && spaceIndex<word.length()-1) {
			String beforeWord=word.substring(0, spaceIndex-1);
			String afterWord=word.substring(spaceIndex+1);
			if (isLetter(beforeWord) && isChinese(afterWord)) {
				return word + ",空格前是英文，空格后中文";
			}
			if (isLetter(beforeWord) && isOther(afterWord)) {
				return word + ",空格前是英文，空格后是其他的符号";
			}
			if (isChinese(beforeWord) && isLetter(afterWord)) {
				return word + ",空格前是中文，空格后为英文";
			}
			if (isChinese(beforeWord) && isChinese(afterWord)) {
				if (!beforeWord.equals(afterWord)) {
					return word + ",空格前是中文，空格后中文且和前面的中文不同";
				}
			}
			if (isChinese(beforeWord) && isDigit(afterWord)) {
				return word + ",空格前是中文，空格后为数字";
			}
			if (isChinese(beforeWord) && isOther(afterWord)) {
				return word + ",空格前是中文，空格后是其他的符号";
			}
			if (isDigit(beforeWord) && isChinese(afterWord)) {
				return word + ",空格前是数字，空格后中文";
			}
			if (isDigit(beforeWord) && isOther(afterWord)) {
				return word + ",空格前是数字，空格后是其他的符号";
			}
			if (isDigit(beforeWord) && isOther(afterWord)) {
				return word + ",空格前是数字，空格后是其他的符号";
			}
			if (isOther(beforeWord)) {
				return word + ",空格前是不能是非数字、字母、中文之外的字符";
			}
		}
		return null;
	}
	
	public static boolean isOther(String str) {
		if (isChinese(str) || isDigit(str) || isLetter(str)) {
			return false;
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
     * 是否合法英文字符
     * @param str
     * @return 字符为数字，字母，符号（-_/:;'""~^.,?!*()<>$%&#@+半角空格），返回true；否则false
     */
    public static boolean isValidEngChar(String str){
    	Pattern p1 = Pattern.compile("[A-Za-z0-9\\-_/:;'\"~^.,?!*()<>$%&#@+ ]{1}");
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
     * 1） 回车符检查：包含回车符的记录；
     * 2） Tab符检查：包含Tab符号的记录；
     * 3） 多个空格检查：两个及两个以上连续空格的记录；
     * 4） 前后空格检查：名称开始前或者结尾处包含空格的记录；
     * @param word
     * @return 错误原因列表 ["前后空格","多个空格"]
     */
    public static List<String> checkIllegalBlank(String word){
    	List<String> errorList=new ArrayList<String>();
    	//2、前后空格检查：不能以空格开头或结尾；
		if(word.startsWith(" ")||word.endsWith(" ")){
			errorList.add("前后空格");
		}
		//3、多个空格检查：不能出现连续空格；
		if (word.contains("  ")) {
			errorList.add("多个空格");
		}
		//4、回车符检查：不能包含回车符；
		Pattern pattern = Pattern.compile("\\r|\n");
		Matcher matcher = pattern.matcher(word);
		if (matcher.find()){
			errorList.add("回车符");
		}
		//5、Tab符检查：不能包含Tab符号；
		if (word.contains("\t")) {
			errorList.add("Tab符");
		}
		return errorList;
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
    
    /**
     * 将地址拆分的18个字段合成一个地址
     * @param addr
     * @return
     * @throws Exception
     */
    public static String getMergerAddr(IxPoiAddress addr) throws Exception {
		String mergeAddr = "";
		if (addr.getProvince() != null) {
			mergeAddr += addr.getProvince();
		}
		if (addr.getCity() != null) {
			mergeAddr += addr.getCity();
		}
		if (addr.getCounty() != null) {
			mergeAddr += addr.getCounty();
		}
		if (addr.getTown() != null) {
			mergeAddr += addr.getTown();
		}
		if (addr.getPlace() != null) {
			mergeAddr += addr.getPlace();
		}
		if (addr.getStreet() != null) {
			mergeAddr += addr.getStreet();
		}
		if (addr.getLandmark() != null) {
			mergeAddr += addr.getLandmark();
		}
		if (addr.getPrefix() != null) {
			mergeAddr += addr.getPrefix();
		}
		if (addr.getHousenum() != null) {
			mergeAddr += addr.getHousenum();
		}
		if (addr.getType() != null) {
			mergeAddr += addr.getType();
		}
		if (addr.getSubnum() != null) {
			mergeAddr += addr.getSubnum();
		}
		if (addr.getSurfix() != null) {
			mergeAddr += addr.getSurfix();
		}
		if (addr.getEstab() != null) {
			mergeAddr += addr.getEstab();
		}
		if (addr.getBuilding() != null) {
			mergeAddr += addr.getBuilding();
		}
		if (addr.getUnit() != null) {
			mergeAddr += addr.getUnit();
		}
		if (addr.getFloor() != null) {
			mergeAddr += addr.getFloor();
		}
		if (addr.getRoom() != null) {
			mergeAddr += addr.getRoom();
		}
		if (addr.getAddons() != null) {
			mergeAddr += addr.getAddons();
		}
		return mergeAddr;
	}
    /**
     * 将地址拆分的18个字段拼音合成一个
     * @param addr
     * @return
     * @throws Exception
     */
    public static String getMergerAddrPhonetic(IxPoiAddress addr) throws Exception {
		String mergeAddrPhonetic = "";
		if (addr.getProvPhonetic() != null) {
			mergeAddrPhonetic += addr.getProvPhonetic();
		}
		if (addr.getCityPhonetic() != null) {
			mergeAddrPhonetic += addr.getCityPhonetic();
		}
		if (addr.getCountyPhonetic() != null) {
			mergeAddrPhonetic += addr.getCountyPhonetic();
		}
		if (addr.getTownPhonetic() != null) {
			mergeAddrPhonetic += addr.getTownPhonetic();
		}
		if (addr.getPlacePhonetic() != null) {
			mergeAddrPhonetic += addr.getPlacePhonetic();
		}
		if (addr.getStreetPhonetic() != null) {
			mergeAddrPhonetic += addr.getStreetPhonetic();
		}
		if (addr.getLandmarkPhonetic() != null) {
			mergeAddrPhonetic += addr.getLandmarkPhonetic();
		}
		if (addr.getPrefixPhonetic() != null) {
			mergeAddrPhonetic += addr.getPrefixPhonetic();
		}
		if (addr.getHousenumPhonetic() != null) {
			mergeAddrPhonetic += addr.getHousenumPhonetic();
		}
		if (addr.getTypePhonetic() != null) {
			mergeAddrPhonetic += addr.getTypePhonetic();
		}
		if (addr.getSubnumPhonetic() != null) {
			mergeAddrPhonetic += addr.getSubnumPhonetic();
		}
		if (addr.getSurfixPhonetic() != null) {
			mergeAddrPhonetic += addr.getSurfixPhonetic();
		}
		if (addr.getEstabPhonetic() != null) {
			mergeAddrPhonetic += addr.getEstabPhonetic();
		}
		if (addr.getBuildingPhonetic() != null) {
			mergeAddrPhonetic += addr.getBuildingPhonetic();
		}
		if (addr.getUnitPhonetic() != null) {
			mergeAddrPhonetic += addr.getUnitPhonetic();
		}
		if (addr.getFloorPhonetic() != null) {
			mergeAddrPhonetic += addr.getFloorPhonetic();
		}
		if (addr.getRoomPhonetic() != null) {
			mergeAddrPhonetic += addr.getRoomPhonetic();
		}
		if (addr.getAddonsPhonetic() != null) {
			mergeAddrPhonetic += addr.getAddonsPhonetic();
		}
		return mergeAddrPhonetic;
	}
    /**
     * 查询街巷名与道路名是否匹配
     * @param input String.
     * @return boolean
     * @throws Exception 
     */
    public static boolean matchStreet(String street,int regionId,Connection connRegion) throws Exception {
    	Connection connMeta = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	try{
    		AdAdminSearch adAdminSearch = new AdAdminSearch(connRegion);
			AdAdmin adAdmin = (AdAdmin) adAdminSearch.searchDataByPid(regionId);
			int adminId=adAdmin.getAdminId();
			String admin=String.valueOf(adminId).substring(0, 2);
			
			connMeta = DBConnector.getInstance().getMetaConnection();
	    	String spName = "SELECT COUNT(1) ct FROM rd_name r WHERE r.admin_id<>214 AND r.lang_code='CHI' AND r.admin_id like '"+admin+"%' AND r.name='"+street+"'";
	    	pstmt = connMeta.prepareCall(spName);
	    	rs = pstmt.executeQuery();
			while (rs.next()) {
				if(rs.getInt("ct")==0){return true;}					
			}
			return false;
    	} catch (Exception e) {
    		throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(connMeta);
		}
    }
    /**
     * FMGLM60377
     * @param input String.
     * @return boolean
     * @throws Exception 
     */
    public static boolean matchAdminName(String data,int regionId,Connection connRegion) throws Exception {
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	try{
	    	String spName = "SELECT adn.name FROM ad_admin_name adn WHERE adn.region_id="+regionId+" AND adn.lang_code IN ('CHI','CHT') AND adn.name_class=1";
	    	pstmt = connRegion.prepareCall(spName);
	    	rs = pstmt.executeQuery();
			while (rs.next()) {
				if(rs.getString("name").equals(data)){return true;}					
			}
			return false;
    	} catch (Exception e) {
    		throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(connRegion);
		}
    }
    
}
