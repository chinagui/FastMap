package com.navinfo.dataservice.commons.geom;

import java.util.List;

import com.navinfo.dataservice.commons.util.MeshUtils;

import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: JGeometryUtil 
* @author Xiao Xiaowen 
* @date 2016年4月18日 下午6:27:09 
* @Description: TODO
*/
public class JGeometryUtil {

	public static String[] geo2MeshIds(JGeometry geo){
		if(geo!=null){
			int type = geo.getType();
			if(type==1){
				double[] point = geo.getPoint();
				List<String> result  = MeshUtils.lonlat2MeshIds(point[0], point[1]);
				return result.toArray(new String[0]);
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
