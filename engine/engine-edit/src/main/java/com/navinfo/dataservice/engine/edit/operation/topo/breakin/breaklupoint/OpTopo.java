package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.LuLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
		String msg = "";
		this.breakPoint(result);
		return msg;
	}

	/*
	 * 土地利用点打断土地利用线
	 * 
	 * @param result
	 * 
	 * @throws Exception
	 */
	private void breakPoint(Result result) throws Exception {
		// 打断点的信息
		Point point = command.getPoint();
		Geometry geo = GeoTranslator.transform(point, 100000, 5);
		double lon = geo.getCoordinate().x;
		double lat = geo.getCoordinate().y;

		// 打断后线段两条线段的几何属性
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();

		boolean hasFound = false;

		result.setPrimaryPid(command.getLinkPid());
		// 获取需要打断的土地利用线的信息 并将该线删除
		LuLink luLink = (LuLink) new LuLinkSelector(conn).loadById(
				command.getLinkPid(), true);
		result.insertObject(luLink, ObjStatus.DELETE, luLink.pid());

		// 获取打断前线段的几何属性
		JSONObject geojson = GeoTranslator.jts2Geojson(luLink.getGeometry());
		// 获取形状点坐标
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
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
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

		this.createLinksForLUNode(luLink, ja1, ja2, result);

	}

	/**
	 * 土地利用点 、线生成和关系维护:</br> 1.生成打断点的信息 </br> 2.根据link1
	 * 和link2的几何属性生成新的一组link</br> 3.维护link和点的关系 以及维护linkMesh的关系
	 * 
	 * @param luLink
	 *            要打断的link
	 * @param sArray
	 *            link1的几何属性
	 * @param eArray
	 *            link2的几何属性
	 * @param result
	 * 
	 * @throws Exception
	 */
	private void createLinksForLUNode(LuLink luLink, JSONArray sArray,
			JSONArray eArray, Result result) throws Exception {
		int breakNodePid = 0;
		if (this.command.getBreakNodePid() == 0) {
			LuNode node = NodeOperateUtils.createLuNode(command.getPoint()
					.getX(), command.getPoint().getY());
			result.insertObject(node, ObjStatus.INSERT, node.pid());
			breakNodePid = node.pid();
			this.command.setBreakNode(node);
		}else
		{
			breakNodePid = this.command.getBreakNodePid();
			for (IRow row : result.getAddObjects()) {
				
				if (row instanceof LuNode) {

					LuNode node = (LuNode) row;
					
					if(node.getPid()==command.getBreakNodePid())
					{
						command.setBreakNode(node);
						
						break;
					}
				}
			}
		}

		log.debug("3.1 打断点的pid = " + breakNodePid);

		// 生成打断后的第一条土地利用线
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", sArray);
		LuLink slink = (LuLink) LuLinkOperateUtils.addLinkBySourceLink(
				GeoTranslator.geojson2Jts(sGeojson, 0.00001, 5),
				luLink.getsNodePid(), breakNodePid, luLink, result);
		command.setsLuLink(slink);

		// 生成打断后的第二条土地利用线
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", eArray);
		LuLink elink = (LuLink) LuLinkOperateUtils.addLinkBySourceLink(
				GeoTranslator.geojson2Jts(eGeojson, 0.00001, 5), breakNodePid,
				luLink.geteNodePid(), luLink, result);
		command.seteLuLink(elink);

		updataRelationObj(luLink, result);
	}

	private void updataRelationObj(LuLink breakLink, Result result)
			throws Exception {
		if (!this.command.getOperationType().equals("innerRun")) {
			OpRefRelationObj opRefRelationObj = new OpRefRelationObj(this.conn);

			// 处理同一线
			opRefRelationObj.handleSameLink(breakLink, this.command, result);
		}
	}
}
