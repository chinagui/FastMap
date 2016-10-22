package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/*
 * SHAPING_CHECK_CROSS_RDLINK_RDLINK	两条Link相交，必须做立交或者打断	两条Link相交，必须做立交或者打断
 */


public class ShapingCheckCrossRdLinkRdLink extends baseRule {

	public ShapingCheckCrossRdLinkRdLink() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				Geometry geo=rdLink.getGeometry();
				
				//相交的线是否建立立交
				String sql="SELECT B.*"
						+ "  FROM RD_LINK A, RD_LINK B"
						+ " WHERE A.LINK_PID = "+rdLink.getPid()
						+ "   AND B.LINK_PID <> A.LINK_PID AND A.U_RECORD != 2 AND B.U_RECORD != 2 "
						+ "   AND SDO_RELATE(B.GEOMETRY, A.GEOMETRY, "
						+ "'MASK=CONTAINS+COVEREDBY+COVERS+EQUAL+INSIDE+OVERLAPBDYDISJOINT+OVERLAPBDYINTERSECT') = 'TRUE'";
				RdLinkSelector linkSelector=new RdLinkSelector(getConn());				
				List<RdLink> rdList=linkSelector.loadBySql(sql, false);
				
				RdGscSelector gscSelector=new RdGscSelector(getConn());
				boolean isError=false;
				for(int i=0;i<rdList.size();i++){
					RdLink linkB=rdList.get(i);
					String sqltmp="SELECT G.*"
							+ "  FROM RD_GSC_LINK L1, RD_GSC_LINK L2, RD_GSC G"
							+ " WHERE L1.TABLE_NAME = 'RD_LINK'"
							+ "   AND L2.TABLE_NAME = 'RD_LINK'"
							+ "   AND L1.U_RECORD != 2 AND L2.U_RECORD != 2 AND G.U_RECORD != 2 "
							+ "   AND L1.LINK_PID = "+rdLink.getPid()
							+ "   AND L1.PID = L2.PID"
							+ "   AND L2.LINK_PID = "+linkB.getPid()
							+ "   AND L1.PID = G.PID";
					List<RdGsc> gscList=gscSelector.loadBySql(sqltmp, false);
					if(gscList.size()==0){isError=true;break;}
					List<Point> inters=GeoHelper.CalculateIntersection(linkB.getGeometry(),geo,false);
					List<Point> intersError=new ArrayList<Point>();
					intersError.addAll(inters);
					for(int n=0;n<inters.size();n++){
						Point pptmp=inters.get(n);
						for(int m=0;m<gscList.size();m++){
							RdGsc gscTmp=gscList.get(m);
							Point gscPoint=(Point) gscTmp.getGeometry();
							if(GeoHelper.isPointEquals(pptmp.getX(), pptmp.getY(), gscPoint.getX(), gscPoint.getY())){
								intersError.remove(pptmp);
							}
						}
					}
					if(intersError.size()>0){isError=true;break;}
				}
				
				if(isError){
					this.setCheckResult(geo, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}
		}
	}


