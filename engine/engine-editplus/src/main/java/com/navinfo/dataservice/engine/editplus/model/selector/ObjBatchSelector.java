package com.navinfo.dataservice.engine.editplus.model.selector;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.vividsolutions.jts.geom.Polygon;

/** 
 * @ClassName: ObjBatchSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: ObjBatchSelector.java
 */
public class ObjBatchSelector {

	public static List<BasicObj> selectByPids(String objType,SelectorConfig selConfig,Collection<Long> pids,boolean isOnlyMain,boolean isLock){
		
		return null;
	}

	public static List<BasicObj> selectByRowids(String objType,SelectorConfig selConfig,Collection<String> rowids,boolean isOnlyMain,boolean isLock){
		return null;
	}

	public static List<BasicObj> selectBySpecColumn(String objType,SelectorConfig selConfig,String colName,Collection<Object> colValues,boolean isOnlyMain,boolean isLock){
		return null;
	}
	
	public static List<BasicObj> selectByPolygon(String objType,SelectorConfig selConfig,Polygon polygon,boolean isOnlyMain,boolean isLock){
		return null;
	}
	public static List<BasicObj> selectByMeshIds(String objType,SelectorConfig selConfig,Collection<String> meshIds,boolean isOnlyMain,boolean isLock){
		return null;
	}
}
