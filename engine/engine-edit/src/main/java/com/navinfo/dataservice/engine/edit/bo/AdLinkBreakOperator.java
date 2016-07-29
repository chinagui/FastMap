package com.navinfo.dataservice.engine.edit.bo;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.engine.edit.bo.ad.AdFaceBo;
import com.navinfo.dataservice.engine.edit.bo.ad.AdLinkBo;
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
		
		AdLink link = new AdLink();
		link.setPid(cmd.getLinkPid());
		IObj po = PoFactory.getInstance().get(conn, link, true);
		this.adLinkBo = (AdLinkBo) BoFactory.getInstance().create(po);
		this.adLinkBo.getMeshes();
		this.adLinkBo.getsNode();
		this.adLinkBo.geteNode();
		
		AdFaceBo faceBo = new AdFaceBo();
		this.adFaceBoList = faceBo.query(conn, cmd.getLinkPid(), true);
	}

	@Override
	public Result execute() throws Exception {
		
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
