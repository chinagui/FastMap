package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM10003
 * @author songdongyan
 * @date 2016年8月22日
 * @Description: 有坡度信息的Link的通行方向如果是单向，并且是进入坡度信息记录主点的，报错
 */
public class GLM10003 extends baseRule {

	/**
	 * 
	 */
	public GLM10003() {
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
			if(obj instanceof RdSlope ){
				RdSlope rdSlope = (RdSlope)obj;
				//记录主点
				int startNode = rdSlope.getNodePid();
				//接续线信息
				List<IRow> viaLinks = rdSlope.getSlopeVias();
				//获取退出线信息
				int outLinkPid = rdSlope.getLinkPid();
				
				Map<String, Object> changedFields = rdSlope.changedFields();
				
				if(!changedFields.isEmpty()){
					if(changedFields.containsKey("linkPid")){
						outLinkPid = (int) changedFields.get("linkPid");
					}
					if(changedFields.containsKey("slopeVias")){
						viaLinks = (List<IRow>) changedFields.get("slopeVias");
					}
				}

				RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
				RdLink outLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(outLinkPid, false);
				
				//退出是否沿通行方向
				if(outLink.getDirect()==2 && outLink.geteNodePid() == startNode){
					this.setCheckResult("", "", 0);
					return;
				}else if(outLink.getDirect()==3 && outLink.getsNodePid() == startNode){
					this.setCheckResult("", "", 0);
					return;
				}
				//接续线是否沿通行方向联通
				if(viaLinks.size() == 0){
					continue;
				}
				
				if(outLink.getsNodePid() == startNode){
					startNode = outLink.geteNodePid();
				}else{
					startNode = outLink.getsNodePid();
				}
				
				if(!rdSlopViasConnectedInOneDirection(startNode,viaLinks)){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}

	}
	
	private boolean rdSlopViasConnectedInOneDirection(int startNode,List<IRow> viaLinks) throws Exception{
		//获取接续link的顺序pid
		List<Integer> linkPids = new ArrayList<Integer>();
		Iterator<IRow> viaLinksIter = viaLinks.iterator();
		while(viaLinksIter.hasNext()){
			RdSlopeVia rdSlopeVia = (RdSlopeVia) viaLinksIter.next();
			linkPids.add(rdSlopeVia.getSeqNum()-1, rdSlopeVia.getLinkPid());
		}
		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
		List<RdLink> rdLinkList = rdLinkSelector.loadByPids(linkPids, false);
		Map<Integer,RdLink> rdLinkMap = new HashMap<Integer,RdLink>();
		//获取接续link顺序列表
		Iterator<RdLink> rdLinkListIer = rdLinkList.iterator();
		while(rdLinkListIer.hasNext()){
			RdLink rdLink = (RdLink) rdLinkListIer.next();
			int index = linkPids.indexOf(rdLink.getPid());
			rdLinkMap.put(index, rdLink);
		}
		//接续link是否沿通行方向
		for(int i = 0;i<rdLinkMap.size();i++){
			if(!rdLinkMap.containsKey(i)){
				return false;
			}
			RdLink rdLink = (RdLink) rdLinkMap.get(i);
			if(rdLink.getsNodePid()==startNode){
				startNode = rdLink.geteNodePid();
				if(rdLink.getDirect() == 3){
					return false;
				}
			}else if(rdLink.geteNodePid()==startNode){
				startNode = rdLink.getsNodePid();
				if(rdLink.getDirect() == 2){
					return false;
				}
			}else{
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
