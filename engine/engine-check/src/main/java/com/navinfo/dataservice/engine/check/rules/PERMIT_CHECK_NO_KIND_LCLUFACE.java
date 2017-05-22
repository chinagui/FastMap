package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 土地利用面\土地覆盖面
 * @rule_desc 不能存在"未分类"种别的土地覆盖面,土地利用面
 * @author fhx
 * @since 2017/5/9
 */
public class PERMIT_CHECK_NO_KIND_LCLUFACE extends baseRule {
	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row instanceof LuFace) {
				checkLuFaceKind((LuFace) row);
			}

			if (row instanceof LcFace) {
				checkLcFaceKind((LcFace) row);
			}
		} // for循环
	}

	private void checkLuFaceKind(LuFace luFace) throws Exception {
		int kind = luFace.getKind();
		if (luFace.changedFields().containsKey("kind")) {
			kind = (int) luFace.changedFields().get("kind");
		}

		if (kind == 0) {
			this.setCheckResult(luFace.getGeometry(), "[LU_FACE," + luFace.getPid() + "]", luFace.getMeshId());
		}
	}

	private void checkLcFaceKind(LcFace lcFace) throws Exception {
		int kind = lcFace.getKind();
		if (lcFace.changedFields().containsKey("kind")) {
			kind = (int) lcFace.changedFields().get("kind");
		}

		if (kind == 0) {
			this.setCheckResult(lcFace.getGeometry(), "[LC_FACE," + lcFace.getPid() + "]", lcFace.getMeshId());
		}
	}
}
