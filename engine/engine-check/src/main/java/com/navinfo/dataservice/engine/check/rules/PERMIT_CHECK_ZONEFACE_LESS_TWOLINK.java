package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.engine.check.core.baseRule;


/**
 * @category ZoneFace
 * @rule_desc ZoneFace必须要有2根及以上组成link
 * @author fhx
 * @since 2017/4/11
 */
public class PERMIT_CHECK_ZONEFACE_LESS_TWOLINK extends baseRule {
	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row.objType() != ObjType.ZONEFACE) {
				continue;
			}

			ZoneFace face = (ZoneFace) row;
			List<IRow> faceTopos = face.getFaceTopos();

			if (faceTopos.size() < 2) {
				this.setCheckResult(face.getGeometry(), "[ZONE_FACE," + face.getPid() + "]", face.getMeshId());
			}
		}
	}

}
