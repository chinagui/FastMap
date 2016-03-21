package com.navinfo.dataservice.engine.edit.edit.search;

import java.sql.Connection;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;

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
