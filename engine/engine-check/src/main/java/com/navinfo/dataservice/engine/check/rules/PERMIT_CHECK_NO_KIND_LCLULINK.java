package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.search.LcLinkSearch;
import com.navinfo.dataservice.dao.glm.search.LuLinkSearch;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 土地利用线\土地覆盖线
 * @rule_desc 不能存在未分类种别的土地覆盖线,土地利用线
 * @author fhx
 * @since 2017/5/9
 */
public class PERMIT_CHECK_NO_KIND_LCLULINK extends baseRule {
	public void preCheck(CheckCommand checkCommand) {
	}

	Set<Integer> hasCheckedLink = new HashSet<Integer>();

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row instanceof LuLinkKind) {
				checkLuLinkKind((LuLinkKind) row);
			}

			if (row instanceof LcLinkKind) {
				checkLcLinkKind((LcLinkKind) row);
			}
		} // for
	}

	private void checkLcLinkKind(LcLinkKind lcLinkKind) throws Exception {
		if (hasCheckedLink.contains(lcLinkKind.getLinkPid())) {
			return;
		}

		LcLinkSearch lcLinkSearch = new LcLinkSearch(this.getConn());
		LcLink lcLink = (LcLink) lcLinkSearch.searchDataByPid(lcLinkKind.getLinkPid());

		if (lcLinkKind.getKind() == 0) {
			hasCheckedLink.add(lcLinkKind.getLinkPid());
			this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLinkKind.getLinkPid() + "]",
					((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
		}
	}

	private void checkLuLinkKind(LuLinkKind luLinkKind) throws Exception {
		if (hasCheckedLink.contains(luLinkKind.getLinkPid())) {
			return;
		}

		LuLinkSearch luLinkSearch = new LuLinkSearch(this.getConn());
		LuLink luLink = (LuLink) luLinkSearch.searchDataByPid(luLinkKind.getLinkPid());

		if (luLinkKind.getKind() == 0) {
			hasCheckedLink.add(luLinkKind.getLinkPid());
			this.setCheckResult(luLink.getGeometry(), "[LU_LINK," + luLinkKind.getLinkPid() + "]",
					((LuLinkMesh) luLink.getMeshes().get(0)).getMeshId());
		}
	}
}
