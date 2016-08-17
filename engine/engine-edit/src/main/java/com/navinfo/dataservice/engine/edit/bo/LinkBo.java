package com.navinfo.dataservice.engine.edit.bo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.bo.ad.AdNodeBo;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class LinkBo extends AbstractBo {

	public abstract Geometry getGeometry();
	public abstract void setGeometry(Geometry geo);
	
	public abstract NodeBo getSnodeBo();
	public abstract NodeBo loadSnodeBo()throws Exception;
	public abstract void setSnodeBo(NodeBo snodeBo)throws Exception;
	
	public abstract NodeBo getEnodeBo();
	public abstract NodeBo loadEnodeBo()throws Exception;
	public abstract void setEnodeBo(NodeBo enodeBo)throws Exception;
	
	public abstract void computeMeshes()throws Exception;
	public abstract NodeBo createNewNodeBo(double x,double y)throws Exception;
	
	/**
	 * 完成了跟几何相关的所有属性和子表赋值
	 * 其他属性直接来自目标link，如有特殊修改，在每个具体Link类中实现
	 * @param point
	 * @return
	 * @throws Exception
	 */
	public LinkBreakResult breakoff(Point point) throws Exception {
		LinkBreakResult result = new LinkBreakResult();
		result.setPrimaryPid(getPo().pid());
		//判断打断点是否在形状点上还是在线段上
		log.info("判断打断点是否在形状点上还是在线段上");
		Geometry geo = GeoTranslator.transform(point, 100000, 5);
		double lon = geo.getCoordinate().x;
		double lat = geo.getCoordinate().y;
		JSONArray leftLink = new JSONArray();
		JSONArray rightLink = new JSONArray();
		boolean hasFound = false;
		JSONObject geojson = GeoTranslator.jts2Geojson(getGeometry());
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				leftLink.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			if (!hasFound) {
				// 打断点在形状点上
				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
					leftLink.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					leftLink.add(new double[] { lon, lat });
					rightLink.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					if (i > 0) {
						leftLink.add(jaPS);
					}

					leftLink.add(jaPE);
				}
			} else {
				rightLink.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				rightLink.add(jaPE);
			}

		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}
		log.info("打断点在LINK上");
		
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", leftLink);
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", rightLink);
		Geometry newLeftGeo = GeoTranslator.geojson2Jts(sGeojson,0.00001,5);
		Geometry newRightGeo = GeoTranslator.geojson2Jts(eGeojson,0.00001,5);
		//
		log.info("2 删除要打断的线信息");
		result.insertObject(getPo(), ObjStatus.DELETE, getPo().pid());
		result.setTargetLinkBo(this);

		log.debug("3 生成打断点的信息");
		NodeBo breakNodeBo = createNewNodeBo(point.getX(), point.getY());
		result.insertObject(breakNodeBo.getPo(), ObjStatus.INSERT, breakNodeBo.getPo().pid());
		result.setNewNode(breakNodeBo);
		int breakNodePid = breakNodeBo.getPo().pid();
		log.debug("3.1 打断点的pid = " + breakNodePid);

		log.debug("4 组装 第一条link 的信息");
		LinkBo leftLinkBo = (LinkBo)this.copy();
		leftLinkBo.setGeometry(GeoTranslator.transform(newLeftGeo, 100000, 0));
		leftLinkBo.setSnodeBo(this.getSnodeBo());
		leftLinkBo.setEnodeBo(breakNodeBo);
		result.insertObject(leftLinkBo.getPo(), ObjStatus.INSERT, leftLinkBo.getPo().pid());
		result.setNewLeftLink(leftLinkBo);
		log.debug("4.1 生成第一条link信息 pid = " + leftLinkBo.getPo().pid());

		log.debug("5 组装 第二条link 的信息");
		LinkBo rightLinkBo = (LinkBo)this.copy();
		rightLinkBo.setGeometry(GeoTranslator.transform(newRightGeo, 100000, 0));
		rightLinkBo.setSnodeBo(breakNodeBo);
		rightLinkBo.setEnodeBo(this.getEnodeBo());
		result.insertObject(rightLinkBo.getPo(), ObjStatus.INSERT, rightLinkBo.getPo().pid());
		result.setNewRightLink(rightLinkBo);
		log.debug("5.1 生成第二条link信息 pid = " + rightLinkBo.getPo().pid());
		
		return result;
	}
}
