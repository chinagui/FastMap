package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * GLM01118
 * 
 * @ClassName: GLM01118
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 高速/城高/国道/省道的“开发状态”不应是“未验证”
 */

public class GLM01118 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01118.class);

	private List<RdLink> checkLinkList = new ArrayList<>();

	public GLM01118() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		List<Integer> linkPidList = new ArrayList<>();
		for (RdLink rdLink : checkLinkList) {
			if(!linkPidList.contains(rdLink.getPid()))
			{
				logger.info("检查类型：postCheck， 检查规则：GLM01118， 检查要素：RDLINK(" + rdLink.pid() + ")");
				this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
			}
			linkPidList.add(rdLink.getPid());
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
				RdLink rdLink = (RdLink) row;
				int kind = rdLink.getKind();
				if (rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				int devState = rdLink.getDevelopState();
				if (rdLink.changedFields().containsKey("developState")) {
					devState = (int) rdLink.changedFields().get("developState");
				}
				if (kind <= 4 && devState == 2) {
					checkLinkList.add(rdLink);
				}
			}
		}
	}
}
