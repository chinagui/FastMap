/** 
* @ClassName: CompGeoConvertor 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午3:55:16 
* @Description: TODO
*/
package com.navinfo.dataservice.engine.limit.commons.geom.computation;

/** 
* @ClassName: CompGeoConvertor 
* @author Xiao Xiaowen 
* @date 2016年6月2日 下午3:55:16 
* @Description: TODO
*  
*/
public class MyGeoConvertor {
	
	/**
	 * 换算成second再乘以1000
	 * @param latlon
	 * @return
	 */
	public static long degree2Millisec(double latlon){
		return Math.round(latlon*3600*1000);
	}
	/**
	 * 换算成second再乘以1000
	 * @param arr
	 * @return
	 */
	public static long[] degree2Millisec(double[] arr){
		long[] longArr = new long[arr.length];
		for(int i=0;i<arr.length;i++){
			longArr[i]=Math.round(arr[i]*3600*1000);
		}
		return longArr;
	}
	public static LongPoint degree2Millisec(DoublePoint point){
		return new LongPoint(degree2Millisec(point.getX())
				,degree2Millisec(point.getY()));
	}
	public static LongLine degree2Millisec(DoubleLine line){
		return new LongLine(degree2Millisec(line.getSpoint())
				,degree2Millisec(line.getEpoint()));
	}
	public static double[] lineArr2RectArr(double[] line){
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
	public static long[] lineArr2RectArr(long[] line){
		long[] rect = new long[4];
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
	public static DoubleLine lineArr2Line(double[] line){
		return new DoubleLine(new DoublePoint(line[0],line[1])
				,new DoublePoint(line[2],line[3]));
	}
	public static LongLine lineArr2Line(long[] line){
		return new LongLine(new LongPoint(line[0],line[1])
				,new LongPoint(line[2],line[3]));
	}
	public static DoubleRect rectArr2Rect(double[] rect){
		return new DoubleRect(new DoublePoint(rect[0],rect[1])
				,new DoublePoint(rect[2],rect[3]));
	}
	public static LongRect rectArr2Rect(long[] rect){
		return new LongRect(new LongPoint(rect[0],rect[1])
				,new LongPoint(rect[2],rect[3]));
	}
	/**
	 * 生成line的外接矩形
	 * @param line
	 * @return
	 */
	public static LongRect line2Rect(LongLine line){
		return new LongRect(new LongPoint(line.getMinX(),line.getMinY())
				,new LongPoint(line.getMaxX(),line.getMaxY()));
	}
	/**
	 * 生成line的外接矩形
	 * @param line
	 * @return
	 */
	public static DoubleRect line2Rect(DoubleLine line){
		return new DoubleRect(new DoublePoint(line.getMinX(),line.getMinY())
				,new DoublePoint(line.getMaxX(),line.getMaxY()));
	}
}
