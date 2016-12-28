package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK
 * @author Han Shaoming
 * @date 2016年12月27日 下午8:32:26
 * @Description TODO
 * 创建node必须捕捉到link上
 * 新增NODE服务端前检查:
 * 新增AD_NODE服务端前检查:
 */
public class PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow row: checkCommand.getGlmList()){
			//新增NODE
			if(row instanceof RdNode){
				RdNode rdNode = (RdNode)row;
				checkRdNode(rdNode);
			}
			//新增AD_NODE
			else if(row instanceof AdNode){
				AdNode adNode = (AdNode)row;
				checkAdNode(adNode);
			}
		}
		
	}

	/**
	 * @author Han Shaoming
	 * @param adNode
	 */
	private void checkAdNode(AdNode adNode) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @author Han Shaoming
	 * @param rdNode
	 */
	private void checkRdNode(RdNode rdNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
