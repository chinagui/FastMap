package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.TwoNodeConnected;


/** 
 * @ClassName: RDBRANCHOutLinkUnidirectional
 * @author songdongyan
 * @date 2016年8月19日
 * @Description: 退出Link为单方向且通行方向不能进入路口
 */
public class RDBRANCHOutLinkUnidirectional extends baseRule {

	/**
	 * 
	 */
	public RDBRANCHOutLinkUnidirectional() {
		// TODO Auto-generated constructor stub
	}
	

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow obj:checkCommand.getGlmList()){
			//获取新建RdBranch信息
			if(obj instanceof RdBranch ){
				RdBranch rdBranch = (RdBranch)obj;
				
				int outLinkPid = rdBranch.getOutLinkPid();
				int nodePid = rdBranch.getNodePid();
				List<IRow> vias = rdBranch.getVias();
				
				Map<String, Object> changedFields = rdBranch.changedFields();
				if(!changedFields.isEmpty()&&changedFields.containsKey("outLinkPid")){
					outLinkPid = (int) changedFields.get("outLinkPid");
				}
				if(!changedFields.isEmpty()&&changedFields.containsKey("vias")){
					vias = (List<IRow>) changedFields.get("vias");
				}
				//退出线
				RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
				RdLink outLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(outLinkPid, false);
				//退出是否沿通行方向
				if(outLink.getDirect()==1){
					continue;
				}
				
				if(vias.size() == 0){
					if(outLink.getDirect()==2&&outLink.getsNodePid()==nodePid){
						continue;
					}
					if(outLink.getDirect()==3&&outLink.geteNodePid()==nodePid){
						continue;
					}
				}
				
				int endNode = 0;
				if(outLink.getDirect() == 2){
					endNode = outLink.getsNodePid();
				}else if(outLink.getDirect() == 3){
					endNode = outLink.geteNodePid();
				}

				if(!rdBranchViasConnectedInOneDirection(nodePid,endNode,vias)){
					this.setCheckResult("", "", 0);
					return;
				}	
			}
		}

	}
	
	private boolean rdBranchViasConnectedInOneDirection(int startNode,int endNode,List<IRow> viaLinks) throws Exception{
		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
		Map<Integer,Object> viaLinkGroups = new HashMap<Integer,Object>();
		Iterator<IRow> viaLinksIter = viaLinks.iterator();
		while(viaLinksIter.hasNext()){
			RdBranchVia rdBranchVia = (RdBranchVia) viaLinksIter.next();
			int groupId = rdBranchVia.getGroupId();
			if(viaLinkGroups.containsKey(groupId)){
				Map<Integer,RdLink> rdLinkMap = (Map<Integer, RdLink>) viaLinkGroups.get(groupId);
				RdLink rdLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(rdBranchVia.getLinkPid(), false);
				rdLinkMap.put(rdBranchVia.getSeqNum(), rdLink);
				viaLinkGroups.put(groupId, rdLinkMap);
			}else{
				Map<Integer,RdLink> rdLinkMap = new HashMap<Integer, RdLink>();
				RdLink rdLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(rdBranchVia.getLinkPid(), false);
				rdLinkMap.put(rdBranchVia.getSeqNum(), rdLink);
				viaLinkGroups.put(groupId, rdLinkMap);
			}
		}
		
		
		return false;
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}


}
