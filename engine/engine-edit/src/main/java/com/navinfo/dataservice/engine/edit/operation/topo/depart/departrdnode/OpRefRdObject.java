package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
* @ClassName: OpRefRdObject 
* @author Zhang Xiaolong
* @date 2016年11月30日 上午11:24:51 
* @Description: 分离节点维护CRF
 */
public class OpRefRdObject {
    private Connection conn;

    public OpRefRdObject(Connection conn) {
        this.conn = conn;
    }

    public void updateRelation(Command command, List<RdLink> newLinks, Result result) throws Exception {
    	
        //分离节点后，如果link作为CRFO的组成link，需要删除RDOBJECTLINK关系
        com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
				this.conn);
		
		List<Integer> linkPidList = new ArrayList<>();
		
		linkPidList.add(command.getLinkPid());
		
		rdinterOperation.deleteByType(linkPidList, ObjType.RDLINK, result);
    }
}
