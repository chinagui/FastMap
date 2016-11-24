package com.navinfo.dataservice.engine.editplus.convert;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

import net.sf.json.JSONObject;

/** 
 * 对象层级结构及属性名一致互转
 * @ClassName: DefaultObjConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: DefaultObjConvertor.java
 */
public class DefaultObjConvertor implements ObjConvertor {

	/**
	 * 参考接口说明
	 */
	@Override
	public BasicObj fromJson(JSONObject jo, String objType,boolean isSetPid) throws ObjConvertException {
		// TODO
		return null;
	}

	/**
	 * 参考接口说明
	 */
	@Override
	public JSONObject toJson(BasicObj obj) throws ObjConvertException {
		// TODO
		return null;
	}

}
