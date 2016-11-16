package com.navinfo.dataservice.engine.editplus.model.obj;

import org.apache.log4j.Logger;

/** 
 * @ClassName: ObjFactory
 * @author xiaoxiaowen4127
 * @date 2016年9月13日
 * @Description: ObjFactory.java
 */
public class ObjFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private volatile static ObjFactory instance=null;
	public static ObjFactory getInstance(){
		if(instance==null){
			synchronized(ObjFactory.class){
				if(instance==null){
					instance=new ObjFactory();
				}
			}
		}
		return instance;
	}
	private ObjFactory(){}
	
	/**
	 * 
	 * @param clazz:主表的模型类
	 * @param isSetPid：是否申请pid
	 * @return
	 */
	public <T> T create(Class<T> clazz,boolean isSetPid){
		return null;
	}
	/**
	 * 根据对象类型创建一个新的对象，所有属性子表会初始化，但list.size()==0
	 * @param objType:对象类型
	 * @param isSetPid：
	 * @return
	 */
	public BasicObj create(String objType,boolean isSetPid){
		return null;
	}
}
