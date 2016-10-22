package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM06005
 * @author songdongyan
 * @date 2016年8月24日
 * @Description: 分岔口中一条进入线只能对应两条或两条以上的退出线
 * 理解：分岔口中一条进入线，需要沿通行方向挂接至少两条link
 */
public class GLM06005 extends baseRule {

	/**
	 * 
	 */
	public GLM06005() {
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
			if(obj instanceof RdSe ){
				RdSe rdSe = (RdSe)obj;
				int node = rdSe.getNodePid();
				
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK RL");
				sb.append(" WHERE RL.U_RECORD != 2 AND (RL.DIRECT != 3 AND RL.S_NODE_PID = ");
				sb.append(node);
				sb.append(")");
				sb.append(" OR (RL.DIRECT != 2 AND RL.E_NODE_PID = ");
				sb.append(node); 
				sb.append(")");

				String sql = sb.toString();
				
		        DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				
				if (resultList.size()<2){
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
