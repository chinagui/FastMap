package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    private RdLink updateLink;

    @Override
    public boolean prepareData() throws Exception {

        RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

        this.updateLink = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);

        return false;
    }

    @Override
    public String exeOperation() throws Exception {
        Operation operation = new Operation(this.getCommand(), updateLink, this.getConn());
        // 判断是否检查，如检查发现没有受影响信号灯直接执行修改，如有影响则返回提示信息
        if (getCommand().isInfect()) {
            String msg = operation.updateRdTraffic(this.getResult());
            if (null == msg || msg.isEmpty()) {
                operation.run(this.getResult());
            }
        } else {
            operation.run(this.getResult());
        }
        return "";
    }

    public String innerRun() throws Exception {
        String msg;
        try {
            this.prepareData();

            String preCheckMsg = this.preCheck();

            if (preCheckMsg != null) {
                throw new Exception(preCheckMsg);
            }

            IOperation operation = new Operation(this.getCommand(), this.updateLink);

            msg = operation.run(this.getResult());

            this.postCheck();

        } catch (Exception e) {

            this.getConn().rollback();

            throw e;
        }

        return msg;
    }
}
