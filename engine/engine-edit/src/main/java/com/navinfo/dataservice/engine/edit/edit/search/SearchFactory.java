package com.navinfo.dataservice.engine.edit.edit.search;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.RdBranchSearch;
import com.navinfo.dataservice.dao.glm.search.RdCrossSearch;
import com.navinfo.dataservice.dao.glm.search.RdLaneConnexitySearch;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.search.RdNodeSearch;
import com.navinfo.dataservice.dao.glm.search.RdRestrictionSearch;
import com.navinfo.dataservice.dao.glm.search.RdSpeedlimitSearch;

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
	 * @param ot 对象类型
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
		default:
			return null;
		}
	}
}
