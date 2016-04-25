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

import oracle.sql.STRUCT;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.ClobProxyImpl;
import com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
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
import com.vividsolutions.jts.geom.Geometry;

public class RdLinkSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdLinkSelector.class);

	private Connection conn = null;

	public RdLinkSelector() {

	}

	public RdLinkSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdLink rdLink = new RdLink();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where link_pid = :1 ");

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
				rdLink.setPid(id);

				rdLink.setDirect(resultSet.getInt("direct"));

				rdLink.seteNodePid(resultSet.getInt("e_node_pid"));

				rdLink.setFunctionClass(resultSet.getInt("function_class"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				rdLink.setGeometry(geometry);

				rdLink.setKind(resultSet.getInt("kind"));

				rdLink.setLaneLeft(resultSet.getInt("lane_left"));

				rdLink.setLaneNum(resultSet.getInt("lane_num"));

				rdLink.setLaneRight(resultSet.getInt("lane_right"));

				rdLink.setMultiDigitized(resultSet.getInt("multi_digitized"));

				rdLink.setsNodePid(resultSet.getInt("s_node_pid"));

				rdLink.setRowId(resultSet.getString("row_id"));

				rdLink.setAppInfo(resultSet.getInt("app_info"));

				rdLink.setTollInfo(resultSet.getInt("toll_info"));

				rdLink.setRouteAdopt(resultSet.getInt("route_adopt"));

				rdLink.setDevelopState(resultSet.getInt("develop_state"));

				rdLink.setImiCode(resultSet.getInt("imi_code"));

				rdLink.setSpecialTraffic(resultSet.getInt("special_traffic"));

				rdLink.setUrban(resultSet.getInt("urban"));

				rdLink.setPaveStatus(resultSet.getInt("pave_status"));

				rdLink.setLaneWidthLeft(resultSet.getInt("lane_width_left"));

				rdLink.setLaneWidthRight(resultSet.getInt("lane_width_right"));

				rdLink.setLaneClass(resultSet.getInt("lane_class"));

				rdLink.setWidth(resultSet.getInt("width"));

				rdLink.setIsViaduct(resultSet.getInt("is_viaduct"));

				rdLink.setLeftRegionId(resultSet.getInt("left_region_id"));

				rdLink.setRightRegionId(resultSet.getInt("right_region_id"));

				rdLink.setLength(resultSet.getDouble("length"));

				rdLink.setMeshId(resultSet.getInt("mesh_id"));

				rdLink.setOnewayMark(resultSet.getInt("oneway_mark"));

				rdLink.setStreetLight(resultSet.getInt("street_light"));

				rdLink.setParkingLot(resultSet.getInt("parking_lot"));

				rdLink.setAdasFlag(resultSet.getInt("adas_flag"));

				rdLink.setSidewalkFlag(resultSet.getInt("sidewalk_flag"));

				rdLink.setWalkstairFlag(resultSet.getInt("walkstair_flag"));

				rdLink.setDiciType(resultSet.getInt("dici_type"));

				rdLink.setWalkFlag(resultSet.getInt("walk_flag"));

				rdLink.setDifGroupid(resultSet.getString("dif_groupid"));

				rdLink.setSrcFlag(resultSet.getInt("src_flag"));

				rdLink.setDigitalLevel(resultSet.getInt("digital_level"));

				rdLink.setEditFlag(resultSet.getInt("edit_flag"));

				rdLink.setTruckFlag(resultSet.getInt("truck_flag"));

				rdLink.setOriginLinkPid(resultSet.getInt("origin_link_pid"));

				rdLink.setCenterDivider(resultSet.getInt("center_divider"));

				rdLink.setParkingFlag(resultSet.getInt("parking_flag"));

				rdLink.setMemo(resultSet.getString("memo"));

				// 获取LINK对应的关联数据

				// rd_link_form
				List<IRow> forms = new RdLinkFormSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : forms) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setForms(forms);

				for (IRow row : rdLink.getForms()) {
					RdLinkForm form = (RdLinkForm) row;

					rdLink.formMap.put(form.rowId(), form);
				}

				// rd_link_int_rtic
				List<IRow> intRtics = new RdLinkIntRticSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : intRtics) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setIntRtics(intRtics);

				for (IRow row : rdLink.getIntRtics()) {
					RdLinkIntRtic obj = (RdLinkIntRtic) row;

					rdLink.intRticMap.put(obj.rowId(), obj);
				}

				// rd_link_limit
				List<IRow> limits = new RdLinkLimitSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : limits) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setLimits(limits);

				for (IRow row : rdLink.getLimits()) {
					RdLinkLimit limit = (RdLinkLimit) row;

					rdLink.limitMap.put(limit.rowId(), limit);
				}

				// rd_link_limit_truck
				List<IRow> trucks = new RdLinkLimitTruckSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : trucks) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setLimitTrucks(trucks);

				for (IRow row : rdLink.getLimitTrucks()) {
					RdLinkLimitTruck obj = (RdLinkLimitTruck) row;

					rdLink.limitTruckMap.put(obj.rowId(), obj);
				}

				// rd_link_name
				List<IRow> names = new RdLinkNameSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : names) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setNames(names);

				for (IRow row : rdLink.getNames()) {
					RdLinkName name = (RdLinkName) row;

					rdLink.nameMap.put(name.rowId(), name);
				}

				// rd_link_rtic
				List<IRow> rtics = new RdLinkRticSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : rtics) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setRtics(rtics);

				for (IRow row : rdLink.getRtics()) {
					RdLinkRtic obj = (RdLinkRtic) row;

					rdLink.rticMap.put(obj.rowId(), obj);
				}

				// rd_link_sidewalk
				List<IRow> sidewalks = new RdLinkSidewalkSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : sidewalks) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setSidewalks(sidewalks);

				for (IRow row : rdLink.getSidewalks()) {
					RdLinkSidewalk obj = (RdLinkSidewalk) row;

					rdLink.sidewalkMap.put(obj.rowId(), obj);
				}

				// rd_link_speedlimit
				List<IRow> speedlimits = new RdLinkSpeedlimitSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : speedlimits) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setSpeedlimits(speedlimits);

				for (IRow row : rdLink.getSpeedlimits()) {
					RdLinkSpeedlimit obj = (RdLinkSpeedlimit) row;

					rdLink.speedlimitMap.put(obj.rowId(), obj);
				}

				// rd_link_walkstair
				List<IRow> walkstairs = new RdLinkWalkstairSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : walkstairs) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setWalkstairs(walkstairs);

				for (IRow row : rdLink.getWalkstairs()) {
					RdLinkWalkstair obj = (RdLinkWalkstair) row;

					rdLink.walkstairMap.put(obj.rowId(), obj);
				}

				// rd_link_zone
				List<IRow> zones = new RdLinkZoneSelector(conn)
						.loadRowsByParentId(id, isLock);

				for (IRow row : zones) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setZones(zones);

				for (IRow row : rdLink.getZones()) {
					RdLinkZone obj = (RdLinkZone) row;

					rdLink.zoneMap.put(obj.rowId(), obj);
				}

				return rdLink;
			} else {

				throw new Exception("对应LINK不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}

	public List<RdLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

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

				rdLink.setPid(resultSet.getInt("link_pid"));

				rdLink.setDirect(resultSet.getInt("direct"));

				rdLink.seteNodePid(resultSet.getInt("e_node_pid"));

				rdLink.setFunctionClass(resultSet.getInt("function_class"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				rdLink.setGeometry(geometry);

				rdLink.setKind(resultSet.getInt("kind"));

				rdLink.setLaneLeft(resultSet.getInt("lane_left"));

				rdLink.setLaneNum(resultSet.getInt("lane_num"));

				rdLink.setLaneRight(resultSet.getInt("lane_right"));

				rdLink.setMultiDigitized(resultSet.getInt("multi_digitized"));

				rdLink.setsNodePid(resultSet.getInt("s_node_pid"));

				rdLink.setRowId(resultSet.getString("row_id"));

				rdLink.setAppInfo(resultSet.getInt("app_info"));

				rdLink.setTollInfo(resultSet.getInt("toll_info"));

				rdLink.setRouteAdopt(resultSet.getInt("route_adopt"));

				rdLink.setDevelopState(resultSet.getInt("develop_state"));

				rdLink.setImiCode(resultSet.getInt("imi_code"));

				rdLink.setSpecialTraffic(resultSet.getInt("special_traffic"));

				rdLink.setUrban(resultSet.getInt("urban"));

				rdLink.setPaveStatus(resultSet.getInt("pave_status"));

				rdLink.setLaneWidthLeft(resultSet.getInt("lane_width_left"));

				rdLink.setLaneWidthRight(resultSet.getInt("lane_width_right"));

				rdLink.setLaneClass(resultSet.getInt("lane_class"));

				rdLink.setWidth(resultSet.getInt("width"));

				rdLink.setIsViaduct(resultSet.getInt("is_viaduct"));

				rdLink.setLeftRegionId(resultSet.getInt("left_region_id"));

				rdLink.setRightRegionId(resultSet.getInt("right_region_id"));

				rdLink.setLength(resultSet.getDouble("length"));

				rdLink.setMeshId(resultSet.getInt("mesh_id"));

				rdLink.setOnewayMark(resultSet.getInt("oneway_mark"));

				rdLink.setStreetLight(resultSet.getInt("street_light"));

				rdLink.setParkingLot(resultSet.getInt("parking_lot"));

				rdLink.setAdasFlag(resultSet.getInt("adas_flag"));

				rdLink.setSidewalkFlag(resultSet.getInt("sidewalk_flag"));

				rdLink.setWalkstairFlag(resultSet.getInt("walkstair_flag"));

				rdLink.setDiciType(resultSet.getInt("dici_type"));

				rdLink.setWalkFlag(resultSet.getInt("walk_flag"));

				rdLink.setDifGroupid(resultSet.getString("dif_groupid"));

				rdLink.setSrcFlag(resultSet.getInt("src_flag"));

				rdLink.setDigitalLevel(resultSet.getInt("digital_level"));

				rdLink.setEditFlag(resultSet.getInt("edit_flag"));

				rdLink.setTruckFlag(resultSet.getInt("truck_flag"));

				rdLink.setOriginLinkPid(resultSet.getInt("origin_link_pid"));

				rdLink.setCenterDivider(resultSet.getInt("center_divider"));

				rdLink.setParkingFlag(resultSet.getInt("parking_flag"));

				rdLink.setMemo(resultSet.getString("memo"));

				// 获取LINK对应的关联数据

				// rd_link_form
				List<IRow> forms = new RdLinkFormSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : forms) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setForms(forms);

				// rd_link_int_rtic
				List<IRow> intRtics = new RdLinkIntRticSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : intRtics) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setIntRtics(intRtics);

				// rd_link_limit
				List<IRow> limits = new RdLinkLimitSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : limits) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setLimits(limits);

				// rd_link_limit_truck
				List<IRow> trucks = new RdLinkLimitTruckSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : trucks) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setLimitTrucks(trucks);

				// rd_link_name
				List<IRow> names = new RdLinkNameSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : names) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setNames(names);

				// rd_link_rtic
				List<IRow> rtics = new RdLinkRticSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : rtics) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setRtics(rtics);

				// rd_link_sidewalk
				List<IRow> sidewalks = new RdLinkSidewalkSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : sidewalks) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setSidewalks(sidewalks);

				// rd_link_speedlimit
				List<IRow> speedlimits = new RdLinkSpeedlimitSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : speedlimits) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setSpeedlimits(speedlimits);

				// rd_link_walkstair
				List<IRow> walkstairs = new RdLinkWalkstairSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				for (IRow row : walkstairs) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setWalkstairs(walkstairs);

				// rd_link_zone
				List<IRow> zones = new RdLinkZoneSelector(conn)
						.loadRowsByParentId(rdLink.getPid(), isLock);

				rdLink.setZones(zones);

				links.add(rdLink);
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

		return links;

	}

	public Map<Integer, String> loadNameByLinkPids(Set<Integer> linkPids) throws Exception{

		Map<Integer, String> map = new HashMap<Integer, String>();
		
		if(linkPids.size()==0)
		{
			return map;
		}
		
		StringBuilder sb = new StringBuilder(
				"select b.link_pid, a.name   from rd_name a, rd_link_name b  where a.name_groupid = b.name_groupid    and b.name_class = 1    and b.seq_num = 1  and  b.u_record != 2  and a.lang_code = 'CHI' ");
		
		Clob clob = null;
		boolean isClob=false;
		
		if(linkPids.size()>1000){
			isClob=true;
			clob=conn.createClob();
			clob.setString(1, StringUtils.join(linkPids, ","));
			sb.append(" and b.link_pid IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(" and b.link_pid IN ("+StringUtils.join(linkPids, ",")+")");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			if(isClob){
				
				if(conn instanceof DruidPooledConnection){
					ClobProxyImpl impl = (ClobProxyImpl) clob;
					pstmt.setClob(1, impl.getRawClob());
				}
				else{
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
	
		return map;
	}
}
