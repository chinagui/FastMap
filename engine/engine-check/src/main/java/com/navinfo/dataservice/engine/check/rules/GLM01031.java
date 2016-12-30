package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Rdlink html GLM01031 后台 道路link长度应大于2米（交叉点内link不查）？？？
 * 
 * @author zhangxiaoyi
 *
 */
public class GLM01031 extends baseRule {

	public void preCheck(CheckCommand checkCommand) {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				//修改link
				if (rdLink.changedFields().size() > 0) {
					if (rdLink.changedFields().containsKey("length")) {
						double length = (double) rdLink.changedFields().get("length");
						if (length <= 2 && !this.isJiaoChaLink(rdLink)) {
							this.setCheckResult("", "", 0,this.getRuleLog()+"PID:"+rdLink.getPid());
							return;
						}
					}
				} 
				//新增link
				else if (rdLink.getLength() <= 2 && !this.isJiaoChaLink(rdLink)) {
					this.setCheckResult("", "", 0,this.getRuleLog()+"PID:"+rdLink.getPid());
					return;
				}
			}
		}
	}

	/**
	 * 除起终点，Link的形状点不能在图廓上
	 * 
	 * @param geo
	 * @return 有形状点在图廓上，return true；否则false
	 */
	private boolean isJiaoChaLink(RdLink rdLink) {
		List<IRow> forms = rdLink.getForms();
		if (forms.size() == 0) {
			return false;
		}
		for (int i = 0; i < forms.size(); i++) {
			RdLinkForm form = (RdLinkForm) forms.get(i);
			if (form.getFormOfWay() == 50) {
				return true;
			}
		}
		return false;
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

}