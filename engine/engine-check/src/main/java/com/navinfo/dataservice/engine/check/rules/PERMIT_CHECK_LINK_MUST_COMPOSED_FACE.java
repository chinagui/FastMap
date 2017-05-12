package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.model.lc.LcLinkMesh;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.search.CmgBuildlinkSearch;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @category 土地利用\土地覆盖\行政区划\市街图\Zone
 * @rule_desc 创建的线必须构成面排除：土地覆盖中kind=7的情况
 * @author fhx
 * @since 2017/5/9
 */
public class PERMIT_CHECK_LINK_MUST_COMPOSED_FACE extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			switch (row.objType()) {
			case LULINK:
				checkLuLink((LuLink) row);
				break;
			case LCLINK:
				checkLcLink((LcLink) row);
				break;
			case ADLINK:
				checkAdLink((AdLink) row);
				break;
			case CMGBUILDLINK:
				checkCmgBuildLink((CmgBuildlink) row);
				break;
			case ZONELINK:
				checkZoneLink((ZoneLink) row);
				break;
			default:
				break;
			}
		} // for
	}

	/**
	 * 创建的土地利用线是否构成面
	 * @param luLink
	 * @throws Exception
	 */
	private void checkLuLink(LuLink luLink) throws Exception {
		LuFaceSelector luFaceTopo = new LuFaceSelector(this.getConn());
		List<LuFace> luFaces = luFaceTopo.loadLuFaceByLinkId(luLink.getPid(), false);

		if (luFaces.size() == 0) {
			this.setCheckResult(luLink.getGeometry(), "[LU_LINK," + luLink.getPid() + "]",
					((LuLinkMesh) luLink.getMeshes().get(0)).getMeshId());
		}
	}

	/**
	 * 创建的土地覆盖线是否构成面
	 * @param lcLink
	 * @throws Exception
	 */
	private void checkLcLink(LcLink lcLink) throws Exception {
		for (IRow row : lcLink.getKinds()) {
			LcLinkKind kind = (LcLinkKind) row;
			if (kind.getKind() == 7) {
				return;
			}
		}

		LcFaceSelector lcFaceTopo = new LcFaceSelector(this.getConn());
		List<LcFace> lcFaces = lcFaceTopo.loadLcFaceByLinkId(lcLink.getPid(), false);

		if (lcFaces.size() == 0) {
			this.setCheckResult(lcLink.getGeometry(), "[LC_LINK," + lcLink.getPid() + "]",
					((LcLinkMesh) lcLink.getMeshes().get(0)).getMeshId());
		}
	}

	/**
	 * 创建的行政区划线是否构成面
	 * @param adLink
	 * @throws Exception
	 */
	private void checkAdLink(AdLink adLink) throws Exception {
		AdFaceSelector adFaceSelector = new AdFaceSelector(this.getConn());
		List<AdFace> adFaces = adFaceSelector.loadAdFaceByLinkId(adLink.getPid(), false);

		if (adFaces.size() == 0) {
			this.setCheckResult(adLink.getGeometry(), "[AD_LINK," + adLink.getPid() + "]",
					((AdLinkMesh) adLink.getMeshes().get(0)).getMeshId());
		}
	}

	/**
	 * 创建的市街图线是否构成面
	 * @param cmgBuildLink
	 * @throws Exception
	 */
	private void checkCmgBuildLink(CmgBuildlink cmgBuildLink) throws Exception {
		CmgBuildfaceSelector cmgFaceSelector = new CmgBuildfaceSelector(this.getConn());
		List<CmgBuildface> cmgBuildFaces = cmgFaceSelector.listTheAssociatedFaceOfTheLink(cmgBuildLink.getPid(), false);
		CmgBuildlinkSearch linkSearch=new CmgBuildlinkSearch(this.getConn());
		CmgBuildlink cmgLink=(CmgBuildlink)linkSearch.searchDataByPid(cmgBuildLink.getPid());

		if (cmgBuildFaces.size() == 0) {
			this.setCheckResult(cmgBuildLink.getGeometry(), "[CMG_BUILDLINK," + cmgBuildLink.getPid() + "]",
					((CmgBuildlinkMesh) cmgLink.getMeshes().get(0)).getMeshId());
		}
	}

	/**
	 * 创建的Zone线是否构成面
	 * @param zoneLink
	 * @throws Exception
	 */
	private void checkZoneLink(ZoneLink zoneLink) throws Exception {
		ZoneFaceSelector zoneFaceSelector = new ZoneFaceSelector(this.getConn());
		List<ZoneFace> zoneFaces = zoneFaceSelector.loadZoneFaceByLinkId(zoneLink.getPid(), false);

		if (zoneFaces.size() == 0) {
			this.setCheckResult(zoneLink.getGeometry(), "[ZONE_LINK," + zoneLink.getPid() + "]",
					((ZoneLinkMesh) zoneLink.getMeshes().get(0)).getMeshId());
		}
	}
}
