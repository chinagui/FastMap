package com.navinfo.dataservice.api.metadata.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** 
 * @ClassName: MetadataMap
 * @author gaopengrong
 * @date 2017年3月1日
 * @Description: MetadataMap.java
 */
public class MetadataMap implements Serializable {
	private Map<String,String> chain;
	private Map<String,String> kindCode;
	private Map<String,String> admin;
	private Map<String,String> character;
	private Map<String,String> kind;
	private Map<String,String> engshort;
	private Map<String,List<String>> navicovpy;
	private Map<String,String> nameUnifyShort;
	private Map<String,String> chishort;
	private Map<String,String> aliasName;
	
	private static class SingletonHolder {
		private static final MetadataMap INSTANCE = new MetadataMap();
	}

	public static final MetadataMap getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Map<String, String> getChain() {
		return chain;
	}

	public void setChain(Map<String, String> chain) {
		this.chain = chain;
	}

	public Map<String, String> getKindCode() {
		return kindCode;
	}

	public void setKindCode(Map<String, String> kindCode) {
		this.kindCode = kindCode;
	}

	public Map<String, String> getAdmin() {
		return admin;
	}

	public void setAdmin(Map<String, String> admin) {
		this.admin = admin;
	}

	public Map<String, String> getCharacter() {
		return character;
	}

	public void setCharacter(Map<String, String> character) {
		this.character = character;
	}

	public Map<String, String> getKind() {
		return kind;
	}

	public void setKind(Map<String, String> kind) {
		this.kind = kind;
	}

	public Map<String, String> getEngshort() {
		return engshort;
	}

	public void setEngshort(Map<String, String> engshort) {
		this.engshort = engshort;
	}

	public Map<String, List<String>> getNavicovpy() {
		return navicovpy;
	}

	public void setNavicovpy(Map<String, List<String>> navicovpy) {
		this.navicovpy = navicovpy;
	}

	public Map<String, String> getNameUnifyShort() {
		return nameUnifyShort;
	}

	public void setNameUnifyShort(Map<String, String> nameUnifyShort) {
		this.nameUnifyShort = nameUnifyShort;
	}

	public Map<String, String> getChishort() {
		return chishort;
	}

	public void setChishort(Map<String, String> chishort) {
		this.chishort = chishort;
	}

	public Map<String, String> getAliasName() {
		return aliasName;
	}

	public void setAliasName(Map<String, String> aliasName) {
		this.aliasName = aliasName;
	}

}
