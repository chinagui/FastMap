package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/** 
 * @ClassName: AdLinkBreakOperator
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: AdLinkBreakOperator.java
 */
public class AdLinkBreakOperator extends AbstractOperator {
	
	protected AdLinkBo adLinkBo;
	
	protected List<AdFaceBo> adFaceBoList;
	
	protected AdLinkBreakCommand cmd;
	
	@Override
	public void createCmd(JSONObject data) throws CommandCreateException{
		this.cmd=new AdLinkBreakCommand();
		this.cmd.parse(data);
	}

	@Override
	public void loadData() throws Exception {
		
		this.adLinkBo = new AdLinkBo(linkPid);
		
		this.adLinkBo.getmesh();
	}

	@Override
	public Result execute() {
		
		Result result = new Result();
		
		GeometryFactory factory = new GeometryFactory();
		Coordinate coord = new Coordinate(this.cmd.getLongitude(),this.cmd.getLatitude());
		Point point = factory.createPoint(coord);
		
		this.adLinkBo.breakoff(result, point);
		
		
		for(IRow row : result.getAddObjects()){
			
		}
		
		for(AdFaceBo adFaceBo : adFaceBoList){

		}
		return result;
		
	}
}
