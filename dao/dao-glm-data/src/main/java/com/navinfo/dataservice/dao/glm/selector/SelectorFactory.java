package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class SelectorFactory {

	private Connection conn;
	public SelectorFactory(Connection conn) {
		// TODO Auto-generated constructor stub
	}
		
	/**
	 * 创建查询类
	 * 
	 * @param ot
	 *            对象类型
	 * @return
	 */
	public AbstractSelector createSelector(ObjType ot) {

		switch (ot) {
		case RDLINK:
			return new RdLinkSelector(conn);
		case RDBRANCH:
			return new RdBranchSelector(conn);
		default:
			return null;
		}
	}

}
