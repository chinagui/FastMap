package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;


/** 
 * @ClassName: RDBRANCHOutLinkUnidirectional
 * @author songdongyan
 * @date 2016年8月19日
 * @Description: 退出Link为单方向且通行方向不能进入路口
 * 理解：退出link为单方向，则退出link沿通行方向与进入node联通
 */
public class RDBRANCHOutLinkUnidirectional extends baseRule {

	/**
	 * 
	 */
	public RDBRANCHOutLinkUnidirectional() {
		// TODO Auto-generated constructor stub
	}
	

//	/* (non-Javadoc)
//	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
//	 */
//	@Override
//	public void preCheck(CheckCommand checkCommand) throws Exception {
//		// TODO Auto-generated method stub
//		for(IRow obj:checkCommand.getGlmList()){
//			//获取新建RdBranch信息
//			if(obj instanceof RdBranch ){
//				RdBranch rdBranch = (RdBranch)obj;
//				//新增或编辑，退出link,进入node,经过线信息
//				int outLinkPid = rdBranch.getOutLinkPid();
//				int nodePid = rdBranch.getNodePid();
//				List<IRow> vias = rdBranch.getVias();
//				
//				Map<String, Object> changedFields = rdBranch.changedFields();
//				if(!changedFields.isEmpty()&&changedFields.containsKey("outLinkPid")){
//					outLinkPid = (int) changedFields.get("outLinkPid");
//				}
//				if(!changedFields.isEmpty()&&changedFields.containsKey("vias")){
//					vias = (List<IRow>) changedFields.get("vias");
//				}
//				
//				//退出线
//				RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
//				RdLink outLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(outLinkPid, false);
//				//退出双向，不检查
//				if(outLink.getDirect()==1){
//					continue;
//				}
//				//无经过线，退出线直接挂接到进入node上。
//				if(vias.size() == 0){
//					if(outLink.getDirect()==2&&outLink.getsNodePid()==nodePid){
//						continue;
//					}
//					if(outLink.getDirect()==3&&outLink.geteNodePid()==nodePid){
//						continue;
//					}
//				}
//				//有经过线，退出线与经过线挂接的node做为检查联通关系的终点，进入node做为起点。
//				int endNode = 0;
//				if(outLink.getDirect() == 2){
//					endNode = outLink.getsNodePid();
//				}else if(outLink.getDirect() == 3){
//					endNode = outLink.geteNodePid();
//				}
//				//起点终点经过线不联通，报错。
//				if(!rdBranchViasConnectedInOneDirection(nodePid,endNode,vias)){
//					this.setCheckResult("", "", 0);
//					return;
//				}	
//			}
//		}
//
//	}
//	
//	//按照groupId,seqNum整理经过线
//	private Map<Integer,Map> initViaLinkGroups(List<IRow> viaLinks) throws Exception{
//		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
//		//按照groupId分组
//		Map<Integer,Map> viaLinkGroups = new HashMap<Integer,Map>();
//		Iterator<IRow> viaLinksIter = viaLinks.iterator();
//		while(viaLinksIter.hasNext()){
//			RdBranchVia rdBranchVia = (RdBranchVia) viaLinksIter.next();
//			int groupId = rdBranchVia.getGroupId();
//			if(viaLinkGroups.containsKey(groupId)){
//				Map<Integer,RdLink> rdLinkMap = (Map<Integer, RdLink>) viaLinkGroups.get(groupId);
//				RdLink rdLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(rdBranchVia.getLinkPid(), false);
//				rdLinkMap.put(rdBranchVia.getSeqNum(), rdLink);
//				viaLinkGroups.put(groupId, rdLinkMap);
//			}else{
//				Map<Integer,RdLink> rdLinkMap = new HashMap<Integer, RdLink>();
//				RdLink rdLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(rdBranchVia.getLinkPid(), false);
//				rdLinkMap.put(rdBranchVia.getSeqNum(), rdLink);
//				viaLinkGroups.put(groupId, rdLinkMap);
//			}
//		}
//		return viaLinkGroups;
//	}
//	
//	private boolean rdBranchViasConnectedInOneDirection(int startNode,int endNode,List<IRow> viaLinks) throws Exception{
//		Map<Integer,Map> viaLinkGroups = initViaLinkGroups(viaLinks);
//		//接续link是否沿通行方向
//		Iterator<Entry<Integer, Map>> viaLinkGroupsIter = viaLinkGroups.entrySet().iterator();
//		while(viaLinkGroupsIter.hasNext()){
//			Map<Integer,RdLink> rdLinkMap = (Map<Integer, RdLink>) viaLinkGroupsIter.next();
//			int i = 0;
//			int temp_startNode = startNode;
//			while(i<rdLinkMap.size()){
//				if(!rdLinkMap.containsKey(i+1)){
//					return false;
//				}else{
//					RdLink rdLink = rdLinkMap.get(i+1);
//					if(rdLink.getsNodePid()==temp_startNode){
//						if(rdLink.getDirect()==3){
//							return false;
//						}
//						temp_startNode = rdLink.geteNodePid();
//					}else if(rdLink.geteNodePid()==temp_startNode){
//						if(rdLink.getDirect()==2){
//							return false;
//						}
//						temp_startNode = rdLink.getsNodePid();
//					}else{
//						return false;
//					}
//				}
//				if(i == rdLinkMap.size()-1 && temp_startNode!=endNode){
//					return false;
//				}
//				i += 1;
//			}
//		}
//		return true;
//	}
	
	

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		
		for (IRow obj : checkCommand.getGlmList()) {
			// 获取新建RdBranch信息
			if (obj instanceof RdBranch) {
				
				RdBranch rdBranch = (RdBranch) obj;

				if (check(rdBranch)) {
					
					this.setCheckResult("", "", 0);
					
					return;
				}
			}
		}
	}
	
	private boolean check(RdBranch rdBranch)throws Exception
	{
		int relationshipType = rdBranch.getRelationshipType();
	
		if (rdBranch.changedFields().containsKey("relationshipType")) {
			
			relationshipType = (int) rdBranch.changedFields().get(
					"relationshipType");
		}
		
		if(relationshipType==2)
		{
			return false;
		}
		
		int outLinkPid = rdBranch.getOutLinkPid();
		
		if (rdBranch.changedFields().containsKey("outLinkPid")) {

			outLinkPid = (int) rdBranch.changedFields().get("outLinkPid");
		}
		
		// 退出线通行方向终点Pid
		int outNodeFlag = getOutNodeFlag(outLinkPid);

		if (outNodeFlag == 0) {
			return false;
		}
	
		// 分歧进入点所在路口的路口组成nodePid
		Set<Integer> crossNodePids = getCrossNode(rdBranch.getNodePid());

		if (crossNodePids.contains(outNodeFlag)) {
			return true;
		}
		
		return false;	
	}
	
	private int getOutNodeFlag(Integer outLinkPid) throws Exception {

		String strFormat = "select t.e_node_pid as nodePid from rd_link t where t.link_pid = {0} and t.direct = 2 and t.u_record <> 2 UNION select t.s_node_pid as nodePid  from rd_link t where t.link_pid = {1} and t.direct = 3 and t.u_record <> 2";

		int outNodeFlag = 0;
		
		String sql = MessageFormat.format(strFormat, String.valueOf(outLinkPid),String.valueOf(outLinkPid));
	
		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList!=null &&resultList.size()>0)
		{
			outNodeFlag = Integer.parseInt(String.valueOf(resultList.get(0)));
		}
	

		return outNodeFlag;
	}

	
	
	private Set<Integer> getCrossNode(Integer nodePid) throws Exception {

		String strFormat = "select t.node_pid from rd_cross_node t where t.u_record <> 2 and t.pid in (select rc.pid from rd_cross_node rc where rc.node_pid = {0} and rc.u_record <> 2)";

		Set<Integer> crossNodePids = new HashSet<Integer>();

		String sql = MessageFormat.format(strFormat, String.valueOf(nodePid));

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		for (Object obj : resultList) {

			int pid = Integer.parseInt(String.valueOf(obj));

			crossNodePids.add(pid);
		}

		return crossNodePids;
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}


}
