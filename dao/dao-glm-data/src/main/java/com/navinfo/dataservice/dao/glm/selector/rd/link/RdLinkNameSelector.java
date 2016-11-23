package com.navinfo.dataservice.dao.glm.selector.rd.link;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.ClobProxyImpl;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdLinkNameSelector extends AbstractSelector {

	private Connection conn = null;

	public RdLinkNameSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLinkName.class);
	}

	/**
	 * 获取中文名称
	 * 
	 * @param linkPids
	 * @return Map<Integer：linkPid, List<RdLinkName>：link的名称组>
	 * @throws Exception
	 */
	public Map<Integer, List<RdLinkName>> loadNameByLinkPids(
			Set<Integer> linkPids) throws Exception {

		Map<Integer, List<RdLinkName>> map = new HashMap<Integer, List<RdLinkName>>();

		if (linkPids.size() == 0) {

			return map;
		}
		
		for(int linkPid: linkPids)
		{
			List<RdLinkName> lstName = new ArrayList<RdLinkName>();

			map.put(linkPid, lstName);
		}

		StringBuilder sb = new StringBuilder(
				"SELECT B.*, A.NAME FROM RD_NAME A, RD_LINK_NAME B WHERE A.NAME_GROUPID = B.NAME_GROUPID AND B.U_RECORD != 2 AND (A.LANG_CODE = 'CHI'  OR A.LANG_CODE = 'CHT' ) ");

		Clob clob = null;

		boolean isClob = false;

		if (linkPids.size() > 1000) {

			isClob = true;

			clob = conn.createClob();

			clob.setString(1, StringUtils.join(linkPids, ","));

			sb.append(" and b.link_pid IN (select to_number(column_value) from table(clob_to_table(?)))");

		} else {

			sb.append(" and b.link_pid IN (" + StringUtils.join(linkPids, ",")
					+ ")");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			if (isClob) {

				if (conn instanceof DruidPooledConnection) {
					ClobProxyImpl impl = (ClobProxyImpl) clob;
					pstmt.setClob(1, impl.getRawClob());
				} else {
					pstmt.setClob(1, clob);
				}
			}

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLinkName linkName = new RdLinkName();

				ReflectionAttrUtils.executeResultSet(linkName, resultSet);

				String name = resultSet.getString("name");

				linkName.setName(name);

				int linkPid = linkName.getLinkPid();

				if (map.containsKey(linkPid)) {

					map.get(linkPid).add(linkName);
				}				
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return map;
	}

}
