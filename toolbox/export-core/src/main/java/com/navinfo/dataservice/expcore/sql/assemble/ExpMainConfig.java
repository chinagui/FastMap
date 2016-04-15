package com.navinfo.dataservice.expcore.sql.assemble;

import java.util.Set;

/** 
 * @ClassName: ScriptsConfig 
 * @author Xiao Xiaowen 
 * @date 2015-11-4 上午10:04:01 
 * @Description: TODO
 *  
 */
public class ExpMainConfig {
	Set<String> expMode;//copy,delete,cut
	Set<String> feature;//all,poi,link,face,pt,...
	Set<String> condition;//full,by-mesh,by-area,...
	String replacerClassName;
	String mainScript;
	
	public ExpMainConfig(Set<String> expMode,Set<String> feature,Set<String> condition,String mainScript){
		this.expMode=expMode;
		this.feature=feature;
		this.condition=condition;
		this.mainScript=mainScript;
	}

	public Set<String> getExpMode() {
		return expMode;
	}

	public void setExpMode(Set<String> expMode) {
		this.expMode = expMode;
	}

	public Set<String> getFeature() {
		return feature;
	}

	public void setFeature(Set<String> feature) {
		this.feature = feature;
	}

	public Set<String> getCondition() {
		return condition;
	}

	public void setCondition(Set<String> condition) {
		this.condition = condition;
	}

	/**
	 * @return the replacerClassName
	 */
	public String getReplacerClassName() {
		return replacerClassName;
	}
	/**
	 * @param replacerClassName the replacerClassName to set
	 */
	public void setReplacerClassName(String replacerClassName) {
		this.replacerClassName = replacerClassName;
	}
	/**
	 * @return the mainScript
	 */
	public String getMainScript() {
		return mainScript;
	}
	/**
	 * @param mainScript the mainScript to set
	 */
	public void setMainScript(String mainScript) {
		this.mainScript = mainScript;
	}

}
