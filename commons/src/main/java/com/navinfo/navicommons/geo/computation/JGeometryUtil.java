package com.navinfo.navicommons.geo.computation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: JGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年4月18日 下午6:27:09 
* @Description: TODO
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
				return MeshUtils.line2MeshId(new double[]{arr[0],arr[1],arr[len-2],arr[len-1]});
			}else if (type==3){
				//...
				return null;
			}
		}
		return null;
	}
}
