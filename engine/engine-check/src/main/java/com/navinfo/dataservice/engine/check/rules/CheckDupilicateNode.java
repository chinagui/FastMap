package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class CheckDupilicateNode extends baseRule {

	public CheckDupilicateNode() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		List<IRow> objList = checkCommand.getGlmList();
		for(int i=0; i<objList.size(); i++){
			IRow obj = objList.get(i);
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				Geometry geo =rdLink.getGeometry();

				Geometry geo2=GeoTranslator.transform(geo, 0.00001, 5);
				
				Coordinate[] coords = geo2.getCoordinates();
				
				for(int j=0;j<coords.length;j++){
					if(j+2<coords.length){
						
						Coordinate current = coords[j];
						
						Coordinate next = coords[j+1];
						
						Coordinate next2 = coords[j+2];
						
						if(current.compareTo(next)==0 || current.compareTo(next2)==0){
							this.setCheckResult("", "", 0);
							return;
							//throw new Exception(" 一根link上不能存在坐标相同的形状点");
							}
						}
					}
				}
			}
		}
	

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
