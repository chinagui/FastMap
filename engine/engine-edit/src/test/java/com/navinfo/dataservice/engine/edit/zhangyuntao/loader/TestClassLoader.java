package com.navinfo.dataservice.engine.edit.zhangyuntao.loader;

/**
 * @Title: TestClassLoader.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 下午1:34:11
 * @version: v1.0
 */
public class TestClassLoader {
	public static void main(String[] args) throws ClassNotFoundException {
		Class clazz = load("com.navinfo.dataservice.dao.glm.model.lc.LcFace");
		
		System.out.println(clazz.getCanonicalName());
		System.out.println(clazz.getSimpleName());
	}
	
	public static Class load(String className) throws ClassNotFoundException{
		return ClassLoader.getSystemClassLoader().loadClass(className);
	}
}
