package com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
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

    @Override
    public boolean prepareData() throws Exception {
        // 加载待分离节点
        CmgBuildnode cmgnode = (CmgBuildnode) new AbstractSelector(CmgBuildnode.class, getConn()).
                loadById(getCommand().getCmgnode().pid(), true);
        getCommand().setCmgnode(cmgnode);
        // 加载待分离线
        CmgBuildlink cmglink = (CmgBuildlink) new AbstractSelector(CmgBuildlink.class, getConn()).
                loadById(getCommand().getCmglink().pid(), true);
        getCommand().setCmglink(cmglink);
        // 加载分离节点挂接线
        List<CmgBuildlink> cmglinks = new CmgBuildlinkSelector(getConn()).listTheAssociatedLinkOfTheNode(cmgnode.pid(), true);
        getCommand().setCmglinks(cmglinks);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
