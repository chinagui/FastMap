package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
/**
 * RDCROSS001	后台	
 * 障碍物属性的点不能与路口共存？？
 * @author zhangxiaoyi
 *
 */
public class RdCross001 extends baseRule {

	public RdCross001() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdCross){
				RdCross crossObj=(RdCross) obj;
				List<Integer> crossNodeList=new ArrayList<Integer>();
				for(IRow crossNode:crossObj.getNodes()){
					crossNodeList.add(((RdCrossNode) crossNode).getNodePid());
				}
				String sql="select 1 from rd_node_form n"
						+ " where n.form_of_way=15"
						+ " and n.node_pid in ("+crossNodeList.toString().replace("[", "").replace("]", "")+")";
				DatabaseOperator operator=new DatabaseOperator();
				List<Object> resutlList=operator.exeSelect(getConn(), sql);
				if(resutlList!=null && resutlList.size()>0){
					this.setCheckResult("", "", 0);
					break;
				}
			}}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
