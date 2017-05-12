package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 土地覆盖
 * @rule_desc 创建土地覆盖面的link种别是单线河时，不允许构成面
 * @author fhx
 * @since 2017/5/10
 */
public class check004 extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row.objType() != ObjType.LCLINKKIND) {
				continue;
			}

			LcLinkKind kind = (LcLinkKind) row;
			if (kind.getKind() != 7) {
				continue;
			}

			LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());
			List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(kind.getLinkPid(), false);
			LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());
			LcLink lcLink = (LcLink) lcLinkSelector.loadById(kind.getLinkPid(), false);

			if (lcFaces.size() > 0) {
				this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
						((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
			}
		} // for
	}
}
