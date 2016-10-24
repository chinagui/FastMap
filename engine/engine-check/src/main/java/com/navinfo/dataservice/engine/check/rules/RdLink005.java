package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
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
 * html/word	RDLINK005	后台	
 * 该Node点已经被做成同一点，不能再移动该Node点
 * @author zhangxiaoyi
 *
 */

public class RdLink005 extends baseRule {

	public RdLink005() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//修改起终点的link
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;	
				Map<String, Object> changedFields = rdLink.changedFields();
				if(changedFields==null || (!changedFields.containsKey("sNodePid")&&!changedFields.containsKey("eNodePid"))){continue;}
				int nodePid=0;
				if(changedFields.containsKey("sNodePid")){nodePid=rdLink.getsNodePid();}
				else if(changedFields.containsKey("eNodePid")){nodePid=rdLink.geteNodePid();}
				if(hasSameNode(nodePid)){
					this.setCheckResult("","",0);
					break;
					}
			}else if (obj instanceof RdNode){
				//修改geo的node
				RdNode rdNode = (RdNode)obj;	
				Map<String, Object> changedFields = rdNode.changedFields();
				if(changedFields==null || !changedFields.containsKey("geometry")){continue;}
				if(hasSameNode(rdNode.getPid())){
					this.setCheckResult("","",0);
					break;
					}
				}
			}
		}
	
	private boolean hasSameNode(int nodePid) throws Exception{
		String sql="SELECT 1"
				+ "  FROM RD_SAMENODE_PART S"
				+ " WHERE S.TABLE_NAME = 'RD_NODE' AND S.U_RECORD != 2 "
				+ "   AND S.NODE_PID ="+nodePid;
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=getObj.exeSelect(this.getConn(), sql);
		if(resultList!=null && resultList.size()>0){return true;}
		return false;
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {}}


