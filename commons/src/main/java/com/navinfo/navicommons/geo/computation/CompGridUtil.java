package com.navinfo.navicommons.geo.computation;

import java.util.HashSet;
import java.util.Set;

/** 
* @ClassName: CompGridUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午9:31:51 
* @Description: TODO
*/
public class CompGridUtil {

	/**
	 * 传入该line所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static Set<String> intersectLineGrid(double[] line,String meshId)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(CompGeometryUtil.line2Rect(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			double[] grid = grid2Rect(gridId);
			if(CompGeometryUtil.intersectLineRect(line,grid)){
				interGrids.add(gridId);
			}
		}
		return interGrids;
	}
	/**
	 * 计算rect所在图幅内相交的grid，rect范围超过图幅范围会抛出异常
	 * @param rect:[minx,miny,maxx,maxy]
	 * @param meshId:rect所属的图幅号
	 * @return
	 * @throws Exception
	 */
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
	/**
	 * 根据grid号获取grid的矩形
	 * @param gridId
	 * @return [minx,miny,maxx,maxy]
	 */
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
	/**
	 * 计算点所属的grid号
	 * @param x
	 * @param y
	 * @param meshId:点所属的图幅号
	 * @return 8位grid号码字符串
	 */
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
	 * 计算点所在的grid号
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
	
	
/* test part*/
	private static void t1(){
		//595671:116.125 39.91667,116.25 40
		//0.03125 0.0208s
		System.out.println(point2Grid(116.0087,39.890));
		System.out.println(point2Grid(116.0087,39.890,"595660"));
	}
	public static void main(String[] args){
		t1();
	}
}
