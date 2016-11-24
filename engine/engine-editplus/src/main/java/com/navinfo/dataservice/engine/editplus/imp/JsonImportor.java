package com.navinfo.dataservice.engine.editplus.imp;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

import net.sf.json.JSONObject;

/** 
 * @ClassName: ObjImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月17日
 * @Description: ObjImportor.java
 */
public interface JsonImportor {
	boolean importByJson(BasicObj obj,JSONObject jo)throws ImportException;
}
