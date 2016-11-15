package com.navinfo.dataservice.engine.editplus.bo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;




import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.editplus.model.AbstractLink;
//import com.navinfo.dataservice.engine.editplus.operation.LinkBreakResult;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 实现了通用Link有的属性，geometry，length，s_node_pid,e_node_pid
 * @ClassName: LinkBo
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: LinkBo.java
 */
//public abstract class AbstractLinkObj extends AbstractBo {

//	public Geometry getGeometry(){
//		return ((AbstractLink)getObj()).getGeometry();
//	}
//	public void setGeometry(Geometry geo){
//		if(getObj().checkValue("GEOMETRY", getGeometry(), geo)){
//			((AbstractLink)getObj()).setGeometry(geo);
//		}
//	}
//	public long getsNodePid() {
//		return ((AbstractLink)getObj()).getsNodePid();
//	}
//	public void setsNodePid(long sNodePid) {
//		if(getObj().checkValue("S_NODE_PID", getsNodePid(), sNodePid)){
//			((AbstractLink)getObj()).setsNodePid(sNodePid);
//		}
//	}
//	public long geteNodePid() {
//		return ((AbstractLink)getObj()).geteNodePid();
//	}
//	public void seteNodePid(long eNodePid) {
//		if(getObj().checkValue("E_NODE_PID", geteNodePid(), eNodePid)){
//			((AbstractLink)getObj()).seteNodePid(eNodePid);
//		}
//	}
//
//	public double getLength() {
//		return ((AbstractLink)getObj()).getLength();
//	}
//	public void setLength(double length) {
//		if(getObj().checkValue("LENGTH", getLength(), length)){
//			((AbstractLink)getObj()).setLength(length);
//		}
//	}
//	
//	public abstract AbstractNodeBo getSnodeBo();
//	public abstract void setSnodeBo(AbstractNodeBo snodeBo)throws Exception;
//	
//	public abstract AbstractNodeBo getEnodeBo();
//	public abstract void setEnodeBo(AbstractNodeBo enodeBo)throws Exception;
//	
//	public abstract void computeMeshes()throws Exception;
//	public abstract AbstractNodeBo createNewNodeBo(double x,double y)throws Exception;
//	
//	/**
//	 * 完成了跟几何相关的所有属性和子表赋值
//	 * 其他属性直接来自目标link，如有特殊修改，在每个具体Link类中实现
//	 * @param point
//	 * @return
//	 * @throws Exception
//	 */
//	public LinkBreakResult breakoff(Point point) throws Exception {
//		LinkBreakResult result = new LinkBreakResult();
//		//判断打断点是否在形状点上还是在线段上
//		log.info("判断打断点是否在形状点上还是在线段上");
//		Geometry geo = GeoTranslator.transform(point, 100000, 5);
//		double lon = geo.getCoordinate().x;
//		double lat = geo.getCoordinate().y;
//		JSONArray leftLink = new JSONArray();
//		JSONArray rightLink = new JSONArray();
//		boolean hasFound = false;
//		JSONObject geojson = GeoTranslator.jts2Geojson(getGeometry());
//		JSONArray jaLink = geojson.getJSONArray("coordinates");
//		for (int i = 0; i < jaLink.size() - 1; i++) {
//			JSONArray jaPS = jaLink.getJSONArray(i);
//			if (i == 0) {
//				leftLink.add(jaPS);
//			}
//			JSONArray jaPE = jaLink.getJSONArray(i + 1);
//
//			if (!hasFound) {
//				// 打断点在形状点上
//				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
//					leftLink.add(new double[] { lon, lat });
//					hasFound = true;
//				}
//				// 打断点在线段上
//				else if (GeoTranslator.isIntersection(
//						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
//						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
//						new double[] { lon, lat })) {
//					leftLink.add(new double[] { lon, lat });
//					rightLink.add(new double[] { lon, lat });
//					hasFound = true;
//				} else {
//					if (i > 0) {
//						leftLink.add(jaPS);
//					}
//
//					leftLink.add(jaPE);
//				}
//			} else {
//				rightLink.add(jaPS);
//			}
//			if (i == jaLink.size() - 2) {
//				rightLink.add(jaPE);
//			}
//
//		}
//		if (!hasFound) {
//			throw new Exception("打断的点不在打断LINK上");
//		}
//		log.info("打断点在LINK上");
//		
//		JSONObject sGeojson = new JSONObject();
//		sGeojson.put("type", "LineString");
//		sGeojson.put("coordinates", leftLink);
//		JSONObject eGeojson = new JSONObject();
//		eGeojson.put("type", "LineString");
//		eGeojson.put("coordinates", rightLink);
//		Geometry newLeftGeo = GeoTranslator.geojson2Jts(sGeojson,0.00001,5);
//		Geometry newRightGeo = GeoTranslator.geojson2Jts(eGeojson,0.00001,5);
//		//
//		log.info("2 删除要打断的线信息");
//		getObj().setOpType(OperationType.DELETE);
//		result.putObj(getObj());
//		result.setTargetLinkBo(this);
//
//		log.debug("3 生成打断点的信息");
//		AbstractNodeBo breakNodeBo = createNewNodeBo(point.getX(), point.getY());
//		breakNodeBo.getObj().setOpType(OperationType.INSERT);
//		result.putObj(breakNodeBo.getObj());
//		result.setNewNode(breakNodeBo);
//		long breakNodePid = breakNodeBo.getObj().objPid();
//		log.debug("3.1 打断点的pid = " + breakNodePid);
//
//		log.debug("4 组装 第一条link 的信息");
//		AbstractLinkBo leftLinkBo = (AbstractLinkBo)this.copy();
//		leftLinkBo.getObj().setOpType(OperationType.INSERT);
//		leftLinkBo.setGeometry(GeoTranslator.transform(newLeftGeo, 100000, 0));
//		leftLinkBo.setLength(GeometryUtils.getLinkLength(getGeometry()));
//		leftLinkBo.setSnodeBo(this.getSnodeBo());
//		leftLinkBo.setEnodeBo(breakNodeBo);
//		result.putObj(leftLinkBo.getObj());
//		result.setNewLeftLink(leftLinkBo);
//		log.debug("4.1 生成第一条link信息 pid = " + leftLinkBo.getObj().objPid());
//
//		log.debug("5 组装 第二条link 的信息");
//		AbstractLinkBo rightLinkBo = (AbstractLinkBo)this.copy();
//		rightLinkBo.setGeometry(GeoTranslator.transform(newRightGeo, 100000, 0));
//		rightLinkBo.setLength(GeometryUtils.getLinkLength(getGeometry()));
//		rightLinkBo.setSnodeBo(breakNodeBo);
//		rightLinkBo.setEnodeBo(this.getEnodeBo());
//		result.putObj(rightLinkBo.getObj());
//		result.setNewRightLink(rightLinkBo);
//		log.debug("5.1 生成第二条link信息 pid = " + rightLinkBo.getObj().objPid());
//		
//		return result;
//	}
//}
