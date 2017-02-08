package com.navinfo.dataservice.engine.check.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: GLM01065
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 具有公交车专用道形态的link，开发状态只能为详细开发，其它报log。
 */
public class GLM01065 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01065.class);

	/**
	 * link形态为公交车道，并且为非详细开发状态的link
	 */
	private Map<Integer, RdLink> linkDevStateMap = new HashMap<>();

	public GLM01065() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Map.Entry<Integer, RdLink> entry : linkDevStateMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			logger.debug("检查类型：postCheck， 检查规则：GLM01065， 检查要素：RDLINK(" + linkPid + "), 触法时机：道路形态编辑；link开发状态属性编辑");
			this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + linkPid + "]", rdLink.getMeshId());
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		AbstractSelector selector = new AbstractSelector(RdLink.class, getConn());
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int developState = rdLink.getDevelopState();
				if (rdLink.changedFields().containsKey("developState")) {
					developState = (int) rdLink.changedFields().get("developState");
				}
				if (developState != 1) {
					for (IRow formRow : rdLink.getForms()) {
						RdLinkForm form = (RdLinkForm) formRow;

						if (form.getFormOfWay() == 22) {
							linkDevStateMap.put(rdLink.getPid(), rdLink);
							break;
						}
					}
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() == ObjStatus.UPDATE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 22) {
					if (!linkDevStateMap.containsKey(form.getLinkPid())) {
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);

						if (link.getDevelopState() != 1) {
							linkDevStateMap.put(link.getPid(), link);
						}
					}
				}
				if (form.status() == ObjStatus.DELETE && formOfWay == 22) {
					linkDevStateMap.remove(form.getLinkPid());
				}
			}
		}
	}

}
