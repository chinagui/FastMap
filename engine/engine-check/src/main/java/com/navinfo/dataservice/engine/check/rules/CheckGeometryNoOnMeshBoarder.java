package com.navinfo.dataservice.engine.check.rules;


import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;

//点限速点位不能在图框线上

public class CheckGeometryNoOnMeshBoarder extends baseRule {

	public CheckGeometryNoOnMeshBoarder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdSpeedlimit){
				RdSpeedlimit rdSpeedlimit = (RdSpeedlimit)obj;	
				
				if(MeshUtils.isPointAtMeshBorderWith100000(rdSpeedlimit.getGeometry().getCoordinate().x,rdSpeedlimit.getGeometry().getCoordinate().y)){
					this.setCheckResult("", "", 0);
					}
				}
			}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}
	
	}