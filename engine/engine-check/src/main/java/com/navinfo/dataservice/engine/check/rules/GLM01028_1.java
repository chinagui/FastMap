package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/*
 * @ClassName：GLM01028_1
 * @author:Feng Haixia
 * @data:2017/03/24
 * @Description:10级路/步行街/人渡不能是关系型收费站的进入、退出线
 */
public class GLM01028_1 extends baseRule {

	private Set<Integer> expectedRoad = new HashSet<Integer>();

	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			GetExpectedLinkPidByChangeKind(row);
			GetExpectedLinkPidByChangeForm(row);
		}

		for (Integer linkPid : expectedRoad) {
			List<RdTollgate> rdTollgate = new RdTollgateSelector(getConn()).loadRdTollgatesWithLinkPid(linkPid, false);

			if (rdTollgate.isEmpty()) {
				continue;
			}

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid, false);
			setCheckResult(link.getGeometry(), "[RD_LINK," + linkPid + "]", link.getMeshId());
		}//遍历找到的linkPid
	}

	/*
	 * @Function:种别为10级路、人渡、步行道路的linkPid
	 * 
	 * @Param:当前更改的对象
	 */
	private void GetExpectedLinkPidByChangeKind(IRow row) {
		if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
			return;
		}

		RdLink link = (RdLink) row;

		int kind = link.getKind();
		if (link.changedFields().containsKey("kind"))
			kind = Integer.valueOf(link.changedFields().get("kind").toString());

		if (kind == 11 || kind == 10 || kind == 15) {
			expectedRoad.add(link.getPid());
		}
	}

	/*
	 * @Function:道路属性为10级路、人渡、步行道路的linkPid
	 * 
	 * @Param:当前更改的对象
	 */
	private void GetExpectedLinkPidByChangeForm(IRow row) {
		if (!(row instanceof RdLinkForm) || row.status() == ObjStatus.DELETE) {
			return;
		}

		RdLinkForm form = (RdLinkForm) row;

		int formOfWay = form.getFormOfWay();
		if (form.changedFields().containsKey("formOfWay"))
			formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

		if (formOfWay == 20) {
			expectedRoad.add(form.getLinkPid());
		}
	}
}
