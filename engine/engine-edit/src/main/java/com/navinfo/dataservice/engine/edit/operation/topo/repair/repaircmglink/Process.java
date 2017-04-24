package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;
import java.util.List;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/17
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
        CmgBuildlink cmglink = (CmgBuildlink) new AbstractSelector(CmgBuildlink.class, getConn())
                .loadById(getCommand().getCmglink().pid(), true);
        getCommand().setCmglink(cmglink);

        List<CmgBuildface> cmgfaces = new CmgBuildfaceSelector(getConn()).listTheAssociatedFaceOfTheLink(cmglink.pid(), true);
        getCommand().setCmgfaces(cmgfaces);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
