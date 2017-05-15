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
 * @rule_desc 当土地覆盖的大种别面（水系、绿地）不同并且共边时，该link上应该有两个种别
 * @author fhx
 * @since 2017/5/10
 */
public class Check005 extends baseRule {
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

		if ((faceProperty(lcFaces.get(0)) == 1 && faceProperty(lcFaces.get(1)) == 2)
				|| (faceProperty(lcFaces.get(0)) == 2 && faceProperty(lcFaces.get(1)) == 1)) {
			if (isLinkHasTwoKind(lcLink) == false) {

				this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
						((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
			}
		}
	}

	/**
	 * 检查LC_FACE
	 * @param lcFace
	 * @throws Exception
	 */
	private void checkLcFace(LcFace lcFace) throws Exception {
		List<IRow> faceTopo = lcFace.getTopos();

		for (IRow row : faceTopo) {
			LcFaceTopo topo = (LcFaceTopo) row;
			LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());
			LcLink lcLink = (LcLink) lcLinkSelector.loadById(topo.getLinkPid(), false);

			LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());
			List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(topo.getLinkPid(), false);

			if (lcFaces == null || lcFaces.size() != 2) {
				continue;
			}

			if ((faceProperty(lcFaces.get(0)) == 1 && faceProperty(lcFaces.get(1)) == 2)
					|| (faceProperty(lcFaces.get(0)) == 2 && faceProperty(lcFaces.get(1)) == 1)) {
				if (isLinkHasTwoKind(lcLink) == false) {

					this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
							((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
				}
			}
		} // for
	}

	/**
	 * 判断LC_FACE的大种别的共边线是否有两个种别
	 * 
	 * @param lcFace
	 * @return 1→水系；2→绿地
	 */
	private int faceProperty(LcFace lcFace) {
		if (lcFace.getKind() > 0 && lcFace.getKind() < 7) {
			return 1;
		}

		if (lcFace.getKind() > 10 && lcFace.getKind() < 18) {
			return 2;
		}
		return 0;
	}

	/**
	 * 判断两个大种别面
	 * 
	 * @param lcLink
	 * @return
	 */
	private boolean isLinkHasTwoKind(LcLink lcLink) {
		List<IRow> linkKinds = lcLink.getKinds();
		boolean isWaterLink = false;
		boolean isGreenLink = false;

		for (IRow row : linkKinds) {
			LcLinkKind linkKind = (LcLinkKind) row;

			if (linkKind.getKind() >= 1 && linkKind.getKind() <= 8) {
				isWaterLink = true;
			}

			if (linkKind.getKind() >= 11 && linkKind.getKind() <= 18) {
				isGreenLink = true;
			}
		}

		if (isWaterLink && isGreenLink) {
			return true;
		}
		return false;
	}
}
