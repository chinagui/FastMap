package com.navinfo.dataservice.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/** 
 * @ClassName: MeshUtils 
 * @author Xiao Xiaowen 
 * @date 2015-12-29 上午11:46:06 
 * @Description: TODO
 */
public class MeshUtils {


	/**
	 * 根据图幅号计算左下/中心坐标点/右上坐标点
	 * 
	 * @param meshId
	 * @return 左下/中心坐标点/右上坐标点
	 */
	public static int[] mesh2Location(String meshId) {
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
		return "POLYGON ((" + lbX + " " + lbY + ", " + lbX + " " + rtY + ", " + rtX + " " + rtY + ", " + rtX + " " + lbY + "))";

	}

	/**
	 * 给定坐标范围（秒），计算范围内包含的图幅
	 * 
	 * @param lbX
	 *            左下经度 (秒)
	 * @param lbY
	 *            左下维度 (秒)
	 * @param rtX
	 *            右上经度 (秒)
	 * @param rtY
	 *            右上维度 (秒)
	 * @return
	 */
	public static String[] area2Meshes(double lbX, double lbY, double rtX, double rtY) {
		// 计算左下坐标位于图幅
		String lbMesh = location2Mesh(lbX, lbY);
		// 计算右上坐标位于图幅
		String rtMesh = location2Mesh(rtX, rtY);
		if (lbMesh.equals(rtMesh)) {
			return new String[] { lbMesh };
		} else {
			// 跨多个图幅
			int[] lbLocations = mesh2Location(lbMesh);
			int[] rtLocations = mesh2Location(rtMesh);
			int lbcX = lbLocations[2]; // 左下图幅中心点X
			int lbcY = lbLocations[3]; // 左下图幅中心点Y
			int rtcX = rtLocations[2]; // 右上图幅中心点X
			int rtcY = rtLocations[3]; // 右上图幅中心点Y
			// 计算横向跨多少个图幅
			int hSize = (rtcX - lbcX) / 450 + 1;
			int vSize = (rtcY - lbcY) / 300 + 1;
			int meshSize = hSize * vSize; // 图幅数
			// 从左下角开始计算图幅
			String allMesh[] = new String[meshSize];
			int k = 0;
			for (int i = 0; i < hSize; i++) {
				for (int j = 0; j < vSize; j++) {
					allMesh[k] = location2Mesh(lbcX + i * 450, lbcY + j * 300);
					k++;
				}
			}

			return allMesh;

		}
	}

	/**
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
			// 跨多个图幅
			int[] lbLocations = mesh2Location(lbMesh);
			int[] rtLocations = mesh2Location(rtMesh);
			int lbcX = lbLocations[2]; // 左下图幅中心点X
			int lbcY = lbLocations[3]; // 左下图幅中心点Y
			int rtcX = rtLocations[2]; // 右上图幅中心点X
			int rtcY = rtLocations[3]; // 右上图幅中心点Y
			// 计算横向跨多少个图幅
			int hSize = (rtcX - lbcX) / 450 + 1;
			int vSize = (rtcY - lbcY) / 300 + 1;
			int meshSize = hSize * vSize; // 图幅数
			// 从左下角开始计算图幅
			String allMesh[] = new String[meshSize];
			int k = 0;
			for (int i = 0; i < hSize; i++) {
				for (int j = 0; j < vSize; j++) {
					allMesh[k] = location2Mesh(lbcX + i * 450, lbcY + j * 300);
					k++;
				}
			}

			return allMesh;

		}
	}
	
	/**
	 * 传入rectangle的坐标字符串组，x1 y1,x2 y2,x3 y3,x4 y4,x1 y1
	 * @param polygon
	 * @return
	 */
	public static String[] rectangle2Meshes(String rect){
		if(StringUtils.isNotEmpty(rect)){
			float lbX=999;
			float lbY=999;
			float rtX=-999;
			float rtY=-999;
			for(String point:rect.split(",")){
				String pointTrim=point.trim();
				String[] pointArray = pointTrim.split(" ");
				if(pointArray!=null&&pointArray.length==2){
					float x = Float.parseFloat(pointArray[0]);
					float y = Float.parseFloat(pointArray[1]);
					if(x<lbX) lbX=x;
					if(x>rtX) rtX=x;
					if(y<lbY) lbY=y;
					if(y>rtY) rtY=y;
				}
			}
			if(lbX!=999&&lbY!=999&&rtX!=-999&&rtY!=-999){
				return area2Meshes(lbX*3600, lbY*3600, rtX*3600, rtY*3600);
			}
		}
		return null;

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
		return Double.parseDouble(new java.text.DecimalFormat("#.000000").format(x / 3600));
	}

	/**
	 * 位置转换成图幅号
	 * 
	 * @param x
	 *            //经度 秒
	 * @param y
	 *            //维度 秒
	 * @return
	 */
	public static String location2Mesh(double x, double y) {
		x /= 3600.0;
		y /= 3600.0;

		double W;
		int M1M2;
		int J1;
		double J2;
		int M3M4;
		int M5;
		int M6;

		W = y;
		M1M2 = (int) (W * 1.5);

		J1 = (int) x;
		J2 = x - J1;

		M3M4 = J1 - 60;
		M5 = (int) ((W - M1M2 / 1.5) * 12.0);
		M6 = (int) (J2 * 8.0);
		StringBuilder builder = new StringBuilder();
		builder.append(M1M2);
		builder.append(M3M4);
		builder.append(M5);
		builder.append(M6);
		String meshId = builder.toString();
		if (meshId.length() == 5) {
			meshId = "0" + meshId;
		}
		return meshId;
	}

	/**
	 * 计算单个图幅邻接的9个图幅，包含本身
	 * @param meshId
	 * @return 9邻接图幅的数组
	 */
	public static String[] get9NeighborMesh(String meshId) {
		meshId = StringUtils.leftPad(meshId, 6, '0');
		String allMesh[] = new String[9];
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		allMesh[0] = location2Mesh(x - 450, y + 300);
		allMesh[1] = location2Mesh(x, y + 300);
		allMesh[2] = location2Mesh(x + 450, y + 300);
		allMesh[3] = location2Mesh(x - 450, y);
		allMesh[4] = location2Mesh(x, y);
		allMesh[5] = location2Mesh(x + 450, y);
		allMesh[6] = location2Mesh(x - 450, y - 300);
		allMesh[7] = location2Mesh(x, y - 300);
		allMesh[8] = location2Mesh(x + 450, y - 300);
		return allMesh;

	}

	/**
	 * 计算单个图幅邻接的8个图幅
	 * @param meshId
	 * @return 8邻接图幅的数组
	 */
	public static String[] get8NeighborMesh(String meshId) {
		meshId = StringUtils.leftPad(meshId, 6, '0');
		String allMesh[] = new String[8];
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		allMesh[0] = location2Mesh(x - 450, y + 300);
		allMesh[1] = location2Mesh(x, y + 300);
		allMesh[2] = location2Mesh(x + 450, y + 300);
		allMesh[3] = location2Mesh(x - 450, y);
		//allMesh[4] = location2Mesh(x, y);
		allMesh[4] = location2Mesh(x + 450, y);
		allMesh[5] = location2Mesh(x - 450, y - 300);
		allMesh[6] = location2Mesh(x, y - 300);
		allMesh[7] = location2Mesh(x + 450, y - 300);
		return allMesh;

	}
	/**
	 * 计算单个图幅邻接的9个图
	 * @param meshId
	 * @return 9邻接图幅set
	 */
	private static Set<String> get9NeighborMeshSet(String meshId){
		Set<String> meshes = new HashSet<String>();
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		meshes.add(location2Mesh(x - 450, y + 300));
		meshes.add( location2Mesh(x, y + 300));
		meshes.add( location2Mesh(x + 450, y + 300));
		meshes.add( location2Mesh(x - 450, y));
		meshes.add(location2Mesh(x, y));
		meshes.add( location2Mesh(x + 450, y));
		meshes.add(location2Mesh(x - 450, y - 300));
		meshes.add(location2Mesh(x, y - 300));
		meshes.add(location2Mesh(x + 450, y - 300));
		return meshes;
	}

	/**
	 * 计算单个图幅邻接的8个图
	 * @param meshId
	 * @return 8邻接图幅set
	 */
	private static Set<String> get8NeighborMeshSet(String meshId){
		Set<String> meshes = new HashSet<String>();
		int m1 = Integer.valueOf(meshId.substring(0, 1));
		int m2 = Integer.valueOf(meshId.substring(1, 2));
		int m3 = Integer.valueOf(meshId.substring(2, 3));
		int m4 = Integer.valueOf(meshId.substring(3, 4));
		int m5 = Integer.valueOf(meshId.substring(4, 5));
		int m6 = Integer.valueOf(meshId.substring(5, 6));
		int x = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
		int y = (m1 * 10 + m2) * 2400 + m5 * 300;
		x += 450.0 / 2;
		y += 300.0 / 2;
		meshes.add(location2Mesh(x - 450, y + 300));
		meshes.add( location2Mesh(x, y + 300));
		meshes.add( location2Mesh(x + 450, y + 300));
		meshes.add( location2Mesh(x - 450, y));
		meshes.add( location2Mesh(x + 450, y));
		meshes.add(location2Mesh(x - 450, y - 300));
		meshes.add(location2Mesh(x, y - 300));
		meshes.add(location2Mesh(x + 450, y - 300));
		return meshes;
	}

	/**
	 * 计算1圈的扩圈图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	public static Set<String> getNeighborMeshSet(Set<String> meshSet){
		//
		if(meshSet!=null){
			Set<String> neiMeshSet=new HashSet<String>();
			for(String meshId:meshSet){
				neiMeshSet.addAll(get9NeighborMeshSet(meshId));
			}
			return neiMeshSet;
		}
		return null;
	}
	
	/**
	 * 计算1圈的扩圈图幅，不包含输入的图幅
	 * 使用set可以保证输入和输出不会有重复
	 * @param meshSet
	 * @return 不包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
	 * @author XXW
	 */
	public static Set<String> getNeighborMeshSetButSelves(Set<String> meshSet){
		//
		if(meshSet!=null){
			Set<String> neiMeshSet=new HashSet<String>();
			for(String meshId:meshSet){
				neiMeshSet.addAll(get8NeighborMeshSet(meshId));
			}
			//去除自身
			Iterator<String> it = meshSet.iterator();
			while(it.hasNext()){
				String mesh = it.next();
				if(meshSet.contains(mesh)){
					it.remove();
				}
			}
			return meshSet;
		}
		return null;
	}
	/**
	 * 计算n圈的邻接图幅
	 * @param meshId
	 * @param extendCount
	 * @return
	 */
	public static Set<String> getNeighborMeshSet(String meshId,int extendCount){
		if(StringUtils.isEmpty(meshId)||extendCount<1){
			return null;
		}
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			outMeshes= get9NeighborMeshSet(meshId);
			return outMeshes;
		}else{
			Set<String> meshes = getNeighborMeshSet(meshId,extendCount-1);
			outMeshes = getNeighborMeshSet(meshes);
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
		if(meshSet==null||extendCount<1){
			return null;
		}
		Set<String> outMeshes = new HashSet<String>();
		if(extendCount==1){
			outMeshes= getNeighborMeshSet(meshSet);
			return outMeshes;
		}else{
			Set<String> meshes = getNeighborMeshSet(meshSet,extendCount-1);
			outMeshes = getNeighborMeshSet(meshes);
			return outMeshes;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		Integer a = 100;
//		Integer b = 100;
//		if(a==b){
//			System.out.println("a==b");
//		}else{
//			System.out.println("a!=b");
//		}
//		if(a.equals(b)){
//			System.out.println("a equals b");
//		}else{
//			System.out.println("a !equals b");
//		}
//		
//		Integer c = new Integer(100);
//		Integer d = new Integer(100);
//		if(c==d){
//			System.out.println("c==d");
//		}else{
//			System.out.println("c!=d");
//		}
//		if(c.equals(d)){
//			System.out.println("c equals d");
//		}else{
//			System.out.println("c !equals d");
//		}
//		System.out.println(System.currentTimeMillis());
//		String in1 = "595671";
//		Set<String> in2 = new HashSet<String>();in2.add("595671");
//		Set<String> out1 = MeshUtils.get9NeighborMeshSet(in1);
//		Set<String> out2 = MeshUtils.getNeighborMeshSet(in2);
//		System.out.println(out1);
//		System.out.println(out2);
//		System.out.println(System.currentTimeMillis());
//		System.out.println(MeshUtils.getNeighborMeshSet(in1,62));
//		System.out.println(System.currentTimeMillis());
//		Set<String> in3 = MeshUtils.getNeighborMeshSet(out1,60);
//		System.out.println(System.currentTimeMillis());
//		System.out.println(MeshUtils.getNeighborMeshSet(in3,1));
//		System.out.println(System.currentTimeMillis());
		
		Set<String> set1 = new HashSet<String>();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<10000000;i++){
			String str = StringUtils.leftPad(String.valueOf(i), 8, "0");
			set1.add(str);
		}
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1);
		System.out.println(set1.size());
		long t3 = System.currentTimeMillis();
		System.out.println(t3-t2);
		for(String s:set1){
			//
		}
		long t4 = System.currentTimeMillis();
		System.out.println(t4-t3);
		//
		System.out.println("-----------------------");
		List<String> ls1 = new ArrayList<String>();
		long _t1 = System.currentTimeMillis();
		for(int i=0;i<10000000;i++){
			String str = StringUtils.leftPad(String.valueOf(i), 8, "0");
			ls1.add(str);
		}
		long _t2 = System.currentTimeMillis();
		System.out.println(_t2-_t1);
		System.out.println(ls1.size());
		long _t3 = System.currentTimeMillis();
		System.out.println(_t3-_t2);
		for(String s:ls1){
			//
		}
		long _t4 = System.currentTimeMillis();
		System.out.println(_t4-_t3);
		Set<String> set = new HashSet<String>(ls1);
		long _t5 = System.currentTimeMillis();
		System.out.println(_t5-_t4);
	}

}
