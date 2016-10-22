package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RELATING_CHECK_CROSSNODE_PASSLINK
 * @author songdongyan
 * @date 2016年8月23日
 * @Description: 顺行没有经过线，则进入点必须有路口信息
 */
public class RELATING_CHECK_CROSSNODE_PASSLINK extends baseRule {

	/**
	 * 
	 */
	public RELATING_CHECK_CROSSNODE_PASSLINK() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow obj:checkCommand.getGlmList()){
			//获取新建RdDirectroute信息
			if(obj instanceof RdDirectroute ){
				RdDirectroute rdDirectroute = (RdDirectroute)obj;
				//进入node
				int node = rdDirectroute.getNodePid();
				//经过线信息
				List<IRow> viaLinks = rdDirectroute.getVias();
				//获取修改RdDirectroute信息
				Map<String, Object> changedFields = rdDirectroute.changedFields();
				
				if(!changedFields.isEmpty()){
					if(changedFields.containsKey("nodePid")){
						node = (int) changedFields.get("nodePid");
					}
					if(changedFields.containsKey("slopeVias")){
						viaLinks = (List<IRow>) changedFields.get("slopeVias");
					}
				}
				//顺行没有经过线
				if(viaLinks.isEmpty()){
					StringBuilder sb = new StringBuilder();

					sb.append("SELECT RCN.NODE_PID FROM RD_CROSS_NODE RCN ");
					sb.append(" WHERE RCN.U_RECORD != 2 AND RCN.NODE_PID = ");
					sb.append(node);

					String sql = sb.toString();
					
			        DatabaseOperator getObj=new DatabaseOperator();
					List<Object> resultList=new ArrayList<Object>();
					resultList=getObj.exeSelect(this.getConn(), sql);
					
					if (resultList.size()<=0){
						this.setCheckResult("", "", 0);
						return;
					}
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
