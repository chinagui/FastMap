package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private RdBranch branch;

	private IRow row;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		int branchType = this.getCommand().getBranchType();

		int detailId = this.getCommand().getDetailId();

		String rowId = this.getCommand().getRowId();

		if (branchType >= 0 && branchType <= 4) {
			this.row = new AbstractSelector(RdBranchDetail.class, getConn()).loadById(detailId, true);
		}
		if (branchType == 5) {
			this.row = new AbstractSelector(RdBranchRealimage.class, this.getConn()).loadByRowId(rowId, true);
		}
		if (branchType == 6) {
			this.row = new AbstractSelector(RdSignasreal.class, getConn()).loadById(detailId, true);
		}
		if (branchType == 7) {
			this.row = new AbstractSelector(RdSeriesbranch.class, getConn()).loadByRowId(rowId, true);
		}
		if (branchType == 8) {
			this.row = new AbstractSelector(RdBranchSchematic.class, getConn()).loadById(detailId, true);
		}
		if (branchType == 9) {
			this.row = new AbstractSelector(RdSignboard.class, getConn()).loadById(detailId, true);
		}
		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		this.branch = (RdBranch) selector.loadById(row.parentPKValue(), true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.branch,this.row).run(this.getResult());
	}
}
