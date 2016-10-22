package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	private Connection conn;

	public OpTopo(Command command, Check check, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = "";
		this.breakPoint(result);
		return msg;
	}

	/*
	 * 土地覆盖点打断土地覆盖线 1.打断点是土地覆盖线的形状点 2.打断点不是土地覆盖线的形状点
	 * 
	 * @param result
	 * 
	 * @throws Exception
	 */
	private void breakPoint(Result result) throws Exception {
		Point point = command.getPoint();
		Geometry geo = GeoTranslator.transform(point, 100000, 5);
		double lon = geo.getCoordinate().x;
		double lat = geo.getCoordinate().y;
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		boolean hasFound = false;
		result.setPrimaryPid(command.getLinkPid());
		log.info("1 获取要打断土地覆盖线的信息 linkPid = " + command.getLinkPid());
		LcLink lcLink = (LcLink) new LcLinkSelector(conn).loadById(command.getLinkPid(), true);
		log.info("2 删除要打断的土地覆盖线信息");
		result.insertObject(lcLink, ObjStatus.DELETE, lcLink.pid());

		log.info("3 获取要打断土地覆盖线的几何属性 判断打断点是否在形状点上还是在线段上");
		JSONObject geojson = GeoTranslator.jts2Geojson(lcLink.getGeometry());
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			if (!hasFound) {
				// 打断点在形状点上
				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					if (i > 0) {
						ja1.add(jaPS);
					}

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

		this.createLinksForLcNode(lcLink, ja1, ja2, result);

	}

	/*
	 * 土地覆盖点 、线生成和关系维护。 1.生成打断点的信息 2.根据link1 和link2的几何属性生成新的一组link 3.维护link和点的关系
	 * 以及维护linkMesh的关系
	 * 
	 * @param LcLink 要打断的link sArray link1的几何属性 eArray link2的几何属性 result
	 * 
	 * @throws Exception
	 */
	private void createLinksForLcNode(LcLink lcLink, JSONArray sArray, JSONArray eArray, Result result)
			throws Exception {
		log.debug("3 生成打断点的信息");
		int breakNodePid = 0;
		if (this.command.getBreakNodePid() == 0) {
			LcNode node = NodeOperateUtils.createLcNode(command.getPoint().getX(), command.getPoint().getY());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
			breakNodePid = node.pid();
		} else {
			breakNodePid = this.command.getBreakNodePid();
		}

		log.debug("3.1 打断点的pid = " + breakNodePid);
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", sArray);
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", eArray);
		log.debug("4 组装 第一条link 的信息");
		LcLink slink = (LcLink) LcLinkOperateUtils.addLinkBySourceLink(GeoTranslator.geojson2Jts(sGeojson, 0.00001, 5),
				lcLink.getsNodePid(), breakNodePid, lcLink, result);
		command.setsLcLink(slink);
		log.debug("4.1 生成第一条link信息 pid = " + slink.getPid());
		log.debug("5 组装 第一条link 的信息");
		LcLink elink = (LcLink) LcLinkOperateUtils.addLinkBySourceLink(GeoTranslator.geojson2Jts(eGeojson, 0.00001, 5),
				breakNodePid, lcLink.geteNodePid(), lcLink, result);
		command.seteLcLink(elink);
		log.debug("5.1 生成第二条link信息 pid = " + elink.getPid());
	}

}
