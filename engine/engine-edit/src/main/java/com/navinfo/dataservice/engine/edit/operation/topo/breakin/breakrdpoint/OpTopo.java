package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/***
 * 打断操作
 * 
 * @author zhaokk
 * 
 */
public class OpTopo implements IOperation {

	private Command command;

	private RdLink breakLink;// 被打断的link

	private JSONArray jaDisplayLink;

	private Connection conn;

	public OpTopo(Command command, Connection conn, RdLink rdLinkBreakpoint,
			JSONArray jaDisplayLink) {
		this.command = command;

		this.breakLink = rdLinkBreakpoint;

		this.jaDisplayLink = jaDisplayLink;

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
			this.breakpoint(result);
			this.createBreakNode(result);
		}

		return jaDisplayLink.toString();
	}

	/***
	 * 创建打断点 并且挂接生成新的线
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void createBreakNode(Result result) throws Exception {
		// 组装打断点的参数
		JSONObject geoPoint = new JSONObject();
		geoPoint.put("type", "Point");
		geoPoint.put("coordinates", new double[] { command.getPoint().getX(),
				command.getPoint().getY() });
		// 如果打断点pid为0.需要重新创建点
		if (command.getBreakNodePid() == 0) {
			RdNode node = NodeOperateUtils.createRdNode(command.getPoint()
					.getX(), command.getPoint().getY());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
			command.setBreakNodePid(node.getPid());
			command.setBreakNode(node);

		}// 如果是挂接点打断不需要重新创建
		else {
			for (IRow row : result.getAddObjects()) {
				if (row instanceof RdNode) {
					RdNode node = (RdNode) row;
					if (node.getPid() == command.getBreakNodePid()) {
						command.setBreakNode(node);
						break;
					}
				}
			}
		}

		// 维护交叉口类link打断(小龙移走 不要放这里维护)
		OpRefRdCross rdOpRefRdCross = new OpRefRdCross(command, conn);
		rdOpRefRdCross.run(result);
		// 组装新生成两条link
		result.setPrimaryPid(command.getBreakNodePid());
		this.command.getNewLinks().get(0)
				.seteNodePid(command.getBreakNodePid());
		this.command.getNewLinks().get(1)
				.setsNodePid(command.getBreakNodePid());
		for (RdLink link : this.command.getNewLinks()) {
			result.insertObject(link, ObjStatus.INSERT, link.pid());
			jaDisplayLink.add(link.Serialize(ObjLevel.BRIEF));
		}

	}

	/***
	 * 一个点打断link 打断判断打断的点是否在形状点上，或者是否在线段上，切割线
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void breakpoint(Result result) throws Exception {

		JSONObject geojson = GeoTranslator.jts2Geojson(breakLink.getGeometry());
		List<JSONArray> arrays = new ArrayList<JSONArray>();
		Point point = command.getPoint();
		double lon = point.getCoordinate().x * 100000;
		double lat = point.getCoordinate().y * 100000;
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		boolean hasFound = false;// 打断的点是否和形状点重合或者是否在线段上
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
			if (!hasFound) {
				// 打断点和形状点重合
				if (Math.abs(lon - jaPE.getDouble(0)) < 0.0000001
						&& Math.abs(lat - jaPE.getDouble(1)) < 0.0000001) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					ja1.add(jaPE);
				}
			} else {
				ja2.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				ja2.add(jaPE);
			}
		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}
		// 生成新的link
		arrays.add(ja1);
		arrays.add(ja2);
		this.create2NewLink(result, arrays);
	}

	/***
	 * 生成2条新的link 继承原有link属性
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void create2NewLink(Result result, List<JSONArray> arrays)
			throws Exception {
		for (JSONArray array : arrays) {
			// 组装几何
			JSONObject geojson = new JSONObject();
			geojson.put("type", "LineString");
			geojson.put("coordinates", array);
			RdLink link = new RdLink();
			// 申請pid
			link.setPid(PidUtil.getInstance().applyLinkPid());
			link.copy(breakLink);
			link.setGeometry(GeoTranslator.geojson2Jts(geojson));
			// 计算长度
			double length = GeometryUtils.getLinkLength(GeoTranslator
					.transform(link.getGeometry(), 0.00001, 5));
			link.setLength(length);
			this.command.getNewLinks().add(link);

		}
	}

	/***
	 * 连续打断功能 1.多点分割功能
	 * 
	 * @throws Exception
	 */
	private void seriesBreak(Result result) throws Exception {
		// 如果连续打断点不为空，且有值

		int sNodePid = this.breakLink.getsNodePid();// 分割线的起点
		int eNodePid = this.breakLink.geteNodePid();// 分割线的终点
		Geometry geometry = GeoTranslator.transform(
				this.breakLink.getGeometry(), 0.00001, 5);// 分割线的几何
		Map<Geometry, JSONObject> map = RdLinkOperateUtils.splitRdLink(
				geometry, sNodePid, eNodePid, this.command.getBreakNodes(),
				result);
		this.createLinks(map, result);

	}

	/***
	 * 创建多次打断后的rdlink
	 * 
	 * @param map
	 * @param result
	 * @throws Exception
	 */
	private void createLinks(Map<Geometry, JSONObject> map, Result result)
			throws Exception {

		for (Geometry g : map.keySet()) {
			RdLink link = RdLinkOperateUtils.addLink(g, map.get(g).getInt("s"),
					map.get(g).getInt("e"), result, this.breakLink);
			result.insertObject(link, ObjStatus.INSERT, link.getPid());
			this.command.getNewLinks().add(link);
			jaDisplayLink.add(link.Serialize(ObjLevel.BRIEF));
		}
	}
}