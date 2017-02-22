package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.parameterCheck.DepartCheck;

public class Process extends AbstractProcess<Command> {

    private Check check = new Check();

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    public Process(AbstractCommand command, Connection conn) throws Exception {
        this(command);
        this.setConn(conn);
    }

    @Override
    public boolean prepareData() throws Exception {

        RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

        RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());

        RdLink link = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);
        List<RdLink> links = linkSelector.loadByNodePidOnlyRdLink(this.getCommand().getNodePid(), true);
        RdNode node = (RdNode) nodeSelector.loadById(this.getCommand().getNodePid(), true);
        this.getCommand().setLinks(links);
        this.getCommand().setRdLink(link);
        this.getCommand().setNode(node); 
    	
        return true;
    }
    
    private void parameterCheck() throws Exception {
		DepartCheck departCheck = new DepartCheck(this.getConn());

		departCheck.checkIsVia(this.getCommand().getLinkPid());

		departCheck.checkIsSameNode(this.getCommand().getNodePid(),
				"RD_NODE");
	}

    @Override
    public String preCheck() throws Exception {
    	
        check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
        // 分离节点检查CRFI
        check.checkCRFI(getConn(), getCommand().getNodePid());
        // 分离节点检查顺行
        check.checkRdDirectRAndLaneC(getConn(), getCommand().getNodePid(), getCommand().getLinkPid());
        return super.preCheck();
    }

    @Override
    public String exeOperation() throws Exception {
    	
    	parameterCheck();
    	
        Operation operation = new Operation(this.getCommand(), this.getConn());
        String msg = operation.run(this.getResult());
        return msg;

    }

    public String innerRun() throws Exception {
        String msg;
        try {

            this.prepareData();

            String preCheckMsg = this.preCheck();

            if (preCheckMsg != null) {
                throw new Exception(preCheckMsg);
            }

            Operation operation = new Operation(this.getCommand(), this.getConn());
            msg = operation.run(this.getResult());

            this.postCheck();
        } catch (Exception e) {

            this.getConn().rollback();

            throw e;
        }
        return msg;
    }

}
