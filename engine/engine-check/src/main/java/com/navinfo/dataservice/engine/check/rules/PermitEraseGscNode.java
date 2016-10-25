package com.navinfo.dataservice.engine.check.rules;

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
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.awt.PointShapeFactory.X;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * html/word	PERMIT_ERASE_GSC_NODE	后台	
 * 不允许去除或修改有立交关系的形状点
 * @author zhangxiaoyi
 *
 */

public class PermitEraseGscNode extends baseRule {

	public PermitEraseGscNode() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;	
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields==null || !changedFields.containsKey("geometry")){continue;}
				
				//link上是否有立交
				RdGscSelector gscSelector=new RdGscSelector(getConn());
				String sqltmp="SELECT G.*"
						+ "  FROM RD_GSC_LINK L1, RD_GSC G"
						+ " WHERE L1.TABLE_NAME = 'RD_LINK' AND L1.U_RECORD != 2 "
						+ "  AND G.U_RECORD != 2 "
						+ "   AND L1.LINK_PID = "+rdLink.getPid()
						+ "   AND L1.PID = G.PID";
				List<RdGsc> gscList=gscSelector.loadBySql(sqltmp, false);
				if(gscList.size()==0){break;}
				JSONObject geojson=(JSONObject) changedFields.get("geometry");
				Geometry geoNew=GeoTranslator.geojson2Jts(geojson,100000, 0);
				Coordinate[] coords = geoNew.getCoordinates();
				//立交点是否还在link上	
				for(int i=0;i<gscList.size();i++){
					boolean hasPoints=false;
					Geometry gscPoint=gscList.get(i).getGeometry();
					for(int j=0;j<coords.length;j++){
						if(coords[j].compareTo(gscPoint.getCoordinate())==0){hasPoints=true;}
						}
					if(!hasPoints){
						this.setCheckResult(gscPoint, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
						break;
						}
					}
				}
			}}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}}


