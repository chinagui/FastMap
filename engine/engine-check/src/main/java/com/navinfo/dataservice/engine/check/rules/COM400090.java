package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName COM400090
 * @author Han Shaoming
 * @date 2017年1月19日 下午3:05:57
 * @Description TODO
 * 减速带的进入线必须与信息主点挂接，进入线到信息主点可通行
 * 道路方向编辑	服务端后检查
 */
public class COM400090 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路方向编辑
			if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		//道路方向编辑
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			if(changedFields.containsKey("direct")){
				StringBuilder sb = new StringBuilder();
				
				sb.append("WITH NN AS(SELECT L.LINK_PID, L.S_NODE_PID, L.E_NODE_PID, L.LINK_PID CHAIN_LINKID");
				sb.append(" FROM RD_LINK L WHERE L.LINK_PID= "+rdLink.getPid());
				sb.append(" AND L.DIRECT IN (0, 1, 2) AND L.U_RECORD <>2");
				sb.append(" UNION");
				sb.append(" SELECT L.LINK_PID, L.E_NODE_PID, L.S_NODE_PID, -L.LINK_PID");
				sb.append(" FROM RD_LINK L WHERE L.LINK_PID= "+rdLink.getPid());
				sb.append(" AND L.DIRECT IN (0, 1) AND L.U_RECORD <>2");
				sb.append(" UNION");
				sb.append(" SELECT L.LINK_PID, L.E_NODE_PID, L.S_NODE_PID, L.LINK_PID");
				sb.append(" FROM RD_LINK L WHERE L.LINK_PID= "+rdLink.getPid());
				sb.append(" AND L.DIRECT = 3 AND L.U_RECORD <>2)");
				sb.append(" SELECT DISTINCT RSB.LINK_PID FROM RD_SPEEDBUMP RSB");
				sb.append(" WHERE RSB.U_RECORD <>2 AND EXISTS (SELECT 1 FROM NN");
				sb.append(" WHERE NN.LINK_PID = RSB.LINK_PID AND RSB.NODE_PID = NN.S_NODE_PID)");
				
				String sql = sb.toString();
				log.info("RdLink后检查COM400090--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

}
