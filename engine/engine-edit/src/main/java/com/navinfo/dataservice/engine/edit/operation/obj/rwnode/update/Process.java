package com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command>{
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	public Process(Command command,Result result,Connection conn) throws Exception {
		super(command,result,conn);
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand()).run(this.getResult());

	}
	
	@Override
	public boolean prepareData() throws Exception {

		RwNodeSelector selector = new RwNodeSelector(this.getConn());

		RwNode rwnode = (RwNode) selector.loadById(this.getCommand().getPid(),
				true);
		this.getCommand().setRwNode(rwnode);

		return true;
	}
	
	
	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand());

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

}
