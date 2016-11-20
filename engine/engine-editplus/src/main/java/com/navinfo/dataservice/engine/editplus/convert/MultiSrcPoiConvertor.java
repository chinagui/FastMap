package com.navinfo.dataservice.engine.editplus.convert;

import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: IxPoiConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: MultiSrcPoiConvertor.java
 */
public class MultiSrcPoiConvertor implements ObjConvertor {

	/**
	 * 参考接口说明
	 */
	@Override
	public JSONObject toJson(BasicObj obj) throws ObjConvertException {
		// TODO 
		return null;
	}

	/**
	 * 参考接口说明
	 */
	@Override
	public BasicObj fromJson(JSONObject jo, String objType,boolean isSetPid) throws ObjConvertException {
		// 暂不实现
		return null;
	}


}
