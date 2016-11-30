package com.navinfo.dataservice.dao.glm.selector.rd.crf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdRoadLinkSelector extends AbstractSelector {

	private Connection conn = null;

	/**
	 * @param cls
	 * @param conn
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public RdRoadLinkSelector(Connection conn) {
		super(conn);
		this.setCls(RdRoadLink.class);
		this.conn = conn;
	}

	public List<RdRoadLink> loadByLinks(List<Integer> linkPids, boolean isLock) throws Exception {

		List<RdRoadLink> rows = new ArrayList<RdRoadLink>();

		if (linkPids == null || linkPids.size() == 0) {
			return rows;
		}

		List<Integer> linkPidTemp = new ArrayList<Integer>();

		linkPidTemp.addAll(linkPids);

		int pointsDataLimit = 100;

		while (linkPidTemp.size() >= pointsDataLimit) {

			List<Integer> listPid = linkPidTemp.subList(0, pointsDataLimit);

			rows.addAll(loadByLinkPids(listPid, isLock));

			linkPidTemp.subList(0, pointsDataLimit).clear();
		}

		if (!linkPidTemp.isEmpty()) {
			rows.addAll(loadByLinkPids(linkPidTemp, isLock));
		}

		return rows;
	}

	/**
	 * 根据linkPids查询CRFROAD组成LINK
	 * 
	 * @param linkPids
	 * @param isLock
	 * @return CRFROAD组成LINK
	 * @throws Exception
	 */
	private List<RdRoadLink> loadByLinkPids(List<Integer> linkPids, boolean isLock) throws Exception {

		List<RdRoadLink> roadLinks = new ArrayList<RdRoadLink>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			StringBuilder sb = new StringBuilder("select * from rd_road_link where link_pid in ("
					+ StringUtils.getInteStr(linkPids) + ") and u_record !=2");
			if (isLock) {

				sb.append(" for update nowait");
			}

			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				RdRoadLink roadLink = new RdRoadLink();

				ReflectionAttrUtils.executeResultSet(roadLink, resultSet);

				roadLinks.add(roadLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return roadLinks;
	}

	/**
	 * 根据linkPid查询CRFROAD对象
	 * @param linkPid 
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public RdRoad loadRdRoadByLinkPid(int linkPid, boolean isLock) throws Exception {
		RdRoad road = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			StringBuilder sb = new StringBuilder(
					"select a.* from rd_road a,rd_road_link b where a.pid = b.pid and b.link_pid =:1 and a.U_RECORD !=2 and b.U_RECORD !=2");
			if (isLock) {

				sb.append(" for update nowait");
			}

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				road = new RdRoad();

				ReflectionAttrUtils.executeResultSet(road, resultSet);
				
				List<IRow> links = new AbstractSelector(conn).loadRowsByClassParentId(RdRoadLink.class, road.getPid(), isLock, null, null);
				
				road.setLinks(links);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return road;
	}
}
