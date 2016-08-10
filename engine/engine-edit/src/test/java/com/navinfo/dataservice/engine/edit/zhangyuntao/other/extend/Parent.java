package com.navinfo.dataservice.engine.edit.zhangyuntao.other.extend;

/**
 * @Title: TestExtend.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午2:21:58
 * @version: v1.0
 */
public class Parent {
	protected int a;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return "a = " + a;
	}

}
