package com.navinfo.dataservice.engine.editplus.imp;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.IxPoiObj;

import net.sf.json.JSONObject;

/** 
 * @ClassName: MultiSrcPoiDayImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月17日
 * @Description: MultiSrcPoiDayImportor.java
 */
public class MultiSrcPoiDayImportor implements JsonImportor {
	
	@Override
	public boolean importByJson(BasicObj obj,JSONObject jo)throws ImportException {
		if(obj!=null&&jo!=null){
			if(obj instanceof IxPoiObj){
				IxPoiObj poi = (IxPoiObj)obj;
				//todo
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}

}
