package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/*
 * GLM01194	Link信息	特殊交通类型检查	形态	同一个特殊交通类型的组成link，速度限制值必须相同。	特殊交通类型的速度限制值不同RD_LINK_SPEEDLIMIT
 */
public class GLM01194 extends baseRule {

	public GLM01194() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		List<Integer> linkPidList=new ArrayList<Integer>();
		for(IRow obj : checkCommand.getGlmList()){
			//只有主表rdlink的修改才引起该检查项
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				//一条特殊交通类型链上的link不重复检查
				if(linkPidList.contains(rdLink.getPid())){continue;}
				//非特殊交通类型link不查此规则
				if(rdLink.getSpecialTraffic()==0){linkPidList.add(rdLink.getPid());continue;}
				checkWithRdLink(rdLink,linkPidList);
			}else if (obj instanceof RdLinkSpeedlimit){
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit)obj;
				int linkPid=rdLinkSpeedlimit.getLinkPid();
				//一条特殊交通类型链上的link不重复检查
				if(linkPidList.contains(linkPid)){continue;}
				RdLinkSelector rdSelector=new RdLinkSelector(getConn());
				RdLink rdLink=(RdLink) rdSelector.loadById(linkPid, false);
				//非特殊交通类型link不查此规则
				if(rdLink.getSpecialTraffic()==0){linkPidList.add(linkPid);continue;}				
				checkWithRdLink(rdLink,linkPidList);
			}
		}
	}
	
	private void checkWithRdLink(RdLink rdLink,List<Integer> linkPidList) throws Exception{
		//获取rdLink对应的特殊交通类型链
		HashSetRdLinkAndPid specTrafficChain=getLoader().loadSpecTrafficChain(getConn(), rdLink);
		
		linkPidList.removeAll(specTrafficChain.getRdLinkPidSet());
		linkPidList.addAll(specTrafficChain.getRdLinkPidSet());
		
		Set<Integer> chainPidSet=specTrafficChain.getRdLinkPidSet();
		String pidStr=chainPidSet.toString().replace("[", "").replace("]", "");
		String sql="SELECT L.SPEED_TYPE"
				+ "  FROM RD_LINK_SPEEDLIMIT L"
				+ " WHERE L.LINK_PID IN ("+pidStr+")"
				+ " GROUP BY L.SPEED_TYPE"
				+ " HAVING COUNT(DISTINCT DECODE(L.FROM_SPEED_LIMIT, 0, L.TO_SPEED_LIMIT, L.FROM_SPEED_LIMIT)) > 1";
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(getConn(), sql);
		if(resultList.size()>0){
			String target=chainPidSet.toString().replace(" ", "").
			replace("[", "[RD_LINK%").replace(",", "];[RD_LINK,").replace("%", ",");
			this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
		}
	}
	
	

}
