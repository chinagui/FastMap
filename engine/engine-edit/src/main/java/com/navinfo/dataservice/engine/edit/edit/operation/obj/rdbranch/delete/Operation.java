package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	public Operation(Command command, RdBranch branch) {
		this.command = command;

		this.branch = branch;

	}

	@Override
	public String run(Result result) throws Exception {

		int branchType = command.getBranchType();

		int detailId = command.getDetailId();

		String rowId = command.getRowId();

		if (branchType < 5) {
			if (branch.getDetails().size() == 1 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getDetails()) {
					RdBranchDetail branchDetail = (RdBranchDetail) detail;

					if (branchDetail.getPid() == detailId) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}

		// 实景图
		if (branchType == 5) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 1 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getRealimages()) {
					RdBranchRealimage branchDetail = (RdBranchRealimage) detail;

					if (branchDetail.getRowId().equals(rowId)) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}
		// 实景看板
		if (branchType == 6) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 1 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getRealimages()) {
					RdSignboard branchDetail = (RdSignboard) detail;

					if (branchDetail.getPid() == detailId) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}
		// 连续分歧
		if (branchType == 7) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 1
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getRealimages()) {
					RdSeriesbranch branchDetail = (RdSeriesbranch) detail;

					if (branchDetail.getRowId().equals(rowId)) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}
		// 大规模交叉点
		if (branchType == 8) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 0
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 1) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getRealimages()) {
					RdBranchSchematic branchDetail = (RdBranchSchematic) detail;

					if (branchDetail.getPid() == detailId) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}

		// 方向看板
		if (branchType == 9) {
			if (branch.getDetails().size() == 0 && branch.getSignboards().size() == 1
					&& branch.getSignasreals().size() == 0 && branch.getSeriesbranches().size() == 0
					&& branch.getRealimages().size() == 0 && branch.getSchematics().size() == 0) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());
			} else {
				for (IRow detail : branch.getRealimages()) {
					RdSignboard branchDetail = (RdSignboard) detail;

					if (branchDetail.getPid() == detailId) {
						result.insertObject(branchDetail, ObjStatus.DELETE, branch.getPid());
					}
					break;
				}
			}
		}
		return null;
	}

}
