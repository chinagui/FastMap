package com.navinfo.dataservice.dao.plus.diff;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.obj.ObjectType;

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
		this.objType=ObjectType.IX_POI;
		if(types!=null&&types.size()>0){
			//todo
			for(String type:types){
				if("SIGHT_LEVEL".equals(type)){
					if(this.specTables.containsKey("IX_POI")){
						this.specTables.get("IX_POI").add("KIND_CODE");
						
					}else{
						Set<String> cols = new HashSet<String>();
						cols.add("KIND_CODE");
						this.specTables.put("IX_POI", cols);
					}
				}else if("AIRPORT".equals(type)){
					//his.specTables.put("IX_POI", "IX_POI_NAME");
				}
			}
		}
	}

}
