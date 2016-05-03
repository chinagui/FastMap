package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {
		
		check.checkDupilicateNode(this.getCommand().getGeometry());
		
		check.checkGLM04002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
		
		check.checkGLM13002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
		
		return null;
	}

	@Override
	public String run() throws Exception {
		String msg;
		try {
			this.getConn().setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			Operation operation = (Operation)createOperation();

			msg = operation.run(this.getResult());

			this.recordData();
			
			operation.breakLine();

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

		return msg;
	}

	@Override
	public void postCheck() throws Exception {
		
		check.postCheck(this.getConn(), this.getResult());
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), check, this.getConn());
	}
	
}
