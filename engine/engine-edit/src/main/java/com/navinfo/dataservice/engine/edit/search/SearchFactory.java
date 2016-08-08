package com.navinfo.dataservice.engine.edit.search;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.AdAdminSearch;
import com.navinfo.dataservice.dao.glm.search.AdFaceSearch;
import com.navinfo.dataservice.dao.glm.search.AdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.AdNodeSearch;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.search.LuFaceSearch;
import com.navinfo.dataservice.dao.glm.search.LuLinkSearch;
import com.navinfo.dataservice.dao.glm.search.LuNodeSearch;
import com.navinfo.dataservice.dao.glm.search.RdBranchSearch;
import com.navinfo.dataservice.dao.glm.search.RdCrossSearch;
import com.navinfo.dataservice.dao.glm.search.RdElectroniceyeSearch;
import com.navinfo.dataservice.dao.glm.search.RdGateSearch;
import com.navinfo.dataservice.dao.glm.search.RdGscSearch;
import com.navinfo.dataservice.dao.glm.search.RdInterSearch;
import com.navinfo.dataservice.dao.glm.search.RdLaneConnexitySearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkIntRticSearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.dao.glm.search.RdRestrictionSearch;
import com.navinfo.dataservice.dao.glm.search.RdSeSearch;
import com.navinfo.dataservice.dao.glm.search.RdSlopeSearch;
import com.navinfo.dataservice.dao.glm.search.RdSpeedbumpSearch;
import com.navinfo.dataservice.dao.glm.search.RdSpeedlimitSearch;
import com.navinfo.dataservice.dao.glm.search.RdTrafficsignalSearch;
import com.navinfo.dataservice.dao.glm.search.RdWarninginfoSearch;
import com.navinfo.dataservice.dao.glm.search.RwLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RwNodeSearch;
import com.navinfo.dataservice.dao.glm.search.ZoneFaceSearch;
import com.navinfo.dataservice.dao.glm.search.ZoneLinkSearch;
import com.navinfo.dataservice.dao.glm.search.ZoneNodeSearch;

/**
 * 查询工厂
 */
public class SearchFactory {

	private Connection conn;

	public SearchFactory(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 创建查询类
	 * 
	 * @param ot
	 *            对象类型
	 * @return
	 */
	public ISearch createSearch(ObjType ot) {

		switch (ot) {
		case RDLINK:
			return new RdLinkSearch(conn);
		case RDRESTRICTION:
			return new RdRestrictionSearch(conn);
		case RDCROSS:
			return new RdCrossSearch(conn);
		case RDNODE:
			return new RdNodeSearch(conn);
		case RDLANECONNEXITY:
			return new RdLaneConnexitySearch(conn);
		case RDSPEEDLIMIT:
			return new RdSpeedlimitSearch(conn);
		case RDBRANCH:
			return new RdBranchSearch(conn);
		case RDLINKINTRTIC:
			return new RdLinkIntRticSearch(conn);
		case RDGSC:
			return new RdGscSearch(conn);
		case ADLINK:
			return new AdLinkSearch(conn);
		case ADFACE:
			return new AdFaceSearch(conn);
		case ADNODE:
			return new AdNodeSearch(conn);
		case RWLINK:
			return new RwLinkSearch(conn);
		case RWNODE:
			return new RwNodeSearch(conn);
		case ADADMIN:
			return new AdAdminSearch(conn);
		case IXPOI:
			return new IxPoiSearch(conn);
		case ZONENODE:
			return new ZoneNodeSearch(conn);
		case ZONELINK:
			return new ZoneLinkSearch(conn);
		case ZONEFACE:
			return new ZoneFaceSearch(conn);
		case LUNODE:
			return new LuNodeSearch(conn);
		case LULINK:
			return new LuLinkSearch(conn);
		case LUFACE:
			return new LuFaceSearch(conn);
		case RDTRAFFICSIGNAL:
			return new RdTrafficsignalSearch(conn);
		case RDELECTRONICEYE:
			return new RdElectroniceyeSearch(conn);
		case RDWARNINGINFO:
			return new RdWarninginfoSearch(conn);
		case RDSLOPE:
			return new RdSlopeSearch(conn);
		case RDGATE:
			return new RdGateSearch(conn);
		case RDINTER:
			return new RdInterSearch(conn);
		case RDSE:
			return new RdSeSearch(conn);
		case RDSPEEDBUMP:
			return new RdSpeedbumpSearch(conn);
		default:
			return null;
		}
	}
}
