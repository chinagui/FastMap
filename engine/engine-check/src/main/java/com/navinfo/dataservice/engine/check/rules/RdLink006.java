package com.navinfo.dataservice.engine.check.rules;


import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Rdnode	word	RDLINK006	后台	图廓点只能在图廓线上移动(后台需检查图廓点是否在图廓线上)???
 * @author zhangxiaoyi
 *
 */

public class RdLink006 extends baseRule {

	public RdLink006() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode)obj;
				Map<String, Object> changedFields = rdNode.changedFields();
				if(changedFields==null || !changedFields.containsKey("geometry")){continue;}
				//获取rd_node_form,判断是否为图廓点
				RdNodeSelector nodeSelector=new RdNodeSelector(getConn());
				List<IRow> list= nodeSelector.loadRowsByClassParentId(RdNodeForm.class, rdNode.getPid(), false, null);
				boolean isBorderNode=false;
				for(int i=0;i<list.size();i++){
					if(((RdNodeForm) list.get(i)).getFormOfWay()==2){
						isBorderNode=true;
						break;
					}
				}
				//图廓点坐标不在图廓线上
				JSONObject geojson=(JSONObject) changedFields.get("geometry");
				Geometry geoNew=GeoTranslator.geojson2Jts(geojson);
				//先取5位精度
				//Geometry geo2=GeoTranslator.transform(geoNew, 0.00001, 5);
				Coordinate[] coords = geoNew.getCoordinates();	
				if(isBorderNode && !MeshUtils.isPointAtMeshBorder(coords[0].x,coords[0].y)){
					this.setCheckResult("", "", 0);
					break;
					}
				}
			}
		}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}
	
	}