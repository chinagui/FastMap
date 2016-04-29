package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
				
				if(MeshUtils.isPointAtMeshBorder(rdSpeedlimit.getGeometry().getCoordinate().x,rdSpeedlimit.getGeometry().getCoordinate().y)){
					this.setCheckResult("", "", 0);
					}
				}
			}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}
	
	}