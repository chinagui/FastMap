package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 在一组同一Node关系中，必须包含一个道路Node，否则报Log
 * 
 * @author wangdongbin
 *
 */
public class GLM22003 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			if (obj instanceof RdSameNode) {
				RdSameNode rdSameNode = (RdSameNode) obj;
				checkSameNode(rdSameNode);
			}
		}
	}

	/**
	 * 查询同一点关系
	 * @param rdSameNode
	 * @throws Exception
	 */
	private void checkSameNode(RdSameNode rdSameNode) throws Exception {

		List<IRow> parts = rdSameNode.getParts();

		boolean hadRdNode=false;

		RdSameNodePart rdSameNodePart =null;
		for (IRow part:parts) {
			 rdSameNodePart = (RdSameNodePart)part;

			if(rdSameNodePart.getTableName().toUpperCase().equals("RD_NODE"))
			{
				hadRdNode=true;

				break;
			}
		}

		// 不包含道路node，报log
		if(!hadRdNode&&rdSameNodePart!=null){

			RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(this.getConn());

			Geometry nodeGeo = sameNodeSelector.getGeoByNodePidAndTableName(
					rdSameNodePart.getNodePid(), rdSameNodePart.getTableName(), true);

			String target = "[RD_SAME_NODE," + rdSameNode.getPid() + "]";

			this.setCheckResult(nodeGeo, target, 0);
		}
	}

}
