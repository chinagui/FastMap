package com.navinfo.dataservice.expcore.source.parameter;

/** 
 * @ClassName: ScriptsConfig 
 * @author Xiao Xiaowen 
 * @date 2015-11-4 上午10:04:01 
 * @Description: TODO
 *  
 */
public class ScriptsConfig {
	//String gdbTye;//indoor,au,meta,...
	//String scale;//2.5w,20w,100w,top
	String expMode;//
	String feature;//all,poi,link,face,pt,...
	String condition;//full,by-mesh,by-area,...
	String replacerClassName;
	String mainScript;
	
	public ScriptsConfig(String expMode,String feature,String condition,String mainScript){
		this.expMode=expMode;
		this.feature=feature;
		this.condition=condition;
		this.mainScript=mainScript;
	}
	/**
	 * @return the expMode
	 */
	public String getExpMode() {
		return expMode;
	}
	/**
	 * @param expMode the expMode to set
	 */
	public void setExpMode(String expMode) {
		this.expMode = expMode;
	}
	/**
	 * @return the feature
	 */
	public String getFeature() {
		return feature;
	}
	/**
	 * @param feature the feature to set
	 */
	public void setFeature(String feature) {
		this.feature = feature;
	}
	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}
	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
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
