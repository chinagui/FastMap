package com.navinfo.dataservice.commons;

import java.util.Set;

import com.navinfo.navicommons.geo.computation.CompGridUtil;

/** 
* @ClassName: CompGridUtilTest 
* @author Xiao Xiaowen 
* @date 2016年4月19日 上午10:11:04 
* @Description: TODO
*/
public class CompGridUtilTest {
	private static void t1(){
		try{
			//59567003
			double[] rect = CompGridUtil.grid2Rect("59567012");
			for(double o:rect){
				System.out.println(o);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t1_1(){
		try{
			double[] line = new double[]{116.0625,39.9379,116.0625,39.958};
			Set<String> res = CompGridUtil.intersectLineGrid(line, "595670");
			for(String o:res){
				System.out.println(o);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t1_2(){
		try{
			CompGridUtil.point2Grid(116.0625, 39.9379);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void t2(){
		try{
			//59567003
			String s = CompGridUtil.point2Grid(116.09375
					,39.916667);
			System.out.println(s);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
//		t1();
//		t1_1();
//		double x1=15.01;
//		double x2 = 16.01;
//		System.out.println(x1%1);
//		System.out.println(x2%1);
//		double y1 = x1%1;
//		double y2 = x2%1;
//		if(y1==y2){
//			System.out.println("YES!!!");
//		}else{
//			System.out.println("NO!!!");
//		}
//		System.out.println((int)(y1*100));
//		System.out.println((int)(y2*100));

		System.out.println(29*1.5);
		System.out.println(1.7*1.5);
		System.out.println((int)(4.015*1000));
		
		
//		System.out.println(15.01%1);
//		System.out.println(16.01%1);
//		System.out.println(1.01%1);
//		System.out.println(1.0%1);
//		System.out.println(2.0%1);
//		System.out.println(Double.valueOf(1%1));
//		System.out.println(5.0000000%1);
//		long t1 = System.currentTimeMillis();
//		double t;
//		for(int i=0;i<1000000000;i++){
//			t = 15.01-(int)15.01;
//			if(i%100000000==0){
//				System.out.println(t);
//			}
//		}
	}
}
