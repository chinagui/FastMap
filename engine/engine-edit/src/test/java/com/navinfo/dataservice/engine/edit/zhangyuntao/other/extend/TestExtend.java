package com.navinfo.dataservice.engine.edit.zhangyuntao.other.extend;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @Title: Test.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午2:28:27
 * @version: v1.0
 */
public class TestExtend {
	public static void main(String[] args) {
		testException();
		// Child c1 = new Child();
		// c1.setA(1);
		// c1.setB(2);
		// c1.setC(3);
		//
		// Parent p1 = (Parent) c1;
		// System.out.println(p1.toString());
		//
		// Class<?> clazz = p1.getClass();
		// printFunction(clazz);
	}

	public static void printFunction(Class<?> clazz) {
		Class<?> superClazz = clazz.getSuperclass();
		if (null == superClazz) {
			return;
		} else {
			for (Method m : clazz.getDeclaredMethods()) {
				System.out.println(m.getName());
			}
			printFunction(superClazz);
		}
	}

	public static void testException() {
		Child c1 = null;
		try {
			c1.toString();
		} catch (Exception e) {
			throw e;
		}
		c1 = new Child();
		c1.setA(1);
		Child c2 = null;
		try {
			c2.toString();
		} catch (Exception e) {
			System.out.println("c2为空");
		} finally {
			System.out.println(c1.getA());
			System.out.println("一直执行");
		}
	}
}
