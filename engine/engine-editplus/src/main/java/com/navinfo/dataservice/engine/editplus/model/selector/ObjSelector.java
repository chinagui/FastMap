package com.navinfo.dataservice.engine.editplus.model.selector;

import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

/** 
 * 
 * @ClassName: ObjSelector
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: ObjSelector.java
 */
public class ObjSelector {

	public static BasicObj selectByPid(String objType,SelectorConfig selConfig,long pid,boolean isOnlyMain,boolean isLock)throws Exception{
		GlmObject glmObj = GlmFactory.getInstance().getTablesByObjType(objType);
		GlmTable mainTable = glmObj.getMainTable();
		Class<?> clazz = Class.forName(mainTable.getModelClassName());
		BasicObj obj = (BasicObj)clazz.getConstructor(long.class).newInstance(pid);
		String sql = "SELECT * FROM "+mainTable.getName()+" WHERE "+mainTable.getPkColumn()+"=?";
		
		return null;
	}

	public static BasicObj selectByRowid(String objType,SelectorConfig selConfig,String rowid,boolean isOnlyMain,boolean isLock){
		return null;
	}

	public static BasicObj selectBySpecColumn(String objType,SelectorConfig selConfig,String colName,Object colValue,boolean isOnlyMain,boolean isLock){
		return null;
	}
	
	public static void selectChildren(BasicObj obj,SelectorConfig selConfig){
		
	}

	public static void selectChildren(BasicObj obj,String specTable){
		
	}
}
