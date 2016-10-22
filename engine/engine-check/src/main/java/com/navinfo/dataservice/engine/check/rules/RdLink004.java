package com.navinfo.dataservice.engine.check.rules;


import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Rdlink	word	RDLINK004	后台	对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点
 * @author zhangxiaoyi
 *
 */

public class RdLink004 extends baseRule {

	public RdLink004() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;	
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields==null){continue;}
				String nodeIds="";
				if(changedFields.containsKey("sNodePid")){
					nodeIds=nodeIds+changedFields.get("sNodePid")+","+rdLink.getsNodePid();					
				}
				if(changedFields.containsKey("eNodePid")){
					nodeIds=nodeIds+changedFields.get("eNodePid")+","+rdLink.geteNodePid();					
				}
				if(nodeIds.isEmpty()){continue;}
				
				String sql="SELECT 1"
						+ "  FROM RD_CROSS_NODE S"
						+ " WHERE S.NODE_PID IN ("+nodeIds+") AND S.U_RECORD != 2 ";
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=getObj.exeSelect(this.getConn(), sql);
				if(resultList!=null && resultList.size()>0){
					this.setCheckResult("","",0);
					break;
					}
				}
			}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}
	
	}