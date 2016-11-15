//package com.navinfo.dataservice.engine.editplus.bo;
//
//import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
//
//public class BoFactory {
//	private volatile static BoFactory instance;
//
//	public static BoFactory getInstance() {
//		if (instance == null) {
//			synchronized (BoFactory.class) {
//				if (instance == null) {
//					instance = new BoFactory();
//				}
//			}
//		}
//		return instance;
//	}
//
//	private BoFactory() {
//
//	}
//	
//	public AbstractBo create(BasicObj obj) throws Exception{
//		String className = obj.getClass().getName();
//		Class<?> clazz = Class.forName(className.replace("dao.glm.model", "engine.edit.bo") + "Bo");
//		AbstractBo bo = (AbstractBo) clazz.newInstance();
//		bo.setObj(obj);
//		return bo;
//	}
//}
