package com.navinfo.dataservice.engine.edit.bo;

import com.navinfo.dataservice.engine.edit.model.BasicObj;

public class BoFactory {
	private volatile static BoFactory instance;

	public static BoFactory getInstance() {
		if (instance == null) {
			synchronized (BoFactory.class) {
				if (instance == null) {
					instance = new BoFactory();
				}
			}
		}
		return instance;
	}

	private BoFactory() {

	}
	
	public AbstractBo create(BasicObj obj) throws Exception{
		String className = obj.getClass().getName();
		Class<?> clazz = Class.forName(className.replace("dao.glm.model", "engine.edit.bo") + "Bo");
		AbstractBo bo = (AbstractBo) clazz.newInstance();
		bo.setObj(obj);
		return bo;
	}
}
