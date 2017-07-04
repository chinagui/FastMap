package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 土地覆盖
 * @rule_desc 绿地假想线必须对应两个绿地种别的面；两个绿地种别的面的公共边必须是绿地假想线
 *            特殊说明：当岛屿与其他非岛屿（公园、绿化带、滑雪场、高尔夫、绿林林地、草地）类绿地共边时，共边线种别应该为岛屿与非岛屿类绿地的种别，
 *            否则报log
 * @author fhx
 * @since 2017/5/10
 */
public class PERMIT_CHECK_GREEN_IMAGELINK_WITH_TWOFACE extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	List<Integer> hasCheckedLink = new ArrayList<>();

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row.objType() == ObjType.LCLINKKIND) {
				checkLcLink((LcLinkKind) row);
			}

			if (row.objType() == ObjType.LCFACE) {
				checkLcFace((LcFace) row);
			}
		}
	}

	/**
	 * 检查LC_LINK
	 * 
	 * @param lcLinkKind
	 * @throws Exception
	 */
	private void checkLcLink(LcLinkKind lcLinkKind) throws Exception {
		if (hasCheckedLink.contains(lcLinkKind.getLinkPid())) {
			return;
		}
		hasCheckedLink.add(lcLinkKind.getLinkPid());

		LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());
		LcLink lcLink = (LcLink) lcLinkSelector.loadById(lcLinkKind.getLinkPid(), false);

		LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());
		List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(lcLinkKind.getLinkPid(), false);

		if (lcFaces == null || lcFaces.size() != 2) {
			return;
		}

		if (faceProperty(lcFaces.get(0), lcFaces.get(1), lcLink) == false) {
			this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
					((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
		}

	}

	/**
	 * 检查LC_FACE
	 * 
	 * @param lcFace
	 * @throws Exception
	 */
	private void checkLcFace(LcFace lcFace) throws Exception {
		List<IRow> faceTopo = lcFace.getTopos();
		LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());
		LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());

		for (IRow row : faceTopo) {
			LcFaceTopo topo = (LcFaceTopo) row;
			
			LcLink lcLink = (LcLink) lcLinkSelector.loadById(topo.getLinkPid(), false);
			List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(topo.getLinkPid(), false);

			if (lcFaces == null || lcFaces.size() != 2) {
				continue;
			}

			if (faceProperty(lcFaces.get(0), lcFaces.get(1), lcLink) == false) {
				this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
						((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
			}
		} // for
	}

	/**
	 * 判断LC_FACE的大种别的共边线是否有两个种别
	 * 
	 * @param lcFace
	 * @return 1→绿地
	 */
	private boolean faceProperty(LcFace lcFace1, LcFace lcFace2, LcLink lcLink) throws Exception {
		// 如果是“岛屿”与“其他绿地”的组合，不许face种别与link种别一致
		if ((lcFace1.getKind() == 17 && lcFace2.getKind() > 10 && lcFace2.getKind() < 18)
				|| (lcFace2.getKind() == 17 && lcFace1.getKind() > 10 && lcFace1.getKind() < 18)) {
			return isKindEqualFaceKinds(lcLink, lcFace1.getKind(), lcFace2.getKind());
		}
		// 如果是非"岛屿"与“其他绿地”的组合，link必须为绿地假想线

		if ((lcFace1.getKind() != 17 && lcFace1.getKind() > 10 && lcFace1.getKind() < 18)
				&& (lcFace2.getKind() != 17 && lcFace2.getKind() > 10 && lcFace2.getKind() < 18)) {
			return isImageKind(lcLink);
		}
		return true;
	}

	/**
	 * 判断两个大种别面的共用线是否与face的种别一致
	 * 
	 * @param lcLink
	 * @return
	 */
	private boolean isKindEqualFaceKinds(LcLink lcLink, int face1kind, int face2kind) {
		List<IRow> linkKinds = lcLink.getKinds();
		boolean isEqualFaceKind = false;

		if (lcLink.getKinds().size() != 2) {
			return isEqualFaceKind;
		}

		boolean isEqualFace1 = false;
		boolean isEqualFace2 = false;

		for (IRow row : linkKinds) {
			LcLinkKind linkKind = (LcLinkKind) row;

			if (linkKind.getKind() == face1kind) {
				isEqualFace1 = true;
			}
			if (linkKind.getKind() == face2kind) {
				isEqualFace2 = true;
			}
		}

		isEqualFaceKind = isEqualFace1 & isEqualFace2;
		return isEqualFaceKind;
	}

	/**
	 * 判断两个大种别面的共用线是否为“绿地假想线”
	 * 
	 * @param lcLink
	 * @return
	 */
	private boolean isImageKind(LcLink lcLink) {
		List<IRow> linkKinds = lcLink.getKinds();
		boolean isGreenLink = false;

		for (IRow row : linkKinds) {
			LcLinkKind linkKind = (LcLinkKind) row;
			
			if (linkKind.getKind() == 18) {
				isGreenLink = true;
			}
		}

		return isGreenLink;
	}
}
