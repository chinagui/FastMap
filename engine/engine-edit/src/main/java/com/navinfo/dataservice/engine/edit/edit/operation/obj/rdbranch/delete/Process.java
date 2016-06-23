package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private RdBranch branch;
	
//	private RdBranchDetail detail;
//	
//	private RdSignboard signboard;
//	
//	private RdSignasreal signasreal;
//	
//	private RdSeriesbranch seriesbranch;
//	
//	private RdBranchRealimage realimage;
//	
//	private RdBranchSchematic schematic;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public boolean prepareData() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());
		
		int type = this.getCommand().getBranchType();
		
		int detailId = this.getCommand().getDetailId();
		
		String rowId = this.getCommand().getRowId();
		
		this.branch = (RdBranch) selector.loadByDetailId(detailId,type,rowId,true);
		
		return true;
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.branch).run(this.getResult());
	}
}
