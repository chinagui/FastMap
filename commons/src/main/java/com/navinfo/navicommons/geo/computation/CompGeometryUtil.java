package com.navinfo.navicommons.geo.computation;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

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
	 * 传入该line所属的图幅号
	 * @param line
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static String[] intersectLineGrid(double[] line,String meshId)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(line2Rect(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			double[] grid = grid2Rect(gridId);
			if(intersectLineRect(line,grid)){
				interGrids.add(gridId);
			}
		}
		return (String[])interGrids.toArray();
	}
	public static String[] intersectRectGrid(double[] rect,String meshId)throws Exception{
		//计算矩形左下角点所在的grid矩形
		String lbGrid = point2Grid(rect[0],rect[1]);
		//计算矩形右上角点所在的grid矩形
		String rtGrid =  point2Grid(rect[2],rect[3]);
		//判断两次算的grid所在的图幅是否和meshId一致
		if(meshId.equals(lbGrid.substring(0,6))&&
				meshId.equals(rtGrid.substring(0,6))){
			//算法是左下grid的第7位和右上grid的第7位数字之间及左下grid的第8位和右上grid的第8位数字之间都相交
			int lbGridM7 = Integer.valueOf(lbGrid.substring(6, 7));
			int lbGridM8 = Integer.valueOf(lbGrid.substring(7, 8));
			int rtGridM7 = Integer.valueOf(rtGrid.substring(6, 7));
			int rtGridM8 = Integer.valueOf(rtGrid.substring(7, 8));
			if(rtGridM7>lbGridM7||rtGridM8>lbGridM8){
				int size = (rtGridM7-lbGridM7+1)*(rtGridM8-lbGridM8+1);
				String[] grids = new String[size];
				int index = 0;
				for(int i=lbGridM7;i<=rtGridM7;i++){
					for(int j=lbGridM8;j<=rtGridM8;j++){
						grids[index]=meshId+i+j;
					}
				}
				return grids;
			}else{
				return new String[]{lbGrid};
			}
		}else{
			throw new Exception("参数错误：rect超过图幅。");
		}
	}
	public static double[] grid2Rect(String gridId){
		int m1 = Integer.valueOf(gridId.substring(0, 1));
		int m2 = Integer.valueOf(gridId.substring(1, 2));
		int m3 = Integer.valueOf(gridId.substring(2, 3));
		int m4 = Integer.valueOf(gridId.substring(3, 4));
		int m5 = Integer.valueOf(gridId.substring(4, 5));
		int m6 = Integer.valueOf(gridId.substring(5, 6));
		int m7 = Integer.valueOf(gridId.substring(6, 7));
		int m8 = Integer.valueOf(gridId.substring(7, 8));
		double minx = (m3 * 10 + m4) + (m6 * 450 + m8*450/4.0)/3600 + 60;
		double miny = ((m1 * 10 + m2) * 2400 + m5 * 300 + m7*300/4)/3600.0;
//		double rtX = lbX+1/(8*4);
		double maxx = minx+0.03125;
		double maxy = miny+1/(12*4);
		return new double[]{minx,miny,maxx,maxy};
	}
	
	public static String point2Grid(double x,double y,String meshId){
		if (meshId.length() < 6) {
			int length = 6 - meshId.length();
			for (int i = 0; i < length; i++) {
				meshId = "0" + meshId;
			}
		}
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int lbX = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int lbY = (m1 * 10 + m2) * 2400 + m5 * 300;
		
		int m7 = (int)((y*3600-lbY)/75);
		int m8 = (int)((x*3600-lbX)/112.5);
		
		StringBuffer sb = new StringBuffer();
		sb.append(meshId);
		sb.append(m7);
		sb.append(m8);
		return sb.toString();
	}
	/**
	 * 取右上grid
	 * @param x
	 * @param y
	 * @return
	 */
	public static String point2Grid(double x,double y){
		int M1M2;
		int M3M4;
		int M5;
		int M6;
		int M7;
		int M8;

		M1M2 = (int) (y * 1.5);
		M3M4 = ((int)x) - 60;
		
		//double yt = y*3600/300;
		double yt = y*12.0;
		M5 = ((int)yt)%8;
		double xt = (x-(int)x) * 8.0;
		M6 = (int) xt;
		
		M7 = (int)((yt-(int)yt)*4.0);
		M8 = (int)((xt-M6)*4.0);
		StringBuilder builder = new StringBuilder();
		builder.append(M1M2);
		builder.append(M3M4);
		builder.append(M5);
		builder.append(M6);
		builder.append(M7);
		builder.append(M8);
		String id = builder.toString();
		if (id.length() == 7) {
			id = "0" + id;
		}
		return id;
	}
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
	
	private static void t1(){
		//595671:116.125 39.91667,116.25 40
		//0.03125 0.0208s
		System.out.println(point2Grid(116.0087,39.890));
		System.out.println(point2Grid(116.0087,39.890,"595660"));
	}
	public static void main(String[] args){
		t1();
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
