package com.navinfo.dataservice.engine.edit.obj;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;

/** 
 * @ClassName: RdLinkTopoObj
 * @author xiaoxiaowen4127
 * @date 2016年12月26日
 * @Description: RdLinkTopoObj.java
 */
public class RdLinkTopoObj {
	protected List<RdLink> delLinks;
	protected List<RdLink> addLinks;
	protected List<RdLink> modLinks;
	protected List<RdLink> relLinks;
	protected Map<Long,Long> inheritRelations;//key是源link，value是继承的link
	protected Map<Long,List<RdLane>> lanes;//key:link_pid
	protected List<RdLaneTopoDetail> laneTopoDetails;
	protected RdTollgate tollage;//inlink,innode参与的收费站
	protected RdLaneConnexity laneConexity;//inlink，innode参与的车信
	protected RdCross cross;//inLink参与的路口
	protected RdRestriction restriction;//inlink,innode参与的交限
	protected RdGate gate;//inlink,innode参与的大门
	
	public List<RdLinkForkObj> getForks(){
		
		return null;
	}
}
