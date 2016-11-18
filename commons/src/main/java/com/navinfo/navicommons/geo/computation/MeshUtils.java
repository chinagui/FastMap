package com.navinfo.navicommons.geo.computation;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * Created by IntelliJ IDEA. User: liuqing Date: 2010-8-4 Time: 8:58:15
 * 地理坐标相关的工具类
 * 注意：方法中传入图幅号和GRID号参数，如果类型是number型，则可以传5位图幅或者7位grid号，String类型的必须传入之前图幅补齐至6位，grid号补齐至8位
 */
public abstract class MeshUtils {

	public static void main(String[] args) throws Exception {
//		System.out.println(""+second2Decimal(300.0));
//		String[] results = point2Meshes(116.74963, 39.0);
//		Set<String> results = getNeighborMeshSet("605602",2);
		String wkt = mesh2WKT("595652");//595650,595651,595652
		System.out.println(wkt);
//		List<String> results = lonlat2MeshIds(76.01,30.33333);
//		System.out.println(StringUtils.join(results,","));
		
//		int[] locs = mesh2Location("595671");//116.125,39.91667--116.25,40
//		for(int l:locs){
//			System.out.println(l);
//		}
//		List<String> meshes = lonlat2MeshIds(116.375,39.87867);//595663,595662
//
//		for(String str:meshes){
//			System.out.println(str);
//		}
//		int[] results = mesh2Location("595664");
//		for(int r:results){
//			System.out.println(r/3600.0);
//		}
//		System.out.println(String.format("%02d",9));
		
//		System.out.println(location2Mesh(23.1*3600, 88.9*3600));
//		String meshId = "595671";
//		Set<String> meshIdSet = new HashSet<String>();
//		meshIdSet.add("35672");
//		Set<String> result = MeshUtils.getNeighborMeshSet(meshIdSet, 3);
//		System.out.println(result.size());
//		System.out.println(result);
		
//		double[] a = mesh2LocationLatLon("595671");
		
//		System.out.println(a[0]+","+a[1]);
//		
//		List<String> b = lonlat2MeshIds(a[0],a[1]);
//		
//		System.out.println(b.toString());


		
		// int meshid = 595673;
		// double[] a =mesh2LocationLatLon("24967");
		// System.out.println(a[0]+","+a[1]);
		//
		// System.out.println(lonlat2Mesh(109.875,1.833333));

		// String wkt = mesh2WKT("595651"); System.out.println(wkt);

//		String wkt1 = "LINESTRING (1 1, 4 3)";
//
//		String wkt2 = "POLYGON ((2 2,3 2,3 3,2 3,2 2))";
//
//		Geometry geom1 = new WKTReader().read(wkt1);
//
//		Geometry geom2 = new WKTReader().read(wkt2);
//
//		Geometry geom = linkInterMeshPolygon(geom1, geom2);
//
//		System.out.println(geom.getGeometryType());

		// (M3*10+M4)*3600+M6*450+60*3600
		// (M1*10+M2)*2400+M5*300
		// int x = (5 * 10 + 6) * 3600 + 3 * 450 + 60 * 3600;
		// int y = (5 * 10 + 9) * 2400 + 7 * 300;

		// System.out.println(decimal2Second(22.97195));
		// System.out.println(second2Decimal(decimal2Second(22.97195)));

		// System.out.println(x); //经度
		// System.out.println(y); //维度
		// System.out.println(location2Mesh(x + 225, y + 150));

		/*
		 * System.out.println("=================================================="
		 * ); String[] allMesh = get9NeighborMesh("595673"); for (int i = 0; i <
		 * allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
		// System.out
		// .println("==================================================");
		// Set<String> allMesh = get9NeighborMesh2("45172", 3);
		// int i = 0;
		// for (String s : allMesh) {
		// System.out.println("mesh" + (i + 1) + ":" + s);
		// i++;
		// }

		/*
		 * System.out.println("=================================================="
		 * ); int lo[] = mesh2Location("595662"); for (int i = 0; i < lo.length;
		 * i++) { int i1 = lo[i]; System.out.println(i1); }
		 * System.out.println("=================================================="
		 * ); lo = mesh2Location("605604"); for (int i = 0; i < lo.length; i++)
		 * { int i1 = lo[i]; System.out.println(i1); }
		 * 
		 * System.out.println("=================================================="
		 * ); allMesh = area2Meshes(418725, 143550, 419625, 144150); for (int i
		 * = 0; i < allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
		/*
		 * int a[] = mesh2Location("595651"); for (int m : a) {
		 * 
		 * // System.out.println(m); ; System.out.println(second2Decimal(m)); ;
		 * } String wkt = mesh2WKT("595651"); System.out.println(wkt);
		 */
	}
	

	/**
	 * 2016.5重新实现Java版，By Xiao Xiaowen
	 * @param x
	 * @param y
	 * @return 图幅数组，且有顺序，按顺序为左下，右下，右上，左上
	 */
	public static String[] point2Meshes(double x,double y){
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
		}else if((longY%300000)<=12){//距离理想行号下图廓线距离
			if(yt%3==2){
				//处于横轴图廓线上
				M5_bak = M5-1;
				if(M5_bak<0){
					M1M2_bak--;
					M5_bak=7;
				}
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
			}
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
		}
		
		String[] meshes = null;
		if(M5_bak>-999&&M6_bak>-999){//图廓点，4个图幅,4个grid号
			meshes = new String[4];
			if(M1M2_bak<M1M2||M5_bak<M5){
				if(M3M4_bak<M3M4||M6_bak<M6){
					meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
					meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
					meshes[2] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
					meshes[3] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
				}else{
					meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
					meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
					meshes[2] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
					meshes[3] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
				}
			}else{
				if(M3M4_bak<M3M4||M6_bak<M6){
					meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
					meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
					meshes[2] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
					meshes[3] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
				}else{
					meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
					meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
					meshes[2] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
					meshes[3] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
				}
			}
		}else if(M5_bak>-999){
			meshes = new String[2];
			if(M1M2_bak<M1M2||M5_bak<M5){
				meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
				meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
			}else{
				meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
				meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
			}
		}else if(M6_bak>-999){
			meshes = new String[2];
			if(M3M4_bak<M3M4||M6_bak<M6){
				meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
				meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
			}else{
				meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
				meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
			}
		}else{
			meshes = new String[]{String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6)};
		}
		return meshes;
	}
	/**
	 * 计算线段所属图幅号，可以在图廓线上
	 * 计算原则：1.只要有任意一点在图幅内部,2.两点都在图幅的边线上
	 * @param line:[x1,y1,x2,y2]
	 * @return
	 */
	public static String[] line2Meshes(double x1,double y1,double x2,double y2){
		Set<String> meshes = new HashSet<String>();
		double[] rect = MyGeoConvertor.lineArr2RectArr(new double[]{x1,y1,x2,y2});
		String[] rectMeshes = rect2Meshes(rect[0],rect[1],rect[2],rect[3]);
		LongLine line = new LongLine(new LongPoint(MyGeoConvertor.degree2Millisec(x1),MyGeoConvertor.degree2Millisec(y1))
				,new LongPoint(MyGeoConvertor.degree2Millisec(x2),MyGeoConvertor.degree2Millisec(y2)));
		for(String mesh:rectMeshes){
			double[] meshRect = mesh2Rect(mesh);
			LongRect longRect = MyGeoConvertor.rectArr2Rect(MyGeoConvertor.degree2Millisec(meshRect));
			if(LongLineUtil.intersectant(line, longRect)){
				meshes.add(mesh);
			}
		}
		return meshes.toArray(new String[0]);
	}
	/**
	 * 给定坐标范围（度），计算范围内包含的图幅
	 * 两个点都在同一图幅的图廓线上，会计算属于这个图幅
	 * 
	 * @param lbX
	 *            左下经度 
	 * @param lbY
	 *            左下维度 
	 * @param rtX
	 *            右上经度 
	 * @param rtY
	 *            右上维度 
	 * @return
	 */
	public static String[] rect2Meshes(double lbX, double lbY, double rtX,
			double rtY) {
		// 计算左下坐标位于图幅
		String lbMesh = null;
		String[] lbMeshes = point2Meshes(lbX,lbY);
		if(lbMeshes.length==1){
			lbMesh = lbMeshes[0];
		}else if(lbMeshes.length==2){
			lbMesh = lbMeshes[1];
		}else{
			lbMesh= lbMeshes[2];
		}
		// 计算右上坐标位于图幅
		String rtMesh = point2Meshes(rtX, rtY)[0];
		if (lbMesh.equals(rtMesh)) {
			return new String[] { lbMesh };
		} else {
			// 跨多个图幅
			return MeshUtils.getBetweenMeshes(lbMesh,rtMesh);

		}
	}
	
	/**
	 * 
	 * @param meshId
	 * @return rect:[minx,miny,maxx,maxy]
	 */
	public static double[] mesh2Rect(String meshId){
		meshId = StringUtils.leftPad(meshId, 6, '0');
		int m12 = Integer.valueOf(meshId.substring(0, 2));
		int m34 = Integer.valueOf(meshId.substring(2, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		double[] rect = new double[4];
		rect[0]=(m34+60)+DoubleUtil.keepSpecDecimal(m6/8.0);
		rect[2]=(m34+60)+DoubleUtil.keepSpecDecimal((m6+1)/8.0);
		//纬度拉伸1.5之后成为m12，则还原时除以1.5
		int intLat = m12/3;
		int modLat = m12%3;
		rect[1]=intLat*2+DoubleUtil.keepSpecDecimal((modLat*8+m5)/12.0);
		rect[3]=intLat*2+DoubleUtil.keepSpecDecimal((modLat*8+m5+1)/12.0);
		return rect;
	}

	/**
	 * 根据图幅号计算左下/中心坐标点/右上坐标点
	 * 
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点
	 */
	public static int[] mesh2Location(String meshId) {

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
		int cX = lbX + 450 / 2;
		int cY = lbY + 300 / 2;
		int rtX = lbX + 450;
		int rtY = lbY + 300;
		return new int[] { lbX, lbY, cX, cY, rtX, rtY };

	}

	/**
	 * 根据图幅号计算左下/中心坐标点/右上坐标点的经纬度
	 * 
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点经纬度
	 */
	public static double[] mesh2LocationLatLon(String meshId) {
		int[] data = mesh2Location(meshId);

		double[] result = new double[data.length];

		for (int i = 0; i < data.length; i++) {
			result[i] = second2Decimal(data[i]);
		}

		return result;
	}

	/**
	 * 图幅号转换成POLYGON的wkt
	 * 
	 * @param meshId
	 * @return
	 */
	public static String mesh2WKT(String meshId) {
		int a[] = mesh2Location(meshId);
		double lbX = second2Decimal(a[0]);
		double lbY = second2Decimal(a[1]);
		double rtX = second2Decimal(a[4]);
		double rtY = second2Decimal(a[5]);
		return "POLYGON ((" + lbX + " " + lbY + ", " + lbX + " " + rtY + ", "
				+ rtX + " " + rtY + ", " + rtX + " " + lbY + "," + lbX + " " + lbY +"))";

	}

	/**
	 * 图幅号转换成POLYGON的JTS对象
	 * 
	 * @param meshId
	 * @return
	 * @throws ParseException
	 */
	public static Geometry mesh2Jts(String meshId) throws ParseException {

		String wkt = mesh2WKT(meshId);
		Geometry jts = new WKTReader().read(wkt);

		return jts;

	}

	public static Geometry linkInterMeshPolygon(Geometry linkGeom,
			Geometry meshGeom) {

		return meshGeom.intersection(linkGeom);

	}


	/**
	 * 暂未实现
	 * 给定左下图幅，右上图幅，计算其外接矩形包含图幅
	 * 
	 * @param lbMesh
	 *            左下图幅
	 * @param rtMesh
	 *            右上图幅
	 * @return
	 */
	public static String[] getBetweenMeshes(String lbMesh, String rtMesh) {
		if (lbMesh.equals(rtMesh)) {
			return new String[] { lbMesh };
		} else {
			// 计算横纵向跨多少个图幅
			int lbM12=Integer.valueOf(lbMesh.substring(0, 2));
			int lbM34=Integer.valueOf(lbMesh.substring(2, 4));
			int lbM5=Integer.valueOf(lbMesh.substring(4, 5));
			int lbM6=Integer.valueOf(lbMesh.substring(5, 6));
			int rtM12=Integer.valueOf(rtMesh.substring(0, 2));
			int rtM34=Integer.valueOf(rtMesh.substring(2, 4));
			int rtM5=Integer.valueOf(rtMesh.substring(4, 5));
			int rtM6=Integer.valueOf(rtMesh.substring(5, 6));
			int hSize = (rtM34-lbM34)*8+(rtM6-lbM6)+1;
			int vSize = (rtM12-lbM12)*8+(rtM5-lbM5)+1;
			int meshSize = hSize * vSize; // 图幅数
			// 从左下角开始计算图幅
			String allMesh[] = new String[meshSize];
			//
			int targetM12=0;
			int targetM34=0;
			int targetM5=0;
			int targetM6=0;
			for(int v=0;v<vSize;v++){
				targetM12=lbM12+((lbM5+v)/8);
				targetM5=(lbM5+v)%8;
				for(int h=0;h<hSize;h++){
					targetM34 = lbM34+((lbM6+h)/8);
					targetM6 = (lbM6+h)%8;
					allMesh[v*hSize+h]=String.format("%02d%02d%d%d", targetM12, targetM34, targetM5, targetM6);
				}
			}
			
			return allMesh;

		}
	}


	/**
	 * 经纬度小数形式转换成秒
	 * 
	 * @param x
	 *            //小数
	 * @return
	 */
	public static double decimal2Second(double x) {

		return x * 3600;

	}

	/**
	 * 经纬度秒转换成小数
	 * 
	 * @param x
	 *            //秒
	 * @return
	 */
	public static double second2Decimal(double x) {
		return Double.parseDouble(new java.text.DecimalFormat("#.000000")
				.format(x / 3600));
	}
	
	public static String getNeighborMesh(String meshId,TopoLocation meshLoc){
		meshId = StringUtils.leftPad(meshId, 6, '0');
		int m12 = Integer.valueOf(meshId.substring(0, 2));
		int m34 = Integer.valueOf(meshId.substring(2, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		switch(meshLoc){
		case Top:
			if((++m5)>7){m12++;m5=0;}
			break;
		case Bottom:
			if((--m5)<0){m12--;m5=7;}
			break;
		case Left:
			if((--m6)<0){m34--;m6=7;}
			break;
		case Right:
			if((++m6)>7){m34++;m6=0;}
			break;
		case LeftTop:
			if((++m5)>7){m12++;m5=0;}
			if((--m6)<0){m34--;m6=7;}
			break;
		case LeftBottom:
			if((--m5)<0){m12--;m5=7;}
			if((--m6)<0){m34--;m6=7;}
			break;
		case RightTop:
			if((++m5)>7){m12++;m5=0;}
			if((++m6)>7){m34++;m6=0;}
			break;
		case RightBottom:
			if((--m5)<0){m12--;m5=7;}
			if((++m6)>7){m34++;m6=0;}
			break;
		default:
			break;
		}
		return String.format("%02d%02d%d%d", m12, m34, m5, m6);
	}

	/**
	 * 计算某图幅周报的9个图幅
	 * [0]为本身图幅，从1-8的顺序为左下开始逆时针方向
	 * @param meshId
	 * @return
	 */
	public static String[] get9NeighborMeshes(String meshId) {
		String allMesh[] = new String[9];
		allMesh[0] = meshId;
		allMesh[1] = getNeighborMesh(meshId,TopoLocation.LeftBottom);
		allMesh[2] = getNeighborMesh(meshId,TopoLocation.Bottom);
		allMesh[3] = getNeighborMesh(meshId,TopoLocation.RightBottom);
		allMesh[4] = getNeighborMesh(meshId,TopoLocation.Right);
		allMesh[5] = getNeighborMesh(meshId,TopoLocation.RightTop);
		allMesh[6] = getNeighborMesh(meshId,TopoLocation.Top);
		allMesh[7] = getNeighborMesh(meshId,TopoLocation.LeftTop);
		allMesh[8] = getNeighborMesh(meshId,TopoLocation.Left);
		return allMesh;

	}
	
	/**
	 * 计算1圈的扩圈图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	private static Set<String> generateNeighborMeshSet(Set<String> meshSet){
		Set<String> neiMeshSet=new HashSet<String>();
		for(String meshId:meshSet){
			CollectionUtils.addAll(neiMeshSet, get9NeighborMeshes(meshId));
		}
		return neiMeshSet;
	}

	/**
	 * 计算1圈的扩圈图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	public static Set<String> getNeighborMeshSet(Set<String> meshSet){
		//check
		if(meshSet!=null){
			Set<String> checkedMeshSet = new HashSet<String>();
			for(String meshId:meshSet){
				checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
			}
			return generateNeighborMeshSet(checkedMeshSet);
		}
		return null;
	}
	
	/**
	 * 不检查图幅位数
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	private static Set<String> generateNeighborMeshSet(String meshId,int extendCount){
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			CollectionUtils.addAll(outMeshes, get9NeighborMeshes(meshId));
			return outMeshes;
		}else{
			Set<String> meshes = generateNeighborMeshSet(meshId,extendCount-1);
			outMeshes = generateNeighborMeshSet(meshes);
			return outMeshes;
		}
	}
	/**
	 * 会检查图幅位数
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	public static Set<String> getNeighborMeshSet(String meshId,int extendCount){
		if(StringUtils.isNotEmpty(meshId)&&extendCount>0){
			meshId = StringUtils.leftPad(meshId, 6, '0');
			return generateNeighborMeshSet(meshId,extendCount);
		}
		return null;
	}
	

	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	private static Set<String> generateNeighborMeshSet(Set<String> meshSet,int extendCount){
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			outMeshes= generateNeighborMeshSet(meshSet);
			return outMeshes;
		}else{
			Set<String> meshes = generateNeighborMeshSet(meshSet,extendCount-1);
			outMeshes = generateNeighborMeshSet(meshes);
			return outMeshes;
		}
	}

	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	public static Set<String> getNeighborMeshSet(Set<String> meshSet,int extendCount){
		if(meshSet!=null&&extendCount>0){
			Set<String> checkedMeshSet = new HashSet<String>();
			for(String meshId:meshSet){
				checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
			}
			return generateNeighborMeshSet(checkedMeshSet,extendCount);
		}
		return null;
	}

	/**
	 * 点是否在图框线上
	 * 
	 * @param dLongitude
	 * @param dLatitude
	 * @return
	 */
	public static boolean isPointAtMeshBorder(double dLongitude,
			double dLatitude) {
		String[] meshes = point2Meshes(dLongitude,dLatitude);
		if(meshes.length>1){
			return true;
		}
		return false;
	}
	
	/**
	 * 点是否在图框线上
	 * 编辑过程中，刚开始的时候把坐标都乘以100000，然后我们空间计算的那些类里头，大多数都是使用原始坐标的 ，
	 * 这期间要调用的话，要注意一下精度转换
	 * 
	 * @param dLongitude
	 * @param dLatitude
	 * @return
	 */
	public static boolean isPointAtMeshBorderWith100000(double dLongitude,
			double dLatitude) {
		String[] meshes = point2Meshes(dLongitude*0.00001,dLatitude*0.00001);
		if(meshes.length>1){
			return true;
		}
		return false;
	}

	/***
     * @author zhaokk
     * @param Geometry g
     * @return 图幅号
     * 判断是否图廓线
     * @throws Exception
     */
	public   static boolean isMeshLine(Geometry g) throws Exception {
		Coordinate[] cs = g.getCoordinates();
		if(g.getCoordinates().length ==2){
			if(isPointAtMeshBorder(cs[0].x, cs[0].y)&&isPointAtMeshBorder(cs[1].x, cs[1].y)){
				if(cs[0].x == cs[1].x || cs[0].y == cs[1].y){
					return true;
				}
			}
		
	 }
		return false;
	}
	public static boolean locateMeshBorder(double x,double y,String mesh){
		TopoLocation loc = TopoLocation.valueOf(meshLocate(x,y,mesh));
		if(loc==TopoLocation.Inside||loc==TopoLocation.Outside){
			return false;
		}
		return true;
	}
	public static String meshLocate(double x,double y,String mesh){
		double[] rect = mesh2Rect(mesh);
		if(x<rect[0]||x>rect[2]||y<rect[1]||y>rect[2]){
			return TopoLocation.Outside.toString();
		}
		if(x==rect[0]&&y==rect[1]){
			return TopoLocation.LeftBottom.toString();
		}else if(x==rect[2]&&y==rect[1]){
			return TopoLocation.RightBottom.toString();
		}else if(x==rect[2]&&y==rect[3]){
			return TopoLocation.RightTop.toString();
		}else if(x==rect[0]&&y==rect[3]){
			return TopoLocation.LeftTop.toString();
		}else if(x==rect[0]){
			return TopoLocation.Left.toString();
		}else if(y==rect[1]){
			return TopoLocation.Bottom.toString();
		}else if(x==rect[2]){
			return TopoLocation.Right.toString();
		}else if(y==rect[3]){
			return TopoLocation.Top.toString();
		}else{
			return TopoLocation.Inside.toString();
		}
	}
	
	/**
	 * 判断grid是否被一个面包含
	 * @param face
	 * @param gridId
	 * @return
	 */
	public static boolean meshInFace(double[] face, String meshId){
		double[] rect = mesh2Rect(meshId);
		
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
