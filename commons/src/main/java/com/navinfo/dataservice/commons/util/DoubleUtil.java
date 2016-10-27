package com.navinfo.dataservice.commons.util;

/** 
* @ClassName: DoubleUtil 
* @author Xiao Xiaowen 
* @date 2016年5月3日 下午3:47:55 
* @Description: TODO
*/
public class DoubleUtil {
	public static double INFINITY = 9999999999999999.0;
	public static double MIN_ANGLE4LINE_INT=Math.PI/12;//用于上下线分离计算两条线相交的交点时，最小的夹角，小于此设置不计算
	public static double keepSpecDecimal(Double num){
		return (double)(Math.round(num*100000)/100000.0);
	} 
	public static boolean equals(double num1,double num2){
		if(Math.round(num1*100000)==Math.round(num2*100000)) return true;
		return false;
	}
	public static boolean equalsBigDecimal(double num1,double num2){
		if(Math.round(num1*10000000000L)==Math.round(num2*10000000000L)) return true;
		return false;
	}
	
	public static void main(String[] args){
		System.out.println(keepSpecDecimal(1.23456666));
		System.out.println(keepSpecDecimal(1.23456777));
		if(keepSpecDecimal(1.23456666)==keepSpecDecimal(1.23456777)){
			System.out.println("YES...");
		}else{
			System.out.println("No...");
		}
		System.out.println(keepSpecDecimal(1.23456666)-keepSpecDecimal(1.23456777));
		
	}
}
