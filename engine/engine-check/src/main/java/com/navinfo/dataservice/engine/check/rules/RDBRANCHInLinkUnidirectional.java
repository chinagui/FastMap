package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.TwoNodeConnected;

/** 
 * @ClassName: RDBRANCHInLinkUnidirectional
 * @author songdongyan
 * @date 2016年8月19日
 * @Description: 进入Link为单方向且通行方向不能离开路口
 */
public class RDBRANCHInLinkUnidirectional extends baseRule {

	/**
	 * 
	 */
	public RDBRANCHInLinkUnidirectional() {
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
				RdLinkSelector rdLinkSelector=new RdLinkSelector(this.getConn());
				int inLinkPid = rdBranch.getInLinkPid();
				int nodePid = rdBranch.getNodePid();
				//进入线
				RdLink inLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(inLinkPid, false);
				//进入线为单方向则进入线需沿通行方向与进入点挂接
				if(inLink.getDirect()==2 && inLink.geteNodePid()!=nodePid){
					this.setCheckResult("", "", 0);
					return;
				}
				if(inLink.getDirect()==3 && inLink.getsNodePid()!=nodePid){
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
