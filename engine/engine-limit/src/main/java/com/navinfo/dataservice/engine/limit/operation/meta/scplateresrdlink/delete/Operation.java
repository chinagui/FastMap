package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation {


    private Connection conn=null;
    
    private Command command = null;

    public Operation(Connection conn) {
        this.conn = conn;
    }
    
    public Operation (Connection conn,Command command){
    	this.conn = conn;
    	this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {

    	for(ScPlateresRdLink link:this.command.getRdLinks()){
    		 result.insertObject(link, ObjStatus.DELETE, String.valueOf(link.getLinkPid()));
    	}
    	
        return null;
    }


    public void delByGeometryId(String geometryId, Result result) throws Exception {

        if (conn == null) {
            return;
        }

        ScPlateresRdlinkSearch search = new ScPlateresRdlinkSearch(this.conn);

        List<ScPlateresRdLink> links = search.loadByGeometryId(geometryId);

        for (ScPlateresRdLink link : links) {

            result.insertObject(link, ObjStatus.DELETE, String.valueOf(link.getLinkPid()));
        }
    }
}
