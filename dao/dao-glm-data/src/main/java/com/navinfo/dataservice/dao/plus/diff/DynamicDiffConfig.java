package com.navinfo.dataservice.dao.plus.diff;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
 * 根据变更履历分析只差分部分属性使用动态差分配置对象
 * @ClassName: DynamicDiffConfig
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: DynamicDiffConfig.java
 */
public class DynamicDiffConfig extends ObjectDiffConfig {

	public DynamicDiffConfig(String objType,Map<String,Collection<String>> specTables,Collection<String> filterTables){
		super();
		this.objType=objType;
		this.specTables=specTables;
		this.filterTables=filterTables;
	}

	@Override
	public void parse() {
	}

}
