package com.navinfo.navicommons.geo.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/** 
* @ClassName: CompGridUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午9:31:51 
* @Description: TODO
*/
public class CompGridUtil {
	

	/**
	 * 计算line在图幅范围内所属的grid
	 * 
	 * @param line
	 * @param meshId
	 * @return
	 */
	public static Set<String> line2Grid(double[] line, String meshId){

		//计算line的外接矩形相交的grid矩形
		Set<String> rawGrids = mesh2Grid(meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			long[] grid = MyGeoConvertor.degree2Millisec(grid2Rect(gridId));
			if(LongLineUtil.intersectant(MyGeoConvertor.lineArr2Line(MyGeoConvertor.degree2Millisec(line))
					,MyGeoConvertor.rectArr2Rect(grid))){
				interGrids.add(gridId);
			}
		}
		return interGrids;
	
		
	}
	/**
	 * 传入该line所属的图幅号
	 * line不能超过图幅范围
	 * @param line:[x1,y1,x2,y2]
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static Set<String> intersectLineGrid(double[] line,String meshId)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(MyGeoConvertor.lineArr2RectArr(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			long[] grid = MyGeoConvertor.degree2Millisec(grid2Rect(gridId));
			if(LongLineUtil.intersectant(MyGeoConvertor.lineArr2Line(MyGeoConvertor.degree2Millisec(line))
					,MyGeoConvertor.rectArr2Rect(grid))){
				interGrids.add(gridId);
			}
		}
		return interGrids;
	}
	/**
	 * 传入该line所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static Set<String> intersectLineGrid(double[] line,String meshId,Set<String> gridsFilter)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(MyGeoConvertor.lineArr2RectArr(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			if(gridsFilter.contains(gridId)) continue;
			long[] gridRect = MyGeoConvertor.degree2Millisec(grid2Rect(gridId));
			if(LongLineUtil.intersectant(MyGeoConvertor.lineArr2Line(MyGeoConvertor.degree2Millisec(line))
					,MyGeoConvertor.rectArr2Rect(gridRect))){
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
		//计算矩形左下角点所在的grid
		//判断两次算的grid所在的图幅是否和meshId一致
		List<String> meshes1 = Arrays.asList(MeshUtils.point2Meshes(rect[0],rect[1]));
		List<String> meshes2 = Arrays.asList(MeshUtils.point2Meshes(rect[2],rect[3]));
		if(meshes1.contains(meshId)&&meshes2.contains(meshId)){
			String lbGrid = point2Grid(rect[0],rect[1],meshId);
			//计算矩形右上角点所在的grid
			String rtGrid =  point2Grid(rect[2],rect[3],meshId);
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
						index++;
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
	 * 计算face所在图幅内相交的grid
	 * @param face:闭合的face经纬度数组
	 * @param meshId:face所属的图幅号
	 * @return
	 * @throws Exception
	 */
	public static Set<String> intersectFaceGrid(double[] face,String meshId)throws Exception{
		//计算图幅内的所有grid
		Set<String> rawGrids = mesh2Grid(meshId);
		
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		int pointCount = face.length/2;
		
		for(int i=0; i<pointCount-1; i++){
			
			double[] line = new double[4];
			
			line[0] = face[2*i];
			line[1] = face[2*i+1];
			line[2] = face[2*i+2];
			line[3] = face[2*i+3];
			
			Iterator<String> it = rawGrids.iterator(); 
			while(it.hasNext()){
				String gridId = it.next();
				
				long[] grid = MyGeoConvertor.degree2Millisec((grid2Rect(gridId)));
				if(LongLineUtil.intersectant(MyGeoConvertor.lineArr2Line(MyGeoConvertor.degree2Millisec(line))
						,MyGeoConvertor.rectArr2Rect(grid))){
					interGrids.add(gridId);
					it.remove();
				}
			}
		}
		
		return interGrids;
	}
	
	public static double[] grid2Rect(int gridId){
		String gridIdStr = String.valueOf(gridId);
		gridIdStr = StringUtils.leftPad(gridIdStr, 8, '0');
		return grid2Rect(gridIdStr);
	}
	
	/**
	 * 根据grid号获取grid的矩形
	 * @param gridId
	 * @return double精度超过5位小数
	 */
	public static double[] grid2Rect(String gridId){
		
		gridId = StringUtils.leftPad(gridId, 8, '0');
		
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
		double maxy = miny+(1.0/(12*4));
		return new double[]{DoubleUtil.keepSpecDecimal(minx)
				,DoubleUtil.keepSpecDecimal(miny)
				,DoubleUtil.keepSpecDecimal(maxx)
				,DoubleUtil.keepSpecDecimal(maxy)};
	}

	/**
	 * 计算点所属的grid号
	 * @param x
	 * @param y
	 * @param meshId:点所属的图幅号
	 * @return 8位grid号码字符串
	 */
	public static String[] point2Grids(double x,double y,String[] meshIds){
		String[] grids = null;
		if(meshIds!=null&&meshIds.length>1){
			if(meshIds.length==2){
				grids = new String[2];
				int m5_0 = Integer.valueOf(meshIds[0].substring(4, 5));
				int m6_0 = Integer.valueOf(meshIds[0].substring(5, 6));
				int m5_1 = Integer.valueOf(meshIds[1].substring(4, 5));
				int m6_1 = Integer.valueOf(meshIds[1].substring(5, 6));
				if(m5_0==m5_1){
					int m7 = point2Grid_M7(y);
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						grids[0]=meshIds[0]+m7+"3";
						grids[1]=meshIds[1]+m7+"0";
					}else{
						grids[0]=meshIds[0]+m7+"0";
						grids[1]=meshIds[1]+m7+"3";
					}
				}else if(m6_0==m6_1){
					int m8 = point2Grid_M8(x);
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						grids[0]=meshIds[0]+"3"+m8;
						grids[1]=meshIds[1]+"0"+m8;
					}else{
						grids[0]=meshIds[0]+"0"+m8;
						grids[1]=meshIds[1]+"3"+m8;
					}
				}
			}else if(meshIds.length==4){
				grids = new String[4];
				List<Integer> sortMesh = new ArrayList<Integer>();
				for(int i=0;i<4;i++){
					sortMesh.add(Integer.valueOf(meshIds[i]));
				}
				Collections.sort(sortMesh);
				grids[0]=sortMesh.get(0)+"33";
				grids[3]=sortMesh.get(3)+"00";
				if(sortMesh.get(2)%10==sortMesh.get(0)%10){
					grids[1]=sortMesh.get(1)+"30";
					grids[2]=sortMesh.get(2)+"03";
				}else{
					grids[1]=sortMesh.get(2)+"30";
					grids[2]=sortMesh.get(1)+"03";
				}
			}
			return grids;
		}else{
			return new String[]{meshIds[0]+point2Grid_M7(y)+point2Grid_M8(x)};
		}
	}
	/**
	 * 计算点所在的grid号
	 * @param x
	 * @param y
	 * @return
	 */
	public static int point2Grid_M7(double y){
		long longY = Math.round(y*3600000);
		return (int)((longY%(300000))*4)/(300000);
	}
	/**
	 * 计算点所在的grid号
	 * @param x
	 * @param y
	 * @return
	 */
	public static int point2Grid_M8(double x){
		long longX = Math.round(x*3600000);
		return (int)((longX%(450000))*4)/(450000);
	}
	/**
	 * 可以优化
	 * @param x
	 * @param y
	 * @param meshId
	 * @return
	 */
	public static String point2Grid(double x,double y,String meshId){
		meshId = StringUtils.leftPad(meshId, 6, '0');
		String[] grids = point2Grids(x,y);
		for(String s:grids){
			if(s.startsWith(meshId)){
				return s;
			}
		}
		return null;
	}

	/**
	 * 参考MeshUtils.point2Meshes(double x,double y)方法
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static String[] point2Grids(double x,double y){
		//将度单位坐标转换为秒*3600，并乘1000消除小数,最后取整
		long longX = Math.round(x*3600000);
		long longY = Math.round(y*3600000);
		int M1M2;
		int M1M2_bak;
		int M3M4;
		int M3M4_bak;
		int M5;
		int M5_bak = -999;
		int M6;
		int M6_bak = -999;
		int M7=0;
		int M7_bak=0;
		int M8=0;
		int M8_bak=0;

		//一个四位图幅的纬度高度为2400秒
		M1M2 = (int)(longY/(2400000));
		M3M4 = ((int)x) - 60;//简便算法
		//
		M1M2_bak=M1M2;
		M3M4_bak=M3M4;
		
		//
		
		int yt = (int)(longY/(300000));
		M5 = yt%8;
		//判断在图幅线上的情况
		if((longY%300000)==0){//直接在理想行号图廓线上，0,0.25,0.5,1,...
			M5_bak = M5-1;
			if(M5_bak<0){
				M1M2_bak--;
				M5_bak=7;
			}
			M7 = 0;
			M7_bak = 3;
		}else if((longY%300000)<=12){//距离理想行号下图廓线距离
			if(yt%3==2){
				//处于横轴图廓线上
				M5_bak = M5-1;
				if(M5_bak<0){
					M1M2_bak--;
					M5_bak=7;
				}
				M7 = 0;
				M7_bak = 3;
			}
			/**
			if(yt%3==0){//0.0,0.25,...
				//不变
			}else if(yt%3==1){//0.08333,0.33333,...
				//不变
			}else if(yt%3==2){//0.16667,0.41667,...
				//处于图廓线上
				M5_bak = M5-1;
			}*/
		}else if((300000-(longY%300000))<=12){//距离理想行号上图廓线距离
			/**
			if(yt%3==0){//0.0,0.25,...
				//处于图廓线上
				M5_bak = M5+1;
			}else if(yt%3==1){//0.08333,0.33333,...
				//不变
			}else if(yt%3==2){//0.16667,0.41667,...
				//处于图廓线上
				M5_bak = M5+1;
			}
			 */
			if(yt%3==0){
				//处于图廓线上
				M5_bak = M5+1;
				if(M5_bak>7){
					M1M2_bak++;
					M5_bak=0;
				}
				M7 = 3;
				M7_bak = 0;
			}
		}else{//不在图廓线上
			M7 = (int)((longY%(300000))*4)/(300000);
		}
		int xt = (int)(longX/(450000));
		M6 = xt%8;
		//经度坐标没有四舍五入，所以理论上只有=0和大于12的情况
		if((longX%450000)<=12){
			M6_bak = M6-1;
			if(M6_bak<0){
				M3M4_bak--;
				M6_bak=7;
			}
			M8 = 0;
			M8_bak = 3;
		}else{
			M8 = (int)((longX%(450000))*4)/(450000);
		}
		
		String[] meshes = null;
		if(M5_bak>-999&&M6_bak>-999){//图廓点，4个图幅,4个grid号
			meshes = new String[4];
			meshes[0] = String.format("%02d%02d%d%d%d%d", M1M2, M3M4, M5, M6,M7,M8);
			meshes[1] = String.format("%02d%02d%d%d%d%d", M1M2, M3M4_bak, M5, M6_bak,M7,M8_bak);
			meshes[2] = String.format("%02d%02d%d%d%d%d", M1M2_bak, M3M4, M5_bak, M6,M7_bak,M8);
			meshes[3] = String.format("%02d%02d%d%d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak,M7_bak,M8_bak);
		}else if(M5_bak>-999){
			meshes = new String[2];
			meshes[0] = String.format("%02d%02d%d%d%d%d", M1M2, M3M4, M5, M6,M7,M8);
			meshes[1] = String.format("%02d%02d%d%d%d%d", M1M2_bak, M3M4, M5_bak, M6,M7_bak,M8);
		}else if(M6_bak>-999){
			meshes = new String[2];
			meshes[0] = String.format("%02d%02d%d%d%d%d", M1M2, M3M4, M5, M6,M7,M8);
			meshes[1] = String.format("%02d%02d%d%d%d%d", M1M2, M3M4_bak, M5, M6_bak,M7,M8_bak);
		}else{
			meshes = new String[]{String.format("%02d%02d%d%d%d%d", M1M2, M3M4, M5, M6,M7,M8)};
		}
		return meshes;
	}
	/**
	 * 计算图幅内所有的grid号
	 * @param meshId
	 * @return
	 */
	public static Set<String> mesh2Grid(String meshId){
		
		Set<String> grids = new HashSet<String>();
		
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				grids.add(meshId + String.valueOf(i)+ j);
			}
		}
		
		return grids;
	}
	
	public static boolean gridInMesh(String meshId,String gridId){
		if(meshId==null&&gridId==null)return false;
		return meshId.equals(gridId.substring(0, gridId.length()-2));
	}
	
	/**
	 * 判断grid是否被一个面包含
	 * @param face
	 * @param gridId
	 * @return
	 */
	public static boolean gridInFace(double[] face, String gridId){
		double[] rect = grid2Rect(gridId);
		
		double[] p1 = new double[]{rect[0],rect[1]};
		double[] p2 = new double[]{rect[0],rect[3]};
		double[] p3 = new double[]{rect[2],rect[1]};
		double[] p4 = new double[]{rect[2],rect[3]};
		
		if(!CompGeometryUtil.pointInFace(p1, face)){
			return false;
		}
		if(!CompGeometryUtil.pointInFace(p2, face)){
			return false;
		}
		if(!CompGeometryUtil.pointInFace(p3, face)){
			return false;
		}
		if(!CompGeometryUtil.pointInFace(p4, face)){
			return false;
		}
		
		return true;
	}

	/**
	 * grid转wkt
	 * @param gridId
	 * @return
	 * @throws ParseException 
	 */
	public static Geometry grids2Jts(Set<String> grids) {
		if(grids==null)return null;
		Geometry geometry = null;
		for(String grid:grids){
			double[] rect = grid2Rect(grid);
			Geometry geo = JtsGeometryConvertor.convert(rect);
			
			if(geometry == null){
				geometry = geo;
			}
			else{
				geometry = geometry.union(geo);
			}
		}
		return geometry;
	}
	
	public static void main(String[] args) {
		String[] grids = point2Grids(116.23296,39.94998);
		for(String g:grids){
			System.out.println(g);
		}
		double[] rect = grid2Rect(59566331);
		for(double d:rect){
			System.out.println(d);
		}
	}	
}
