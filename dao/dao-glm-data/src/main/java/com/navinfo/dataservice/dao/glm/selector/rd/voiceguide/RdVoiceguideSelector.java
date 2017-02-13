package com.navinfo.dataservice.dao.glm.selector.rd.voiceguide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdVoiceguideSelector extends AbstractSelector {

	public RdVoiceguideSelector(Connection conn) {

		super(RdVoiceguide.class, conn);
	}

	/**
	 * 根据link及link类型获取语音引导
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线；3经过线
	 * @return
	 */
	public List<RdVoiceguide> loadRdVoiceguideByLinkPid(int linkPid,
			int linkType, boolean isLock) throws Exception {

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		String sql = null;

		switch (linkType) {

		case 1:
			sql = "select * from rd_voiceguide where in_link_pid = :1 and u_record !=2 ";
			break;
		case 2:
			sql = "select * from rd_voiceguide  where u_record != 2 and pid in (select distinct b.voiceguide_pid from rd_voiceguide_detail b where u_record!=2 and out_link_pid=:1 )";
			break;
		case 3:
			sql = "select * from rd_voiceguide where u_record != 2 and pid in ( select distinct b.voiceguide_pid from rd_voiceguide_detail b where b.u_record!=2 and b.detail_id in (select distinct c.detail_id from rd_voiceguide_via c where c.u_record!=2 and c.link_pid=:1))";
			break;
		default:
			return voiceguides;
		}

		if (isLock) {

			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdVoiceguide voiceguide = new RdVoiceguide();

				ReflectionAttrUtils.executeResultSet(voiceguide, resultSet);

				setChildData(voiceguide, true);

				voiceguides.add(voiceguide);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return voiceguides;
	}

	/**
	 * 根据linkpid获取语音引导。link为语音引导的进入线、退出线、经过线
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdVoiceguide> loadRdVoiceguideByLinkPid(int linkPid,
			boolean isLock) throws Exception {

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		voiceguides.addAll(loadRdVoiceguideByLinkPid(linkPid, 1, isLock));

		voiceguides.addAll(loadRdVoiceguideByLinkPid(linkPid, 2, isLock));

		voiceguides.addAll(loadRdVoiceguideByLinkPid(linkPid, 3, isLock));

		return voiceguides;
	}

	/**
	 * 根据link及link类型获取语音引导的pid
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线；3经过线
	 * @return
	 */
	public Set<Integer> loadPidByLink(int linkPid, int linkType)
			throws Exception {

		Set<Integer> pids = new HashSet<Integer>();

		String sql = null;

		switch (linkType) {

		case 1:
			sql = "select a.pid from rd_voiceguide a where in_link_pid  = :1 and u_record!=2 ";
			break;
		case 2:
			sql = "select a.pid from rd_voiceguide a where u_record != 2 and pid in (select distinct b.voiceguide_pid from rd_voiceguide_detail b where u_record!=2 and out_link_pid=:1 )";
			break;
		case 3:
			sql = "select a.pid from rd_voiceguide a where u_record != 2 and pid in ( select distinct b.voiceguide_pid from rd_voiceguide_detail b where b.u_record!=2 and b.detail_id in (select distinct c.detail_id from rd_voiceguide_via c where c.u_record!=2 and c.link_pid=:1))";
			break;
		default:
			return pids;
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int pid = resultSet.getInt("pid");

				pids.add(pid);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return pids;
	}

	/**
	 * 通过link获取顺行pid。link为顺行的进入线、退出线、经过线
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<Integer> loadPidByLink(int linkPid, boolean isLock)
			throws Exception {

		Set<Integer> SetPids = new HashSet<Integer>();

		SetPids.addAll(loadPidByLink(linkPid, 1));

		SetPids.addAll(loadPidByLink(linkPid, 2));

		SetPids.addAll(loadPidByLink(linkPid, 3));

		List<Integer> pids = new ArrayList<Integer>();

		pids.addAll(SetPids);

		return pids;

	}

	/**
	 * 根据路口pid查询路口关系的顺行
	 * 
	 * @param crossPid
	 *            路口pid
	 * @param isLock
	 *            是否加锁
	 * @return 交限集合
	 * @throws Exception
	 */
	public List<RdVoiceguide> getVoiceGuideByCrossPid(int crossPid,
			boolean isLock) throws Exception {

		List<RdVoiceguide> result = new ArrayList<RdVoiceguide>();

		String sql = "select * from RD_VOICEGUIDE a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, crossPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdVoiceguide voiceguide = new RdVoiceguide();

				ReflectionAttrUtils.executeResultSet(voiceguide, resultSet);

				setChildData(voiceguide, true);

				result.add(voiceguide);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}

	/**
	 * 根据link类型获取语音引导
	 * 
	 * @param linkPids
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdVoiceguide> loadByLinks(List<Integer> linkPids, int linkType,
			boolean isLock) throws Exception {

		List<RdVoiceguide> rows = new ArrayList<RdVoiceguide>();

		if (linkPids == null || linkPids.size() == 0) {
			return rows;
		}

		List<Integer> pidTemp = new ArrayList<Integer>();

		pidTemp.addAll(linkPids);

		int dataLimit = 100;

		while (pidTemp.size() >= dataLimit) {

			List<Integer> listPid = pidTemp.subList(0, dataLimit);

			rows.addAll(loadByLinkPids(listPid, linkType, isLock));

			pidTemp.subList(0, dataLimit).clear();
		}

		if (!pidTemp.isEmpty()) {
			rows.addAll(loadByLinkPids(pidTemp, linkType, isLock));
		}

		return rows;
	}

	/**
	 * 根据link及link类型获取语音引导
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线；3经过线
	 * @return
	 */
	public List<RdVoiceguide> loadByLinkPids(List<Integer> linkPids,
			int linkType, boolean isLock) throws Exception {

		List<RdVoiceguide> voiceguides = new ArrayList<RdVoiceguide>();

		if (linkPids == null || linkPids.isEmpty()) {

			return voiceguides;
		}

		String pids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "";

		switch (linkType) {
		case 1:
			sql = "select * from rd_voiceguide WHERE U_RECORD !=2 AND IN_LINK_PID IN ("
					+ pids + ")  ";

			break;
		case 2:
			sql = "select * from rd_voiceguide  where u_record != 2 and pid in (select distinct b.voiceguide_pid from rd_voiceguide_detail b where u_record!=2 and out_link_pid IN ("
					+ pids + "))";
			break;
		case 3:
			sql = "select * from rd_voiceguide where u_record != 2 and pid in ( select distinct b.voiceguide_pid from rd_voiceguide_detail b where b.u_record!=2 and b.detail_id in (select distinct c.detail_id from rd_voiceguide_via c where c.u_record!=2 and c.link_pid IN ("
					+ pids + ")))";
			break;
		default:
			return voiceguides;
		}

		if (isLock) {

			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdVoiceguide voiceguide = new RdVoiceguide();

				ReflectionAttrUtils.executeResultSet(voiceguide, resultSet);

				setChildData(voiceguide, true);

				voiceguides.add(voiceguide);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return voiceguides;
	}

	private void setChildData(RdVoiceguide voiceguide, boolean isLock)
			throws Exception {

		// 组装RdVoiceguideDetail
		voiceguide.setDetails(new AbstractSelector(RdVoiceguideDetail.class,
				getConn()).loadRowsByParentId(voiceguide.getPid(), isLock));

		for (IRow rowDetail : voiceguide.getDetails()) {

			RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

			// 组装RdVoiceguideVia
			detail.setVias(new AbstractSelector(RdVoiceguideVia.class,
					getConn()).loadRowsByParentId(detail.getPid(), isLock));

			for (IRow viaRow : detail.getVias()) {
				RdVoiceguideVia via = (RdVoiceguideVia) viaRow;

				detail.directrouteViaMap.put(via.getRowId(), via);
			}

			voiceguide.detailMap.put(detail.getRowId(), detail);
		}

	}

}
