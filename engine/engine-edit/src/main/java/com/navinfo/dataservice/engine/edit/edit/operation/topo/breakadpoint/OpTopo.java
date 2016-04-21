package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.sql.Connection;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.comm.util.OperateUtils;
import com.vividsolutions.jts.geom.Point;

/**
 * @author zhaokk
 * 创建行政区划点有关行政区划线具体操作类
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
		String msg="";
		this.breakPoint(result);
		return msg;
	}

	/* 
	 * 行政区划点打断行政区划线
	 * 1.打断点是行政区划线的形状点
	 * 2.打断点不是新政区划线的形状点
	 * @param result
	 * @throws Exception 
	 */
	private void breakPoint(Result result) throws Exception {
		Point point = command.getPoint();
		long lon = (long) (point.getX() * 100000);
		long lat = (long) (point.getY() * 100000);
	    JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		boolean hasFound = false;
		result.setPrimaryPid(command.getLinkPid());
		log.info("1 获取要打断行政区划线的信息 linkPid = "+command.getLinkPid());
	    AdLink adLink =(AdLink)new AdLinkSelector(conn).loadById(command.getLinkPid(),true);
	    log.info("2 删除要打断的行政区划线信息");
	    result.insertObject(adLink, ObjStatus.DELETE, adLink.pid());
	    
	    log.info("3 获取要打断行政区划线的几何属性 判断打断点是否在形状点上还是在线段上");
	    JSONObject geojson = GeoTranslator.jts2Geojson(adLink
					.getGeometry());
	    JSONArray jaLink = geojson.getJSONArray("coordinates");
	    for(int i =0 ; i < jaLink.size()-1; i++){
	    		JSONArray jaPS = jaLink.getJSONArray(i);
	    		if(i == 0){
	    			ja1.add(jaPS);
	    		}
	    		JSONArray jaPE = jaLink.getJSONArray(i + 1);
	    		
	    		if(!hasFound){
	    			//打断点在形状点上
		    		if(lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)){ 
						ja1.add(new double[] { lon, lat });
						hasFound = true;
					}
		    		//打断点在线段上
					else if (GeoTranslator.isIntersection(
							new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
							new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
							new double[] { lon, lat })) {
						ja1.add(new double[] { lon, lat });
						ja2.add(new double[] { lon, lat });
						hasFound = true;
					}else{
						if(i>0){
							ja1.add(jaPS);
						}
						
						ja1.add(jaPE);
					}
	    		}else{
	    			ja2.add(jaPS);
	    		}
	    		if (i == jaLink.size() - 2) {
					ja2.add(jaPE);
				}
	    		if (!hasFound) {
	    			throw new Exception("打断的点不在打断LINK上");
	    		}
	    }
	    	
	    this.createLinksForADNode(adLink,ja1,ja2,result);
	    
	}

	/*
	 * 行政区划点 、线生成和关系维护。
	 * 1.生成打断点的信息
	 * 2.根据link1 和link2的几何属性生成新的一组link
	 * 3.维护link和点的关系 以及维护linkMesh的关系
	 * @param AdLink 要打断的link   sArray link1的几何属性  eArray link2的几何属性 result
	 * @throws Exception 
	 */
	private void  createLinksForADNode(AdLink adLink,JSONArray sArray,JSONArray eArray,Result result) throws Exception {
		log.debug("3 生成打断点的信息");
		AdNode node = OperateUtils.createAdNode(command.getPoint().getX(), command.getPoint().getY());
		result.insertObject(node, ObjStatus.INSERT, node.pid());
		log.debug("3.1 打断点的pid = "+node.pid());
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", sArray);
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", eArray);
		log.debug("4 组装 第一条link 的信息");
		AdLink  slink = new AdLink();
		slink.copy(adLink);
		slink.setPid(PidService.getInstance().applyAdLinkPid());
		slink.setGeometry(GeoTranslator.geojson2Jts(sGeojson));
		slink.setLength(GeometryUtils.getLinkLength(GeoTranslator.transform(slink.getGeometry(), 0.00001, 5)));
		command.setsAdLink(slink);
		log.debug("4.1 生成第一条link信息 pid = "+slink.getPid());
		log.debug("5 组装 第一条link 的信息");
		AdLink  elink = new AdLink();
		elink.copy(adLink);
		elink.setPid(PidService.getInstance().applyAdLinkPid());
		elink.setGeometry(GeoTranslator.geojson2Jts(sGeojson));
		elink.setLength(GeometryUtils.getLinkLength(GeoTranslator.transform(elink.getGeometry(), 0.00001, 5)));
		slink.setStartNodePid(node.getPid());
		elink.setEndNodePid(node.getPid());
		command.seteAdLink(elink);
		log.debug("6.1 生成第二条link信息 pid = "+elink.getPid());
		result.insertObject(slink, ObjStatus.INSERT, slink.pid());
		result.insertObject(elink, ObjStatus.INSERT, elink.pid());
	}
	

}
