package com.navinfo.dataservice.engine.editplus.model.obj;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmObject;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;

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
	
	/**
	 * 
	 * @param row
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public BasicObj create4Select(BasicRow row)throws ClassNotFoundException,NoSuchMethodException,InvocationTargetException,IllegalAccessException,InstantiationException{
		GlmObject glmObj =
		GlmFactory.getInstance().getObjByType(row.getObjType());
		BasicObj bObj = (BasicObj)Class.forName(glmObj.getModelClassName()).getConstructor(BasicRow.class).newInstance(row);
		return bObj;
	}
}
