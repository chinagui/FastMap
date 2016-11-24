package com.navinfo.dataservice.engine.editplus.convert;

import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.IxPoiObj;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: IxPoiConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: MultiSrcPoiConvertor.java
 */
public class MultiSrcPoiConvertor {

	/**
	 * 参考接口说明
	 */
	public JSONObject toJson(IxPoiObj poi) throws ObjConvertException {
		JSONObject jo = new JSONObject();
		IxPoi ixPoi = (IxPoi)poi.getMainrow();
		jo.put("fid", ixPoi.getPoiNum());
		jo.put("name", poi.getNameByLct("CHI", 1, 1).getName());
		return null;
	}

	/**
	 * 参考接口说明
	 */
	public BasicObj fromJson(JSONObject jo, String objType,boolean isSetPid) throws ObjConvertException {
		// 暂不实现
		return null;
	}


}
