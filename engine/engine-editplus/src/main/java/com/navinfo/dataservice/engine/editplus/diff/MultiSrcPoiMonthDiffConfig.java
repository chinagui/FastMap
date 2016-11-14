package com.navinfo.dataservice.engine.editplus.diff;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/** 
 * @ClassName: PoiByChangeLogDiffConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrcPoiDiffConfig.java
 */
public class MultiSrcPoiMonthDiffConfig extends ObjectDiffConfig {
	protected Collection<String> types;
	
	public MultiSrcPoiMonthDiffConfig(Collection<String> types){
		super();
		this.types=types;
		parse();
	}

	@Override
	public void parse() {
		if(types!=null&&types.size()>0){
			//todo
		}
	}

}
