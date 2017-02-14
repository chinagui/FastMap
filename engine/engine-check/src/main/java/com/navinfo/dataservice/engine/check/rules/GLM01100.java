package com.navinfo.dataservice.engine.check.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: GLM01100
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 桥形态的link铺设状态不能是“未铺设”
 */
public class GLM01100 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01100.class);

	/**
	 * link铺设状态为非“未铺设”的link
	 */
	private Map<Integer, RdLink> linkPaveStateMap = new HashMap<>();

	/**
	 * 形态为桥的link
	 */
	private Set<Integer> linkFormOf30List = new HashSet<>();

	public GLM01100() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Map.Entry<Integer, RdLink> entry : linkPaveStateMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			if (linkFormOf30List.contains(linkPid)) {
				logger.debug("检查类型：postCheck， 检查规则：GLM01100， 检查要素：RDLINK(" + linkPid + "), 触法时机：道路形态编辑；link属性编辑");
				this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + linkPid + "]", rdLink.getMeshId());
			}
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
				int paveStatus = rdLink.getPaveStatus();
				if (rdLink.changedFields().containsKey("paveStatus")) {
					paveStatus = (int) rdLink.changedFields().get("paveStatus");
				}
				if (paveStatus == 1) {
					linkPaveStateMap.put(rdLink.getPid(), rdLink);
					for (IRow formRow : rdLink.getForms()) {
						RdLinkForm form = (RdLinkForm) formRow;

						if (form.getFormOfWay() == 30) {
							linkFormOf30List.add(form.getLinkPid());
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
				if (form.status() != ObjStatus.DELETE && formOfWay == 30) {
					linkFormOf30List.add(form.getLinkPid());
					if (!linkPaveStateMap.containsKey(form.getLinkPid())) {
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);

						if (link.getPaveStatus() == 1) {
							linkPaveStateMap.put(link.getPid(), link);
						}
					}
				}
				if (form.status() == ObjStatus.DELETE && formOfWay == 30) {
					linkFormOf30List.remove(form.getLinkPid());
				}
			}
		}
	}

}
