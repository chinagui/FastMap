package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Rdlink	html	PERMIT_CHECK_GSCNODE_NOT_MOVE	
 * 后台	创建或修改link，节点不能到已有的立交点处，请先删除立交关系
 * @author zhangxiaoyi
 *
 */

public class PermitCheckGscnodeNotMove extends baseRule {

	public PermitCheckGscnodeNotMove() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;	
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields!=null && !changedFields.containsKey("geometry")){continue;}
				
				int sPid=rdLink.getsNodePid();
				int ePid=rdLink.geteNodePid();
				LineString rdLine=(LineString) rdLink.getGeometry();
				Point rdS=rdLine.getStartPoint();
				Point rdE=rdLine.getEndPoint();
				Geometry geo=rdLink.getGeometry();
				
				/*RdLinkSelector linkSelector=new RdLinkSelector(getConn());				
				List<RdLink> rdList=linkSelector.loadBySql(sql, false);
				
				RdGscSelector gscSelector=new RdGscSelector(getConn());
				boolean isError=false;
					
					String sqltmp="SELECT G.*"
							+ "  FROM RD_GSC_LINK L1, RD_GSC G"
							+ " WHERE L1.TABLE_NAME = 'RD_LINK'"
							+ "   AND L1.LINK_PID = "+rdLink.getPid()
							+ "   AND L1.PID = G.PID";
					List<RdGsc> gscList=gscSelector.loadBySql(sqltmp, false);
					if(gscList.size()==0){isError=true;break;}
					boolean isGsc=false;
					for(int m=0;m<gscList.size();m++){
						RdGsc gscTmp=gscList.get(m);
						Point gscPoint=(Point) gscTmp.getGeometry();
						if(GeoHelper.isPointEquals(touchPoint.getX(), touchPoint.getY(), gscPoint.getX(), gscPoint.getY())){
							isGsc=true;break;
						}
					if(!isGsc){isError=true;break;}
				}
				
				if(isError){
					this.setCheckResult(geo, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}*/
				}
			}}
	
	private boolean hasGsc(){
		return false;
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}}


