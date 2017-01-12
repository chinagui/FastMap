package com.navinfo.dataservice.dao.glm.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdRestrictionSelector extends AbstractSelector {

	private static Logger logger = Logger
			.getLogger(RdRestrictionSelector.class);

	private Connection conn;

	public RdRestrictionSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdRestriction.class);
	}

	@Override
	public IRow loadById(int id, boolean isLock, boolean... loadChild)
			throws Exception {

		RdRestriction restrict = new RdRestriction();

		String sql = "select * from " + restrict.tableName()
				+ " where pid=:1 and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));

				RdRestrictionDetailSelector detailSelector = new RdRestrictionDetailSelector(
						conn);

				List<IRow> rows = detailSelector.loadRowsByParentId(id,
						isLock);

				List<IRow> details = new ArrayList<>();
				
				String restrics = restrict.getRestricInfo();
				
				List<Integer> hasSelectedOutLinkPid = new ArrayList<>();
				
				for(String tmpRes : restrics.split(","))
				{
					for (IRow row : rows) {
						
						RdRestrictionDetail detail = (RdRestrictionDetail) row;
						
						if(!hasSelectedOutLinkPid.contains(detail.getOutLinkPid()))
						{
							if(tmpRes.contains("["))
							{
								int resInfo = Integer.parseInt(tmpRes.substring(1, 2));
								
								if(detail.getFlag() != 1 && detail.getRestricInfo() == resInfo)
								{
									details.add(detail);
									hasSelectedOutLinkPid.add(detail.getOutLinkPid());
									break;
								}
							}
							else
							{
								int resInfo = Integer.parseInt(tmpRes);
								
								if(detail.getFlag() == 1 && detail.getRestricInfo() == resInfo)
								{
									details.add(detail);
									hasSelectedOutLinkPid.add(detail.getOutLinkPid());
									break;
								}
							}
						}
					}
				}
				
				for(IRow row : rows)
				{
					RdRestrictionDetail detail = (RdRestrictionDetail) row;
					
					restrict.detailMap.put(detail.getPid(), detail);

					for (IRow row2 : detail.getConditions()) {

						RdRestrictionCondition condition = (RdRestrictionCondition) row2;

						restrict.conditionMap.put(condition.getRowId(),
								condition);
					}
				}
				restrict.setDetails(rows);
			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return restrict;
	}

	public List<RdRestriction> loadRdRestrictionByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdRestriction> reses = new ArrayList<RdRestriction>();
		String sql = "select a.* from rd_restriction a where a.in_link_pid = :1 and a.u_record!=:2 ";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				ReflectionAttrUtils.executeResultSet(restrict, resultSet);
				this.setChildData(restrict, isLock);
				reses.add(restrict);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return reses;
	}

	/**
	 * 查询退出线为该link
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdRestriction> loadRdRestrictionByOutLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdRestriction> reses = new ArrayList<RdRestriction>();

		String sql = "select a.* from rd_restriction a  where a.pid in (  select b.restric_pid from rd_restriction_detail b where b.restric_pid in (    select restric_pid from rd_restriction_detail where u_record!=2 and out_link_pid=:1 )     group by b.restric_pid) and     a.u_record != 2    ";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();
				ReflectionAttrUtils.executeResultSet(restrict, resultSet);
				this.setChildData(restrict, isLock);

				reses.add(restrict);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return reses;
	}

	public List<RdRestriction> loadRdRestrictionByNodePid(int nodePid,
			boolean isLock) throws Exception {
		List<RdRestriction> reses = new ArrayList<RdRestriction>();
		String sql = "select a.* from rd_restriction a where a.node_pid = :1 and a.u_record!=:2  ";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				ReflectionAttrUtils.executeResultSet(restrict, resultSet);
				this.setChildData(restrict, isLock);
				reses.add(restrict);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);

		}

		return reses;
	}

	public IRow loadRdRestrictionByLinkNode(int linkPid, int nodePid,
			boolean isLock) throws Exception {

		RdRestriction restrict = new RdRestriction();
		String sql = "select a.* from rd_restriction a  where a.in_link_pid = :1 and a.node_pid=:2 and a.u_record!=:3 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, 2);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				ReflectionAttrUtils.executeResultSet(restrict, resultSet);
				this.setChildData(restrict, isLock);

			} else {
				logger.info("未找到RdRestriction: linkPid " + linkPid
						+ ", nodePid " + nodePid);
				throw new DataNotFoundException("数据不存在");
			}

			if (resultSet.next()) {

				throw new Exception("存在多条交限");
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return restrict;

	}

	public List<RdRestriction> loadRdRestrictionByLinkNode(int linkPid,
			int nodePid1, int nodePid2, boolean isLock) throws Exception {

		List<RdRestriction> result = new ArrayList<RdRestriction>();

		String sql = "select a.*  from rd_restriction a,rd_link b where a.in_link_pid = :1 and a.node_pid in (:2,:3) and a.u_record!=2  ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, nodePid1);

			pstmt.setInt(3, nodePid2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestriction restrict = new RdRestriction();

				ReflectionAttrUtils.executeResultSet(restrict, resultSet);
				this.setChildData(restrict, isLock);
				result.add(restrict);

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
	 * 根据link类型获取RdRestriction
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdRestriction> loadByLink(int linkPid, int linkType,
			boolean isLock) throws Exception {

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		String sql = "";

		if (linkType == 1) {
			sql = "SELECT A.* FROM RD_RESTRICTION A WHERE A.IN_LINK_PID = :1 AND A.U_RECORD != 2 ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_RESTRICTION WHERE U_RECORD != 2  AND PID IN (SELECT DISTINCT (RESTRIC_PID)  FROM RD_RESTRICTION_DETAIL WHERE U_RECORD != 2  AND OUT_LINK_PID = :1)";
		}

		else if (linkType == 3) {

			sql = "SELECT * FROM RD_RESTRICTION WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (RESTRIC_PID) FROM RD_RESTRICTION_DETAIL WHERE U_RECORD != 2 AND DETAIL_ID IN (SELECT DISTINCT (DETAIL_ID) FROM RD_RESTRICTION_VIA WHERE U_RECORD != 2 AND LINK_PID = :1))";
		} else {
			return restrictions;
		}

		if (isLock) {
			sql += " FOR UPDATE NOWAIT";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestriction restriction = new RdRestriction();

				ReflectionAttrUtils.executeResultSet(restriction, resultSet);

				setChildData(restriction, isLock);

				restrictions.add(restriction);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return restrictions;
	}

	

	/**
	 * 根据路口pid查询路口关系的交限
	 * 
	 * @param crossPid
	 *            路口pid
	 * @param isLock
	 *            是否加锁
	 * @return 交限集合
	 * @throws Exception
	 */
	public List<RdRestriction> getRestrictionByCrossPid(int crossPid,
			boolean isLock) throws Exception {

		List<RdRestriction> result = new ArrayList<RdRestriction>();

		String sql = "select * from rd_restriction a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, crossPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdRestriction restrict = new RdRestriction();

				restrict.setPid(resultSet.getInt("pid"));

				restrict.setInLinkPid(resultSet.getInt("in_link_pid"));

				restrict.setNodePid(resultSet.getInt("node_pid"));

				restrict.setRestricInfo(resultSet.getString("restric_info"));

				restrict.setKgFlag(resultSet.getInt("kg_flag"));

				restrict.setRowId(resultSet.getString("row_id"));

				RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(
						getConn());

				restrict.setDetails(detail.loadRowsByParentId(
						restrict.getPid(), true));

				result.add(restrict);
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
	 * 根据link类型获取交限
	 * 
	 * @param linkPids
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdRestriction> loadByLinks(List<Integer> linkPids,
			int linkType, boolean isLock) throws Exception {

		List<RdRestriction> rows = new ArrayList<RdRestriction>();

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
	 * 根据link类型获取RdRestriction
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdRestriction> loadByLinkPids(List<Integer> linkPids,
			int linkType, boolean isLock) throws Exception {

		List<RdRestriction> restrictions = new ArrayList<RdRestriction>();

		if (linkPids == null || linkPids.isEmpty()) {

			return restrictions;
		}

		String pids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "";

		if (linkType == 1) {
			sql = "SELECT * FROM RD_RESTRICTION  WHERE U_RECORD !=2 AND IN_LINK_PID IN ("
					+ pids + ")  ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_RESTRICTION WHERE U_RECORD != 2  AND PID IN (SELECT DISTINCT (RESTRIC_PID)  FROM RD_RESTRICTION_DETAIL WHERE U_RECORD != 2  AND OUT_LINK_PID IN ("
					+ pids + "))";
		}

		else if (linkType == 3) {

			sql = "SELECT * FROM RD_RESTRICTION WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (RESTRIC_PID) FROM RD_RESTRICTION_DETAIL WHERE U_RECORD != 2 AND DETAIL_ID IN (SELECT DISTINCT (DETAIL_ID) FROM RD_RESTRICTION_VIA WHERE U_RECORD != 2 AND LINK_PID IN ("
					+ pids + ")))";

		} else {
			return restrictions;
		}

		if (isLock) {
			sql += " FOR UPDATE NOWAIT";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestriction restriction = new RdRestriction();

				ReflectionAttrUtils.executeResultSet(restriction, resultSet);

				setChildData(restriction, isLock);

				restrictions.add(restriction);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return restrictions;
	}
	
	
	private void setChildData(RdRestriction restrict, boolean isLock)
			throws Exception {

		RdRestrictionDetailSelector detailSelector = new RdRestrictionDetailSelector(
				conn);

		restrict.setDetails(detailSelector.loadRowsByParentId(
				restrict.getPid(), isLock));

		RdRestrictionViaSelector viaSelector = new RdRestrictionViaSelector(
				conn);

		AbstractSelector conditionSelector = new AbstractSelector(
				RdRestrictionCondition.class, conn);

		for (IRow row : restrict.getDetails()) {

			RdRestrictionDetail detail = (RdRestrictionDetail) row;

			detail.setVias(viaSelector.loadRowsByParentId(detail.getPid(),
					isLock));

			detail.setConditions(conditionSelector.loadRowsByParentId(
					detail.getPid(), isLock));

			for (IRow row2 : detail.getConditions()) {

				RdRestrictionCondition condition = (RdRestrictionCondition) row2;

				detail.conditionMap.put(condition.getRowId(), condition);
			}

			restrict.detailMap.put(detail.getPid(), detail);
		}
	}
}