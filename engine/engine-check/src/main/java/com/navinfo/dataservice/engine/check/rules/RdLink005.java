package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * html/word	RDLINK005	后台	
 * 该Node点已经被做成同一点，不能再移动该Node点
 * @author zhangxiaoyi
 *
 */

public class RdLink005 extends baseRule {

	public RdLink005() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;	
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields==null || !changedFields.containsKey("geometry")){continue;}
				JSONObject geojson=(JSONObject) changedFields.get("geometry");
				Geometry geoNew=GeoTranslator.geojson2Jts(geojson);
				Coordinate[] coordsNew = geoNew.getCoordinates();
				
				Geometry geoOld=rdLink.getGeometry();
				Coordinate[] coordsOld = geoOld.getCoordinates();
				String sql="";
				//移动了起点
				if(coordsNew[0].compareTo(coordsOld[0])!=0 && !changedFields.containsKey("sNodePid")){
					sql="SELECT 1"
						+ "  FROM RD_SAMENODE_PART S"
						+ " WHERE S.TABLE_NAME = 'RD_NODE' AND S.U_RECORD != 2 "
						+ "   AND S.NODE_PID ="+rdLink.getsNodePid();
				}
				//移动了终点
				if(coordsNew[coordsNew.length-1].compareTo(coordsOld[coordsOld.length-1])!=0 && !changedFields.containsKey("eNodePid")){
					sql="SELECT 1"
						+ "  FROM RD_SAMENODE_PART S"
						+ " WHERE S.TABLE_NAME = 'RD_NODE' AND S.U_RECORD != 2 "
						+ "   AND S.NODE_PID ="+rdLink.geteNodePid();
				}
				if(!sql.isEmpty()){
					DatabaseOperator getObj=new DatabaseOperator();
					List<Object> resultList=getObj.exeSelect(this.getConn(), sql);
					if(resultList!=null && resultList.size()>0){
						this.setCheckResult("","",0);
						break;
						}
					}
				}
			}
		}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}}


