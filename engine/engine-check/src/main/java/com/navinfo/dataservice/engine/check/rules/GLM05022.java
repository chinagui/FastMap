package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 分歧类型为“方面分歧”的分歧,箭头图标识为“无”，否则报log 区分普通方面、高速方面的方法是：判断进出线、退出线都是高速的为高速方面，其余均为普通方面
 * 
 */
public class GLM05022 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		// 本次操作涉及的分歧的pid
		List<Integer> branchPids = new ArrayList<Integer>();

		for (IRow obj : checkCommand.getGlmList()) {

			if (obj instanceof RdBranch) {

				RdBranch branch = (RdBranch) obj;

				if (!branchPids.contains(branch.pid())) {
					
					branchPids.add(branch.pid());
				}
			}
			if (obj instanceof RdBranchDetail) {

				RdBranchDetail detail = (RdBranchDetail) obj;
				
				if (!branchPids.contains(detail.getBranchPid())) {
					
					branchPids.add(detail.getBranchPid());
				}
			}
		}

		if (branchPids.size() == 0) {
			return;
		}

		// 不符合条件的分歧
		List<RdBranch> filterRdBranchs = new ArrayList<RdBranch>();

		// 不符合条件的分歧对应link的pid
		List<Integer> filterLinkPids = new ArrayList<Integer>();

		RdBranchSelector branchSelector = new RdBranchSelector(getConn());

		List<IRow> rowBranchs = branchSelector.loadByIds(branchPids, false,
				true);

		for (IRow rowBranch : rowBranchs) {

			RdBranch branch = (RdBranch) rowBranch;

			for (IRow rowDetail : branch.getDetails()) {

				RdBranchDetail detail = (RdBranchDetail) rowDetail;

				if (detail.getBranchType() == 1 && detail.getArrowFlag() != 0) {

					filterRdBranchs.add(branch);

					filterLinkPids.add(branch.getInLinkPid());

					filterLinkPids.add(branch.getOutLinkPid());

					break;
				}
			}
		}

		RdLinkSelector rdLinkSelector = new RdLinkSelector(getConn());

		List<IRow> rowLinks = rdLinkSelector.loadByIds(filterLinkPids, false,
				false);

		Map<Integer, RdLink> linkMap = new HashMap<Integer, RdLink>();

		for (IRow rowLink : rowLinks) {

			RdLink rdLink = (RdLink) rowLink;

			linkMap.put(rdLink.getPid(), rdLink);
		}

		for (RdBranch branch : filterRdBranchs) {

			RdLink inLink = linkMap.get(branch.getInLinkPid());

			RdLink outLink = linkMap.get(branch.getOutLinkPid());

			if ((inLink.getKind() == 1 || inLink.getKind() == 2)
					&& (outLink.getKind() == 1 || outLink.getKind() == 2)) {

				this.setCheckResult("", "[RD_ELECTRONICEYE," + branch.getPid()
						+ "]", 0, "高速方面名称，箭头图标识应为“无”");
			} else {

				this.setCheckResult("", "[RD_ELECTRONICEYE," + branch.getPid()
						+ "]", 0, "普通方面名称，箭头图标识应为“无”");
			}
		}
	}
}
