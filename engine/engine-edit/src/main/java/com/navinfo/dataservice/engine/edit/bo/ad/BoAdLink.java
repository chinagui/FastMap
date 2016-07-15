package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.vividsolutions.jts.geom.Point;

/** 
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class BoAdLink {
	protected AdLink adLink;
	protected List<AdLinkMesh> meshes;
	protected BoAdNode sNode;
	protected BoAdNode eNode;
	
	public Result breakoff(Point point){
		return null;
	}
	public Result breakoff(BoAdNode node){
		return null;
	}
	
}
