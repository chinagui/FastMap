package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RwLinkSelector extends AbstractSelector {

	private Connection conn;

	public RwLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RwLink.class);
	}

	
	public List<RwLink> loadByNodePid(int nodePid, boolean isLock) throws Exception {

		List<RwLink> links = new ArrayList<RwLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rw_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RwLink rwLink = new RwLink();

				ReflectionAttrUtils.executeResultSet(rwLink, resultSet);

				// 获取LINK对应的关联数据 rd_link_name
				rwLink.setNames(new AbstractSelector(RwLinkName.class,conn).loadRowsByParentId(rwLink.getPid(), isLock));

				links.add(rwLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	
}
