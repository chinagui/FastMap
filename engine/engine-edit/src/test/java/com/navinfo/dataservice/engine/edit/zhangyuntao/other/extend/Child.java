package com.navinfo.dataservice.engine.edit.zhangyuntao.other.extend;

/**
 * @Title: Child.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午2:27:38
 * @version: v1.0
 */
public class Child extends Parent {
	private int b;

	private int c;

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	@Override
	public String toString() {
		return "a = " + a + ", b = " + b + ", c = " + c;
	}

}
