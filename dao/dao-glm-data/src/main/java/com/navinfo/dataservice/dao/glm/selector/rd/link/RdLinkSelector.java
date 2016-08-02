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
import org.apache.log4j.Logger;

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
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import oracle.sql.STRUCT;

public class RdLinkSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdLinkSelector.class);

	private Connection conn = null;

	public RdLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLink.class);
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
				setChildData(rdLink,isLock);

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
	
	/*
	 * 仅加载主表RDLINK，其他子表若有需要，请单独加载
	 */
	public List<RdLink> loadByNodePidOnlyRdLink(int nodePid, boolean isLock)
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
	
	/*
	 * 仅加载rdlink表，其他子表若有需要，请单独加载
	 */
	public List<RdLink> loadBySql(String sql, boolean isLock)
			throws Exception {

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
	
	/*
	 * 通过主表id，仅加载rdlink表，提高效率
	 */
	public IRow loadByIdOnlyRdLink(int id, boolean isLock) throws Exception {

		RdLink rdLink = new RdLink();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where link_pid = :1 and u_record !=2");

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
		
	public List<RdLink>  loadTrackLink(int linkPid,int nodePidDir, boolean isLock) throws Exception {

		List<RdLink> list = new ArrayList<RdLink>();
		RdLink link = (RdLink)this.loadById(linkPid, isLock);
		StringBuilder sb = new StringBuilder();
		sb.append(" select rl.* from rd_link rl ");
		if((link.getsNodePid() == nodePidDir && link.getDirect() == 2)
				||(link.geteNodePid() == nodePidDir && link.getDirect() == 3)
				||(link.geteNodePid() == nodePidDir && link.getDirect() == 1)){
			sb.append(" where ((rl.e_node_pid = :1 and rl.direct = 3) ");
			sb.append(" or (rl.s_node_pid = :2 and direct = 2)");
		}
		if((link.geteNodePid() == nodePidDir && link.getDirect() == 2)
				||(link.getsNodePid() == nodePidDir && link.getDirect() == 3)
				||(link.getsNodePid() == nodePidDir && link.getDirect() == 1)){
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

			while(resultSet.next()) {
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
				list.add(rdLink);

			} 
			return list;
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
	public List<RdLink> loadByPids(List<Integer> pids, boolean isLock) throws Exception {
		List<RdLink> rdLinks  = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rd_link where link_pid in ( "+com.navinfo.dataservice.commons.util.StringUtils.getInteStr(pids)+") and u_record!=2");
		sb.append(" order by instr('"+com.navinfo.dataservice.commons.util.StringUtils.getInteStr(pids)+"',link_pid)");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

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
						.loadRowsByParentId(resultSet.getInt("link_pid"), isLock);

				for (IRow row : zones) {
					row.setMesh(rdLink.getMeshId());
				}

				rdLink.setZones(zones);

				for (IRow row : rdLink.getZones()) {
					RdLinkZone obj = (RdLinkZone) row;

					rdLink.zoneMap.put(obj.rowId(), obj);
				}
				rdLinks.add(rdLink);
				
			} return rdLinks;
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
	//获取上下线分离节点关节挂接的link
	public List<RdLink> loadByDepartNodePid(int nodePid, int currentLinkPid ,int nextLinkPid,boolean isLock)
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
				setChildData(rdLink,isLock);

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
	
	public JSONArray loadGeomtryByLinkPids(List<Integer> linkPids) throws Exception{ 
		
		StringBuilder sb = new StringBuilder(
				"select geometry,e_node_pid,s_node_pid from rd_link where link_pid in ( "+com.navinfo.dataservice.commons.util.StringUtils.getInteStr(linkPids)+") and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			List<Geometry> geos = new ArrayList<Geometry>();
			
			while(resultSet.next()) {
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct);
				
				geos.add(geometry);
			}
			
			if(geos.size()>0){
				return GeometryUtils.connectLinks(geos).getJSONArray("coordinates");
			}
			else{
				throw new Exception("未找到link");
			}
		}catch (Exception e) {

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
	
	/**
	 * 查询nodePid作为link通行方向终点的link(form类型除外)
	 * @param nodePid
	 * @param isLock
	 * @return
	 * @throws Exception 
	 */
	public List<RdLink> loadInLinkByNodePid(int nodePid,int form,boolean isLock) throws Exception 
	{
		List<RdLink> list = new ArrayList<RdLink>();
		
		String sql = "SELECT a.* FROM rd_link a left join RD_LINK_FORM b on a.LINK_PID = b.link_pid WHERE b.FORM_OF_WAY != :1 and((a.e_node_pid = :2 AND a.direct = 3) OR (a.s_node_pid = :3 AND a.direct = 2) OR (a.direct = 1 AND (a.s_node_pid =:4 OR a.e_node_pid = :5)))";
		
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
			
			while(resultSet.next()) {
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
				list.add(rdLink);
			}
		}catch (Exception e) {

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
		
		return list;
	}
	
	private void setChildData(RdLink rdLink,boolean isLock) throws Exception
	{
		// 获取LINK对应的关联数据

		// rd_link_form
		List<IRow> forms = new AbstractSelector(RdLinkForm.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : forms) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setForms(forms);

		// rd_link_int_rtic
		List<IRow> intRtics =  new AbstractSelector(RdLinkIntRtic.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : intRtics) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setIntRtics(intRtics);

		// rd_link_limit
		List<IRow> limits =  new AbstractSelector(RdLinkLimit.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : limits) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setLimits(limits);

		// rd_link_limit_truck
		List<IRow> trucks = new AbstractSelector(RdLinkLimitTruck.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : trucks) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setLimitTrucks(trucks);

		// rd_link_name
		List<IRow> names = new AbstractSelector(RdLinkName.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : names) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setNames(names);

		// rd_link_rtic
		List<IRow> rtics = new AbstractSelector(RdLinkRtic.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : rtics) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setRtics(rtics);

		// rd_link_sidewalk
		List<IRow> sidewalks = new AbstractSelector(RdLinkSidewalk.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : sidewalks) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setSidewalks(sidewalks);

		// rd_link_speedlimit
		List<IRow> speedlimits = new AbstractSelector(RdLinkSpeedlimit.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : speedlimits) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setSpeedlimits(speedlimits);

		// rd_link_walkstair
		List<IRow> walkstairs = new AbstractSelector(RdLinkWalkstair.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		for (IRow row : walkstairs) {
			row.setMesh(rdLink.getMeshId());
		}

		rdLink.setWalkstairs(walkstairs);

		// rd_link_zone
		List<IRow> zones = new AbstractSelector(RdLinkZone.class,conn)
				.loadRowsByParentId(rdLink.getPid(), isLock);

		rdLink.setZones(zones);
	}
}
