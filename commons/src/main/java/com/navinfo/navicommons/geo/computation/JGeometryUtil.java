package com.navinfo.navicommons.geo.computation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: JGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年4月18日 下午6:27:09 
* @Description: 
* 此类是给从库中查询的数据计算图幅和grid号时使用
* 所使用的类型都是JGeometry，同时所有geometry不会跨越图幅
* 大部分方法在计算grid号时也依赖于所属图幅号
*/
public class JGeometryUtil {
	/**
	 * 此方法是从库中拿到已有数据而计算所属图幅号，只实现了点和线
	 * 基于库里的 数据，线已经被打断，不会出现跨越图幅的线
	 * 跨越图幅的线和面计算请用CompGeometryUtil.geo2MeshesWithoutBreak()方法
	 * @param geo
	 * @return
	 */
	public static String[] geo2MeshIds(JGeometry geo){
		if(geo!=null){
			int type = geo.getType();
			if(type==1){
				double[] point = geo.getPoint();
				return MeshUtils.point2Meshes(point[0], point[1]);
			}else if (type==2){
				double[] arr = geo.getOrdinatesArray();
				int len = arr.length;
				Set<String> set1 = new HashSet<String>();
				CollectionUtils.addAll(set1, MeshUtils.point2Meshes(arr[0],arr[1]));
				Set<String> set2 = new HashSet<String>();
				CollectionUtils.addAll(set2,MeshUtils.point2Meshes(arr[len-2],arr[len-1]));
				set1.retainAll(set2);
				return set1.toArray(new String[0]);
			}else if (type==3){
				//...
				return null;
			}
		}
		return null;
	}
	public static Set<String> intersectGeometryGrid(JGeometry geo,String[] meshIds)throws Exception{
		Set<String> grids = new HashSet<String>();
		int type = geo.getType();
		if(type==1){
			double[] point = geo.getPoint();
			CollectionUtils.addAll(grids, CompGridUtil.point2Grids(point[0],point[1],meshIds));
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
			grids.addAll(Arrays.asList(CompGridUtil.point2Grids(point[0],point[1])));
		}else if(type==2){
			double[] lines = geo.getOrdinatesArray();
			int pointCount = lines.length/2;
			for(int i=1;i<pointCount;i++){
				grids.addAll(CompGridUtil.intersectLineGrid(new double[]{lines[i*2-2],lines[i*2-1],lines[i*2],lines[i*2+1]},meshId,grids));
			}
		}else if(type == 3){
			double[] face = geo.getOrdinatesArray();
			grids.addAll(CompGridUtil.intersectFaceGrid(face, meshId));
		}
		return grids;
	}
	/**
	 * 传入线的几何，和所属的图幅号
	 * 所以线是被打断后的线，可以是图廓线
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
					int m7_s = CompGridUtil.point2Grid_M7(points[1]);
					int m7_e = CompGridUtil.point2Grid_M7(points[points.length-1]);
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
					int m8_s = CompGridUtil.point2Grid_M8(points[0]);
					int m8_e = CompGridUtil.point2Grid_M8(points[points.length-2]);
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
}
