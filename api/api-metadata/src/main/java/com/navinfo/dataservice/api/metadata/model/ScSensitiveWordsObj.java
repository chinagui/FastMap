package com.navinfo.dataservice.api.metadata.model;

import java.io.Serializable;

public class ScSensitiveWordsObj implements Serializable{
	//select sensitive_word,sensitive_word2,kind_code,admincode,type from SC_SENSITIVE_WORDS
	private String sensitiveWord;
	private String sensitiveWord2;
	private String kindCode;
	private String admincode;
	private int type;
	//{"word":word,"orgWord":orgWord,"wordtype":wordtype,"word2":word2,"orgWord2":orgWord2,
	//"word2type":word2type,"kindCode":kindCode,"adminCode":adminCode}
	//根据原始数据翻译成正则表达式的形式。regexWordType/regexWordType2：1表示regexSensitiveWord/regexSensitiveWord2是<>，0表示没有<>
	//主要用于：第二关键字可能会存在“不为XX”的关键字，需要做特殊处理（如：<> %精%）
	private String regexSensitiveWord;
	//地址的正则表达式(特殊处理)
	private String regexSensitiveWordAddress;
	private int regexWordType;
	private String regexSensitiveWord2;
	private int regexWordType2;
	private String regexKindCode;
	private String regexAdmincode;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSensitiveWord2() {
		return sensitiveWord2;
	}
	public void setSensitiveWord2(String sensitiveWord2) {
		this.sensitiveWord2 = sensitiveWord2;
	}
	public String getSensitiveWord() {
		return sensitiveWord;
	}
	public void setSensitiveWord(String sensitiveWord) {
		this.sensitiveWord = sensitiveWord;
	}
	public String getKindCode() {
		return kindCode;
	}
	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}
	public String getAdmincode() {
		return admincode;
	}
	public void setAdmincode(String admincode) {
		this.admincode = admincode;
	}
	public String getRegexSensitiveWord() {
		return regexSensitiveWord;
	}
	public void setRegexSensitiveWord(String regexSensitiveWord) {
		this.regexSensitiveWord = regexSensitiveWord;
	}
	public int getRegexWordType() {
		return regexWordType;
	}
	public void setRegexWordType(int regexWordType) {
		this.regexWordType = regexWordType;
	}
	public String getRegexSensitiveWord2() {
		return regexSensitiveWord2;
	}
	public void setRegexSensitiveWord2(String regexSensitiveWord2) {
		this.regexSensitiveWord2 = regexSensitiveWord2;
	}
	public int getRegexWordType2() {
		return regexWordType2;
	}
	public void setRegexWordType2(int regexWordType2) {
		this.regexWordType2 = regexWordType2;
	}
	public String getRegexKindCode() {
		return regexKindCode;
	}
	public void setRegexKindCode(String regexKindCode) {
		this.regexKindCode = regexKindCode;
	}
	public String getRegexAdmincode() {
		return regexAdmincode;
	}
	public void setRegexAdmincode(String regexAdmincode) {
		this.regexAdmincode = regexAdmincode;
	}
	public String getRegexSensitiveWordAddress() {
		return regexSensitiveWordAddress;
	}
	public void setRegexSensitiveWordAddress(String regexSensitiveWordAddress) {
		this.regexSensitiveWordAddress = regexSensitiveWordAddress;
	}
}
