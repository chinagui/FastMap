package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

//GLM56004	修形中产生自相交，要提示立交或打断	修形中产生自相交，要提示立交或打断	
//1	LC_LINK、RW_LINK、RD_LINK、CMG_BUILDLINK、ADAS_LINK、AD_LINK、ZONE_LINK、LU_LINK	新增link


public class GLM56004 extends baseRule {

	public GLM56004() {
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
				
				//判断是否自相交
				Geometry geo=rdLink.getGeometry();
				List<Point> inters=new ArrayList<Point>();
				boolean isSampleLine=GeoHelper.isSample(geo, inters);
				if(isSampleLine){return;}
				
				//自相交的点是否建立立交
				String sql="SELECT G.*"
						+ "  FROM RD_GSC G, RD_GSC_LINK L"
						+ " WHERE G.PID = L.PID"
						+ "   AND L.TABLE_NAME = 'RD_LINK' AND G.U_RECORD != 2 AND L.U_RECORD != 2 "
						+ "   AND L.LINK_PID="+rdLink.getPid();
				RdGscSelector gscSelector=new RdGscSelector(getConn());
				List<RdGsc> gscList=gscSelector.loadBySql(sql, false);
				
				List<Point> intersError=new ArrayList<Point>();
				intersError.addAll(inters);
				
				if(gscList.size()>0){
					for(int i=0;i<inters.size();i++){
						Point pptmp=inters.get(i);
						for(int j=0;j<gscList.size();j++){
							RdGsc gscTmp=gscList.get(j);
							Point gscPoint=(Point) gscTmp.getGeometry();
							if(GeoHelper.isPointEquals(pptmp.getX(), pptmp.getY(), gscPoint.getX(), gscPoint.getY())){
								intersError.remove(pptmp);
							}
						}
					}}
				if(intersError.size()>0){
					this.setCheckResult(intersError.get(0), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}
		}
	}


