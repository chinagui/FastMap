package com.navinfo.dataservice.dao.plus.selector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;

/** 
 * @ClassName: oBJ
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: oBJ.java
 */
public class ObjChildrenIncreSelector {
	
	/**
	 * 
	 * @param objs:key:对象
	 * @param selConfig
	 * @throws Exception
	 */
	public static void increSelect(Map<String,Map<Long,BasicObj>> objs,Map<String,Set<String>> selConfig)throws Exception{
		if(objs==null) return;
		if(selConfig==null) return;
		for(Map.Entry<String, Map<Long,BasicObj>> entry:objs.entrySet()){
			increSelect(entry.getValue(),selConfig.get(entry.getKey()));
		}
	}
	
	public static void increSelect(Map<Long,BasicObj> objs,Set<String> selConfig){
		if(objs!=null&&objs.size()>0&&selConfig!=null&&selConfig.size()>0){
			//...
			for(String tab:selConfig){
				Map<Long,BasicObj> myObjs = new HashMap<Long,BasicObj>();
				for(Map.Entry<Long, BasicObj> entry:objs.entrySet()){
					BasicObj obj = entry.getValue();
					if(obj.getMainrow().getOpType().equals(OperationType.UPDATE)
							&&obj.getRowsByName(tab)==null){
						myObjs.put(entry.getKey(), obj);
					}
				}
				//sel
			}
		}
	}
}
