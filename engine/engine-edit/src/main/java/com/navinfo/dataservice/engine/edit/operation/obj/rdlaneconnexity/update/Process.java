package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdLaneConnexity lane;

	@Override
	public boolean prepareData() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.getConn());

		this.lane = (RdLaneConnexity) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.lane, this.getConn()).run(this.getResult());
	}
	
	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub
		// this.createPostCheckGlmList();
		List<IRow> glmList = new ArrayList<IRow>();
		glmList.addAll(this.getResult().getAddObjects());
		glmList.addAll(this.getResult().getUpdateObjects());
		for(IRow irow:this.getResult().getDelObjects()){
			if(irow instanceof RdLaneVia){
				glmList.add(irow);
			}
		}
		this.checkCommand.setGlmList(glmList);
		this.checkEngine.postCheck();

	}

}
