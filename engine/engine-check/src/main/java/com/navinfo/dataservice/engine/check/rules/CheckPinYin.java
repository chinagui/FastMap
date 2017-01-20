package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdminName;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceName;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectName;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 拼音多音字未选择检查
 * 
 * @ClassName: CheckPinYin
 * @author Zhang Xiaolong
 * @date 2017年1月19日 下午2:53:46
 * @Description: 涉及表：AD_ADMIN、LC_FACE、LU_FACE、RW_NODE、RW_LINK、RD_LINK、RD_NODE、
 *               RD_CROSS、RD_BRANCH、Rd_tollgate、Rd_crosswalk、RD_Object
 */
public class CheckPinYin extends baseRule {
	
	private static final String SPE_LETTER = "(";
	
	public CheckPinYin() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			ObjType type = obj.objType();
			switch (type) {
			case ADADMINGNAME:
				AdAdminName name = (AdAdminName) obj;
				String phonetic = name.getPhonetic();
				if (name.status() == ObjStatus.UPDATE && name.changedFields().containsKey("phonetic")) {
					phonetic = name.changedFields().get("phonetic").toString();
				}
				if (phonetic != null && phonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case LUFACENAME:
				LuFaceName luFaceName = (LuFaceName) obj;
				String luFacePhonetic = luFaceName.getPhonetic();
				if (luFaceName.status() == ObjStatus.UPDATE && luFaceName.changedFields().containsKey("phonetic")) {
					luFacePhonetic = luFaceName.changedFields().get("phonetic").toString();
				}
				if (luFacePhonetic != null && luFacePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case LCFACENAME:
				LcFaceName lcFaceName = (LcFaceName) obj;
				String lcFacePhonetic = lcFaceName.getPhonetic();
				if (lcFaceName.status() == ObjStatus.UPDATE && lcFaceName.changedFields().containsKey("phonetic")) {
					lcFacePhonetic = lcFaceName.changedFields().get("phonetic").toString();
				}
				if (lcFacePhonetic != null && lcFacePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case RDCROSSNAME:
				RdCrossName crossName = (RdCrossName) obj;
				String crossNamePhonetic = crossName.getPhonetic();
				if (crossName.status() == ObjStatus.UPDATE && crossName.changedFields().containsKey("phonetic")) {
					crossNamePhonetic = crossName.changedFields().get("phonetic").toString();
				}
				if (crossNamePhonetic != null && crossNamePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case RDBRANCHNAME:
				RdBranchName branchName = (RdBranchName) obj;
				String branchNamePhonetic = branchName.getPhonetic();
				if (branchName.status() == ObjStatus.UPDATE && branchName.changedFields().containsKey("phonetic")) {
					branchNamePhonetic = branchName.changedFields().get("phonetic").toString();
				}
				if (branchNamePhonetic != null && branchNamePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case RDSIGNBOARDNAME:
				RdSignboardName signboardName = (RdSignboardName) obj;
				String signalBoardPhonetic = signboardName.getPhonetic();
				if (signboardName.status() == ObjStatus.UPDATE && signboardName.changedFields().containsKey("phonetic")) {
					branchNamePhonetic = signboardName.changedFields().get("phonetic").toString();
				}
				if (signalBoardPhonetic != null && signalBoardPhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case RDTOLLGATENAME:
				RdTollgateName tollgateName = (RdTollgateName) obj;
				String tollgateNamePhonetic = tollgateName.getPhonetic();
				if (tollgateName.status() == ObjStatus.UPDATE && tollgateName.changedFields().containsKey("phonetic")) {
					tollgateNamePhonetic = tollgateName.changedFields().get("phonetic").toString();
				}
				if (tollgateNamePhonetic != null && tollgateNamePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			case RDOBJECTNAME:
				RdObjectName objName = (RdObjectName) obj;
				String objNamePhonetic = objName.getPhonetic();
				if (objName.status() == ObjStatus.UPDATE && objName.changedFields().containsKey("phonetic")) {
					objNamePhonetic = objName.changedFields().get("phonetic").toString();
				}
				if (objNamePhonetic != null && objNamePhonetic.contains(SPE_LETTER)) {
					this.setCheckResult("", "", 0);
					return;
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
}
