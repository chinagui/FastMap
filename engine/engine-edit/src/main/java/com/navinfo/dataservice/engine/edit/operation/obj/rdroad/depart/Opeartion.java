package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.depart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

/**
 * 分离节点维护CRFR
* @ClassName: Opeartion 
* @author Zhang Xiaolong
* @date 2016年11月30日 上午10:30:35 
* @Description: 对构成此信息的Link及其任一端点Node进行分离，那么CRFRoad应该被删除；
* CRFObject中删除与其相关的单Link或Road，如果删除后无其他要素，CRFObj也被删除；原Node上的CRFInter不变；
 */
public class Opeartion {

    private Connection conn;

    public Opeartion(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        RdRoadLinkSelector selector = new RdRoadLinkSelector(conn);
        
        //分离节点的link包含crfroad
        RdRoad road = selector.loadRdRoadByLinkPid(oldLink.getPid(), true);
        
        if(road != null)
        {
        	result.insertObject(road, ObjStatus.DELETE, road.getPid());
        	
        	// 维护CRFO:如果删除的CRFI属于某个CRFO，要从CRFO组成信息中去掉
    		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
    				conn);
    		
    		List<Integer> roadPidList = new ArrayList<>();
    		
    		roadPidList.add(road.getPid());
    		
    		operation.deleteByType(roadPidList, ObjType.RDROAD, result);
        }
    }
}
