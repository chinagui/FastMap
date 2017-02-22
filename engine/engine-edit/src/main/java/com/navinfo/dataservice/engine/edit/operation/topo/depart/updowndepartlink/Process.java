package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Operation;

/**
 * @author zhaokk
 *         制作上下线分离
 */

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    /*
     * 加载上下线分离线
     */
    public void lockRdLinks() throws Exception {
        RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());
        this.getCommand().setLinks(linkSelector.loadByPids(this.getCommand().getLinkPids(), true));
    }

    @Override
    public boolean prepareData() throws Exception {
        lockRdLinks();
        //new Check(getCommand(), getConn()).preCheck();
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
    }

}
