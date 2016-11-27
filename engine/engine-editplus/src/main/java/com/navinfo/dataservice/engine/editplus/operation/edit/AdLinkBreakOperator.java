package com.navinfo.dataservice.engine.editplus.operation.edit;
//package com.navinfo.dataservice.engine.editplus.operation.ad;
//
//import java.util.List;
//
//import net.sf.json.JSONObject;
//
//import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
//import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
//import com.navinfo.dataservice.engine.editplus.bo.AdFaceBo;
//import com.navinfo.dataservice.engine.editplus.bo.AdLinkBo;
//import com.navinfo.dataservice.engine.editplus.bo.CommandCreateException;
//import com.navinfo.dataservice.engine.editplus.operation.AbstractOperation;
//import com.navinfo.dataservice.engine.editplus.operation.LinkBreakResult;
//import com.navinfo.dataservice.engine.editplus.operation.OperationResult;
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.Point;
//
///** 
// * @ClassName: AdLinkBreakOperator
// * @author xiaoxiaowen4127
// * @date 2016年7月15日
// * @Description: AdLinkBreakOperator.java
// */
//public class AdLinkBreakOperator extends AbstractOperation {
//	
//	protected AdLinkBo adLinkBo;
//	
//	protected List<AdFaceBo> adFaceBoList;
//	
//	protected AdLinkBreakCommand cmd;
//	
//	@Override
//	public void createCmd(JSONObject data) throws CommandCreateException{
//		this.cmd=new AdLinkBreakCommand();
//		this.cmd.parse(data);
//	}
//
//	@Override
//	public void loadData() throws Exception {
//		
////		AdLinkSelector linkSel = new AdLinkSelector(conn);
////		AdLink adLink = (AdLink)linkSel.loadById(cmd.getLinkPid(), true);
////		AbstractSelector baseSel = new AbstractSelector(AdLinkMesh.class,conn);
////		baseSel.loadRowsByParentId(cmd.getLinkPid(), true);
////		this.adLinkBo = (AdLinkBo) BoFactory.getInstance().create(adLink);
////		this.adLinkBo.loadSnodeBo();
////		this.adLinkBo.loadEnodeBo();
////		
////		AdFaceBo faceBo = new AdFaceBo();
////		this.adFaceBoList = faceBo.query(conn, cmd.getLinkPid(), true);
//	}
//
//	@Override
//	public OperationResult execute() throws Exception {
//		
//		GeometryFactory factory = new GeometryFactory();
//		Coordinate coord = new Coordinate(this.cmd.getLongitude(),this.cmd.getLatitude());
//		Point point = factory.createPoint(coord);
//		
//		LinkBreakResult result = this.adLinkBo.breakoff(point);
//		
//		for(AdFaceBo adFaceBo : adFaceBoList){
//			result.putAll(adFaceBo.breakoff(result.getTargetLinkBo(), result.getNewLeftLink(), result.getNewRightLink()));
//		}
//		
//		return result;
//	}
//	
//}
