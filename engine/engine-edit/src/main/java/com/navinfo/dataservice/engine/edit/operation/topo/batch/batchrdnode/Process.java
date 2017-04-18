package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process  extends AbstractProcess<Command> {
    public Process() {
    }

    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    /**
     * 将部分删除数据加入待检查列表中
     * @throws Exception
     */
    @Override
    public void postCheck() throws Exception {
        List<IRow> glmList = new ArrayList<>();
        glmList.addAll(this.getResult().getAddObjects());
        glmList.addAll(this.getResult().getUpdateObjects());
        for(IRow irow:this.getResult().getDelObjects()){
            if(irow instanceof RdLinkRtic){
                glmList.add(irow);
            }
            else if(irow instanceof RdNodeForm){
                glmList.add(irow);
            }
            else if(irow instanceof RdNodeMesh){
                glmList.add(irow);
            }else if(irow instanceof RdNodeName){
                glmList.add(irow);
            }
        }
        this.checkCommand.setGlmList(glmList);
        this.checkEngine.postCheck();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
