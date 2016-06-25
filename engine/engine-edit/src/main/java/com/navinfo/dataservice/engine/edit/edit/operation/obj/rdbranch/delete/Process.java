package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchRealimageSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSchematicSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSeriesbranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSignasrealSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdSignboardSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

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
			RdBranchDetailSelector selector = new RdBranchDetailSelector(this.getConn());

			this.row = selector.loadById(detailId, true);
		}
		if (branchType == 5) {
			RdBranchRealimageSelector selector = new RdBranchRealimageSelector(this.getConn());

			this.row = selector.loadByRowId(rowId, true);
		}
		if (branchType == 6) {
			RdSignasrealSelector selector = new RdSignasrealSelector(this.getConn());

			this.row = selector.loadById(detailId, true);
		}
		if (branchType == 7) {
			RdSeriesbranchSelector selector = new RdSeriesbranchSelector(this.getConn());

			this.row = selector.loadByRowId(rowId, true);
		}
		if (branchType == 8) {
			RdBranchSchematicSelector selector = new RdBranchSchematicSelector(this.getConn());

			this.row = selector.loadById(detailId, true);
		}
		if (branchType == 9) {
			RdSignboardSelector selector = new RdSignboardSelector(this.getConn());

			this.row = selector.loadById(detailId, true);
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
