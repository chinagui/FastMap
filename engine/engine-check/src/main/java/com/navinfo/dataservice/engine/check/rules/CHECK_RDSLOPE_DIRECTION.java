package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.LinksConnectedInOneDirection;
import com.navinfo.dataservice.engine.check.graph.TwoNodeConnected;

/** 
 * @ClassName: CHECK_RDSLOPE_DIRECTION
 * @author songdongyan
 * @date 2016年8月22日
 * @Description: 道路坡度如果选择了多根link，则这些link必须是可通行方向上的接续link
 */
public class CHECK_RDSLOPE_DIRECTION extends baseRule {

	/**
	 * 
	 */
	public CHECK_RDSLOPE_DIRECTION() {
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
				LinksConnectedInOneDirection linksConnectedInOneDirection = new LinksConnectedInOneDirection(startNode,outLink,viaLinks);
				if(!linksConnectedInOneDirection.isConnected()){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
