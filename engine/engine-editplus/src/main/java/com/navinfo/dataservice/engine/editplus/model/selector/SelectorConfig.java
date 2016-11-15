package com.navinfo.dataservice.engine.editplus.model.selector;

import java.util.Set;

/** 
 * 对象主表一定会加载，无须配置主表
 * specTables比filterTables优先级高
 * specTables!=null直接使用指定表加载
 * specTables==null再使用glm模型过滤filterTables加载
 * 两者都==null，glm中全部子表都加载，两者都==null时，可以不定义Config，Selector中直接传入null
 * @ClassName: SelectorConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: SelectorConfig.java
 */
public abstract class SelectorConfig {
	protected Set<String> specTables;//指定表，见类说明
	protected Set<String> filterTables;//过滤表，见类说明
	
	protected SelectorConfig(){
		parse();
	}
	public Set<String> getSpecTables() {
		return specTables;
	}
	public Set<String> getFilterTables() {
		return filterTables;
	}
	/**
	 * 重写配置or从配置文件加载
	 */
	protected abstract void parse();
}
