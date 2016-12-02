package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM06007
 * @author songdongyan
 * @date 2016年8月24日
 * @Description: 如果分岔口点,不允许有两条及以上的进入线。屏蔽条件：如果多组分岔口信息，进入线不同退出线相同的，不报log
 * 理解：同意分岔口点，只要进入线相同就报log
 */
public class GLM06007 extends baseRule {

	/**
	 * 
	 */
	public GLM06007() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//获取新建RdBranch信息
			if(obj instanceof RdSe ){
				RdSe rdSe = (RdSe)obj;
				int node = rdSe.getNodePid();
				int inLinkPid = rdSe.getInLinkPid();
				
				StringBuilder sb = new StringBuilder();

				sb.append("select 1 from(select in_link_pid  from rd_se");
				sb.append(" WHERE U_RECORD != 2 AND NODE_PID = ");
				sb.append(node);
				sb.append(" and in_link_pid != ");
				sb.append(inLinkPid);
				sb.append(" group by IN_LINK_PID) having count(1) >0");

				String sql = sb.toString();
				
		        DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				
				if (resultList.size()>0){
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
