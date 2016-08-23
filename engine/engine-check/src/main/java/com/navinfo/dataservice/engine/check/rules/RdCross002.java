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
 * 路口	html/word	RDCROSS002	后台	
 * 选中的点中至少有一个点已经属于“KG共用”的路口，不允许创建，则报：存在不合理数据，无法提交，请继续选择或者放弃编辑
 * @author zhangxiaoyi
 *
 */
public class RdCross002 extends baseRule {

	public RdCross002() {
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
				String sql="select 1 from rd_cross c,rd_cross_node cn "
						+ "where c.pid=cn.pid "
						+ "and c.kg_flag=0 "
						+ "and cn.node_pid in ("+crossNodeList.toString().replace("[", "").replace("]", "")+")";
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
