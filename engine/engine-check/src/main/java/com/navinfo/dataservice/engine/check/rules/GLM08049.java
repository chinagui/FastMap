package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.LinkedListRdLinkAndPid;
import com.navinfo.dataservice.engine.check.model.utils.RdCrossUtils;
/**
 * 交限	html	GLM08049	后台	路口交限的进入线到退出线无通路可行，即报LOG
 * @author zhangxiaoyi
 *新增交限，新增卡车交限RdRestriction
 *修改交限，修改卡车交限RdRestrictionDetail,路口交限，故不考虑经过线.新增退出线，或者修改了退出线才会触发
 */
public class GLM08049 extends baseRule {

	public GLM08049() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdRestriction){//交限
				RdRestriction restriObj=(RdRestriction) obj;
				//非新增，不触发检查
				if(!obj.status().equals(ObjStatus.INSERT)){
					continue;
				}
				checkRdRestriction(restriObj);
				
			}
			else if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)obj;
				checkRdRestrictionDetail(rdRestrictionDetail);
			}
		}
	}

	/**
	 * @param rdRestrictionDetail
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		int outLinkPid = 0;
		int pid = rdRestrictionDetail.getRestricPid();
		if(rdRestrictionDetail.status().equals(ObjStatus.INSERT)){
			if(rdRestrictionDetail.getRelationshipType() == 2){
				return;
			}
			outLinkPid = rdRestrictionDetail.getOutLinkPid();
		}else if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
				outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
			}else{
				return;
			}
		}
		
		RdRestrictionSelector rdRestrictionSelector = new RdRestrictionSelector(getConn());
		RdRestriction rdRestriction = (RdRestriction) rdRestrictionSelector.loadById(pid, false, false);
		int inLinkPid = rdRestriction.getInLinkPid();
		int nodePid = rdRestriction.getNodePid();
		
		RdLinkSelector linkSelector=new RdLinkSelector(getConn());
		RdLink inLink=(RdLink) linkSelector.loadByIdOnlyRdLink(inLinkPid, false);
		RdLink outLink=(RdLink) linkSelector.loadByIdOnlyRdLink(outLinkPid, false);
		
		//进入退出直接挂接
		if(outLink.getsNodePid()==nodePid || outLink.geteNodePid()==nodePid){return;}
		//进入退出不直接挂接
		//获取挂接的路口
		RdCross crossObj=null;
		if(crossObj==null){
			RdCrossSelector selector=new RdCrossSelector(getConn());
			String sql="SELECT DISTINCT C.*"
					+ "  FROM RD_CROSS C, RD_CROSS_NODE CN"
					+ " WHERE C.PID = CN.PID AND C.U_RECORD != 2 AND CN.U_RECORD != 2 "
					+ "   AND CN.NODE_PID = "+nodePid;
			List<RdCross> crossList=selector.loadCrossBySql(sql, false);
			if(crossList==null || crossList.size()!=1){
				log.info("路口数据错误，检查跳过");
				return;
				}
			crossObj=crossList.get(0);}
		if(inLink==null){
			inLink=(RdLink) linkSelector.loadByIdOnlyRdLink(inLinkPid, false);
		}
		ArrayList<LinkedListRdLinkAndPid> crossChain=RdCrossUtils.getGoThroughChainByLinkNode(getConn(),crossObj, inLink, nodePid);
		
		if(crossChain==null||crossChain.size()==0){
			this.setCheckResult("", "", 0);
			return;
		}
		Iterator<LinkedListRdLinkAndPid> chainIterator=crossChain.iterator();
		boolean hasResticChain=false;
		while(chainIterator.hasNext()){
			LinkedListRdLinkAndPid chainTmp=chainIterator.next();
			if(chainTmp.getFirst().getPid()==inLinkPid && chainTmp.getLast().getPid()==outLink.getPid()){
				hasResticChain=true;
				break;
			}
		}
		if(!hasResticChain){
			this.setCheckResult("", "", 0);
			return;
		}
		
		
	}

	/**
	 * @param restriObj
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction restriObj) throws Exception {
		int inLinkPid=restriObj.getInLinkPid();
		int nodePid=restriObj.getNodePid();
		//inLinkPid=49998559;
		//nodePid=1037203;
		RdCross crossObj=null;
		RdLink inLink=null;
		for(IRow detail:restriObj.getDetails()){
			RdRestrictionDetail detailObj=(RdRestrictionDetail) detail;
			if(detailObj.getRelationshipType()==2) {continue;}
			int outLinkPid=detailObj.getOutLinkPid();
			//outLinkPid=49998555;
			RdLinkSelector linkSelector=new RdLinkSelector(getConn());
			RdLink outLink=(RdLink) linkSelector.loadByIdOnlyRdLink(outLinkPid, false);
			//进入退出直接挂接
			if(outLink.getsNodePid()==nodePid || outLink.geteNodePid()==nodePid){continue;}
			//进入退出不直接挂接
			//获取挂接的路口
			if(crossObj==null){
				RdCrossSelector selector=new RdCrossSelector(getConn());
				String sql="SELECT DISTINCT C.*"
						+ "  FROM RD_CROSS C, RD_CROSS_NODE CN"
						+ " WHERE C.PID = CN.PID AND C.U_RECORD != 2 AND CN.U_RECORD != 2 "
						+ "   AND CN.NODE_PID = "+nodePid;
				List<RdCross> crossList=selector.loadCrossBySql(sql, false);
				if(crossList==null || crossList.size()!=1){
					log.info("路口数据错误，检查跳过");
					continue;
					}
				crossObj=crossList.get(0);}
			if(inLink==null){
				inLink=(RdLink) linkSelector.loadByIdOnlyRdLink(inLinkPid, false);
			}
			ArrayList<LinkedListRdLinkAndPid> crossChain=RdCrossUtils.getGoThroughChainByLinkNode(getConn(),crossObj, inLink, nodePid);
			
			if(crossChain==null||crossChain.size()==0){
				this.setCheckResult("", "", 0);
				continue;
			}
			Iterator<LinkedListRdLinkAndPid> chainIterator=crossChain.iterator();
			boolean hasResticChain=false;
			while(chainIterator.hasNext()){
				LinkedListRdLinkAndPid chainTmp=chainIterator.next();
				if(chainTmp.getFirst().getPid()==inLinkPid && chainTmp.getLast().getPid()==outLink.getPid()){
					hasResticChain=true;
					break;
				}
			}
			if(!hasResticChain){
				this.setCheckResult("", "", 0);
				continue;
			}
		}
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
