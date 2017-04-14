package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/11/23 0023.
 */
public class Process extends AbstractProcess<Command> {
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
            }else if(irow instanceof RdLinkIntRtic){
                glmList.add(irow);
            }else if(irow instanceof RdLinkForm){
                glmList.add(irow);
            }else if(irow instanceof RdLinkName){
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
