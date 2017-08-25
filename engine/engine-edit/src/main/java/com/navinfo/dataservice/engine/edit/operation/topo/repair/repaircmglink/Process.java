package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

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
        // 加载修行线
        CmgBuildlink cmglink = (CmgBuildlink) new AbstractSelector(CmgBuildlink.class, getConn())
                .loadById(getCommand().getCmglink().pid(), true);
        getCommand().setCmglink(cmglink);
        // 加载修行线关联面
        List<CmgBuildface> cmgfaces = new CmgBuildfaceSelector(getConn()).listTheAssociatedFaceOfTheLink(cmglink.pid(), true);
        getCommand().setCmgfaces(cmgfaces);
        // 获取由该link组成的立交（RDGSC）
        List<RdGsc> gscs = new RdGscSelector(this.getConn()).
                loadRdGscLinkByLinkPid(this.getCommand().getCmglink().pid(), "CMG_BUILDLINK", true);
        this.getCommand().setGscs(gscs);

        return super.prepareData();
    }

    Check check=new Check();
    @Override
    public String exeOperation() throws Exception {
    	check.PERMIT_MODIFICATE_POLYGON_ENDPOINT(this.getCommand(), this.getConn());
    	check.checkIntersectFace(this.getCommand(), this.getConn());
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
