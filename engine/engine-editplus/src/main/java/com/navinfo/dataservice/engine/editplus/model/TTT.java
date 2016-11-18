package com.navinfo.dataservice.engine.editplus.model;

import java.lang.reflect.Method;

/** 
 * @ClassName: TTT
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: TTT.java
 */
public class TTT {
	public static <T> void setAttrByCol(String colName,T newValue)throws Exception{
		System.out.println(newValue.getClass().getName());
//		Method m = TTT.class.getMethod("runin", int.class);
		Method m = TTT.class.getMethod("runin", newValue.getClass());
		m.invoke(null, newValue);
	}
	public static void runin(int i){
		System.out.println("runin");
	}
	
	public static void main(String[] args) {
		try{
			setAttrByCol("TT",1);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
