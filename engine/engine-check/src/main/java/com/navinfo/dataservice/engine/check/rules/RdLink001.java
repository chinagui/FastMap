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
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Rdlink	word	RDLINK001	后台	
 * 两条RDLink不能首尾点一致
 * @author zhangxiaoyi
 *
 */

public class RdLink001 extends baseRule {

	public RdLink001() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				rdLink.getDevelopState();
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields!=null && !changedFields.containsKey("geometry")){continue;}
				
				String sql="SELECT 1"
						+ " FROM RD_LINK L"
						+ " WHERE L.S_NODE_PID IN ("+rdLink.getsNodePid()+","+rdLink.geteNodePid()+") "
						+ " AND L.E_NODE_PID IN ("+rdLink.getsNodePid()+","+rdLink.geteNodePid()+")"
								+ " AND L.U_RECORD != 2 "
						+ " AND L.LINK_PID <> "+rdLink.getPid();
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=getObj.exeSelect(this.getConn(), sql);
				if(resultList!=null && resultList.size()>0){
					this.setCheckResult(rdLink.getGeometry(), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					break;
					}
			}
			}}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}}


