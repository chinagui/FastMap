package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefBranch implements IOperation {

	private Command command;

	private Result result;

	public OpRefBranch(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleRdBranchsIn(command.getInBranchs());

		this.handleRdBranchsOut(command.getOutBranchs());

		this.handleRdBranchVias(command.getBranchVias());

		return null;
	}

	// 处理交限进入线
	private void handleRdBranchsIn(List<RdBranch> list) throws Exception {

		for (RdBranch branch : list) {
			Map<String, Object> changedFields = branch.changedFields();

			int inLinkPid = 0;
			for (RdLink link : command.getNewLinks()) {
				if (branch.getNodePid() == link.getsNodePid()) {

					inLinkPid = link.getPid();
				}
			}

			changedFields.put("inLinkPid", inLinkPid);

			result.insertObject(branch, ObjStatus.UPDATE, branch.pid());

		}
	}

	// 处理交限退出线
	private void handleRdBranchsOut(List<RdBranch> list) throws Exception {

		for (RdBranch branch : list) {

			Map<String, Object> changedFields = branch.changedFields();

			for (RdLink link : command.getNewLinks()) {
				if (branch.igetOutNodePid() == link.getsNodePid() || branch.igetOutNodePid() == link.geteNodePid()) {

					changedFields.put("outLinkPid", link.getPid());

				}
			}

			result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
		}

	}

	// 处理交限经过线

	private void handleRdBranchVias(List<List<RdBranchVia>> list) throws Exception {

		int newLinkSize = command.getNewLinks().size();

		for (List<RdBranchVia> vias : list) {

			RdBranchVia breakVia = null;
			
			for (RdBranchVia v : vias) {
				//获取分歧进入点，进入点挂接进入线，依次递归查找经过线
				int branchInNodePid = v.igetInNodePid();
				
				//找打打断的link进行维护
				if (v.getLinkPid() == command.getLinkPid()) {

					breakVia = v;
					
					List<RdBranchVia> newBranchViaList = new ArrayList<>();
					while(newBranchViaList.size() != newLinkSize)
					{
						for (RdLink newLink : command.getNewLinks()) {
							//flag:判断是否要新增经过link
							boolean flag = false;
							if (branchInNodePid == newLink.getsNodePid()) {
								branchInNodePid = newLink.geteNodePid();
								flag = true;
							}
							else if(branchInNodePid == newLink.geteNodePid())
							{
								branchInNodePid = newLink.getsNodePid();
								flag = true;
							}
							if(flag)
							{
								RdBranchVia newVia = new  RdBranchVia();
								
								newVia.setGroupId(v.getGroupId());
								
								newVia.setSeqNum(v.getSeqNum() + newBranchViaList.size());
								
								newVia.setLinkPid(newLink.getPid());
								
								newVia.setBranchPid(v.getBranchPid());
								
								newBranchViaList.add(newVia);
								
								result.insertObject(newVia, ObjStatus.INSERT, newVia.getBranchPid());
							}
						}
					}
					result.insertObject(v, ObjStatus.DELETE, v.parentPKValue());
				} else if(breakVia != null){
					//找打打断的link维护后，需要维护后续的接续线的序号
					v.changedFields().put("seqNum", v.getSeqNum()+newLinkSize - 1);

					result.insertObject(v, ObjStatus.UPDATE, v.parentPKValue());
				}
				else if(breakVia == null)
				{
					//还没找打打断的link，递归进入点
					if(v.igetsNodePid() == branchInNodePid)
					{
						branchInNodePid = v.igeteNodePid();
					}
					else
					{
						branchInNodePid = v.igetsNodePid();
					}
				}
			}
		}
	}
}
