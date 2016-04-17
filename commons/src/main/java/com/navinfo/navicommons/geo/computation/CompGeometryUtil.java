package com.navinfo.navicommons.geo.computation;

/** 
* @ClassName: CompGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午7:37:34 
* @Description: TODO
*/
public class CompGeometryUtil {
	/**
	 * [x1,y1,x2,y2]
	 * @param line1
	 * @param line2
	 * @return
	 */
	public static boolean intersectLine(double[] line1,double[] line2){
		//先判断矩形是否相交
		if(!intersectRect(line2Rect(line1),line2Rect(line2))) 
			return false;
		//再判断叉积
		//如果两线段相交，则两线段必然相互跨立对方。
		//若P1P2跨立Q1Q2 ，则矢量 ( P1 - Q1 ) 和( P2 - Q1 )位于矢量( Q2 - Q1 ) 的两侧，即( P1 - Q1 ) × ( Q2 - Q1 ) * ( P2 - Q1 ) × ( Q2 - Q1 ) < 0。上式可改写成( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) > 0。当 ( P1 - Q1 ) × ( Q2 - Q1 ) = 0 时，说明 ( P1 - Q1 ) 和 ( Q2 - Q1 )共线，但是因为已经通过快速排斥试验，所以 P1 一定在线段 Q1Q2上；同理，( Q2 - Q1 ) ×(P2 - Q1 ) = 0 说明 P2 一定在线段 Q1Q2上。所以判断P1P2跨立Q1Q2的依据是：( P1 - Q1 ) × ( Q2 - Q1 ) * ( Q2 - Q1 ) × ( P2 - Q1 ) >= 0。
		//同理判断Q1Q2跨立P1P2的依据是：( Q1 - P1 ) × ( P2 - P1 ) * ( P2 - P1 ) × ( Q2 - P1 ) >= 0。
		if(((line1[0]-line2[0])*(line2[3]-line2[1])-(line2[2]-line2[0])*(line1[1]-line2[1]))
				*((line2[2]-line2[0])*(line1[3]-line2[1])-(line1[2]-line2[0])*(line2[3]-line2[1]))<0
		   ||((line2[0]-line1[0])*(line1[3]-line1[1])-(line1[2]-line1[0])*(line2[1]-line1[1]))
			*((line1[2]-line1[0])*(line2[3]-line1[1])-(line2[2]-line1[0])*(line1[3]-line1[1]))<0)
			return false;
		return true;
	}
	/**
	 * [minx,miny,maxx,maxy]
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static boolean intersectRect(double[] rect1,double[] rect2){
//		if(Math.min(rect1[2],rect2[2])>Math.max(rect1[0], rect2[0])
//				||Math.min(rect1[1], rect2[1])>Math.max(rect1[3], rect2[3])) return false;
//		return true;
		//采用中心点长度算法
		return ( Math.abs((rect1[0]+rect1[2])-(rect2[0]+rect2[2]))<(rect1[2]-rect1[0]+rect2[2]-rect2[0])
		        && Math.abs((rect1[1]+rect1[3])-(rect2[1]+rect2[3]))<(rect1[3]-rect1[1]+rect2[3]-rect1[1]) );
	}
	/**
	 * 
	 * @param line:[x1,y1,x2,y2]
	 * @param rect:[minx,miny,maxx,maxy]
	 * @return
	 */
	public static boolean intersectLineRect(double[] line,double[] rect){
		//和矩形的四条边任意一条相交，则线和矩形相交
		double[] rectLine=new double[]{rect[0],rect[1],rect[2],rect[1]};
		if(intersectLine(line,rectLine))
			return true;
		rectLine = new double[]{rect[2],rect[1],rect[2],rect[3]};
		if(intersectLine(line,rectLine))
			return true;
		rectLine = new double[]{rect[2],rect[3],rect[0],rect[3]};
		if(intersectLine(line,rectLine)){
			return true;
		}
		rectLine = new double[]{rect[0],rect[3],rect[0],rect[1]};
		if(intersectLine(line,rectLine))
			return true;
		return false;
	}
	/**
	 * 生成line的外接矩形
	 * @param line：[x1,y1,x2,y2]
	 * @return rect:[minx,miny,maxx,maxy]
	 */
	public static double[] line2Rect(double[] line){
		double[] rect = new double[4];
		if(line[0]<line[2]){
			rect[0]=line[0];
			rect[2]=line[2];
		}else{
			rect[0]=line[2];
			rect[2]=line[0];
		}
		if(line[1]<line[3]){
			rect[1]=line[1];
			rect[3]=line[3];
		}else{
			rect[1]=line[3];
			rect[3]=line[1];
		}
		return rect;
	}
	
	public static void main(String[] args){
//		double[] line1 = new double[]{0,0,30.0,30.0};
//		double[] line2 = new double[]{15.0,15.0,30.0,15.0};
//		double[] line2 = new double[]{15.00001,15.0,30.0,15.0};
//		double[] line2 = new double[]{0,0,15.0,15.0};
//		double[] rect = new double[]{0,0,15.0,15.0};
//		long t1 = System.currentTimeMillis();
//		if(intersectLineRect(line1,rect)){
//			System.out.println("Yes!!!");
//		}else{
//			System.out.println("Nooooooo");
//		}
//		System.out.println(System.currentTimeMillis()-t1);

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
//		System.out.println(System.currentTimeMillis()-t1);
	}
}
