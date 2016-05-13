package com.navinfo.navicommons.geo.computation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: CompGridUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午9:31:51 
* @Description: TODO
*/
public class CompGridUtil {

	public static Set<String> intersectGeometryGrid(JGeometry geo,String[] meshIds)throws Exception{
		Set<String> grids = new HashSet<String>();
		int type = geo.getType();
		if(type==1){
			double[] point = geo.getPoint();
			CollectionUtils.addAll(grids, point2Grid(point[0],point[1],meshIds));
		}else if(type==2){
			grids.addAll(intersectLineGrid(geo,meshIds));
		}else if(type == 3){
			throw new Exception("面不能有多个图幅号");
		}
		return grids;
	}
	public static Set<String> intersectGeometryGrid(JGeometry geo,String meshId)throws Exception{
		meshId = StringUtils.leftPad(meshId, 6, '0');
		Set<String> grids = new HashSet<String>();
		int type = geo.getType();
		if(type==1){
			double[] point = geo.getPoint();
			grids.addAll(Arrays.asList(point2Grids(point[0],point[1])));
		}else if(type==2){
			double[] lines = geo.getOrdinatesArray();
			int pointCount = lines.length/2;
			for(int i=1;i<pointCount;i++){
				grids.addAll(intersectLineGrid(new double[]{lines[i*2-2],lines[i*2-1],lines[i*2],lines[i*2+1]},meshId,grids));
			}
		}else if(type == 3){
			double[] face = geo.getOrdinatesArray();
			grids.addAll(intersectFaceGrid(face, meshId));
		}
		return grids;
	}
	/**
	 * 传入线的几何，和所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshIds：
	 * @return
	 */
	private static Set<String> intersectLineGrid(JGeometry line,String[] meshIds)throws Exception{
		if(meshIds!=null&&meshIds.length>0){
			if(meshIds.length==1){
				return intersectGeometryGrid(line,meshIds[0]);
			}else if(meshIds.length==2){
				//
				Set<String> grids = new HashSet<String>();
				double[] points = line.getOrdinatesArray();
				int m5_0 = Integer.valueOf(meshIds[0].substring(4, 5));
				int m6_0 = Integer.valueOf(meshIds[0].substring(5, 6));
				int m5_1 = Integer.valueOf(meshIds[1].substring(4, 5));
				int m6_1 = Integer.valueOf(meshIds[1].substring(5, 6));
				if(m5_0==m5_1){
					int m7_s = point2Grid_M7(points[1]);
					int m7_e = point2Grid_M7(points[points.length-1]);
					int m7_min = m7_s>m7_e?m7_e:m7_s;
					int m7_max = m7_s>m7_e?m7_s:m7_e;
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						for(int i = m7_min;i<=m7_max;i++){
							grids.add(meshIds[0]+i+"3");
							grids.add(meshIds[1]+i+"0");
						}
					}else{
						for(int i = m7_min;i<=m7_max;i++){
							grids.add(meshIds[0]+i+"0");
							grids.add(meshIds[1]+i+"3");
						}
					}
				}else if(m6_0==m6_1){
					int m8_s = point2Grid_M8(points[0]);
					int m8_e = point2Grid_M8(points[points.length-2]);
					int m8_min = m8_s>m8_e?m8_e:m8_s;
					int m8_max = m8_s>m8_e?m8_s:m8_e;
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						for(int i = m8_min;i<=m8_max;i++){
							grids.add(meshIds[0]+"3"+i);
							grids.add(meshIds[1]+"0"+i);
						}
					}else{
						for(int i = m8_min;i<=m8_max;i++){
							grids.add(meshIds[0]+"0"+i);
							grids.add(meshIds[1]+"3"+i);
						}
					}
				}
				return grids;
			}else{
				throw new Exception("");
			}
		}else{
			throw new Exception("");
		}
	}
	/**
	 * 传入该line所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static Set<String> intersectLineGrid(double[] line,String meshId)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(line2Rect(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			long[] grid = convertLatlon(grid2Rect(gridId));
			if(CompGeometryUtil.intersectLineRect(convertLatlon(line),grid)){
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
		String[] rawGrids = intersectRectGrid(line2Rect(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			if(gridsFilter.contains(gridId)) continue;
			long[] gridRect = convertLatlon(grid2Rect(gridId));
			if(CompGeometryUtil.intersectLineRect(convertLatlon(line),gridRect)){
				interGrids.add(gridId);
			}
		}
		return interGrids;
	}

	/**
	 * 生成line的外接矩形
	 * @param line：[x1,y1,x2,y2]
	 * @return rect:[minx,miny,maxx,maxy]
	 */
	private static double[] line2Rect(double[] line){
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
	public static long convertLatlon(double latlon){
		return Math.round(latlon*3600*1000);
	}
	public static long[] convertLatlon(double[] arr){
		long[] longArr = new long[arr.length];
		for(int i=0;i<arr.length;i++){
			longArr[i]=Math.round(arr[i]*3600*1000);
		}
		return longArr;
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
		String sameMesh = MeshUtils.sameMesh(rect[0],rect[1], rect[2],rect[3]);
		if(sameMesh!=null){
			String lbGrid = point2Grid(rect[0],rect[1],sameMesh);
			//计算矩形右上角点所在的grid
			String rtGrid =  point2Grid(rect[2],rect[3],sameMesh);
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
				
				long[] grid = convertLatlon(grid2Rect(gridId));
				if(CompGeometryUtil.intersectLineRect(convertLatlon(line),grid)){
					interGrids.add(gridId);
					it.remove();
				}
			}
		}
		
		return interGrids;
	}
	
	
	
	/**
	 * 根据grid号获取grid的矩形
	 * @param gridId
	 * @return double精度超过5位小数
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
		double maxy = miny+(1.0/(12*4));
		return new double[]{minx,miny,maxx,maxy};
	}

	/**
	 * 计算点所属的grid号
	 * @param x
	 * @param y
	 * @param meshId:点所属的图幅号
	 * @return 8位grid号码字符串
	 */
	public static String[] point2Grid(double x,double y,String[] meshIds){
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
			return point2Grids(x,y);
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
	 * 计算点所在的grid号
	 * 如果正好在图幅线上，取右/上的grid
	 * @param x：单位度
	 * @param y：单位度
	 * @return
	 */
	public static String point2Grid(double x,double y){
		//将度单位坐标转换为秒*3600，并乘1000消除小数,最后取整
		long longX = Math.round(x*3600000);
		long longY = Math.round(y*3600000);
		int M1M2;
		int M3M4;
		int M5;
		int M6;
		int M7;
		int M8;

		//一个四位图幅的纬度高度为2400秒
		M1M2 = (int)(longY/(2400000));
		M3M4 = ((int)x) - 60;//简便算法
		
		
		int yt = (int)(longY/(300000));
		M5 = yt%8;
		int xt = (int)(longX/(450000));
		M6 = xt%8;
		
		M7 = (int)((longY%(300000))*4)/(300000);
		M8 = (int)((longX%(450000))*4)/(450000);
		
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
	/**
	 * 计算grid过程中依赖的计算图幅是自己实现的，暂未测试
	 * @param x
	 * @param y
	 * @return
	 */
	public static String[] point2Grids2(double x,double y){
		//将度单位坐标转换为秒*3600，并乘1000消除小数,最后取整
		long longX = Math.round(x*3600000);
		long longY = Math.round(y*3600000);
		int M1M2;
		int M3M4;
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
		
		int yt = (int)(longY/(300000));
		M5 = yt%8;
		//判断在图幅线上的情况
		if((longY%300000)<=12){//距离理想行号下图廓线距离
			if(yt%3==2){
				//处于图廓线上
				M5_bak = M5-1;
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
				M7 = 3;
				M7_bak = 0;
			}
		}
		int xt = (int)(longX/(450000));
		M6 = xt%8;
		//经度坐标没有四舍五入，所以理论上只有=0和大于12的情况
		if((longX%450000)<=12){
			M6_bak = M6-1;
			M8 = 0;
			M8_bak = 3;
		}
		
		String[] meshes = null;
		if(M5_bak>-999&&M6_bak>-999){//图廓点，4个图幅,4个grid号
			meshes = new String[4];
			//第一个grid
			meshes[0] = String.valueOf(M1M2)+M3M4+M5+M6+M7+M8;
			//...
			meshes[1] = "";
		}else if(M5_bak>-999){
			
		}else if(M6_bak>-999){
			
		}else{
			M7 = (int)((longY%(300000))*4)/(300000);
			M8 = (int)((longX%(450000))*4)/(450000);
		}
		
		return meshes;
	}
	/**
	 * 计算点所在的grid号，依赖的图幅号来自NM
	 * 如果正好在grid线上，取右/上的grid
	 * @param x：单位度
	 * @param y：单位度
	 * @return
	 */
	public static String[] point2Grids(double x,double y){
		List<String> meshes = MeshUtils.lonlat2MeshIds(x,y);
		if(meshes.size()>1){
			return point2Grid(x,y,meshes.toArray(new String[0]));
		}else{
			return new String[]{meshes.get(0)+point2Grid_M7(y)+point2Grid_M8(x)};
		}
	}
	
	/**
	 * 计算图幅内所有的grid号
	 * @param meshId
	 * @return
	 */
	private static Set<String> mesh2Grid(String meshId){
		
		Set<String> grids = new HashSet<String>();
		
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				grids.add(meshId + String.valueOf(i)+ j);
			}
		}
		
		return grids;
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

}
