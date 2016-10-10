package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	private IRow row;

	public Operation()
	{
	}

	public Operation(Command command, RdBranch branch, IRow row) {
		this.command = command;

		this.branch = branch;

		this.row = row;

	}

	@Override
	public String run(Result result) throws Exception {

		int branchType = command.getBranchType();

		if (branchType < 5 && branchType >= 0) {
			if (branch.getDetails().size() == 1 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {
				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
			}
			command.setObjType(ObjType.RDBRANCHDETAIL);
		}

		// 实景图
		if (branchType == 5) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 1 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
				command.setObjType(ObjType.RDBRANCHREALIMAGE);
			}
		}
		// 实景看板
		if (branchType == 6) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 1 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
				command.setObjType(ObjType.RDSIGNASREAL);
			}
		}
		// 连续分歧
		if (branchType == 7) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 1
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
				command.setObjType(ObjType.RDSERIESBRANCH);
			}
		}
		// 大规模交叉点
		if (branchType == 8) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 1) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
				command.setObjType(ObjType.RDBRANCHSCHEMATIC);
			}
		}

		// 方向看板
		if (branchType == 9) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 1
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				result.insertObject(row, ObjStatus.DELETE, branch.getPid());
				command.setObjType(ObjType.RDSIGNBOARD);
			}
		}
		return null;
	}

	/**
	 * 删除进入link对分歧的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteBranchInfectData(int linkPid, Connection conn) throws Exception {

		RdBranchSelector selector = new RdBranchSelector(conn);

		List<RdBranch> branches = selector.loadRdBranchByInLinkPid(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : branches) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除退出link对分歧的删除影响
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteBOutLinkranchInfectData(int linkPid,Connection conn) throws Exception {
		
		RdBranchSelector selector = new RdBranchSelector(conn);

		// 获取退出线为该link
		List<RdBranch> branches2 = selector.loadRdBranchByOutLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : branches2) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
	
	/**
	 * 删除link作为经过线对分歧的删除影响
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteBViaLinkranchInfectData(int linkPid,Connection conn) throws Exception {
		
		RdBranchSelector selector = new RdBranchSelector(conn);

		// 获取退出线为该link
		List<RdBranch> branches = selector.loadRdBranchByViaLinkPid(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : branches) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
	
	/**
	 * 删除路口对分歧的删除影响
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteCrossBranchInfectData(List<RdBranch> branches) throws Exception {
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : branches) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
