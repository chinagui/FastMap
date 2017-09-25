package com.navinfo.dataservice.engine.limit.search;

import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;

import java.sql.Connection;

/**
 * 查询工厂
 */
public class SearchFactory {

	private Connection conn;

	public SearchFactory(Connection conn) {
		this.conn = conn;
	}

	private int dbId;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	/**
	 * 创建查询类
	 * 
	 * @param ot
	 *            对象类型
	 * @return
	 */
	public ISearch createSearch(LimitObjType ot) {

		switch (ot) {
			case SCPLATERESFACE:
				return new ScPlateresFaceSearch(conn);

			case SCPLATERESLINK:
				return new ScPlateresLinkSearch(conn);
			case SCPLATERESGEOMETRY:
				return new ScPlateresGeometrySearch(conn);
			default:
				return null;
		}
	}
}
