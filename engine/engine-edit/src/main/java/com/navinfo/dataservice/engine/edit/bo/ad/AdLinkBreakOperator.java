package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

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
		this.adLinkBo = new AdLinkBo(conn, cmd.getLinkPid(), true);
		this.adLinkBo.getMeshes();
		this.adLinkBo.getsNode();
		this.adLinkBo.geteNode();
		
		
	}

	@Override
	public Result execute() {
		
		GeometryFactory factory = new GeometryFactory();
		Coordinate coord = new Coordinate(this.cmd.getLongitude(),this.cmd.getLatitude());
		Point point = factory.createPoint(coord);
		
		BreakResult result = this.adLinkBo.breakoff(point);
		
		for(AdFaceBo adFaceBo : adFaceBoList){
			result.add(adFaceBo.breakoff(result.targetLinkBo, result.newLeftLink, result.newRightLink));
		}
		
		return result;
	}
}
