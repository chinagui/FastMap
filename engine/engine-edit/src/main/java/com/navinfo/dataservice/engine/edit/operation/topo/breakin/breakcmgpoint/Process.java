package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint
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
        // 加载CMG-LINK信息
        IRow row = new AbstractSelector(CmgBuildlink.class, getConn()).loadById(getCommand().getCmglink().pid(), true);
        getCommand().setCmglink((CmgBuildlink) row);
        // 加载关联CMG-FACE信息
        List<CmgBuildface> cmgfaces = new CmgBuildfaceSelector(getConn()).
                listTheAssociatedFaceOfTheLink(getCommand().getCmglink().pid(), true);
        getCommand().setCmgfaces(cmgfaces);
        // 获取由该link组成的立交（RDGSC）
        List<RdGsc> gscs = new RdGscSelector(this.getConn()).
                loadRdGscLinkByLinkPid(this.getCommand().getCmglink().pid(), "CMG_BUILDLINK", true);
        this.getCommand().setGscs(gscs);

        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        // 处理CMG-LINK打断/CMG-FACE打断/处理立交
        return new Operation(getCommand(), getConn()).run(getResult());
    }

    @Override
    public String innerRun() throws Exception {
        this.prepareData();

        // 处理CMG-LINK打断/CMG-FACE打断/处理立交
        new Operation(getCommand(), getConn()).run(getResult());

        String preCheckMsg = super.preCheck();
        if (StringUtils.isNotEmpty(preCheckMsg)) {
            throw new Exception(preCheckMsg);
        }
        return null;
    }
}
