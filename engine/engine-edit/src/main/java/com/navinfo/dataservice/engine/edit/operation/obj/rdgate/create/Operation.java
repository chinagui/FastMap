package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;


public class Operation implements IOperation {
	
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	private Connection conn;

	private Result result;
	
	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			String resultMsg = null;
			createGate(result);
			return resultMsg;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 创建大门
	 * @param result
	 * @throws Exception
	 */
	public void createGate(Result result) throws Exception {
		try {
			this.result = result;
			RdGate rdGate = new RdGate();
			rdGate.setPid(PidUtil.getInstance().applyRdGate());
			rdGate.setInLinkPid(command.getInLinkPid());
			rdGate.setOutLinkPid(command.getOutLinkPid());
			rdGate.setNodePid(command.getNodePid());
			rdGate.setDir(getDir(command.getInLinkPid(),command.getOutLinkPid()));
			rdGate.setCondition(getCondition(rdGate.getPid()));
			this.result.insertObject(rdGate, ObjStatus.INSERT, rdGate.getPid());
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 判断默认大门方向
	 * @param inPid
	 * @param outPid
	 * @return
	 * @throws Exception
	 */
	public int getDir(int inPid,int outPid) throws Exception {
		int dir = 0;
		log.info("开始判断大门方向");
		try {
			RdLinkSelector rdLinkSelector = new RdLinkSelector(conn);
			RdLink rdLinkIn = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(inPid,false);
			RdLink rdLinkOut = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(outPid,false);
			if (rdLinkIn.getDirect() == 2 || rdLinkIn.getDirect() == 3 || rdLinkOut.getDirect() == 2 || rdLinkOut.getDirect() == 3) {
				dir = 1;
			} else {
				dir = 2;
			}
		} catch (Exception e) {
			throw e;
		}
		return dir;
	}
	
	public List<IRow> getCondition(int pid) {
		List<IRow> conditionList = new ArrayList<IRow>();
		RdGateCondition conditionOne = new RdGateCondition();
		conditionOne.setPid(pid);
		conditionList.add(conditionOne);
		RdGateCondition conditionTwo = new RdGateCondition();
		conditionTwo.setPid(pid);
		conditionTwo.setValidObj(1);
		conditionList.add(conditionTwo);
		return conditionList;
	}

}
