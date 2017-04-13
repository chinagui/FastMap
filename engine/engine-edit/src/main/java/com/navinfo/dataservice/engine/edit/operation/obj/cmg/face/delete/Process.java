package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildnodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;
import java.util.List;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
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
        // 加载CMG-FACE
        CmgBuildface cmgface = (CmgBuildface) new AbstractSelector(CmgBuildface.class, getConn()).
                loadById(getCommand().getCmgface().pid(), false);
        getCommand().setCmgface(cmgface);
        // 加载CMG-LINK
        List<CmgBuildlink> cmglinks = new CmgBuildlinkSelector(getConn()).listTheAssociatedLinkOfTheFace(cmgface.pid(), false);
        getCommand().setCmglinks(cmglinks);
        // 加载CMG-NODE
        List<CmgBuildnode> cmgnodes = new CmgBuildnodeSelector(getConn()).listTheAssociatedNodeOfTheFace(cmgface.pid(), false);
        getCommand().setCmgnodes(cmgnodes);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand()).run(getResult());
    }
}
