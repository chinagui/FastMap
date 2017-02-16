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
 * @ClassName: GLM02085_2
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 特殊交通为“是”、是否高架为“是”或者IMI代码为1（交叉点内部道路）,2（转弯道）,3（无法描述的）时，不应该有门牌号码
 */
public class GLM02085_2 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM02085_2.class);

	private List<RdLink> rdLinkList = new ArrayList<>();

	public GLM02085_2() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		List<Integer> linkPids = new ArrayList<>();
		for (RdLink rdLink : rdLinkList) {
			if (linkPids.contains(rdLink.pid()))
				continue;
			logger.debug("检查类型：postCheck， 检查规则：GLM02085_2， 检查要素：RDLINK(" + rdLink.pid() + ")");

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
				int imiCode = rdLink.getImiCode();
				int specialTraffic = rdLink.getSpecialTraffic();
				int isViaduct = rdLink.getIsViaduct();
				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("imiCode")) {
						imiCode = (int) rdLink.changedFields().get("imiCode");
					}
					if (rdLink.changedFields().containsKey("specialTraffic")) {
						specialTraffic = (int) rdLink.changedFields().get("specialTraffic");
					}
					if (rdLink.changedFields().containsKey("isViaduct")) {
						isViaduct = (int) rdLink.changedFields().get("isViaduct");
					}
				}
				if (imiCode != 0 || (isViaduct == 1 && specialTraffic == 1)) {
					rdLinkList.add(rdLink);
				}
			}
		}
	}

}
