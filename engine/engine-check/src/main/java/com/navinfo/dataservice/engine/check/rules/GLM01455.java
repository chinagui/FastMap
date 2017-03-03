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

	// 所在路口的nodes
	private List<Integer> getCrossNode(int nodePid) throws Exception {

		List<Integer> nodes = new ArrayList<>();

		RdCrossSelector crossSelector = new RdCrossSelector(getConn());
		RdCross cross = null;
		try {
			cross = crossSelector.loadCrossByNodePid(nodePid, false);
		} catch (Exception e) {
			return nodes;
		}

		for (IRow n : cross.getNodes()) {
			nodes.add(((RdCrossNode) n).getNodePid());
		}

		return nodes;
	}

	// 交叉口内links
	private List<Integer> getInnerLink(List<RdLink> links) throws Exception {

		List<Integer> innerLinks = new ArrayList<>();

		for (RdLink link : links) {
			List<IRow> linkForm = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(link.pid(),
					false);
			link.setForms(linkForm);

			for (IRow linkform : linkForm) {
				RdLinkForm form = (RdLinkForm) linkform;

				if (form.getFormOfWay() == 50) {
					innerLinks.add(link.getOriginLinkPid());
					break;
				}
			}
		}

		return innerLinks;
	}

	private boolean isPoiRoadExceptInner(List<RdLink> links, List<Integer> innerLinks) throws Exception {
		boolean flag = false;

		for (RdLink l : links) {
			if (!innerLinks.contains(l.pid())) {
				List<IRow> forms = l.getForms();

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

		return flag;
	}

	private void checkCrossNode(RdLinkSelector linkSelector, RdLink link, int sNodePid) throws Exception {

		List<Integer> nodes = getCrossNode(sNodePid);

		List<RdLink> links = linkSelector.loadByNodePids(nodes, false);

		List<Integer> innerLinks = getInnerLink(links);

		boolean flag = isPoiRoadExceptInner(links, innerLinks);

		if (true == flag) {
			return;
		}
		
		// 除交叉口内link外，其余link均为poi连接路
		for (RdLink innerLink : links) {
			if (innerLinks.contains(innerLink.pid())) {
				List<IRow> forms = innerLink.getForms();
				flag = true;

				for (IRow linkform : forms) {
					RdLinkForm ff = (RdLinkForm) linkform;

					if (ff.getFormOfWay() == 36) {
						flag = false;
						break;
					}
				}
				if (flag) {
					setCheckResult(innerLink.getGeometry(), "[RD_LINK," + innerLink.pid() + "]",innerLink.mesh());
					break;
				}
			}
		}

	}
}
