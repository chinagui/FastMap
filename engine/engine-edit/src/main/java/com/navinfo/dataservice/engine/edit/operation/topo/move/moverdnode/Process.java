package com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	public Process(Command command, Result result, Connection conn) throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}
	private RdNode updateNode;
	
	
	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		List<RdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		
		List<Integer> linkPids = new ArrayList<Integer>();
		
		for(RdLink link : links){
			linkPids.add(link.getPid());
		}

		this.getCommand().setLinks(links);
	}
	
	@Override
	public boolean prepareData() throws Exception {
		RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());
		
		this.updateNode = (RdNode) nodeSelector.loadById(this.getCommand().getNodePid(), true);
		
		lockRdLink();
		
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),updateNode,this.getConn()).run(this.getResult());
	}
	
	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand(), updateNode,this.getConn());

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}
}
