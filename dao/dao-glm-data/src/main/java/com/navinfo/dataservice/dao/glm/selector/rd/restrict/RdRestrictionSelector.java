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
	public IRow loadById(int id, boolean isLock,boolean ... loadChild) throws Exception {

		RdRestriction restrict = new RdRestriction();

		String sql = "select * from " + restrict.tableName() + " where pid=:1 and u_record!=2";

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

				restrict.setDetails(detailSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : restrict.getDetails()){
					
					RdRestrictionDetail detail = (RdRestrictionDetail)row;
					
					restrict.detailMap.put(detail.getPid(), detail);
					
					for(IRow row2 : detail.getConditions()){
						
						RdRestrictionCondition condition = (RdRestrictionCondition)row2;
						
						restrict.conditionMap.put(condition.getRowId(), condition);
					}
				}
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

	private void setChildData(RdRestriction restrict, boolean isLock)
			throws Exception {

		RdRestrictionDetailSelector detail = new RdRestrictionDetailSelector(
				conn);

		restrict.setDetails(detail.loadRowsByParentId(restrict.getPid(), isLock));

	}

}