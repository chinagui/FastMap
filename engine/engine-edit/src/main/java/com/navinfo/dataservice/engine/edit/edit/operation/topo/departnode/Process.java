package com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private RdLink updateLink;

	private Check check = new Check();
	
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	public Process(Command command, Connection conn) throws Exception  {
		super(command);
		this.setCommand((Command) command); 

		this.setResult(new Result());

		this.setConn(conn);
	}

	// 锁定进入线为该link的交限
	public void lockRdRestriction() throws Exception {
		RdRestrictionSelector restriction = new RdRestrictionSelector(this.getConn());

		List<RdRestriction> restrictions = restriction
				.loadRdRestrictionByLinkNode(this.getCommand().getLinkPid(),
						this.getCommand().getsNodePid(), this.getCommand().geteNodePid(), true);

		this.getCommand().setRestrictions(restrictions);
	}

	public void lockRdLaneConnexity() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.getConn());

		List<RdLaneConnexity> lanes = selector.loadRdLaneConnexityByLinkNode(
				this.getCommand().getLinkPid(), this.getCommand().getsNodePid(),
				this.getCommand().geteNodePid(), true);

		this.getCommand().setLanes(lanes);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		List<RdBranch> branches = selector.loadRdBranchByLinkNode(
				this.getCommand().getLinkPid(), this.getCommand().getsNodePid(),
				this.getCommand().geteNodePid(), true);

		this.getCommand().setBranches(branches);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

		this.updateLink = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(),
				true);

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		return true;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkIsCrossNode(this.getConn(), this.getCommand().getsNodePid());

		check.checkIsCrossNode(this.getConn(), this.getCommand().geteNodePid());

		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());

		return null;
	}

	@Override
	public String run() throws Exception {
		try {
			this.getConn().setAutoCommit(false);

			String preCheckMsg = this.preCheck();

			this.prepareData();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = createOperation();

			operation.run(this.getResult());

			processRefObj();

			this.recordData();

			this.postCheck();

			this.getConn().commit();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		} finally {
			try {
				this.getConn().close();
			} catch (Exception e) {

			}
		}

		return null;
	}

	public void processRefObj() throws Exception {
		IOperation opRefRestrict = new OpRefRestrict(this.getCommand());
		opRefRestrict.run(this.getResult());

		IOperation opRefBranch = new OpRefBranch(this.getCommand());
		opRefBranch.run(this.getResult());

		IOperation opRefLaneConnexity = new OpRefLaneConnexity(this.getCommand());
		opRefLaneConnexity.run(this.getResult());
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), updateLink, check);
	}

}
