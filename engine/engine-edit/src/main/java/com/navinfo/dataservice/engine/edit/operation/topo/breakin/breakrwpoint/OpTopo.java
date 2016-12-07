package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.utils.BasicServiceUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RwLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 铁路打断相关操作
 * 
 * @author zhangxiaolong
 * 
 */
public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	private Check check;

	private Connection conn;

	public OpTopo(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		// 如果是连续打断
		if (this.command.getBreakNodes() != null
				&& this.command.getBreakNodes().size() > 0) {
			this.seriesBreak(result);
		}
		// 如果不是连续打断
		else {
			this.breakPoint(result);
			this.createBreakNode(result);

		}
		return "";
	}

	/**
	 * 铁路点打断铁路线 1.打断点是铁路线的形状点 2.打断点不是铁路线的形状点
	 */
	private void breakPoint(Result result) throws Exception {
		List<JSONArray> arrays = BasicServiceUtils.breakpoint(this.command
				.getBreakLink().getGeometry(), command.getPoint());

		this.create2NewLink(result, arrays);

	}

	/*
	 * 铁路点 、线生成和关系维护。 1.生成打断点的信息 2.根据link1 和link2的几何属性生成新的一组link 3.维护link和点的关系
	 * 以及维护linkMesh的关系
	 * 
	 * @param RwLink 要打断的link sArray link1的几何属性 eArray link2的几何属性 result
	 * 
	 * @throws Exception
	 */
	private void create2NewLink(Result result, List<JSONArray> arrays)
			throws Exception {
		for (JSONArray array : arrays) {
			// 组装几何
			JSONObject geojson = new JSONObject();
			geojson.put("type", "LineString");
			geojson.put("coordinates", array);
			RwLink link = new RwLink();
			// 申請pid
			link.setPid(PidUtil.getInstance().applyLinkPid());
			link.copy(this.command.getBreakLink());
			link.setGeometry(GeoTranslator.geojson2Jts(geojson));
			// 计算长度
			double length = GeometryUtils.getLinkLength(GeoTranslator
					.transform(link.getGeometry(), 0.00001, 5));
			link.setLength(length);
			this.command.getNewLinks().add(link);

		}
	}

	/*
	 * 土地覆盖点 、线生成和关系维护。 1.生成打断点的信息 2.根据link1 和link2的几何属性生成新的一组link 3.维护link和点的关系
	 * 以及维护linkMesh的关系
	 * 
	 * @throws Exception
	 */
	private void createBreakNode(Result result) throws Exception {
		log.debug("3 生成打断点的信息");
		if (this.command.getBreakNodePid() == 0) {
			RwNode node = NodeOperateUtils.createRwNode(command.getPoint()
					.getX(), command.getPoint().getY());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
			this.command.setBreakNode(node);
			command.setBreakNodePid(node.getPid());
		} else {
			for (IRow row : result.getAddObjects()) {

				if (row instanceof RwNode) {

					RwNode node = (RwNode) row;

					if (node.getPid() == command.getBreakNodePid()) {
						command.setBreakNode(node);

						break;
					}
				}
			}
		}

		// 组装新生成两条link
		result.setPrimaryPid(command.getBreakNodePid());
		this.command.getNewLinks().get(0)
				.seteNodePid(command.getBreakNodePid());
		this.command.getNewLinks().get(1)
				.setsNodePid(command.getBreakNodePid());
		for (RwLink link : this.command.getNewLinks()) {
			result.insertObject(link, ObjStatus.INSERT, link.pid());

		}
	}

	/***
	 * 连续打断功能 1.多点分割功能
	 * 
	 * @throws Exception
	 */
	private void seriesBreak(Result result) throws Exception {
		// 如果连续打断点不为空，且有值
		int sNodePid = this.command.getBreakLink().getsNodePid();// 分割线的起点
		int eNodePid = this.command.getBreakLink().geteNodePid();// 分割线的终点
		Set<Point> points = new HashSet<Point>();// 连续打断的点
		// 返回多次打断的点插入几何
		LineString line = this.getReformGeometry(points);
		// 返回连续打断的点在几何上有序的集合
		List<Point> orderPoints = GeoTranslator.getOrderPoints(line, points);
		// 返回分割时有序的参数几何
		JSONArray breakArr = BasicServiceUtils.getSplitOrderPara(orderPoints,
				this.command.getBreakNodes());
		Map<Geometry, JSONObject> map = RwLinkOperateUtils.splitRwLink(line,
				sNodePid, eNodePid, breakArr, result);
		this.createLinks(map, result);

	}

	/***
	 * 获取形状点组成完整的几何（多次打断的点插入几何）
	 * 
	 * @return
	 * @throws Exception
	 */
	private LineString getReformGeometry(Set<Point> points) throws Exception {
		Geometry geometry = GeoTranslator.transform(this.command.getBreakLink()
				.getGeometry(), 0.00001, 5);// 分割线的几何
		// 组装Point点信息 ，前端传入的Point是无序的

		for (int i = 0; i < this.command.getBreakNodes().size(); i++) {
			JSONObject obj = this.command.getBreakNodes().getJSONObject(i);
			double lon = obj.getDouble("longitude");
			double lat = obj.getDouble("latitude");
			points.add(JtsGeometryFactory.createPoint(new Coordinate(lon, lat)));
		}
		return GeoTranslator.getReformLineString((LineString) geometry, points);
	}

	/***
	 * 创建多次打断后的Adlink
	 * 
	 * @param map
	 * @param result
	 * @throws Exception
	 */
	private void createLinks(Map<Geometry, JSONObject> map, Result result)
			throws Exception {

		for (Geometry g : map.keySet()) {
			RwLink link = RwLinkOperateUtils
					.addLink(g, map.get(g).getInt("s"), map.get(g).getInt("e"),
							result, this.command.getBreakLink());
			  result.insertObject(link, ObjStatus.INSERT, link.pid());
			this.command.getNewLinks().add(link);

		}
	}

}
