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

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.ClobProxyImpl;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimitTruck;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSidewalk;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkWalkstair;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import oracle.sql.STRUCT;

public class RdLinkSelector extends AbstractSelector {

	private Connection conn = null;

	public RdLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLink.class);
	}

	public List<RdLink> loadByNodePid(int nodePid, boolean isLock) throws Exception {

		List<RdLink> links = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
				RdLink rdLink = new RdLink();

				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);

				// 获取LINK对应的关联数据
				setChildData(rdLink, isLock);

				links.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	/*
	 * 仅加载主表RDLINK，其他子表若有需要，请单独加载
	 */
	public List<RdLink> loadByNodePidOnlyRdLink(int nodePid, boolean isLock) throws Exception {

		List<RdLink> links = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
				RdLink rdLink = new RdLink();

				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);

				links.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	
	
	/*
	 * 仅加载主表RDLINK，其他子表若有需要，请单独加载
	 */
	public List<RdLink> loadByNodePids(List<Integer> nodePids, boolean isLock) throws Exception 
	{
		List<RdLink> links = new ArrayList<RdLink>();
		
		if (nodePids == null || nodePids.size() == 0) {
			return links;
		}

		List<Integer> pidTemp = new ArrayList<Integer>();

		pidTemp.addAll(nodePids);

		int pointsDataLimit = 100;

		while (pidTemp.size() >= pointsDataLimit) {

			List<Integer> listPid = pidTemp.subList(0, pointsDataLimit);

			links.addAll(loadByNodePid(listPid, isLock));

			pidTemp.subList(0, pointsDataLimit).clear();
		}

		if (!pidTemp.isEmpty()) {
			links.addAll(loadByNodePid(pidTemp, isLock));
		}		
		
		return links;
	}
	/*
	 * 仅加载主表RDLINK，其他子表若有需要，请单独加载
	 */
	private List<RdLink> loadByNodePid(List<Integer> nodePids, boolean isLock) throws Exception {

		String ids = org.apache.commons.lang.StringUtils.join(nodePids, ",");
		
		List<RdLink> links = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"SELECT * FROM RD_LINK WHERE S_NODE_PID IN ( ");
		
		sb.append(ids);
		
		sb.append(" ) AND U_RECORD != 2  ");
		
		sb.append(" UNION ALL ");
		
		sb.append(" SELECT * FROM RD_LINK WHERE E_NODE_PID IN (  ");
		
		sb.append(ids);		
		
		sb.append(" ) AND U_RECORD != 2  ");
		
		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, ids);

			pstmt.setString(2, ids);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();

				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);

				links.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	/*
	 * 仅加载rdlink表，其他子表若有需要，请单独加载
	 */
	public List<RdLink> loadBySql(String sql, boolean isLock) throws Exception {

		List<RdLink> links = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(sql);

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();

				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);

				links.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	/*
	 * 通过主表id，仅加载rdlink表，提高效率
	 */
	public IRow loadByIdOnlyRdLink(int id, boolean isLock) throws Exception {

		RdLink rdLink = new RdLink();

		StringBuilder sb = new StringBuilder("select * from rd_link where link_pid = :1 and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
				return rdLink;
			} else {

				throw new Exception("对应LINK不存在!");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

	public Map<Integer, String> loadNameByLinkPids(Set<Integer> linkPids) throws Exception {

		Map<Integer, String> map = new HashMap<Integer, String>();

		if (linkPids.size() == 0) {
			return map;
		}

		StringBuilder sb = new StringBuilder(
				"select b.link_pid, a.name   from rd_name a, rd_link_name b  where a.name_groupid = b.name_groupid    and b.name_class = 1    and b.seq_num = 1  and  b.u_record != 2  and a.lang_code = 'CHI' ");

		Clob clob = null;
		boolean isClob = false;

		if (linkPids.size() > 1000) {
			isClob = true;
			clob = conn.createClob();
			clob.setString(1, StringUtils.join(linkPids, ","));
			sb.append(" and b.link_pid IN (select to_number(column_value) from table(clob_to_table(?)))");
		} else {
			sb.append(" and b.link_pid IN (" + StringUtils.join(linkPids, ",") + ")");
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

				int linkPid = resultSet.getInt("link_pid");

				String name = resultSet.getString("name");

				map.put(linkPid, name);

			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return map;
	}

	public List<RdLink> loadTrackLink(int linkPid, int nodePidDir, boolean isLock) throws Exception {

		List<RdLink> list = new ArrayList<RdLink>();
		RdLink link = (RdLink) this.loadById(linkPid, isLock);
		StringBuilder sb = new StringBuilder();
		sb.append(" select rl.* from rd_link rl ");
		if ((link.getsNodePid() == nodePidDir && link.getDirect() == 2)
				|| (link.geteNodePid() == nodePidDir && link.getDirect() == 3)
				|| (link.geteNodePid() == nodePidDir && link.getDirect() == 1)) {
			sb.append(" where ((rl.e_node_pid = :1 and rl.direct = 3) ");
			sb.append(" or (rl.s_node_pid = :2 and direct = 2)");
		}
		if ((link.geteNodePid() == nodePidDir && link.getDirect() == 2)
				|| (link.getsNodePid() == nodePidDir && link.getDirect() == 3)
				|| (link.getsNodePid() == nodePidDir && link.getDirect() == 1)) {
			sb.append(" where ((rl.s_node_pid = :1 and rl.direct = 2) ");
			sb.append(" or (rl.e_node_pid = :2 and direct = 3)");
		}
		sb.append(" or ((rl.s_node_pid = :3 or rl.e_node_pid =:4) and direct =1)");
		sb.append(") and rl.link_pid <> :5 and rl.u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePidDir);
			pstmt.setInt(2, nodePidDir);
			pstmt.setInt(3, nodePidDir);
			pstmt.setInt(4, nodePidDir);
			pstmt.setInt(5, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();
				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
				list.add(rdLink);

			}
			return list;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

	public List<RdLink> loadByPids(List<Integer> pids, boolean isLock) throws Exception {
		List<RdLink> rdLinks = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder("select * from rd_link where link_pid in ( "
				+ com.navinfo.dataservice.commons.util.StringUtils.getInteStr(pids) + ") and u_record!=2");
		sb.append(" order by instr('" + com.navinfo.dataservice.commons.util.StringUtils.getInteStr(pids)
				+ "',link_pid)");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();
				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
				// 获取LINK对应的关联数据
				setChildData(rdLink, isLock);
				rdLinks.add(rdLink);

			}
			return rdLinks;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

	// 获取上下线分离节点关节挂接的link
	public List<RdLink> loadByDepartNodePid(int nodePid, int currentLinkPid, int nextLinkPid, boolean isLock)
			throws Exception {

		List<RdLink> links = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");
		sb.append(" and link_pid <> :3 and link_pid <> :4");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);
			pstmt.setInt(2, nodePid);
			pstmt.setInt(3, currentLinkPid);
			pstmt.setInt(4, nextLinkPid);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();
				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
				// 获取LINK对应的关联数据
				setChildData(rdLink, isLock);

				links.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	public JSONArray loadGeomtryByLinkPids(List<Integer> linkPids) throws Exception {

		StringBuilder sb = new StringBuilder("select geometry,e_node_pid,s_node_pid from rd_link where link_pid in ( "
				+ com.navinfo.dataservice.commons.util.StringUtils.getInteStr(linkPids) + ") and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			List<Geometry> geos = new ArrayList<Geometry>();

			while (resultSet.next()) {
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct);

				geos.add(geometry);
			}

			if (geos.size() > 0) {
				return GeometryUtils.connectLinks(geos).getJSONArray("coordinates");
			} else {
				throw new Exception("未找到link");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

	}

	/**
	 * 查询nodePid作为link通行方向终点的link(form类型除外)
	 * 
	 * @param nodePid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdLink> loadInLinkByNodePid(int nodePid, int form, boolean isLock) throws Exception {
		List<RdLink> list = new ArrayList<RdLink>();

		String sql = "SELECT a.* FROM rd_link a  WHERE a.u_record !=2 and a.link_pid not in(select link_pid from rd_link_form b where a.link_pid = b.link_pid and b.FORM_OF_WAY = :1) and((a.s_node_pid = :2 AND a.direct = 3) OR (a.e_node_pid = :3 AND a.direct = 2) OR (a.direct = 1 AND (a.s_node_pid =:4 OR a.e_node_pid = :5)))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, form);
			pstmt.setInt(2, nodePid);
			pstmt.setInt(3, nodePid);
			pstmt.setInt(4, nodePid);
			pstmt.setInt(5, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink rdLink = new RdLink();
				ReflectionAttrUtils.executeResultSet(rdLink, resultSet);
				list.add(rdLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	/**
	 * 根据NodePid查询可以组合的link
	 * 
	 * @param nodePidsStr
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdLink> loadLinkPidByNodePids(String nodePidsStr, boolean isLock) throws Exception {
		List<RdLink> list = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder("select link_pid,IMI_CODE from rd_link where S_NODE_PID in("
					+ nodePidsStr + ") and E_NODE_PID in (" + nodePidsStr + ") and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLink link = new RdLink();

				link.setPid(resultSet.getInt("link_pid"));

				link.setImiCode(resultSet.getInt("IMI_CODE"));

				list.add(link);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return list;
	}

	private void setChildData(RdLink rdLink, boolean isLock) throws Exception {
		// 获取LINK对应的关联数据

		// rd_link_form
		List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn).loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : forms) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setForms(forms);

		// rd_link_int_rtic
		List<IRow> intRtics = new AbstractSelector(RdLinkIntRtic.class, conn).loadRowsByParentId(rdLink.getPid(),
				isLock);

		for (IRow row : intRtics) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setIntRtics(intRtics);

		// rd_link_limit
		List<IRow> limits = new AbstractSelector(RdLinkLimit.class, conn).loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : limits) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setLimits(limits);

		// rd_link_limit_truck
		List<IRow> trucks = new AbstractSelector(RdLinkLimitTruck.class, conn).loadRowsByParentId(rdLink.getPid(),
				isLock);

		for (IRow row : trucks) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setLimitTrucks(trucks);

		// rd_link_name
		List<IRow> names = new AbstractSelector(RdLinkName.class, conn).loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : names) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setNames(names);

		// rd_link_rtic
		List<IRow> rtics = new AbstractSelector(RdLinkRtic.class, conn).loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : rtics) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setRtics(rtics);

		// rd_link_sidewalk
		List<IRow> sidewalks = new AbstractSelector(RdLinkSidewalk.class, conn).loadRowsByParentId(rdLink.getPid(),
				isLock);

		for (IRow row : sidewalks) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setSidewalks(sidewalks);

		// rd_link_speedlimit
		List<IRow> speedlimits = new AbstractSelector(RdLinkSpeedlimit.class, conn).loadRowsByParentId(rdLink.getPid(),
				isLock);

		for (IRow row : speedlimits) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setSpeedlimits(speedlimits);

		// rd_link_walkstair
		List<IRow> walkstairs = new AbstractSelector(RdLinkWalkstair.class, conn).loadRowsByParentId(rdLink.getPid(),
				isLock);

		for (IRow row : walkstairs) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setWalkstairs(walkstairs);

		// rd_link_zone
		List<IRow> zones = new AbstractSelector(RdLinkZone.class, conn).loadRowsByParentId(rdLink.getPid(), isLock);

		rdLink.setZones(zones);
	}

	/*
	 * 仅加载RDLINK的pid
	 */
	public List<Integer> loadLinkPidByNodePid(int nodePid, boolean isLock) throws Exception {

		List<Integer> links = new ArrayList<Integer>();

		StringBuilder sb = new StringBuilder(
				"select link_pid from rd_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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

				int value = resultSet.getInt("link_pid");

				links.add(value);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return links;

	}

	/**
	 * 加载与面相交或者在面内的link
	 * 
	 * @param facePid
	 * @param tableName
	 *            face表名
	 * @param isLock
	 * @return 面内link集合，link只加载主表信息
	 * @throws Exception
	 */
	public List<RdLink> loadLinkByFaceGeo(int facePid, String tableName, boolean isLock) throws Exception {

		List<RdLink> rdLinks = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder("SELECT A.* FROM RD_LINK A, ");

		sb.append(tableName);

		sb.append(
				" B WHERE B.FACE_PID = :1 AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND SDO_RELATE(A.GEOMETRY, B.GEOMETRY, 'mask=anyinteract') = 'TRUE'");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, facePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLink link = new RdLink();

				ReflectionAttrUtils.executeResultSet(link, resultSet);
				
				List<IRow> zones = this.loadRowsByClassParentId(RdLinkZone.class, link.getPid(), true, null);
				
				link.setZones(zones);

				rdLinks.add(link);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return rdLinks;

	}

}
