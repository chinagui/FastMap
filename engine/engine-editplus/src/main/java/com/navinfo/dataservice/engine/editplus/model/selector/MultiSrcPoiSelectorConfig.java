package com.navinfo.dataservice.engine.editplus.model.selector;

import java.util.HashSet;

import org.apache.log4j.Logger;

/** 
 * @ClassName: IxPoiSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: IxPoiSelector.java
 */
public class MultiSrcPoiSelectorConfig extends SelectorConfig{
	protected Logger log = Logger.getLogger(this.getClass());
	private volatile static MultiSrcPoiSelectorConfig instance=null;
	public static MultiSrcPoiSelectorConfig getInstance(){
		if(instance==null){
			synchronized(MultiSrcPoiSelectorConfig.class){
				if(instance==null){
					instance=new MultiSrcPoiSelectorConfig();
				}
			}
		}
		return instance;
	}
	
	protected MultiSrcPoiSelectorConfig(){
		super();
	}

	/**
	 * 直接写或者从配置文件加载
	 * 主表一定会加载，无须配置主表
	 */
	@Override
	protected void parse() {
		specTables=new HashSet<String>();
		specTables.add("IX_POI_NAME");
		specTables.add("IX_POI_RESTAURANT");
		specTables.add("IX_POI_NAME_FLAG");
		//...
	}

}
