package com.navinfo.dataservice.FosEngine.edit.operation.topo.movenode;

import java.sql.Connection;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;

public class Process implements IProcess {
	
	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private RdLink updateLink;
	
	private RdNode updateNode;
	
	public Process(ICommand command) throws Exception {
		

	}

	@Override
	public ICommand getCommand() {
		
		return command;
	}

	@Override
	public Result getResult() {
		
		return result;
	}

	@Override
	public boolean prepareData() throws Exception {
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String run() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPostCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordData() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
