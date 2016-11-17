package com.navinfo.dataservice.engine.editplus.convert;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

import net.sf.json.JSONObject;

/** 
 * @ClassName: ObjConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: ObjConvertor.java
 */
public interface ObjConvertor {
	
	/**
	 * 解析生成的对象的操作类型设置为新增
	 * 由使用方确定是否加入OperationResult，一般加入操作结果的对象都需要申请pid
	 * @param jo:
	 * @param objType:ObjectType中取值
	 * @param isSetPid:转换完成是否申请pid
	 * @return
	 * @throws ObjConvertException
	 */
	public BasicObj fromJson(JSONObject jo,String objType,boolean isSetPid)throws ObjConvertException;
	public JSONObject toJson(BasicObj obj)throws ObjConvertException;
}
