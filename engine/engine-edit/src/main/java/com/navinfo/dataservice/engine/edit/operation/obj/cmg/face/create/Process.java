package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.List;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/11
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
        if (!CollectionUtils.isEmpty(getCommand().getLinkPids())) {
            List<IRow> cmglinks = new AbstractSelector(CmgBuildlink.class, getConn()).loadByIds(getCommand().getLinkPids(), false, true);
            getCommand().setCmglinks(cmglinks);
        }
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
