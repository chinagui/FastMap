package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildingSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 市街图
 * @rule_desc 新添加到市街图要素的Face已构成其它Feature，报出Log
 * @author fhx
 * @since 2017/5/18
 */
public class PERMIT_CHECK_NO_REPEAT_FEATURE extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.objType() != ObjType.CMGBUILDFACE) {
				continue;
			}

			CmgBuildface face = (CmgBuildface) row;
			int buildingPid = face.getBuildingPid();
			int buildingPidNew = 0;
			if (face.changedFields().containsKey("buildingPid")) {
				buildingPidNew = (int) face.changedFields().get("buildingPid");
			}

			if (buildingPid == 0 && buildingPidNew != 0) {
				continue;
			}
			this.setCheckResult("", "", 0);
		}
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
}
