package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.search.LcFaceSearch;
import com.navinfo.dataservice.dao.glm.search.LcLinkSearch;
import com.navinfo.dataservice.dao.glm.search.LuFaceSearch;
import com.navinfo.dataservice.dao.glm.search.LuLinkSearch;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category:土地利用\土地覆盖
 * @rule_desc:Link的种别必须与Face的种别一致
 * @author fhx
 * @since 2017/5/9
 */
public class PERMIT_CHECK_LINKKIND_EQUAL_FACEKIND extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	// 已检查的背景线
	private List<Integer> errorLinkPid = new ArrayList<Integer>();

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row instanceof LuLinkKind) {
				checkLuLink((LuLinkKind) row);
			}

			if (row instanceof LuFace) {
				checkLuFace((LuFace) row);
			}

			if (row instanceof LcLinkKind) {
				checkLcLink((LcLinkKind) row);
			}

			if (row instanceof LcFace) {
				checkLcFace((LcFace) row);
			}
		} // for循环
	}

	/**
	 * @function LU_LINK属性编辑触发检查
	 * @param luLinkKind
	 * @throws Exception
	 */
	private void checkLuLink(LuLinkKind luLinkKind) throws Exception {
		if (errorLinkPid.contains(luLinkKind.getLinkPid())) {
			return;
		}

		errorLinkPid.add(luLinkKind.getLinkPid());

		LuLinkSearch luLinkSearch = new LuLinkSearch(this.getConn());
		LuLink luLink = (LuLink) luLinkSearch.searchDataByPid(luLinkKind.getLinkPid());
		List<IRow> linkKinds = luLink.getLinkKinds();

		LuFaceSelector luFaceTopo = new LuFaceSelector(this.getConn());
		List<LuFace> luFaces = luFaceTopo.loadLuFaceByLinkId(luLink.getPid(), false);

		for (LuFace face : luFaces) {
			if (!isLuLinkKindEqualsFaceKind(linkKinds, face.getKind())) {
				this.setCheckResult(luLink.getGeometry(),
						"[LU_FACE," + face.getPid() + ",LU_LINK," + luLinkKind.getLinkPid() + "]", face.getMeshId());
			}
		}
	}

	/**
	 * @function LC_LINK属性编辑触发检查
	 * @param luFaceIn
	 * @throws Exception
	 */
	private void checkLuFace(LuFace luFaceIn) throws Exception {
		LuFaceSearch luFaceSearch = new LuFaceSearch(this.getConn());
		LuFace luFace = (LuFace) luFaceSearch.searchDataByPid(luFaceIn.getPid());
		List<IRow> luFaceTopo = luFace.getFaceTopos();

		for (IRow topo : luFaceTopo) {
			LuFaceTopo faceTopo = (LuFaceTopo) topo;
			LuLinkSearch luLinkSearch = new LuLinkSearch(this.getConn());
			List<IRow> linkKinds = ((LuLink) luLinkSearch.searchDataByPid(faceTopo.getLinkPid())).getLinkKinds();

			if (!isLuLinkKindEqualsFaceKind(linkKinds, luFace.getKind())) {
				this.setCheckResult(luFace.getGeometry(),
						"[LU_FACE," + luFace.getPid() + ",LU_LINK," + faceTopo.getLinkPid() + "]", luFace.getMeshId());
			}
		}
	}

	/**
	 * @function LC_LINK属性编辑触发检查
	 * @param lcLinkKind
	 * @throws Exception
	 */
	private void checkLcLink(LcLinkKind lcLinkKind) throws Exception {
		if (errorLinkPid.contains(lcLinkKind.getLinkPid())) {
			return;
		}

		errorLinkPid.add(lcLinkKind.getLinkPid());

		LcLinkSearch lcLinkSearch = new LcLinkSearch(this.getConn());
		LcLink lcLink = (LcLink) lcLinkSearch.searchDataByPid(lcLinkKind.getLinkPid());
		List<IRow> linkKinds = lcLink.getKinds();

		LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());
		List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(lcLink.getPid(), false);

		for (LcFace face : lcFaces) {
			if (!isLcLinkKindEqualsFaceKind(linkKinds, face.getKind())) {
				this.setCheckResult(lcLink.getGeometry(),
						"[LC_FACE," + face.getPid() + ",LC_LINK," + lcLinkKind.getLinkPid() + "]", face.getMeshId());
			}
		}
	}

	/**
	 * @function LC_FACE属性编辑触发检查
	 * @param lcFaceIn
	 * @throws Exception
	 */
	private void checkLcFace(LcFace lcFaceIn) throws Exception {
		LcFaceSearch lcFaceSearch = new LcFaceSearch(this.getConn());
		LcFace lcFace = (LcFace) lcFaceSearch.searchDataByPid(lcFaceIn.getPid());
		List<IRow> lcFaceTopo = lcFace.getTopos();

		for (IRow topo : lcFaceTopo) {
			LcFaceTopo faceTopo = (LcFaceTopo) topo;
			LcLinkSearch lcLinkSearch = new LcLinkSearch(this.getConn());
			List<IRow> linkKinds = ((LcLink) lcLinkSearch.searchDataByPid(faceTopo.getLinkPid())).getKinds();

			if (!isLcLinkKindEqualsFaceKind(linkKinds, lcFace.getKind())) {
				this.setCheckResult(lcFace.getGeometry(),
						"[LC_FACE," + lcFace.getPid() + ",LC_LINK," + faceTopo.getLinkPid() + "]", lcFace.getMeshId());
			}
		}
	}

	/**
	 * 判断LuLink的种别是否与构成面的种别一致
	 */
	private boolean isLuLinkKindEqualsFaceKind(List<IRow> linkKinds, int faceKind) {
		for (IRow row : linkKinds) {
			LuLinkKind linkKind = (LuLinkKind) row;

			// 为假想线
			if (linkKind.getKind() == 8) {
				return true;
			}

			if (linkKind.getKind() == faceKind) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断LcLink的种别是否与构成面的种别一致
	 */
	private boolean isLcLinkKindEqualsFaceKind(List<IRow> linkKinds, int faceKind) {
		boolean isWaterLink = false;
		boolean isGreenLink = false;

		for (IRow row : linkKinds) {
			LcLinkKind linkKind = (LcLinkKind) row;

			// 一一对应
			if (linkKind.getKind() == faceKind) {
				return true;
			}

			if (linkKind.getKind() == 8) {
				isWaterLink = true;
			}

			if (linkKind.getKind() == 18) {
				isGreenLink = true;
			}
		}

		// link为水系假象线，face大种别为水系
		if (isWaterLink) {
			if (faceKind == 1 || faceKind == 2 || faceKind == 3 || faceKind == 4 || faceKind == 5 || faceKind == 6) {
				return true;
			}
		}

		// link为绿地假象线，face大种别为绿地
		if (isGreenLink) {
			if (faceKind == 11 || faceKind == 12 || faceKind == 13 || faceKind == 14 || faceKind == 15 || faceKind == 16
					|| faceKind == 17) {
				return true;
			}
		}

		return false;
	}
}
