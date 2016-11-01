package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.TwoNodeConnected;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
/**
 * 车信	html		后台	进入Link为单方向且通行方向不能离开路口
 * 车信	html		后台	退出Link为单方向且通行方向不能进入路口
 * 路口车信，线线车信都要检查
 * @author zhangxiaoyi
 *
 */
public class RdLane003 extends baseRule {

	public RdLane003() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
//		for(IRow obj : checkCommand.getGlmList()){
//			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
//			if (obj instanceof RdLaneConnexity){//交限
//				RdLaneConnexity laneObj=(RdLaneConnexity) obj;
//				Map<String, Object> changedFields=laneObj.changedFields();
//				//新增执行该检查
//				if(changedFields!=null && changedFields.size()>0){continue;}
//				RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
//				RdLink inLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(laneObj.getInLinkPid(), false);
//				//进入link非单向
//				//RdCrossSelector crossSelector = new RdCrossSelector(this.getConn());
//				//RdCross  inCross  = crossSelector.loadCrossByNodePid(laneObj.getNodePid(), false);
//				/*
//				 * 11.进入Link为单方向且通行方向不能离开路口；
//				 * 12.退出Link为单方向且通行方向不能进入路口；
//				 * 需求现在还有出入 先不做检查判断 等需求明确再加上
//				 * */
//				
//				//进入link的退出点
//				int inLinkExitNode=0;
//				if(inLink.getDirect()==2){inLinkExitNode=inLink.geteNodePid();}
//				if(inLink.getDirect()==3){inLinkExitNode=inLink.getsNodePid();}
//				if(inLinkExitNode!=laneObj.getNodePid()){
//					this.setCheckResult("", "", 0,"进入Link通行方向应与车信通行方向一致");
//					return;
//				}
//				/*if((inLink.getDirect() == 2 || inLink.getDirect()==3) && inCross.getPid() == 0){
//					this.setCheckResult("", "", 0,"如果进入Link为单方向且通行方向不能离开路口");
//					return;
//				}*/
//				for(IRow topo:laneObj.getTopos()){
//					RdLaneTopology topoObj=(RdLaneTopology) topo;
//					//退出link
//					RdLink outLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(topoObj.getOutLinkPid(), false);
//					
//					//退出link的进入点
//					int outLinkEnterNode=0;
//					//int outLinkOutNode = 0;
//					if(outLink.getDirect()==2){
//						outLinkEnterNode=outLink.getsNodePid();
//						//outLinkOutNode = outLink.geteNodePid();
//					}
//					if(outLink.getDirect()==3){
//						outLinkEnterNode=outLink.geteNodePid();
//						//outLinkOutNode = outLink.getsNodePid();
//						}
//					
//					
//					//退出link非单向
//					//RdCross outCross  = crossSelector.loadCrossByNodePid(outLinkOutNode, false);
//					/*if((outLink.getDirect() ==2 || outLink.getDirect() ==3)&& outCross.getPid() == 0){
//						this.setCheckResult("", "", 0,"退出Link为单方向且通行方向不能进入路口");
//						return;
//					}*/
//					boolean isRight=false;
//					//线线关系
//					if(topoObj.getRelationshipType()==2){
//						if(this.isValidLineLane(topoObj,inLinkExitNode,outLinkEnterNode)){isRight=true;}
//					}else if(topoObj.getRelationshipType()==1){
//						if(this.isValidCrossLane(inLinkExitNode,outLinkEnterNode)){isRight=true;}
//					}
//					if(!isRight){
//						this.setCheckResult("", "", 0,"退出Link通行方向应与车信通行方向一致");
//						return;
//					}
//				}
//			}}
	}
	
	private boolean isValidLineLane(RdLaneTopology topoObj,int inLinkExitNode,int outLinkEnterNode) throws Exception{
		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
		//经过线Pid
		List<Integer> vialinkPids = new ArrayList<Integer>();
		for(IRow deObj:topoObj.getVias()){
			RdLaneVia rdBranchVia = (RdLaneVia)deObj;
			vialinkPids.add(rdBranchVia.getLinkPid());
		}
		if(vialinkPids.size()==0){return false;}
		//经过线信息
		List<RdLink> viaRdLinks = rdLinkSelector.loadByPids(vialinkPids,false);
		//经过线是否沿通行方向联通
		TwoNodeConnected twoNodeConnected = new TwoNodeConnected(inLinkExitNode,outLinkEnterNode,viaRdLinks);
		if(!twoNodeConnected.isConnected()){
			return false;
		}
		return true;
	}
	
	/**
	 * 路口车信，进入link是进入路口，退出link是退出路口
	 * @return
	 * @throws Exception 
	 */
	private boolean isValidCrossLane(int inLinkExitNode,int outLinkEnterNode) throws Exception{
		//node点属于同一个cross
		String sql="SELECT 1"
				+ "  FROM RD_CROSS_NODE N1, RD_CROSS_NODE N2"
				+ " WHERE N1.NODE_PID = "+inLinkExitNode
				+ "   AND N2.NODE_PID = "+outLinkEnterNode
				+ "   AND N1.PID = N2.PID AND N1.U_RECORD != 2 AND N2.U_RECORD != 2 ";
		DatabaseOperator operator=new DatabaseOperator();
		List<Object> result=operator.exeSelect(getConn(), sql);
		if(result!=null&&result.size()==1){return true;}
		return false;
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		RdCross cross = new RdCross();
		System.out.println(cross.getPid());
	}
}
