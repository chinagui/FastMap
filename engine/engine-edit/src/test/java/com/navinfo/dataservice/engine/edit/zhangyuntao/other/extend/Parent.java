package com.navinfo.dataservice.engine.edit.zhangyuntao.other.extend;

/**
 * @Title: TestExtend.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午2:21:58
 * @version: v1.0
 */
public class Parent {
	protected int a = 2;
	
	protected int modCount = 0;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
		this.a = Parent.this.modCount;
	}

	@Override
	public String toString() {
		return "a = " + a;
	}
	
	class InnerP{
		private int x;
		
		public void setX(){
			x = Parent.this.modCount;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
		
	}

}
