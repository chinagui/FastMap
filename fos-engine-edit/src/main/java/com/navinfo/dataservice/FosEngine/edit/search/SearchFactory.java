package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;

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
			return new RdLinkSearch(this.conn);
		case RDRESTRICTION:
			return new RdRestrictionSearch(this.conn);

		default:
			return null;
		}
	}
}
