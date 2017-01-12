package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhangyt
 * @Title: Process.java
 * @Description: TODO
 * @date: 2016年7月29日 下午3:45:06
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

    public Process() {
        super();
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {
        // 根据EleceyePid加载需要更新的RdElectroniceye
        RdLinkSelector selector = new RdLinkSelector(getConn());
        this.getCommand().setEleceye((RdElectroniceye) new RdElectroniceyeSelector(this.getConn()).loadById(this
                .getCommand().getPid(), true));
        this.getCommand().setLink((RdLink) selector.loadById(getCommand().getContent().getInt("linkPid"), true));
        RdLink sourceLink = (RdLink) selector.loadById(getCommand().getEleceye().getLinkPid(), true);
        Check check = new Check(getCommand(), getConn(), sourceLink, getCommand().getLink());
//        check.precheck();
        return false;
    }

    @Override
    public String exeOperation() throws Exception {
        new Operation(this.getCommand()).run(this.getResult());
        return null;
    }

}
