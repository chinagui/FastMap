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
 * GLM01104_1 Link信息 轮渡、人渡的总车道数必须为1
 * 
 * @ClassName: GLM01104_1
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 轮渡、人渡的总车道数必须为1
 */

public class GLM01104_1 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01104_1.class);

	private List<RdLink> checkRdLink = new ArrayList<>();

	public GLM01104_1() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		List<Integer> linkPids = new ArrayList<>();
		for (RdLink rdLink : checkRdLink) {
			if (linkPids.contains(rdLink.pid()))
				continue;
			logger.debug("检查类型：postCheck， 检查规则：GLM01104_1， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");

			this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
			// 检查过的link加入linkPid集合中
			linkPids.add(rdLink.pid());
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int kind = rdLink.getKind();

				int laneNum = rdLink.getLaneNum();

				if (rdLink.status() == ObjStatus.UPDATE) {
					if (rdLink.changedFields().containsKey("kind")) {
						kind = (int) rdLink.changedFields().get("kind");
					}
					if (rdLink.changedFields().containsKey("laneNum")) {
						laneNum = (int) rdLink.changedFields().get("laneNum");
					}

					if ((kind == 11 || kind == 13) && laneNum != 1) {
						checkRdLink.add(rdLink);
					}
				}
			}
		}
	}
}
