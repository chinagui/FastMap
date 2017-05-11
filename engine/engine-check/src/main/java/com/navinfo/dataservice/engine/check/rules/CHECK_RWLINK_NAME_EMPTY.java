package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.search.RwLinkSearch;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 后检查：rwlink道路名称中的NAME值不能为空
 * 
 * @author Feng Haixia
 * @since 2017/4/12
 */
public class CHECK_RWLINK_NAME_EMPTY extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RwLinkName) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RwLinkName rwName = (RwLinkName) row;
			int groupId = rwName.getNameGroupid();

			if (rwName.changedFields().containsKey("nameGroupid")) {
				groupId = (int) rwName.changedFields().get("nameGroupid");
			}

			RwLinkSearch rwSearch = new RwLinkSearch(this.getConn());
			RwLink rwLink = (RwLink) rwSearch.searchDataByPid(rwName.getLinkPid());
			if (groupId == 0) {
				this.setCheckResult(rwLink.getGeometry(), "[RW_LINK," + rwLink.getPid() + "]", rwLink.getMeshId());
			}
		}//for
	}

}
