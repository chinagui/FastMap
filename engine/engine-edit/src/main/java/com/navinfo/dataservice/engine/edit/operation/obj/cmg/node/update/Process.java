package com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.update;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.update
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class Process extends AbstractProcess<Command> {

    @Override
    public boolean prepareData() throws Exception {
        CmgBuildnode cmgnode = (CmgBuildnode) new AbstractSelector(CmgBuildnode.class, getConn()).
                loadById(getCommand().getCmgnode().pid(), false);
        getCommand().setCmgnode(cmgnode);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand()).run(getResult());
    }
}
