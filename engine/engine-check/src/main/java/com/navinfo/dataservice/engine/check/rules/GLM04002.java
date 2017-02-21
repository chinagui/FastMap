package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04002
 * @author songdongyan
 * @date 2016年12月23日
 * @Description: 大门点的挂接link数必须是2
 * 新增link服务端前检查
 * 移动端点服务端后检查
 * 分离节点服务端后检查
 */
public class GLM04002 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// RdLink
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}	
		}
		
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//新增link(rowId为空，修改map为空)
		if(rdLink.rowId()==null&&rdLink.changedFields().isEmpty()){
			Set<Integer> nodePids = new HashSet<Integer>();
			nodePids.add(rdLink.geteNodePid());
			nodePids.add(rdLink.getsNodePid());
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_GATE G");
			sb.append(" WHERE G.NODE_PID IN (" + StringUtils.join(nodePids.toArray(),",") + ")");
			sb.append(" AND G.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLink前检查GLM04002:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 分离节点RdLink
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				if(rdLink.status().equals(ObjStatus.UPDATE)){
					checkRdLinkPost(rdLink);
				}
			}
			//移动端点RdNode
//			else if (obj instanceof RdNode) {
//				RdNode rdNode = (RdNode) obj;
//				if(rdNode.status().equals(ObjStatus.UPDATE)){
//					checkRdNode(rdNode);
//				}
//			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLinkPost(RdLink rdLink) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT COUNT(1) FROM RD_LINK RR,");
		sb.append(" (SELECT G.PID,G.NODE_PID FROM RD_GATE G ,RD_LINK R");
		sb.append(" WHERE (G.NODE_PID = R.S_NODE_PID OR G.NODE_PID = R.E_NODE_PID)");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND G.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID = " + rdLink.getPid() + ") GATE");
		sb.append(" WHERE (RR.S_NODE_PID = GATE.NODE_PID OR RR.E_NODE_PID = GATE.NODE_PID)");
		sb.append(" AND RR.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLink后检查GLM04002:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if((resultList.size()>0)&&(Integer.parseInt(resultList.get(0).toString())!=2)){
			String target = "[RD_LINK," + rdLink.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

}
