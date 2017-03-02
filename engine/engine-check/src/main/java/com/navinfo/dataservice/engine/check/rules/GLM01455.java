package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 同一路口挂接的所有道路（交叉口内LINK除外）都有POI连接路形态，路口中任意一条交叉口内LINK没有POI连接路形态时，程序报LOG
 * 
 * @ClassName: GLM01455
 * @author Zhang Xiaolong
 * @date 2017年2月24日 下午4:12:03
 * @Description: TODO
 */
public class GLM01455 extends baseRule {
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				RdLinkSelector linkSelector = new RdLinkSelector(getConn());
				RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(form.getLinkPid(), false);

				int sNodePid = link.getsNodePid();
				checkCrossNode(linkSelector, link, sNodePid);

				int eNodePid = link.geteNodePid();
				checkCrossNode(linkSelector, link, eNodePid);
			}
		}
	}

	private void checkCrossNode(RdLinkSelector linkSelector, RdLink link, int sNodePid) throws Exception {
		RdCrossSelector crossSelector = new RdCrossSelector(getConn());
		RdCross cross = null;
		try {
			cross = crossSelector.loadCrossByNodePid(sNodePid, false);
		} catch (Exception e) {
			return;
		}

		// 路口Nodes
		List<Integer> nodes = new ArrayList<>();
		for (IRow n : cross.getNodes()) {
			nodes.add(((RdCrossNode) n).getNodePid());
		}
		List<RdLink> links = linkSelector.loadByNodePids(nodes, false);

		// 路口内Links<交叉口内link>
		List<Integer> innerLinks = new ArrayList<>();
		for (IRow l : cross.getLinks()) {
			innerLinks.add(((RdCrossLink) l).getLinkPid());
		}

		boolean flag = false; 

		for (RdLink l : links) {
			if (!innerLinks.contains(l.pid())) {
				List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(l.pid(), false);
				l.setForms(forms);

				boolean formFlag = false;
				for (IRow f : forms) {
					RdLinkForm ff = (RdLinkForm) f;
					if (ff.getFormOfWay() == 36) {
						formFlag = true;
						break;
					}
				}
				if (!formFlag) {
					flag = true;
					break;
				}
			}
		}

		// 除交叉口内link外，其余link均为poi连接路
		if (!flag) {
			for (RdLink innerLink : links) {
				if (innerLinks.contains(innerLink.pid())) {
					List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(innerLink.pid(), false);
					flag = true;

					for (IRow linkform : forms) {
						RdLinkForm ff = (RdLinkForm) linkform;

						if (ff.getFormOfWay() == 36) {
							flag = false;
							break;
						}
					}
					if (flag) {
						setCheckResult(innerLink.getGeometry(), "[RD_LINK," + innerLink.pid() + "]",

								innerLink.mesh());
						break;
					}
				}
			}
		}
	}
}
