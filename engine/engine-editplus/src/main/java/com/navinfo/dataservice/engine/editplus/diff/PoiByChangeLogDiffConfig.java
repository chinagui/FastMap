package com.navinfo.dataservice.engine.editplus.diff;

import org.apache.commons.lang.StringUtils;

/** 
 * @ClassName: PoiByChangeLogDiffConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrcPoiDiffConfig.java
 */
public class PoiByChangeLogDiffConfig extends ObjectDiffConfig {
	protected String changeLog;
	
	public PoiByChangeLogDiffConfig(String changeLog){
		super();
		this.changeLog=changeLog;
		parse();
	}

	@Override
	public void parse() {
		if(StringUtils.isNotEmpty(changeLog)){
			//todo
		}
	}

}
